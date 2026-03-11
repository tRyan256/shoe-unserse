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
 * 发表评论请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 心得ID
     */
    @NotNull(message = "心得ID不能为空")
    private Long postId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500字符")
    private String content;
}
