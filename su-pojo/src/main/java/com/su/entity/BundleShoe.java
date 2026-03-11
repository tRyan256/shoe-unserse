package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleShoe implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long bundleId;

    private Long skuId;

    private String name;

    private BigDecimal price;

    private Integer copies;
}
