package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心得评论或回复实体类（合并评论和回复）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 心得ID（一级评论时有值）
     */
    private Long postId;

    /**
     * 父评论ID（0表示一级评论，>0表示回复）
     */
    private Long parentId;

    /**
     * 评论者/回复者ID
     */
    private Long userId;

    /**
     * 评论/回复内容
     */
    private String content;

    /**
     * 被回复者ID（回复时有值）
     */
    private Long targetUserId;

    /**
     * 被回复的回复ID（回复回复时有值）
     */
    private Long targetReplyId;

    /**
     * 回复数（一级评论才有）
     */
    private Integer replyCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否作者回复 0:否 1:是
     */
    private Integer isAuthor;

    /**
     * 是否隐藏 0:否 1:是
     */
    private Integer hidden;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
