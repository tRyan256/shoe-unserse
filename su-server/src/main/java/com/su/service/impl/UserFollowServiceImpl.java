package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.constant.RedisKeyConstant;
import com.su.dto.message.MessageType;
import com.su.dto.message.NotificationMessage;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.entity.UserFollow;
import com.su.exception.DuplicateFollowException;
import com.su.exception.SelfFollowException;
import com.su.mapper.UserFollowMapper;
import com.su.mq.producer.MessageQueueProducer;
import com.su.result.PageResult;
import com.su.service.UserFollowService;
import com.su.utils.cache.CacheClient;
import com.su.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户关注服务实现类
 */
@Service
@Slf4j
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private MessageQueueProducer messageQueueProducer;

    /**
     * 关注用户
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    @Override
    @Transactional
    public void followUser(Long followerId, Long followeeId) {
        // 1. 参数验证
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("关注者ID和被关注者ID不能为空");
        }

        // 2. 验证不能关注自己
        if (followerId.equals(followeeId)) {
            throw new SelfFollowException("不能关注自己");
        }

        // 3. 使用互斥锁检查是否已关注（防止并发重复关注）
        String cacheKey = RedisKeyConstant.followStatusKey(followerId, followeeId);
        String lockKey = RedisKeyConstant.lockKey("follow:" + followerId + ":" + followeeId);
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(3));
        
        if (token == null) {
            throw new DuplicateFollowException("操作过于频繁，请稍后再试");
        }
        
        try {
            // 检查缓存中的关注状态
            String cached = cacheClient.get(cacheKey);
            if ("1".equals(cached)) {
                throw new DuplicateFollowException("已关注");
            }

            // 从数据库检查是否已关注
            UserFollow existingFollow = userFollowMapper.getByFollowerIdAndFolloweeId(followerId, followeeId);
            if (existingFollow != null) {
                // 更新缓存
                cacheClient.set(cacheKey, "1", Duration.ofHours(1));
                throw new DuplicateFollowException("已关注");
            }

            // 4. 创建关注记录
            UserFollow follow = UserFollow.builder()
                    .followerId(followerId)
                    .followeeId(followeeId)
                    .createTime(LocalDateTime.now())
                    .build();
            userFollowMapper.insert(follow);
            log.info("创建关注记录成功，followerId={}, followeeId={}", followerId, followeeId);

            // 5. 更新Redis缓存关注状态（1小时）
            cacheClient.set(cacheKey, "1", Duration.ofHours(1));
            log.debug("缓存关注状态，key={}", cacheKey);

            // 6. 发送统计更新消息（异步更新粉丝数和关注数）
            String messageId = "follow_" + followerId + "_" + followeeId + "_" + System.currentTimeMillis();
            StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                    .type(MessageType.USER_FOLLOW)
                    .userId(followerId)
                    .targetUserId(followeeId)
                    .timestamp(System.currentTimeMillis())
                    .messageId(messageId)
                    .build();
            
            boolean sent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
            if (!sent) {
                log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", messageId);
            }

            // 7. 发送通知消息（通知被关注者）
            String notificationId = "notify_follow_" + followerId + "_" + followeeId + "_" + System.currentTimeMillis();
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .type(MessageType.USER_FOLLOWED)
                    .receiverId(followeeId)
                    .senderId(followerId)
                    .content("有用户关注了你")
                    .timestamp(System.currentTimeMillis())
                    .messageId(notificationId)
                    .build();
            
            boolean notificationSent = messageQueueProducer.sendNotificationMessage(notificationMessage);
            if (!notificationSent) {
                log.warn("通知消息发送失败，将通过补偿机制处理，messageId={}", notificationId);
            }
        } finally {
            cacheClient.unlock(lockKey, token);
        }
    }

    /**
     * 取消关注用户
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        // 1. 参数验证
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("关注者ID和被关注者ID不能为空");
        }

        // 2. 检查是否已关注
        UserFollow existingFollow = userFollowMapper.getByFollowerIdAndFolloweeId(followerId, followeeId);
        if (existingFollow == null) {
            log.warn("取消关注失败，用户未关注该用户，followerId={}, followeeId={}", followerId, followeeId);
            return; // 幂等性处理：如果未关注，直接返回成功
        }

        // 3. 删除关注记录
        userFollowMapper.delete(followerId, followeeId);
        log.info("删除关注记录成功，followerId={}, followeeId={}", followerId, followeeId);

        // 4. 删除Redis缓存关注状态
        String cacheKey = RedisKeyConstant.followStatusKey(followerId, followeeId);
        try {
            cacheClient.evict(cacheKey);
            log.debug("删除关注状态缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("删除关注状态缓存失败", e);
        }

        // 5. 发送统计更新消息（异步更新粉丝数和关注数）
        String messageId = "unfollow_" + followerId + "_" + followeeId + "_" + System.currentTimeMillis();
        StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                .type(MessageType.USER_UNFOLLOW)
                .userId(followerId)
                .targetUserId(followeeId)
                .timestamp(System.currentTimeMillis())
                .messageId(messageId)
                .build();
        
        boolean sent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
        if (!sent) {
            log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", messageId);
        }
    }

    /**
     * 查询是否已关注
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        // 1. 参数验证
        if (followerId == null || followeeId == null) {
            return false;
        }

        // 2. 尝试从缓存获取
        String cacheKey = RedisKeyConstant.followStatusKey(followerId, followeeId);
        try {
            String cached = cacheClient.get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取关注状态，key={}", cacheKey);
                return "1".equals(cached);
            }
        } catch (Exception e) {
            log.warn("从缓存读取关注状态失败，降级到数据库查询", e);
        }

        // 3. 从数据库查询
        UserFollow follow = userFollowMapper.getByFollowerIdAndFolloweeId(followerId, followeeId);
        boolean isFollowing = follow != null;

        // 4. 缓存关注状态（1小时）
        try {
            cacheClient.set(cacheKey, isFollowing ? "1" : "0", Duration.ofHours(1));
            log.debug("缓存关注状态，key={}, value={}", cacheKey, isFollowing);
        } catch (Exception e) {
            log.warn("缓存关注状态失败", e);
        }

        return isFollowing;
    }

    /**
     * 查询关注列表（我关注的人）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<UserProfileVO> listFollowing(Long userId, Integer page, Integer size) {
        // 1. 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 10 || size > 50) {
            size = 20; // 默认每页20条
        }

        // 2. 分页查询
        PageHelper.startPage(page, size);
        Page<UserProfileVO> pageResult = userFollowMapper.listFollowing(userId);

        // 3. 返回分页结果
        List<UserProfileVO> list = pageResult.getResult();
        log.debug("查询关注列表成功，userId={}, total={}", userId, pageResult.getTotal());
        
        return new PageResult<>(pageResult.getTotal(), list);
    }

    /**
     * 查询粉丝列表（关注我的人）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<UserProfileVO> listFollowers(Long userId, Integer page, Integer size) {
        // 1. 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 10 || size > 50) {
            size = 20; // 默认每页20条
        }

        // 2. 分页查询
        PageHelper.startPage(page, size);
        Page<UserProfileVO> pageResult = userFollowMapper.listFollowers(userId);

        // 3. 返回分页结果
        List<UserProfileVO> list = pageResult.getResult();
        log.debug("查询粉丝列表成功，userId={}, total={}", userId, pageResult.getTotal());
        
        return new PageResult<>(pageResult.getTotal(), list);
    }
}
