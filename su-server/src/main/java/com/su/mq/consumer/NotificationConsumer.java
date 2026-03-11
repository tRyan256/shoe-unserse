package com.su.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.component.websocket.WebSocketServer;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.dto.message.MessageType;
import com.su.dto.message.NotificationMessage;
import com.su.entity.ExperienceNotification;
import com.su.mapper.ExperienceNotificationMapper;
import com.su.utils.cache.CacheClient;
import com.su.vo.ExperienceNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 通知消费者
 * 负责处理通知消息，通过WebSocket推送实时通知，处理用户离线情况
 */
@Component
@Slf4j
public class NotificationConsumer {
    
    private final WebSocketServer webSocketServer;
    private final ExperienceNotificationMapper notificationMapper;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;
    
    // 消息处理记录保留时间（7天）
    private static final Duration PROCESSED_MESSAGE_TTL = Duration.ofDays(7);
    
    public NotificationConsumer(
            WebSocketServer webSocketServer,
            ExperienceNotificationMapper notificationMapper,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.webSocketServer = webSocketServer;
        this.notificationMapper = notificationMapper;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 处理通知消息
     * 1. 幂等性检查（防止重复处理）
     * 2. 尝试通过WebSocket推送通知
     * 3. 如果用户离线，存储通知到数据库
     * 4. 记录消息已处理状态
     * 
     * @param message 通知消息
     */
    @RabbitListener(queues = RabbitMqConstant.EXPERIENCE_NOTIFICATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class)
    public void handleMessage(NotificationMessage message) {
        if (message == null || message.getMessageId() == null) {
            log.warn("收到空的通知消息或缺少messageId");
            return;
        }
        
        try {
            log.info("开始处理通知消息，messageId={}, type={}, receiverId={}", 
                    message.getMessageId(), message.getType(), message.getReceiverId());
            
            // 幂等性检查：检查消息是否已处理
            if (isMessageProcessed(message.getMessageId())) {
                log.info("通知消息已处理过，跳过，messageId={}", message.getMessageId());
                return;
            }
            
            // 验证消息内容
            if (!validateMessage(message)) {
                log.error("通知消息验证失败，messageId={}", message.getMessageId());
                markMessageAsProcessed(message.getMessageId());
                return;
            }
            
            // 构建通知VO对象用于WebSocket推送
            // 先保存通知到数据库，确保产生通知ID
            ExperienceNotification notification = saveNotificationToDatabase(message);

            // 构建通知VO对象用于WebSocket推送（包含DB生成的ID）
            ExperienceNotificationVO notificationVO = buildNotificationVO(message, notification);

            // 提交后再推送与标记已处理，避免事务回滚导致“幽灵通知”
            runAfterCommit(() -> {
                boolean pushed = pushNotificationViaWebSocket(message.getReceiverId(), notificationVO);
                markMessageAsProcessed(message.getMessageId());
                log.info("通知消息处理完成，messageId={}, pushed={}", message.getMessageId(), pushed);
            });
            
        } catch (Exception e) {
            log.error("处理通知消息失败，messageId={}, type={}, receiverId={}", 
                    message.getMessageId(), message.getType(), message.getReceiverId(), e);
            // 抛出异常触发重试机制
            throw new RuntimeException("处理通知消息失败", e);
        }
    }
    
    /**
     * 获取消息处理记录的缓存键
     *
     * @param messageId 消息ID
     * @return 缓存键
     */
    private String getProcessedMessageKey(String messageId) {
        // 使用统一的缓存键管理
        return RedisKeyConstant.experienceNotificationProcessedKey(messageId);
    }

    /**
     * 检查消息是否已处理（幂等性检查）
     *
     * @param messageId 消息ID
     * @return true表示已处理，false表示未处理
     */
    private boolean isMessageProcessed(String messageId) {
        try {
            String key = getProcessedMessageKey(messageId);
            return cacheClient.exists(key);
        } catch (Exception e) {
            log.warn("检查消息处理状态失败，messageId={}", messageId, e);
            // Redis异常时，为了安全起见，假设未处理
            return false;
        }
    }

    /**
     * 标记消息已处理
     *
     * @param messageId 消息ID
     */
    private void markMessageAsProcessed(String messageId) {
        try {
            String key = getProcessedMessageKey(messageId);
            cacheClient.set(key, "1", PROCESSED_MESSAGE_TTL);
        } catch (Exception e) {
            log.error("标记消息已处理失败，messageId={}", messageId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 验证消息内容
     * 
     * @param message 通知消息
     * @return true表示验证通过，false表示验证失败
     */
    private boolean validateMessage(NotificationMessage message) {
        if (message.getType() == null) {
            log.error("通知类型为空");
            return false;
        }
        if (message.getReceiverId() == null) {
            log.error("接收者ID为空");
            return false;
        }
        if (message.getSenderId() == null) {
            log.error("发送者ID为空");
            return false;
        }
        
        // 根据不同的通知类型验证必需字段
        switch (message.getType()) {
            case POST_LIKED:
            case POST_COMMENTED:
                if (message.getPostId() == null) {
                    log.error("心得相关通知缺少postId");
                    return false;
                }
                break;
            case COMMENT_REPLIED:
            case COMMENT_LIKED:
                if (message.getCommentId() == null) {
                    log.error("评论相关通知缺少commentId");
                    return false;
                }
                break;
            case USER_FOLLOWED:
                // 关注通知不需要额外字段
                break;
            default:
                log.error("未知的通知类型: {}", message.getType());
                return false;
        }
        
        return true;
    }
    
    /**
     * 构建通知VO对象
     * 
     * @param message 通知消息
     * @return 通知VO对象
     */
    private ExperienceNotificationVO buildNotificationVO(NotificationMessage message, ExperienceNotification notification) {
        return ExperienceNotificationVO.builder()
                .id(notification.getId())
                .type(convertMessageTypeToNotificationType(message.getType()))
                .userId(message.getReceiverId())
                .sourceUserId(message.getSenderId())
                .postId(message.getPostId())
                .commentId(message.getCommentId())
                .content(message.getContent())
                .isRead(0)
                .createTime(notification.getCreateTime())
                .build();
    }
    
    /**
     * 通过WebSocket推送通知
     * 
     * @param userId 用户ID
     * @param notificationVO 通知VO对象
     * @return true表示推送成功，false表示用户离线
     */
    private boolean pushNotificationViaWebSocket(Long userId, ExperienceNotificationVO notificationVO) {
        try {
            // 将通知对象转换为JSON字符串
            String notificationJson = objectMapper.writeValueAsString(notificationVO);
            
            // 通过WebSocket推送给指定用户
            // WebSocketServer使用userId作为sid
            webSocketServer.sendToClient(String.valueOf(userId), notificationJson);
            
            log.debug("WebSocket通知推送成功，userId={}", userId);
            return true;
            
        } catch (Exception e) {
            log.warn("WebSocket通知推送失败，用户可能离线，userId={}", userId, e);
            return false;
        }
    }
    
    /**
     * 保存通知到数据库
     * 用于用户离线时存储通知，或作为通知历史记录
     * 
     * @param message 通知消息
     */
    private ExperienceNotification saveNotificationToDatabase(NotificationMessage message) {
        try {
            ExperienceNotification notification = ExperienceNotification.builder()
                    .userId(message.getReceiverId())
                    .type(convertMessageTypeToNotificationType(message.getType()))
                    .sourceUserId(message.getSenderId())
                    .postId(message.getPostId())
                    .commentId(message.getCommentId())
                    .content(message.getContent())
                    .isRead(0)
                    .createTime(LocalDateTime.now())
                    .build();
            
            notificationMapper.insert(notification);
            log.debug("通知已保存到数据库，userId={}, type={}", 
                    message.getReceiverId(), message.getType());
            return notification;
        } catch (Exception e) {
            log.error("保存通知到数据库失败，messageId={}", message.getMessageId(), e);
            throw new RuntimeException("保存通知到数据库失败", e);
        }
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
    
    /**
     * 将消息类型转换为通知类型
     * 
     * @param messageType 消息类型
     * @return 通知类型
     */
    private Integer convertMessageTypeToNotificationType(MessageType messageType) {
        switch (messageType) {
            case POST_LIKED:
                return ExperienceNotification.TYPE_POST_LIKED;
            case POST_COMMENTED:
                return ExperienceNotification.TYPE_POST_COMMENTED;
            case COMMENT_REPLIED:
                return ExperienceNotification.TYPE_COMMENT_REPLIED;
            case COMMENT_LIKED:
                return ExperienceNotification.TYPE_COMMENT_LIKED;
            case USER_FOLLOWED:
                return ExperienceNotification.TYPE_USER_FOLLOWED;
            default:
                throw new IllegalArgumentException("未知的消息类型: " + messageType);
        }
    }
}
