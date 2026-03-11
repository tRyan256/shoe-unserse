package com.su.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RedisKeyConstant;
import com.su.mq.support.MqFailureRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MqFailureRecorderTest {
    @Test
    void returnsCallback_pushesToRedis() throws Exception {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(stringRedisTemplate.opsForList()).thenReturn(listOps);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        MqFailureRecorder recorder = new MqFailureRecorder(stringRedisTemplate, objectMapper);

        MessageProperties props = new MessageProperties();
        props.setCorrelationId("CID");
        Message message = new Message("{\"a\":1}".getBytes(StandardCharsets.UTF_8), props);
        recorder.recordReturn(message, 312, "NO_ROUTE", "ex", "rk");

        verify(listOps).leftPush(eq(RedisKeyConstant.mqReturnsKey()), eq("{\"ok\":true}"));
    }

    @Test
    void confirmCallback_nackPushesToRedis() throws Exception {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(stringRedisTemplate.opsForList()).thenReturn(listOps);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        MqFailureRecorder recorder = new MqFailureRecorder(stringRedisTemplate, objectMapper);

        recorder.recordConfirm(new CorrelationData("MID"), false, "nack");

        verify(listOps).leftPush(eq(RedisKeyConstant.mqConfirmNackKey()), eq("{\"ok\":true}"));
    }
}
