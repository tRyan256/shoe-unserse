package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShoeSkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spuId;

    private String colorName;

    private BigDecimal price;

    private String image;

    private Integer isDefault;

    private Integer status;

    private List<ShoeSkuSizeDTO> sizes = new ArrayList<>();
}
