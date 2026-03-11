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
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long orderId;

    private Long spuId;

    private Long skuId;

    private Long bundleId;

    private String shoeSize;

    private Integer number;

    private BigDecimal amount;

    private String image;
}
