package com.su.task.airdrop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.mq.message.AirdropClaimMessage;
import com.su.mq.service.AirdropClaimPersistService;
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
public class AirdropMqRetryTask {
    private static final int MAX_BATCH_SIZE = 500;

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AirdropClaimPersistService airdropClaimPersistService;

    public AirdropMqRetryTask(
            RabbitTemplate rabbitTemplate,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            AirdropClaimPersistService airdropClaimPersistService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.airdropClaimPersistService = airdropClaimPersistService;
    }

    @Scheduled(cron = "*/5 * * * * ?")
    public void resend() {
        String key = RedisKeyConstant.airdropClaimMqRetryKey();
        List<AirdropClaimMessage> directPersist = new ArrayList<>();
        List<String> rawToRequeue = new ArrayList<>();
        boolean brokerAvailable = true;
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            String json = stringRedisTemplate.opsForList().rightPop(key);
            if (json == null || json.isBlank()) {
                break;
            }
            AirdropClaimMessage message;
            try {
                message = objectMapper.readValue(json, AirdropClaimMessage.class);
            } catch (Exception e) {
                stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.airdropClaimDlqKey(), json);
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
            airdropClaimPersistService.persistBatch(directPersist);
        } catch (Exception e) {
            for (int i = rawToRequeue.size() - 1; i >= 0; i--) {
                stringRedisTemplate.opsForList().rightPush(key, rawToRequeue.get(i));
            }
        }
    }

    private boolean send(AirdropClaimMessage message) {
        if (message == null || message.getMsgId() == null || message.getMsgId().isBlank()) {
            return false;
        }
        try {
            CorrelationData correlationData = new CorrelationData(message.getMsgId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.AIRDROP_EXCHANGE,
                    RabbitMqConstant.AIRDROP_CLAIM_ROUTING_KEY,
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
