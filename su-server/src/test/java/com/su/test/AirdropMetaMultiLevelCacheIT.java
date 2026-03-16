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
