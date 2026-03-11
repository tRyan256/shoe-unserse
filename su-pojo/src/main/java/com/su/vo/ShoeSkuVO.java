package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeSkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spuId;

    private String spuName;

    private String colorName;

    private BigDecimal price;

    private String image;

    private Integer status;

    private Integer spuStatus;

    private Integer stock;

    private Integer isDefault;

    private Integer salesCount;

    @Builder.Default
    private List<ShoeSkuSizeVO> sizes = new ArrayList<>();
}
