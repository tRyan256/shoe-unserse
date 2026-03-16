# Multi-Level Cache for Draw/Airdrop Details Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add L1 (Caffeine) caches for draw detail and airdrop meta with Redis logical-expire L2, caching only non-null values and ensuring L1 invalidation happens after L2 delete.

**Architecture:** Introduce two Caffeine caches (DrawDetail/AirdropMeta) with short TTL. Service methods read-through L1→L2 and backfill L1 on miss. Warmup writes L2 then L1. Evictions delete L2 first, then invalidate L1 to reduce race-based dirty reads.

**Tech Stack:** Spring Boot 3, Caffeine, Redis, JUnit 5, Mockito.

---

## Chunk 1: Airdrop L1 Cache (Tests + Implementation)

### Task 1: Airdrop L1 cache read-through + warmup/evict

**Files:**
- Create: `su-server/src/test/java/com/su/service/airdrop/support/AirdropRedisServiceLocalCacheTest.java`
- Modify: `su-server/src/main/java/com/su/config/LocalCacheConfiguration.java`
- Modify: `su-server/src/main/java/com/su/service/airdrop/support/AirdropRedisService.java`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo -Dtest=AirdropRedisServiceLocalCacheTest
```
Expected: **FAIL** (compile error: constructor `AirdropRedisService(...)` not found / L1 logic missing).

- [ ] **Step 3: Write minimal implementation**

1) Add the L1 bean in `LocalCacheConfiguration.java` (only airdrop for now):
```java
import com.su.service.airdrop.support.AirdropMeta;

@Bean
public Cache<Long, AirdropMeta> airdropMetaLocalCache() {
    return Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofSeconds(10))
            .build();
}
```

2) Update `AirdropRedisService.java` to inject and use L1 (non-null only):
```java
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;

private final Cache<Long, AirdropMeta> airdropMetaLocalCache;

public AirdropRedisService(
        StringRedisTemplate stringRedisTemplate,
        ObjectMapper objectMapper,
        CacheClient cacheClient,
        @Qualifier("airdropMetaLocalCache") Cache<Long, AirdropMeta> airdropMetaLocalCache
) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.cacheClient = cacheClient;
    this.airdropMetaLocalCache = airdropMetaLocalCache;
    this.claimScript = new DefaultRedisScript<>();
    this.claimScript.setLocation(new ClassPathResource("lua/airdrop_claim.lua"));
    this.claimScript.setResultType(Long.class);
}

public AirdropMeta getMeta(Long airdropId) {
    if (airdropId == null) {
        return null;
    }
    AirdropMeta local = airdropMetaLocalCache.getIfPresent(airdropId);
    if (local != null) {
        return local;
    }
    AirdropMeta meta = cacheClient.getLogicalExpireValue(RedisKeyConstant.airdropMetaKey(airdropId), AirdropMeta.class);
    if (meta != null) {
        airdropMetaLocalCache.put(airdropId, meta);
    }
    return meta;
}

public void delete(Long airdropId) {
    if (airdropId == null) {
        return;
    }
    deleteStockAndUsers(airdropId);
    cacheClient.evict(RedisKeyConstant.airdropMetaKey(airdropId));
    airdropMetaLocalCache.invalidate(airdropId);
}

public void warmupAirdropMeta(Airdrop airdrop, Coupon coupon) {
    // ...existing build logic...
    cacheClient.setLogicalExpireValue(key, meta, logicalTtl, physicalTtl);
    if (meta != null) {
        airdropMetaLocalCache.put(airdrop.getId(), meta);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo -Dtest=AirdropRedisServiceLocalCacheTest
```
Expected: **PASS**.

- [ ] **Step 5: Commit**

```bash
git add su-server/src/test/java/com/su/service/airdrop/support/AirdropRedisServiceLocalCacheTest.java \
        su-server/src/main/java/com/su/config/LocalCacheConfiguration.java \
        su-server/src/main/java/com/su/service/airdrop/support/AirdropRedisService.java
git commit -m "feat: add L1 cache for airdrop meta"
```

---

## Chunk 2: Draw L1 Cache (Tests + Implementation)

### Task 2: Draw detail L1 cache read-through + warmup/evict ordering

**Files:**
- Create: `su-server/src/test/java/com/su/service/DrawServiceImplLocalCacheTest.java`
- Modify: `su-server/src/main/java/com/su/config/LocalCacheConfiguration.java`
- Modify: `su-server/src/main/java/com/su/service/impl/DrawServiceImpl.java`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo -Dtest=DrawServiceImplLocalCacheTest
```
Expected: **FAIL** (NoSuchField/logic missing or cacheClient still invoked).

- [ ] **Step 3: Write minimal implementation**

1) Add draw cache bean to `LocalCacheConfiguration.java`:
```java
import com.su.vo.DrawDetailVO;

@Bean
public Cache<Long, DrawDetailVO> drawDetailLocalCache() {
    return Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(Duration.ofSeconds(10))
            .build();
}
```

2) Update `DrawServiceImpl.java` to inject and use L1 (non-null only):
```java
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;

@Autowired
@Qualifier("drawDetailLocalCache")
private Cache<Long, DrawDetailVO> drawDetailLocalCache;

@Override
public DrawDetailVO detail(Long drawId) {
    if (drawId == null) {
        return null;
    }
    DrawDetailVO local = drawDetailLocalCache.getIfPresent(drawId);
    if (local != null) {
        return local;
    }
    String key = RedisKeyConstant.drawDetailKey(drawId);
    String lockKey = RedisKeyConstant.lockKey(key);
    DrawDetailVO vo = cacheClient.queryWithLogicalExpire(
            key,
            lockKey,
            DrawDetailVO.class,
            () -> buildDrawDetail(drawId),
            Duration.ofMinutes(10),
            Duration.ofHours(2),
            Duration.ofMinutes(1),
            null,
            null
    );
    if (vo != null) {
        drawDetailLocalCache.put(drawId, vo);
    }
    return vo;
}

@Override
public void warmupDrawCache(Long drawId) {
    // ...existing logic...
    if (detailVO != null) {
        cacheClient.setLogicalExpireValue(detailKey, detailVO, Duration.ofMinutes(10), Duration.ofHours(2));
        drawDetailLocalCache.put(drawId, detailVO);
    }
}

private void clearDrawCache(Long drawId) {
    if (drawId == null) {
        return;
    }
    cacheClient.evict(RedisKeyConstant.drawDetailKey(drawId));
    // L1 invalidate must happen after L2 delete
    drawDetailLocalCache.invalidate(drawId);
    drawRedisService.deleteForDraw(drawId);
}

@Override
public void deleteById(Long id) {
    drawMapper.deleteById(id);
    clearDrawCache(id);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo -Dtest=DrawServiceImplLocalCacheTest
```
Expected: **PASS**.

