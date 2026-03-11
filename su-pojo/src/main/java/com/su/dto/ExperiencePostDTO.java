package com.su.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 体验心得发布请求DTO
 */
@Data
public class ExperiencePostDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 心得内容
     */
    @NotBlank(message = "心得内容不能为空")
    private String content;

    /**
     * 商品类型 1:SPU 2:组合包
     */
    @NotNull(message = "商品类型不能为空")
    private Integer productType;

    /**
     * 商品ID(spu_id或bundle_id)
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 图片路径列表（最多9张）
     */
    @Size(max = 9, message = "图片数量不能超过9张")
    private List<String> images;
}
