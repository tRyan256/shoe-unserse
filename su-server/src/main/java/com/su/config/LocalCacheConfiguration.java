package com.su.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.su.entity.Category;
import com.su.service.airdrop.support.AirdropMeta;
import com.su.vo.DrawDetailVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class LocalCacheConfiguration {
    @Bean
    public Cache<Integer, List<Category>> categoryLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public Cache<Long, AirdropMeta> airdropMetaLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public Cache<Long, DrawDetailVO> drawDetailLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .build();
    }
}
