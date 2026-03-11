package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员端体验心得评论响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceCommentAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long postId;

    private String postTitle;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Integer hidden;

    private LocalDateTime createTime;
}
