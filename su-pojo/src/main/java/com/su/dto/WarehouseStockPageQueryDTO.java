package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarehouseStockPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Long skuId;
    private String skuName;
    private String size;
    private Integer stockMin;
    private Integer stockMax;
}

