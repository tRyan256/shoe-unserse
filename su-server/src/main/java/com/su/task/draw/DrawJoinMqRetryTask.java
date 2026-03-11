package com.su.task.draw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.mq.message.DrawJoinPersistMessage;
import com.su.mq.service.DrawJoinPersistService;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DrawJoinMqRetryTask {
    private static final int MAX_BATCH_SIZE = 500;

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final DrawJoinPersistService drawJoinPersistService;

    public DrawJoinMqRetryTask(
            RabbitTemplate rabbitTemplate,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            DrawJoinPersistService drawJoinPersistService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.drawJoinPersistService = drawJoinPersistService;
    }

    @Scheduled(initialDelayString = "${SU_DRAW_JOIN_RETRY_INITIAL_DELAY_MS:5000}", fixedDelayString = "${SU_DRAW_JOIN_RETRY_DELAY_MS:2000}")
    public void resend() {
        String key = RedisKeyConstant.drawJoinMqRetryKey();
        List<DrawJoinPersistMessage> directPersist = new ArrayList<>();
        List<String> rawToRequeue = new ArrayList<>();
        boolean brokerAvailable = true;
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            String json = stringRedisTemplate.opsForList().rightPop(key);
            if (json == null || json.isBlank()) {
                break;
            }
            DrawJoinPersistMessage message;
            try {
                message = objectMapper.readValue(json, DrawJoinPersistMessage.class);
            } catch (Exception e) {
                stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.drawJoinDlqKey(), json);
                continue;
            }
            if (brokerAvailable && send(message)) {
                continue;
            }
            brokerAvailable = false;
            directPersist.add(message);
            rawToRequeue.add(json);
        }
        if (directPersist.isEmpty()) {
            return;
        }
        try {
            drawJoinPersistService.persistBatch(directPersist);
        } catch (Exception e) {
            for (int i = rawToRequeue.size() - 1; i >= 0; i--) {
                stringRedisTemplate.opsForList().rightPush(key, rawToRequeue.get(i));
            }
        }
    }

    private boolean send(DrawJoinPersistMessage message) {
        if (message == null || message.getMsgId() == null || message.getMsgId().isBlank()) {
            return false;
        }
        try {
            CorrelationData correlationData = new CorrelationData(message.getMsgId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.DRAW_JOIN_EXCHANGE,
                    RabbitMqConstant.DRAW_JOIN_PERSIST_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            return false;
        }
    }
}
