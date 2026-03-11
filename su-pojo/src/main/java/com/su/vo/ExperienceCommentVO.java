package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 体验心得评论响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 心得ID
     */
    private Long postId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论者昵称
     */
    private String userName;

    /**
     * 评论者头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 是否隐藏 0:否 1:是
     */
    private Integer hidden;

    /**
     * 回复列表
     */
    private List<ExperienceReplyVO> replies;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
