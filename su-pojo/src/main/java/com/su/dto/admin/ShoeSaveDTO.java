package com.su.dto.admin;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 鞋款保存DTO（支持多分类）
 */
@Data
public class ShoeSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鞋款名称
     */
    @NotBlank(message = "鞋款名称不能为空")
    private String name;

    /**
     * 品牌ID
     */
    @NotNull(message = "品牌ID不能为空")
    private Long brandId;

    /**
     * 分类ID列表（支持多选）
     */
    @NotEmpty(message = "至少选择一个分类")
    private List<Long> categoryIds;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 状态 0:停用 1:启用
     */
    private Integer status;
}
