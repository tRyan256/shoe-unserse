package com.su.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.dto.message.NotificationMessage;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.utils.cache.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列生产者
 * 负责发送统计更新消息和通知消息到RabbitMQ
 */
@Slf4j
@Component
public class MessageQueueProducer {
    
    private final RabbitTemplate rabbitTemplate;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;
    
    public MessageQueueProducer(
            RabbitTemplate rabbitTemplate,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 发送统计更新消息
     * 用于异步更新用户统计数据（粉丝数、点赞数、关注数等）
     * 
     * @param message 统计更新消息
     * @return 是否发送成功
     */
    public boolean sendStatisticsUpdateMessage(StatisticsUpdateMessage message) {
        if (message == null || message.getMessageId() == null) {
            log.error("统计更新消息为空或缺少messageId");
            return false;
        }
        
        try {
            CorrelationData correlationData = new CorrelationData(message.getMessageId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.EXPERIENCE_STATISTICS_EXCHANGE,
                    RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            
            // 等待确认（最多2秒）
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
            boolean success = confirm != null && confirm.isAck();
            
            if (!success) {
                log.error("统计更新消息发送失败，messageId={}, type={}", 
                        message.getMessageId(), message.getType());
                // 记录失败消息到Redis，用于后续补偿
                recordFailedMessage("statistics", message);
            } else {
                log.debug("统计更新消息发送成功，messageId={}, type={}", 
                        message.getMessageId(), message.getType());
            }
            
            return success;
        } catch (Exception e) {
            log.error("发送统计更新消息异常，messageId={}, type={}", 
                    message.getMessageId(), message.getType(), e);
            // 记录失败消息到Redis，用于后续补偿
            recordFailedMessage("statistics", message);
            return false;
        }
    }
    
    /**
     * 发送通知消息
     * 用于通过WebSocket推送实时通知给用户
     * 
     * @param message 通知消息
     * @return 是否发送成功
     */
    public boolean sendNotificationMessage(NotificationMessage message) {
        if (message == null || message.getMessageId() == null) {
            log.error("通知消息为空或缺少messageId");
            return false;
        }
        
        try {
            CorrelationData correlationData = new CorrelationData(message.getMessageId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.EXPERIENCE_NOTIFICATION_EXCHANGE,
                    RabbitMqConstant.EXPERIENCE_NOTIFICATION_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            
            // 等待确认（最多2秒）
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
            boolean success = confirm != null && confirm.isAck();
            
            if (!success) {
                log.error("通知消息发送失败，messageId={}, type={}, receiverId={}", 
                        message.getMessageId(), message.getType(), message.getReceiverId());
                // 记录失败消息到Redis，用于后续补偿
                recordFailedMessage("notification", message);
            } else {
                log.debug("通知消息发送成功，messageId={}, type={}, receiverId={}", 
                        message.getMessageId(), message.getType(), message.getReceiverId());
            }
            
            return success;
        } catch (Exception e) {
            log.error("发送通知消息异常，messageId={}, type={}, receiverId={}", 
                    message.getMessageId(), message.getType(), message.getReceiverId(), e);
            // 记录失败消息到Redis，用于后续补偿
            recordFailedMessage("notification", message);
            return false;
        }
    }
    
    /**
     * 记录失败的消息到Redis
     * 用于后续补偿重试
     * 
     * @param messageType 消息类型（statistics或notification）
     * @param message 消息对象
     */
    private void recordFailedMessage(String messageType, Object message) {
        try {
            String key = RedisKeyConstant.experienceFailedMessageKey(messageType, 
                    message instanceof StatisticsUpdateMessage ? 
                            ((StatisticsUpdateMessage) message).getMessageId() : 
                            ((NotificationMessage) message).getMessageId());
            String value = objectMapper.writeValueAsString(message);
            cacheClient.set(key, value, Duration.ofDays(7));
            log.info("失败消息已记录到Redis，key={}", key);
        } catch (Exception e) {
            log.error("记录失败消息到Redis异常", e);
        }
    }
}
