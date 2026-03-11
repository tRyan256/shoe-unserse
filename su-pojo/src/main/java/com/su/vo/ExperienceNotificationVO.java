package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体验心得通知响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceNotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 接收者ID
     */
    private Long userId;

    /**
     * 通知类型 1:点赞心得 2:评论心得 3:回复评论 4:点赞评论 5:关注 6:订单通知
     */
    private Integer type;

    /**
     * 触发者ID
     */
    private Long sourceUserId;

    /**
     * 触发者昵称
     */
    private String sourceUserName;

    /**
     * 触发者头像
     */
    private String sourceUserAvatar;

    /**
     * 心得ID
     */
    private Long postId;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读 0:否 1:是
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
