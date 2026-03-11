package com.su.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 配置消息代理
     * 启用简单的内存消息代理，用于将消息从服务器发送到客户端
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理，用于向客户端推送消息
        // 客户端订阅路径前缀为 /topic 或 /queue
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 配置客户端发送消息的目的地前缀
        // 客户端发送消息时需要添加 /app 前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置点对点消息的前缀
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 配置WebSocket端点
     * 注册STOMP协议的端点，客户端通过此端点连接WebSocket服务
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册体验心得通知端点
        registry.addEndpoint("/ws/experience/notifications")
                .setAllowedOriginPatterns("*")  // 允许跨域
                .withSockJS();  // 启用SockJS支持，提供降级选项
    }
}
