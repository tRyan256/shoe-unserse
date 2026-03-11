package com.su.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis配置类
 * 
 * 配置说明：
 * 1. RedisTemplate配置：提供Redis操作模板
 * 2. 序列化配置：
 *    - Key使用String序列化（可读性好）
 *    - Value使用JSON序列化（支持复杂对象）
 * 3. 连接池配置（application.yml）：
 *    - max-active: 20（最大连接数）
 *    - max-idle: 10（最大空闲连接）
 *    - min-idle: 5（最小空闲连接）
 *    - max-wait: 2000ms（最大等待时间）
 * 
 * 使用场景：
 * - 体验心得详情缓存
 * - 用户统计数据缓存
 * - 点赞/关注状态缓存
 * - 评论列表缓存
 */
@Configuration
public class RedisConfiguration {
    
    /**
     * 配置RedisTemplate
     * 
     * @param connectionFactory Redis连接工厂（由Spring Boot自动配置）
     * @param objectMapper Jackson对象映射器（用于JSON序列化）
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        
        // 配置JSON序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 配置Key序列化：使用String序列化（可读性好，便于调试）
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        
        // 配置Value序列化：使用JSON序列化（支持复杂对象，自动处理类型信息）
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        
        return template;
    }
}
