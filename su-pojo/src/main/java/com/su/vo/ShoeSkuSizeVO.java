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
public class ShoeSkuSizeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long skuId;

    private String size;

    private Integer stock;
}
