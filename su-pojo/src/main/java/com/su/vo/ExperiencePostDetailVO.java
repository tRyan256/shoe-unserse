package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 体验心得详情响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperiencePostDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 心得ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户统计信息
     */
    private UserProfileVO userProfile;

    /**
     * 心得内容
     */
    private String content;

    /**
     * 商品类型 1:SPU 2:组合包
     */
    private Integer productType;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 图片路径列表
     */
    private List<String> images;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已关注作者
     */
    private Boolean isFollowed;

    /**
     * 是否隐藏 0:未隐藏 1:已隐藏
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
