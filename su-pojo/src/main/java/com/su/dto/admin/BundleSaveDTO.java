package com.su.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 组合包保存DTO
 * 用于创建和更新组合包
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组合包名称
     */
    @NotBlank(message = "组合包名称不能为空")
    private String name;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String image;

    /**
     * 状态 0:停用 1:启用
     */
    private Integer status;

    /**
     * 组合包包含的商品项列表
     */
    private List<BundleItemDTO> items;

    /**
     * 组合包商品项DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleItemDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;

        /**
         * SKU ID
         */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /**
         * 商品名称
         */
        private String name;

        /**
         * 商品价格
         */
        private BigDecimal price;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer copies;
    }
}
