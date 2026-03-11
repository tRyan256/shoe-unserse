package com.su.utils.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 统一缓存工具类，整合分布式锁功能
 * 解决缓存三大问题：穿透、击穿、雪崩
 */
@Slf4j
@Component
public class CacheClient {

    private static final String NULL_VALUE = "__NULL__";

    /** Lua 脚本：仅当 value 匹配时才删除 key（原子解锁） */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Executor cacheRebuildExecutor;

    public CacheClient(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            Executor cacheRebuildExecutor
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    // =========================================================
    // 分布式锁方法（整合自 RedisLockService）
    // =========================================================

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的 Redis key
     * @param expire  锁的持有超时时间
     * @return 成功返回 token（解锁凭证），失败返回 null
     */
    public String tryLock(String lockKey, Duration expire) {
        String token = UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, token, expire);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    /**
     * 释放分布式锁（Lua 脚本保证原子性）
     *
     * @param lockKey 锁的 Redis key
     * @param token   加锁时返回的 token
     * @return 是否成功释放
     */
    public boolean unlock(String lockKey, String token) {
        Long result = stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        return Objects.equals(result, 1L);
    }

    // =========================================================
    // 简单 TTL 缓存（互斥锁 + 空值防穿透 + 布隆过滤器）
    // =========================================================

    /**
     * 简单的TTL缓存查询，支持 Class 类型
     * 适用场景：鞋款详情、门店详情等数据变更时直接删除缓存的场景
     */
    public <T> T queryWithSimpleTTL(
            String key,
            String lockKey,
            Class<T> type,
            Supplier<T> dbFallback,
            Duration ttl,
            Duration nullTtl,
            RBloomFilter<Long> bloom,
            Long bloomId
    ) {
        // 1. 查询Redis缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (NULL_VALUE.equals(cached)) {
                return null;
            }
            return readJson(cached, type);
        }

        // 2. 布隆过滤器判断（防止缓存穿透）
        if (bloom != null && bloomId != null && !bloom.contains(bloomId)) {
            setNull(key, nullTtl);
            return null;
        }

        // 3. 获取互斥锁重建缓存
        String token = tryLock(lockKey, Duration.ofSeconds(5));
        if (token == null) {
            // 锁失败，等待一下再试
            for (int i = 0; i < 5; i++) {
                sleepMillis(50L);
                cached = stringRedisTemplate.opsForValue().get(key);
                if (cached != null) {
                    return NULL_VALUE.equals(cached) ? null : readJson(cached, type);
                }
            }
            // 仍然没有，直接查数据库
            T fallback = dbFallback.get();
            if (fallback == null) {
                setNull(key, nullTtl);
                return null;
            }
            setJson(key, fallback, ttl);
            return fallback;
        }

