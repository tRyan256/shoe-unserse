package com.su.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 体验心得查询请求DTO
 */
@Data
public class ExperiencePostQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品类型 1:SPU 2:组合包
     */
    @NotNull(message = "商品类型不能为空")
    private Integer productType;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 排序方式 time:按时间 like:按点赞数
     */
    private String sortBy = "time";

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
