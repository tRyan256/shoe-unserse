package com.su.dto.admin;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员端体验心得查询请求DTO
 */
@Data
public class ExperiencePostAdminQueryDTO implements Serializable {

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
     * 用户名搜索
     */
    private String userName;

    /**
     * 内容关键词搜索
     */
    private String contentKeyword;

    /**
     * 商品类型过滤 1:鞋款 2:组合包 3:抽签 4:空投
     */
    private Integer productType;

    /**
     * 商品名称搜索
     */
    private String productName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 隐藏状态过滤 0:未隐藏 1:已隐藏 null:全部
     */
    private Integer hidden;
}
