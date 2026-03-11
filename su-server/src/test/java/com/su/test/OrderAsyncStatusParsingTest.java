package com.su.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.service.impl.OrderServiceImpl;
import com.su.vo.OrderAsyncStatusVO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderAsyncStatusParsingTest {
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAsyncStatus_returnsFailedWhenJsonCorrupted() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("preorder:ON_BAD"))).thenReturn("{");

        OrderServiceImpl service = new OrderServiceImpl();
        setField(service, "stringRedisTemplate", stringRedisTemplate);
        setField(service, "objectMapper", new ObjectMapper());

        OrderAsyncStatusVO vo = service.getAsyncStatus("ON_BAD");

        assertNotNull(vo);
        assertEquals("ON_BAD", vo.getOrderNumber());
        assertEquals(OrderAsyncStatusConstant.FAILED, vo.getStatus());
        assertEquals("预下单状态数据异常", vo.getError());
    }

    @Test
    void getAsyncStatus_returnsNotFoundWhenMissing() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq("preorder:ON_MISS"))).thenReturn(null);

        OrderServiceImpl service = new OrderServiceImpl();
        setField(service, "stringRedisTemplate", stringRedisTemplate);
        setField(service, "objectMapper", new ObjectMapper());

        OrderAsyncStatusVO vo = service.getAsyncStatus("ON_MISS");

        assertNotNull(vo);
        assertEquals("ON_MISS", vo.getOrderNumber());
        assertEquals(OrderAsyncStatusConstant.NOT_FOUND, vo.getStatus());
    }
}

