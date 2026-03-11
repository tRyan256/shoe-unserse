package com.su.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息
 * 用于通过WebSocket推送实时通知给用户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 通知类型
     * POST_LIKED: 心得被点赞
     * POST_COMMENTED: 心得被评论
     * COMMENT_REPLIED: 评论被回复
     * COMMENT_LIKED: 评论被点赞
     * USER_FOLLOWED: 被用户关注
     */
    private MessageType type;
    
    /**
     * 接收者用户ID
     */
    private Long receiverId;
    
    /**
     * 发送者用户ID（触发通知的用户）
     */
    private Long senderId;
    
    /**
     * 心得ID（与心得相关的通知时使用）
     */
    private Long postId;
    
    /**
     * 评论ID（与评论相关的通知时使用）
     */
    private Long commentId;
    
    /**
     * 回复ID（与回复相关的通知时使用）
     */
    private Long replyId;
    
    /**
     * 通知内容文本
     */
    private String content;
    
    /**
     * 消息时间戳
     */
    private Long timestamp;
    
    /**
     * 消息唯一ID（用于幂等性处理）
     */
    private String messageId;
}
