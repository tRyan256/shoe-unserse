package com.su.mq.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Base64;

@Component
@Slf4j
public class MqFailureRecorder {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public MqFailureRecorder(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public RabbitTemplate.ReturnsCallback returnsCallback() {
        return returned -> {
            recordReturn(returned.getMessage(), returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
        };
    }

    public RabbitTemplate.ConfirmCallback confirmCallback() {
        return this::recordConfirm;
    }

    public void recordReturn(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String correlationId = null;
        if (message != null && message.getMessageProperties() != null && message.getMessageProperties().getCorrelationId() != null) {
            correlationId = message.getMessageProperties().getCorrelationId();
        }
        String bodyBase64 = null;
        if (message != null && message.getBody() != null) {
            bodyBase64 = Base64.getEncoder().encodeToString(message.getBody());
        }
        MqFailedEnvelope envelope = MqFailedEnvelope.builder()
                .type("returns")
                .exchange(exchange)
                .routingKey(routingKey)
                .replyCode(replyCode)
                .replyText(replyText)
                .correlationId(correlationId)
                .bodyBase64(bodyBase64)
                .attempts(0)
                .createTime(LocalDateTime.now())
                .build();
        push(RedisKeyConstant.mqReturnsKey(), envelope);
    }

    public void recordConfirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            return;
        }
        MqFailedEnvelope envelope = MqFailedEnvelope.builder()
                .type("confirm_nack")
                .correlationId(correlationData == null ? null : correlationData.getId())
                .lastError(cause)
                .attempts(0)
                .createTime(LocalDateTime.now())
                .build();
        push(RedisKeyConstant.mqConfirmNackKey(), envelope);
    }

    private void push(String key, MqFailedEnvelope envelope) {
        if (key == null || key.isBlank() || envelope == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(envelope));
        } catch (Exception ignored) {
            log.error("记录MQ失败事件到Redis失败，key={}, type={}, correlationId={}", key, envelope.getType(), envelope.getCorrelationId(), ignored);
        }
    }
}
