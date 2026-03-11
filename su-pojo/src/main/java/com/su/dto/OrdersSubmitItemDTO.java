package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersSubmitItemDTO implements Serializable {

    private Long spuId;

    private Long skuId;

    private Long bundleId;

    private String shoeSize;

    private Integer quantity;
}
