package com.su.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.mq.message.AirdropClaimMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AirdropClaimDlqConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AirdropClaimDlqConsumer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConstant.AIRDROP_CLAIM_DLQ, containerFactory = "rabbitListenerContainerFactory")
    public void handle(AirdropClaimMessage message) {
        if (message == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.airdropClaimDlqKey(), objectMapper.writeValueAsString(message));
        } catch (Exception ignored) {
        }
    }
}
