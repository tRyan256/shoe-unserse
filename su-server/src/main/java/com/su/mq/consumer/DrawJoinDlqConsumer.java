package com.su.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.mq.message.DrawJoinPersistMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DrawJoinDlqConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public DrawJoinDlqConsumer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConstant.DRAW_JOIN_PERSIST_DLQ, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DrawJoinPersistMessage message) {
        if (message == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.drawJoinDlqKey(), objectMapper.writeValueAsString(message));
        } catch (Exception ignored) {
        }
    }
}
