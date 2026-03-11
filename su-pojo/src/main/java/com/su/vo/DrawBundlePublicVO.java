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
public class DrawBundlePublicVO implements Serializable {
    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private List<DrawBundleItemPublicVO> items;
}

