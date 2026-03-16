package com.su.service.airdrop.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.utils.cache.CacheClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AirdropRedisService {
    public static final long OUT_OF_STOCK = -2L;
    public static final long ALREADY_CLAIMED = -1L;
    public static final long CACHE_NOT_READY = -3L;
    public static final long INVALID_ARGUMENT = -4L;

    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    private final CacheClient cacheClient;
    private final Cache<Long, AirdropMeta> airdropMetaLocalCache;
    private final DefaultRedisScript<Long> claimScript;

    public AirdropRedisService(
            org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
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

    public void initIfAbsent(Airdrop airdrop) {
        if (airdrop == null || airdrop.getId() == null || airdrop.getRemainCount() == null) {
            return;
        }
        String stockKey = RedisKeyConstant.airdropStockKey(airdrop.getId());
        Duration ttl = ttlForAirdrop(airdrop);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(airdrop.getRemainCount()), ttl);
        if (ok == null || !ok) {
            return;
        }
        cacheClient.expire(RedisKeyConstant.airdropUsersKey(airdrop.getId()), ttl);
    }

    public long claim(Long airdropId, Long userId, Integer initStock, LocalDateTime endTime) {
        if (airdropId == null || userId == null) {
            return INVALID_ARGUMENT;
        }
        Duration ttl = ttlForEndTime(endTime);
        long ttlSeconds = ttl == null ? 0L : Math.max(0L, ttl.toSeconds());
        long init = initStock == null ? 0L : Math.max(0L, initStock.longValue());
        Long result = stringRedisTemplate.execute(
                claimScript,
                List.of(RedisKeyConstant.airdropStockKey(airdropId), RedisKeyConstant.airdropUsersKey(airdropId)),
                String.valueOf(userId),
                String.valueOf(ttlSeconds),
                String.valueOf(init)
        );
        return result == null ? INVALID_ARGUMENT : result;
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

    public void deleteStockAndUsers(Long airdropId) {
        if (airdropId == null) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.airdropStockKey(airdropId));
        cacheClient.evict(RedisKeyConstant.airdropUsersKey(airdropId));
    }

    /**
     * 使用逻辑过期方案预热空投活动元数据（包含优惠券信息）
     * 解决缓存击穿和缓存雪崩问题
     * 优化：直接缓存优惠券完整信息，避免二次查询
     */
    public void warmupAirdropMeta(Airdrop airdrop, Coupon coupon) {
        if (airdrop == null || airdrop.getId() == null) {
            return;
        }

        CouponMeta couponMeta = null;
        if (coupon != null) {
            couponMeta = CouponMeta.builder()
                    .id(coupon.getId())
                    .name(coupon.getName())
                    .type(coupon.getType())
                    .value(coupon.getValue())
                    .minAmount(coupon.getMinAmount())
                    .startTime(coupon.getStartTime())
                    .endTime(coupon.getEndTime())
                    .status(coupon.getStatus())
                    .createTime(coupon.getCreateTime())
                    .updateTime(coupon.getUpdateTime())
                    .build();
        }

        AirdropMeta meta = AirdropMeta.builder()
                .id(airdrop.getId())
                .title(airdrop.getTitle())
                .couponId(airdrop.getCouponId())
                .status(airdrop.getStatus())
                .remainCount(airdrop.getRemainCount())
                .startTime(airdrop.getStartTime())
                .endTime(airdrop.getEndTime())
                .coupon(couponMeta)
                .build();

        String key = RedisKeyConstant.airdropMetaKey(airdrop.getId());
        Duration logicalTtl = logicalTtlForAirdrop(airdrop);
        Duration physicalTtl = logicalTtl.plusHours(2);

        cacheClient.setLogicalExpireValue(key, meta, logicalTtl, physicalTtl);
        if (meta != null) {
            airdropMetaLocalCache.put(airdrop.getId(), meta);
        }
    }

    private Duration ttlForAirdrop(Airdrop airdrop) {
        LocalDateTime now = LocalDateTime.now();
        if (airdrop.getEndTime() != null && airdrop.getEndTime().isAfter(now)) {
            long endMillis = airdrop.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long nowMillis = System.currentTimeMillis();
            long ttlMillis = Math.max(60_000, endMillis - nowMillis) + Duration.ofDays(1).toMillis();
            return Duration.ofMillis(ttlMillis);
        }
        return Duration.ofDays(7);
    }

    private Duration ttlForEndTime(LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (endTime != null && endTime.isAfter(now)) {
            long endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long nowMillis = System.currentTimeMillis();
            long ttlMillis = Math.max(60_000, endMillis - nowMillis) + Duration.ofDays(1).toMillis();
            return Duration.ofMillis(ttlMillis);
        }
        return Duration.ofDays(7);
    }

    /**
     * 计算空投活动的逻辑过期时间
     * 策略：活动结束时间 + 1天
     */
    private Duration logicalTtlForAirdrop(Airdrop airdrop) {
        if (airdrop != null && airdrop.getEndTime() != null) {
            LocalDateTime logicalExpireTime = airdrop.getEndTime().plusDays(1);
            long expireMillis = logicalExpireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long nowMillis = System.currentTimeMillis();
            if (expireMillis > nowMillis) {
                return Duration.ofMillis(expireMillis - nowMillis);
            }
        }
        return Duration.ofMinutes(10);
    }
}
