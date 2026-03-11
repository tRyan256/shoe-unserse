package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员端鞋款SPU响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeSpuAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鞋款ID
     */
    private Long id;

    /**
     * 鞋款名称
     */
    private String name;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 分类列表
     */
    private List<CategoryVO> categories;

    /**
     * 价格
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 分类VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 分类ID
         */
        private Long id;

        /**
         * 分类名称
         */
        private String name;
    }
}
