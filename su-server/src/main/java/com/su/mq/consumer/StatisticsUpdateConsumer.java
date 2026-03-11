package com.su.mq.consumer;

import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.dto.message.MessageType;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.mapper.UserMapper;
import com.su.utils.cache.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Component
public class StatisticsUpdateConsumer {
    
    private final UserMapper userMapper;
    private final CacheClient cacheClient;
    
    private static final Duration MESSAGE_PROCESSED_TTL = Duration.ofDays(7);
    
    public StatisticsUpdateConsumer(
            UserMapper userMapper,
            CacheClient cacheClient
    ) {
        this.userMapper = userMapper;
        this.cacheClient = cacheClient;
    }
    
    @RabbitListener(queues = RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_QUEUE, 
                    containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(StatisticsUpdateMessage message) {
        if (message == null || message.getMessageId() == null) {
            log.warn("收到空的统计更新消息或缺少messageId");
            return;
        }
        
        try {
            log.info("开始处理统计更新消息，messageId={}, type={}, userId={}, targetUserId={}", 
                    message.getMessageId(), message.getType(), message.getUserId(), message.getTargetUserId());
            
            if (isMessageProcessed(message.getMessageId())) {
                log.info("消息已处理过，跳过，messageId={}", message.getMessageId());
                return;
            }
            
            processMessage(message);
            
            markMessageProcessed(message.getMessageId());
            
            log.info("统计更新消息处理成功，messageId={}, type={}", 
                    message.getMessageId(), message.getType());
            
        } catch (Exception e) {
            log.error("统计更新消息处理失败，messageId={}, type={}", 
                    message.getMessageId(), message.getType(), e);
            throw new RuntimeException("统计更新消息处理失败", e);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    protected void processMessage(StatisticsUpdateMessage message) {
        MessageType type = message.getType();
        
        if (type == null) {
            log.warn("消息类型为空，messageId={}", message.getMessageId());
            return;
        }
        
        switch (type) {
            case POST_LIKE:
                handlePostLike(message);
                break;
            case POST_UNLIKE:
                handlePostUnlike(message);
                break;
            case USER_FOLLOW:
                handleUserFollow(message);
                break;
            case USER_UNFOLLOW:
                handleUserUnfollow(message);
                break;
            case POST_CREATE:
                handlePostCreate(message);
                break;
            case POST_DELETE:
                handlePostDelete(message);
                break;
            case COMMENT_LIKE:
                handleCommentLike(message);
                break;
            case COMMENT_UNLIKE:
                handleCommentUnlike(message);
                break;
            default:
                log.warn("未知的消息类型，messageId={}, type={}", message.getMessageId(), type);
        }
    }
    
    private void handlePostLike(StatisticsUpdateMessage message) {
        Long targetUserId = message.getTargetUserId();
        if (targetUserId == null) {
            log.warn("点赞消息缺少targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateLikedCount(targetUserId, 1);
        log.debug("增加用户被点赞数，userId={}", targetUserId);
        
        evictUserStatisticsCache(targetUserId);
    }
    
    private void handlePostUnlike(StatisticsUpdateMessage message) {
        Long targetUserId = message.getTargetUserId();
        if (targetUserId == null) {
            log.warn("取消点赞消息缺少targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateLikedCount(targetUserId, -1);
        log.debug("减少用户被点赞数，userId={}", targetUserId);
        
        evictUserStatisticsCache(targetUserId);
    }
    
    private void handleUserFollow(StatisticsUpdateMessage message) {
        Long followerId = message.getUserId();
        Long followeeId = message.getTargetUserId();
        
        if (followerId == null || followeeId == null) {
            log.warn("关注消息缺少userId或targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateFollowerCount(followeeId, 1);
        log.debug("增加用户粉丝数，userId={}", followeeId);
        
        userMapper.updateFollowingCount(followerId, 1);
        log.debug("增加用户关注数，userId={}", followerId);
        
        evictUserStatisticsCache(followerId);
        evictUserStatisticsCache(followeeId);
    }
    
    private void handleUserUnfollow(StatisticsUpdateMessage message) {
        Long followerId = message.getUserId();
        Long followeeId = message.getTargetUserId();
        
        if (followerId == null || followeeId == null) {
            log.warn("取消关注消息缺少userId或targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateFollowerCount(followeeId, -1);
        log.debug("减少用户粉丝数，userId={}", followeeId);
        
        userMapper.updateFollowingCount(followerId, -1);
        log.debug("减少用户关注数，userId={}", followerId);
        
        evictUserStatisticsCache(followerId);
        evictUserStatisticsCache(followeeId);
    }
    
    private void handlePostCreate(StatisticsUpdateMessage message) {
        Long userId = message.getUserId();
        if (userId == null) {
            log.warn("创建心得消息缺少userId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updatePostCount(userId, 1);
        log.debug("增加用户发布心得数，userId={}", userId);
        
        evictUserStatisticsCache(userId);
    }
    
    private void handlePostDelete(StatisticsUpdateMessage message) {
        Long userId = message.getUserId();
        if (userId == null) {
            log.warn("删除心得消息缺少userId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updatePostCount(userId, -1);
        log.debug("减少用户发布心得数，userId={}", userId);
        
        evictUserStatisticsCache(userId);
    }
    
    private void handleCommentLike(StatisticsUpdateMessage message) {
        Long targetUserId = message.getTargetUserId();
        if (targetUserId == null) {
            log.warn("评论点赞消息缺少targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateLikedCount(targetUserId, 1);
        log.debug("增加用户被点赞数（评论点赞），userId={}", targetUserId);
        
        evictUserStatisticsCache(targetUserId);
    }
    
    private void handleCommentUnlike(StatisticsUpdateMessage message) {
        Long targetUserId = message.getTargetUserId();
        if (targetUserId == null) {
            log.warn("取消评论点赞消息缺少targetUserId，messageId={}", message.getMessageId());
            return;
        }
        
        userMapper.updateLikedCount(targetUserId, -1);
        log.debug("减少用户被点赞数（取消评论点赞），userId={}", targetUserId);
        
        evictUserStatisticsCache(targetUserId);
    }
    
    private boolean isMessageProcessed(String messageId) {
        try {
            String key = getMessageProcessedKey(messageId);
            return cacheClient.exists(key);
        } catch (Exception e) {
            log.error("检查消息处理状态失败，messageId={}", messageId, e);
            return false;
        }
    }
    
    private void markMessageProcessed(String messageId) {
        try {
            String key = getMessageProcessedKey(messageId);
            cacheClient.set(key, "1", MESSAGE_PROCESSED_TTL);
            log.debug("标记消息已处理，messageId={}", messageId);
        } catch (Exception e) {
            log.error("标记消息处理状态失败，messageId={}", messageId, e);
        }
    }
    
    private String getMessageProcessedKey(String messageId) {
        return RedisKeyConstant.experienceMessageProcessedKey(messageId);
    }
    
    private void evictUserStatisticsCache(Long userId) {
        String key = RedisKeyConstant.userStatisticsKey(userId);
        cacheClient.evict(key);
        log.debug("清除用户统计缓存，userId={}", userId);
    }
}
