package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知类型常量
     */
    public static final Integer TYPE_POST_LIKED = 1;      // 点赞心得
    public static final Integer TYPE_POST_COMMENTED = 2;  // 评论心得
    public static final Integer TYPE_COMMENT_REPLIED = 3; // 回复评论
    public static final Integer TYPE_COMMENT_LIKED = 4;   // 点赞评论
    public static final Integer TYPE_USER_FOLLOWED = 5;   // 关注
    public static final Integer TYPE_ORDER = 6;           // 订单通知

    /**
     * 主键
     */
    private Long id;

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long userId;

    /**
     * 通知类型 1:点赞心得 2:评论心得 3:回复评论 4:点赞评论 5:关注 6:订单通知
     */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /**
     * 触发者ID
     */
    @NotNull(message = "触发者ID不能为空")
    private Long sourceUserId;

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
