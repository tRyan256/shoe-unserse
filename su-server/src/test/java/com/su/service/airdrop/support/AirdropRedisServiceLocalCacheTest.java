package com.su.service.airdrop.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.utils.cache.CacheClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AirdropRedisServiceLocalCacheTest {

    private StringRedisTemplate stringRedisTemplate;
    private CacheClient cacheClient;
    private Cache<Long, AirdropMeta> localCache;
    private AirdropRedisService service;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        cacheClient = mock(CacheClient.class);
        localCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();
        service = new AirdropRedisService(stringRedisTemplate, new ObjectMapper(), cacheClient, localCache);
    }

    @Test
    void getMeta_L1Hit_skipsRedis() {
        AirdropMeta meta = AirdropMeta.builder().id(1L).title("t").build();
        localCache.put(1L, meta);

        AirdropMeta result = service.getMeta(1L);

        assertSame(meta, result);
        verify(cacheClient, never()).getLogicalExpireValue(anyString(), eq(AirdropMeta.class));
    }

    @Test
    void getMeta_L1Miss_backfills() {
        AirdropMeta meta = AirdropMeta.builder().id(1L).title("t").build();
        when(cacheClient.getLogicalExpireValue(eq(RedisKeyConstant.airdropMetaKey(1L)), eq(AirdropMeta.class)))
                .thenReturn(meta);

        AirdropMeta result = service.getMeta(1L);

        assertEquals(meta, result);
        assertEquals(meta, localCache.getIfPresent(1L));
    }

    @Test
    void warmup_putsL1() {
        Airdrop airdrop = Airdrop.builder()
                .id(10L)
                .title("air")
                .couponId(1L)
                .status(1)
                .remainCount(10)
                .startTime(LocalDateTime.now().minusMinutes(1))
                .endTime(LocalDateTime.now().plusMinutes(10))
                .build();
        Coupon coupon = Coupon.builder()
                .id(1L)
                .name("c")
                .type(1)
                .value(BigDecimal.ONE)
                .minAmount(BigDecimal.ZERO)
                .status(1)
                .build();

        service.warmupAirdropMeta(airdrop, coupon);

        assertNotNull(localCache.getIfPresent(airdrop.getId()));
        verify(cacheClient, times(1))
                .setLogicalExpireValue(eq(RedisKeyConstant.airdropMetaKey(airdrop.getId())),
                        any(AirdropMeta.class), any(Duration.class), any(Duration.class));
    }

    @Test
    void delete_invalidatesL1() {
        AirdropMeta meta = AirdropMeta.builder().id(2L).title("t").build();
        localCache.put(2L, meta);

        service.delete(2L);

        assertNull(localCache.getIfPresent(2L));
        verify(cacheClient, times(1)).evict(RedisKeyConstant.airdropMetaKey(2L));
    }
}
