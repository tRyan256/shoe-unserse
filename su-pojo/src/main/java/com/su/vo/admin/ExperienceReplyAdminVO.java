package com.su.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员端评论回复响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceReplyAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回复ID
     */
    private Long id;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 回复者ID
     */
    private Long userId;

    /**
     * 回复者昵称
     */
    private String userName;

    /**
     * 回复者头像
     */
    private String userAvatar;

    /**
     * 被回复者ID
     */
    private Long targetUserId;

    /**
     * 被回复者昵称
     */
    private String targetUserName;

    /**
     * 被回复的回复ID
     */
    private Long targetReplyId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 是否作者回复 0:否 1:是
     */
    private Integer isAuthor;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 隐藏状态 0:未隐藏 1:已隐藏
     */
    private Integer hidden;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