        // 4. 获取锁成功，双重检查后查询数据库
        try {
            cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                return NULL_VALUE.equals(cached) ? null : readJson(cached, type);
            }
            T value = dbFallback.get();
            if (value == null) {
                setNull(key, nullTtl);
                return null;
            }
            setJson(key, value, ttl);
            return value;
        } finally {
            unlock(lockKey, token);
        }
    }

    /**
     * 简单的TTL缓存查询，支持 JavaType（用于集合类型）
     */
    public <T> T queryWithSimpleTTL(
            String key,
            String lockKey,
            JavaType type,
            Supplier<T> dbFallback,
            Duration ttl,
            Duration nullTtl,
            RBloomFilter<Long> bloom,
            Long bloomId
    ) {
        // 1. 查询Redis缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (NULL_VALUE.equals(cached)) {
                return null;
            }
            return readJson(cached, type);
        }

        // 2. 布隆过滤器判断（防止缓存穿透）
        if (bloom != null && bloomId != null && !bloom.contains(bloomId)) {
            setNull(key, nullTtl);
            return null;
        }

        // 3. 获取互斥锁重建缓存
        String token = tryLock(lockKey, Duration.ofSeconds(5));
        if (token == null) {
            for (int i = 0; i < 5; i++) {
                sleepMillis(50L);
                cached = stringRedisTemplate.opsForValue().get(key);
                if (cached != null) {
                    return NULL_VALUE.equals(cached) ? null : readJson(cached, type);
                }
            }
            T fallback = dbFallback.get();
            if (fallback == null) {
                setNull(key, nullTtl);
                return null;
            }
            setJson(key, fallback, ttl);
            return fallback;
        }

        // 4. 获取锁成功，双重检查后查询数据库
        try {
            cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                return NULL_VALUE.equals(cached) ? null : readJson(cached, type);
            }
            T value = dbFallback.get();
            if (value == null) {
                setNull(key, nullTtl);
                return null;
            }
            setJson(key, value, ttl);
            return value;
        } finally {
            unlock(lockKey, token);
        }
    }

    // =========================================================
    // 逻辑过期缓存（异步重建 + 空值防穿透 + 布隆过滤器）
    // =========================================================

    /**
     * 逻辑过期缓存查询，解决缓存击穿和缓存穿透问题
     * 适用场景：活动信息预热，需要预先缓存并异步更新的场景
     */
    public <T> T queryWithLogicalExpire(
            String key,
            String lockKey,
            Class<T> type,
            Supplier<T> dbFallback,
            Duration logicalTtl,
            Duration physicalTtl,
            Duration nullTtl,
            RBloomFilter<Long> bloom,
            Long bloomId
    ) {
        // 1. 查询Redis缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null) {
            // 布隆过滤器判断
            if (bloom != null && bloomId != null && !bloom.contains(bloomId)) {
                setNull(key, nullTtl);
                return null;
            }
            // 缓存不存在，获取锁后查询数据库（首次预热）
            String token = tryLock(lockKey, Duration.ofSeconds(5));
            if (token == null) {
                for (int i = 0; i < 5; i++) {
                    sleepMillis(50L);
                    cached = stringRedisTemplate.opsForValue().get(key);
                    if (cached != null) {
                        if (NULL_VALUE.equals(cached)) {
                            return null;
                        }
                        LogicalExpire<T> logical = readLogicalExpire(cached, type);
                        return logical == null ? null : logical.getData();
                    }
                }
                T fallback = dbFallback.get();
                if (fallback == null) {
                    setNull(key, nullTtl);
                    return null;
                }
                setLogicalExpire(key, fallback, logicalTtl, physicalTtl);
                return fallback;
            }
            try {
                // 双重检查
                cached = stringRedisTemplate.opsForValue().get(key);
                if (cached != null) {
                    if (NULL_VALUE.equals(cached)) {
                        return null;
                    }
                    LogicalExpire<T> logical = readLogicalExpire(cached, type);
                    return logical == null ? null : logical.getData();
                }
                T value = dbFallback.get();
                if (value == null) {
                    setNull(key, nullTtl);
                    return null;
                }
                setLogicalExpire(key, value, logicalTtl, physicalTtl);
                return value;
            } finally {
                unlock(lockKey, token);
            }
        }

        if (NULL_VALUE.equals(cached)) {
            return null;
        }

        // 2. 解析逻辑过期数据
        LogicalExpire<T> logical = readLogicalExpire(cached, type);
        if (logical == null || logical.getData() == null) {
            return null;
        }

        // 3. 检查是否逻辑过期
        long now = System.currentTimeMillis();
        if (logical.getExpireAtEpochMilli() > now) {
            // 未过期，直接返回
            return logical.getData();
        }

        // 4. 已过期，尝试异步重建，但仍返回旧数据（避免缓存击穿）
        String token = tryLock(lockKey, Duration.ofSeconds(5));
        if (token != null) {
            cacheRebuildExecutor.execute(() -> {
                try {
                    T fresh = dbFallback.get();
                    if (fresh == null) {
                        setNull(key, nullTtl);
                        return;
                    }
                    setLogicalExpire(key, fresh, logicalTtl, physicalTtl);
                } finally {
                    unlock(lockKey, token);
                }
            });
        }

        // 返回旧数据
        return logical.getData();
    }

    // =========================================================
    // 公开的预热写入方法
    // =========================================================

    /**
     * 预热：将数据以逻辑过期方式写入缓存（供定时任务/启动预热使用）
     */
    public void setLogicalExpireValue(String key, Object value, Duration logicalTtl, Duration physicalTtl) {
        if (key == null || key.isBlank() || value == null || logicalTtl == null || physicalTtl == null) {
            return;
        }
        setLogicalExpire(key, value, logicalTtl, physicalTtl);
    }

    // =========================================================
    // 私有工具方法
    // =========================================================

    private void setJson(String key, Object value, Duration ttl) {
        // 在基础TTL上增加随机抖动（防止缓存雪崩）
        Duration realTtl = withJitter(ttl, Duration.ofMinutes(3));
        stringRedisTemplate.opsForValue().set(key, writeJson(value), realTtl);
    }

    private void setLogicalExpire(String key, Object value, Duration logicalTtl, Duration physicalTtl) {
        LogicalExpire<Object> wrapper = new LogicalExpire<>(value, System.currentTimeMillis() + logicalTtl.toMillis());
        // 物理TTL也加上抖动（防止缓存雪崩）
        Duration realTtl = withJitter(physicalTtl, Duration.ofMinutes(10));
        stringRedisTemplate.opsForValue().set(key, writeJson(wrapper), realTtl);
    }

    private void setNull(String key, Duration nullTtl) {
        // 空值TTL也加上抖动
        Duration realTtl = withJitter(nullTtl, Duration.ofSeconds(30));
        stringRedisTemplate.opsForValue().set(key, NULL_VALUE, realTtl);
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T readJson(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> LogicalExpire<T> readLogicalExpire(String json, Class<T> type) {
        try {
            JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(LogicalExpire.class, type);
            return objectMapper.readValue(json, wrapperType);
        } catch (Exception e) {
            return null;
        }
    }

    private Duration withJitter(Duration base, Duration maxJitter) {
        if (base == null) {
            return null;
        }
        long jitterMillis = maxJitter == null ? 0L : Math.max(0L, maxJitter.toMillis());
        if (jitterMillis == 0L) {
            return base;
        }
        long extra = ThreadLocalRandom.current().nextLong(jitterMillis + 1);
        return base.plusMillis(extra);
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public <T> T getLogicalExpireValue(String key, Class<T> type) {
        if (key == null || key.isBlank() || type == null) {
            return null;
        }
        String cached = get(key);
        if (cached == null || NULL_VALUE.equals(cached)) {
            return null;
        }
        LogicalExpire<T> logical = readLogicalExpire(cached, type);
        return logical == null ? null : logical.getData();
    }
    // =========================================================
    // 缓存删除方法（统一使用异步删除）
    // =========================================================

    /**
     * 删除单个缓存key（异步删除，不阻塞Redis）
     * 支持缓存降级：Redis异常时记录日志但不抛出异常
     *
     * @param key 缓存键
     */
    public void evict(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.unlink(key);
            log.debug("已删除缓存，key={}", key);
        } catch (Exception e) {
            log.error("Redis evict operation failed for key: {}, error: {}", key, e.getMessage(), e);
        }
    }
    /**
     * 兼容旧测试代码的删除别名。
     */
    public void delete(String key) {
        evict(key);
    }

    /**
     * 批量删除缓存key（异步删除，不阻塞Redis）
     * 支持缓存降级：Redis异常时记录日志但不抛出异常
     *
     * @param keys 缓存键集合
     */
    public void evictBatch(java.util.Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.unlink(keys);
            log.debug("已批量删除缓存，共 {} 个key", keys.size());
        } catch (Exception e) {
            log.error("Redis evictBatch operation failed, keys count: {}, error: {}", keys.size(), e.getMessage(), e);
        }
    }

    // =========================================================
    // 简单缓存操作（带降级和异常处理）
    // =========================================================

    /**
     * 获取缓存值（String类型）
     * 支持缓存降级：Redis异常时返回null而不抛出异常
     *
     * @param key 缓存键
     * @return 缓存值，不存在或异常时返回null
     */
    public String get(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get operation failed for key: {}, error: {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置缓存值（String类型）
     * 支持缓存降级：Redis异常时记录日志但不抛出异常
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     */
    public void set(String key, String value, Duration ttl) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        try {
            if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
                Duration realTtl = withJitter(ttl, Duration.ofMinutes(1));
                stringRedisTemplate.opsForValue().set(key, value, realTtl);
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("Redis set operation failed for key: {}, error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 设置缓存对象（自动序列化）
     * 支持缓存降级：Redis异常时记录日志但不抛出异常
     *
     * @param key   缓存键
     * @param value 缓存对象
     * @param ttl   过期时间
     */
    public void set(String key, Object value, Duration ttl) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        try {
            String json = writeJson(value);
            set(key, json, ttl);
        } catch (Exception e) {
            log.error("Redis set operation failed for key: {}, error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 设置缓存过期时间
     * 支持缓存降级：Redis异常时记录日志但不抛出异常
     *
     * @param key 缓存键
     * @param ttl 过期时间
     * @return 是否设置成功（异常时返回false）
     */
    public boolean expire(String key, Duration ttl) {
        if (key == null || key.isBlank() || ttl == null || ttl.isNegative()) {
            return false;
        }
        try {
            Boolean result = stringRedisTemplate.expire(key, ttl);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis expire operation failed for key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查缓存键是否存在
     * 支持缓存降级：Redis异常时返回false
     *
     * @param key 缓存键
     * @return 是否存在（异常时返回false）
     */
    public boolean exists(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        try {
            Boolean result = stringRedisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis exists operation failed for key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

}