- [ ] **Step 5: Commit**

```bash
git add su-server/src/test/java/com/su/service/DrawServiceImplLocalCacheTest.java \
        su-server/src/main/java/com/su/config/LocalCacheConfiguration.java \
        su-server/src/main/java/com/su/service/impl/DrawServiceImpl.java
git commit -m "feat: add L1 cache for draw detail"
```

---

## Chunk 3: Integration Verification (Optional, Redis Required)

### Task 3: Integration test for L1 TTL + L2 source of truth

**Files:**
- Create: `su-server/src/test/java/com/su/test/AirdropMetaMultiLevelCacheIT.java`

- [ ] **Step 1: Write the integration test**

```java
package com.su.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.su.constant.RedisKeyConstant;
import com.su.service.airdrop.support.AirdropMeta;
import com.su.service.airdrop.support.AirdropRedisService;
import com.su.utils.cache.CacheClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfSystemProperty(named = "it.redis.enabled", matches = "true")
class AirdropMetaMultiLevelCacheIT {

    @Test
    void l1TtlAndRedisSourceOfTruth() throws Exception {
        String host = System.getProperty("it.redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("it.redis.port", "6379"));
        String password = System.getProperty("it.redis.password");
        int db = Integer.parseInt(System.getProperty("it.redis.db", "1"));

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            config.setPassword(RedisPassword.of(password));
        }
        config.setDatabase(db);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Executor direct = Runnable::run;
        CacheClient cacheClient = new CacheClient(template, mapper, direct);
        Cache<Long, AirdropMeta> localCache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMillis(300))
                .build();
        AirdropRedisService service = new AirdropRedisService(template, mapper, cacheClient, localCache);

        Long id = 42L;
        AirdropMeta v1 = AirdropMeta.builder().id(id).title("v1").build();
        cacheClient.setLogicalExpireValue(RedisKeyConstant.airdropMetaKey(id), v1, Duration.ofMinutes(10), Duration.ofHours(2));

        assertEquals("v1", service.getMeta(id).getTitle());
        assertEquals("v1", localCache.getIfPresent(id).getTitle());

        AirdropMeta v2 = AirdropMeta.builder().id(id).title("v2").build();
        cacheClient.setLogicalExpireValue(RedisKeyConstant.airdropMetaKey(id), v2, Duration.ofMinutes(10), Duration.ofHours(2));

        assertEquals("v1", service.getMeta(id).getTitle());

        Thread.sleep(400);
        assertEquals("v2", service.getMeta(id).getTitle());

        factory.destroy();
    }
}
```

- [ ] **Step 2: Run test to verify it passes (only if Redis available)**

Run:
```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo -Dtest=AirdropMetaMultiLevelCacheIT \
  -Dit.redis.enabled=true -Dit.redis.host=127.0.0.1 -Dit.redis.port=6379 -Dit.redis.db=1
```
Expected: **PASS** (requires reachable Redis). If Redis is unavailable, skip this step.

- [ ] **Step 3: Commit**

```bash
git add su-server/src/test/java/com/su/test/AirdropMetaMultiLevelCacheIT.java
git commit -m "test: add multi-level cache integration test"
```

---

## Final Verification

- [ ] Run full test suite:

```bash
mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo
```

Expected: **BUILD SUCCESS**.
