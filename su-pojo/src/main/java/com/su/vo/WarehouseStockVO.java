package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStockVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private String skuName;
    private String skuImage;
    private String size;
    private Integer stock;
}

