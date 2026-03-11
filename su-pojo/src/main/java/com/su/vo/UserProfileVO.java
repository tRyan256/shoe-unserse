package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户资料响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 粉丝数
     */
    private Integer followerCount;

    /**
     * 关注数
     */
    private Integer followingCount;

    /**
     * 被点赞总数
     */
    private Integer likedCount;

    /**
     * 发布心得数
     */
    private Integer postCount;

    /**
     * 当前用户是否已关注
     */
    private Boolean isFollowed;
}
