package com.su.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 回复评论请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceReplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID（一级评论ID）
     */
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容长度不能超过500字符")
    private String content;

    /**
     * 被回复者ID
     */
    @NotNull(message = "被回复者ID不能为空")
    private Long targetUserId;

    /**
     * 被回复的回复ID（可选，回复回复时使用）
     */
    private Long targetReplyId;
}
