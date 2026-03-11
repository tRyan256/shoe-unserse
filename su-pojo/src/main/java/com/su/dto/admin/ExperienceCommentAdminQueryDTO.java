package com.su.dto.admin;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;

/**
 * 管理员端体验心得评论查询请求DTO
 */
@Data
public class ExperienceCommentAdminQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 20;

    /**
     * 心得ID过滤
     */
    private Long postId;

    /**
     * 用户名搜索
     */
    private String userName;

    /**
     * 内容关键词搜索
     */
    private String contentKeyword;

    /**
     * 隐藏状态过滤 0:未隐藏 1:已隐藏 null:全部
     */
    private Integer hidden;
}
