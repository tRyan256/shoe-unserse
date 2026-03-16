package com.su.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.su.service.impl.DrawServiceImpl;
import com.su.utils.cache.CacheClient;
import com.su.vo.DrawDetailVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawServiceImplLocalCacheTest {

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
    void detail_L1Hit_skipsRedis() {
        DrawServiceImpl service = new DrawServiceImpl();
        CacheClient cacheClient = mock(CacheClient.class);
        Cache<Long, DrawDetailVO> localCache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();

        DrawDetailVO vo = new DrawDetailVO();
        vo.setId(1L);
        vo.setTitle("t");
        localCache.put(1L, vo);

        setField(service, "cacheClient", cacheClient);
        setField(service, "drawDetailLocalCache", localCache);

        DrawDetailVO result = service.detail(1L);

        assertSame(vo, result);
        verify(cacheClient, never())
                .queryWithLogicalExpire(anyString(), anyString(), eq(DrawDetailVO.class), any(), any(), any(), any(), any(), any());
    }
}
