package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体验点赞实体类（心得/评论）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 心得ID（心得点赞时有值）
     */
    private Long postId;

    /**
     * 评论ID（评论点赞时有值）
     */
    private Long commentId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
