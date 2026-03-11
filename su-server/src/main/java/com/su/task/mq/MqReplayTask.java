package com.su.task.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RedisKeyConstant;
import com.su.mq.support.MqFailedEnvelope;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class MqReplayTask {
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public MqReplayTask(RabbitTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "*/5 * * * * ?")
    public void replay() {
        replayKey(RedisKeyConstant.mqReturnsKey(), true);
        replayKey(RedisKeyConstant.mqConfirmNackKey(), false);
    }

    private void replayKey(String key, boolean allowReplay) {
        for (int i = 0; i < 200; i++) {
            String json = stringRedisTemplate.opsForList().rightPop(key);
            if (json == null || json.isBlank()) {
                return;
            }
            MqFailedEnvelope envelope;
            try {
                envelope = objectMapper.readValue(json, MqFailedEnvelope.class);
            } catch (Exception e) {
                pushDlq(json);
                continue;
            }

            if (envelope == null || envelope.getType() == null || envelope.getType().isBlank()) {
                pushDlq(json);
                continue;
            }

            if (!allowReplay || !"returns".equals(envelope.getType())) {
                pushDlq(json);
                continue;
            }

            if (!send(envelope)) {
                requeue(key, envelope, "replay_failed");
                return;
            }
        }
    }

    private boolean send(MqFailedEnvelope envelope) {
        if (envelope.getExchange() == null || envelope.getExchange().isBlank()
                || envelope.getRoutingKey() == null || envelope.getRoutingKey().isBlank()
                || envelope.getBodyBase64() == null || envelope.getBodyBase64().isBlank()) {
            return false;
        }
        try {
            byte[] body = Base64.getDecoder().decode(envelope.getBodyBase64());
            MessageProperties props = new MessageProperties();
            props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            if (envelope.getCorrelationId() != null && !envelope.getCorrelationId().isBlank()) {
                props.setCorrelationId(envelope.getCorrelationId());
            }
            Message message = new Message(body, props);
            rabbitTemplate.send(envelope.getExchange(), envelope.getRoutingKey(), message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void requeue(String key, MqFailedEnvelope envelope, String lastError) {
        if (key == null || key.isBlank() || envelope == null) {
            return;
        }
        int attempts = envelope.getAttempts() == null ? 0 : envelope.getAttempts();
        attempts++;
        envelope.setAttempts(attempts);
        envelope.setLastError(lastError);
        if (envelope.getCreateTime() == null) {
            envelope.setCreateTime(LocalDateTime.now());
        }
        if (attempts > 20) {
            try {
                pushDlq(objectMapper.writeValueAsString(envelope));
            } catch (Exception ignored) {
            }
            return;
        }
        try {
            stringRedisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(envelope));
        } catch (Exception ignored) {
        }
    }

    private void pushDlq(String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.mqDlqKey(), json);
        } catch (Exception ignored) {
        }
    }
}
