package com.su.dto.message;

/**
 * 消息类型枚举
 * 用于RabbitMQ消息队列中的消息分类
 */
public enum MessageType {
    
    // 统计更新消息类型
    /**
     * 心得点赞
     */
    POST_LIKE,
    
    /**
     * 取消点赞
     */
    POST_UNLIKE,
    
    /**
     * 用户关注
     */
    USER_FOLLOW,
    
    /**
     * 取消关注
     */
    USER_UNFOLLOW,
    
    /**
     * 创建心得
     */
    POST_CREATE,
    
    /**
     * 删除心得
     */
    POST_DELETE,
    
    /**
     * 评论点赞
     */
    COMMENT_LIKE,
    
    /**
     * 取消评论点赞
     */
    COMMENT_UNLIKE,
    
    // 通知消息类型
    /**
     * 心得被点赞
     */
    POST_LIKED,
    
    /**
     * 心得被评论
     */
    POST_COMMENTED,
    
    /**
     * 评论被回复
     */
    COMMENT_REPLIED,
    
    /**
     * 评论被点赞
     */
    COMMENT_LIKED,
    
    /**
     * 被用户关注
     */
    USER_FOLLOWED
}
