package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long userId;

    private Long spuId;

    private Long skuId;

    private Long bundleId;

    private String shoeSize;

    private Integer number;

    private BigDecimal amount;

    private String image;

    private Integer selected;

    private LocalDateTime createTime;
}
