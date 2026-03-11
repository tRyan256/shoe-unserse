package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawBundleItemPublicVO implements Serializable {
    private Long skuId;
    private String name;
    private String image;
    private String description;
    private BigDecimal price;
    private Integer copies;
    private List<String> sizes;
}

