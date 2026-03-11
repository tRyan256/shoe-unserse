package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShoeSkuSizeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long skuId;

    private String size;

    private Integer stock;
}
