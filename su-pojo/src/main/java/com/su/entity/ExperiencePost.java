package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体验心得实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperiencePost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 心得内容
     */
    @NotBlank(message = "心得内容不能为空")
    private String content;

    /**
     * 商品类型 1:SPU 2:组合包
     */
    @NotNull(message = "商品类型不能为空")
    private Integer productType;

    /**
     * 商品ID(spu_id或bundle_id)
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 图片路径JSON数组
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

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
