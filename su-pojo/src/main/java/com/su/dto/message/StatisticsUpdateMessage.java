package com.su.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统计更新消息
 * 用于异步更新用户统计数据（粉丝数、点赞数、关注数等）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsUpdateMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息类型
     * POST_LIKE: 心得点赞
     * POST_UNLIKE: 取消点赞
     * USER_FOLLOW: 用户关注
     * USER_UNFOLLOW: 取消关注
     */
    private MessageType type;
    
    /**
     * 操作发起者用户ID
     */
    private Long userId;
    
    /**
     * 目标用户ID（被点赞者或被关注者）
     */
    private Long targetUserId;
    
    /**
     * 心得ID（点赞相关操作时使用）
     */
    private Long postId;
    
    /**
     * 评论ID（评论点赞相关操作时使用）
     */
    private Long commentId;
    
    /**
     * 消息时间戳
     */
    private Long timestamp;
    
    /**
     * 消息唯一ID（用于幂等性处理）
     */
    private String messageId;
}
