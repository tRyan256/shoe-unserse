package com.su.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 体验心得评论查询请求DTO
 */
@Data
public class ExperienceCommentQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 心得ID
     */
    @NotNull(message = "心得ID不能为空")
    private Long postId;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Min(value = 10, message = "每页数量不能少于10条")
    @Max(value = 50, message = "每页数量不能超过50条")
    private Integer size = 10;
}
