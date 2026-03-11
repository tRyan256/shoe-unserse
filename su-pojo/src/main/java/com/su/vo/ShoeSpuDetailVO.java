package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeSpuDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String brand;

    private String model;

    private String description;

    private Integer isLimited;

    private LocalDate releaseDate;

    private Integer status;

    @Builder.Default
    private List<Long> categoryIds = new ArrayList<>();

    @Builder.Default
    private List<String> categoryNames = new ArrayList<>();

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalStock;

    private Integer salesCount;

    private String defaultImage;

    @Builder.Default
    private List<ShoeSkuVO> skus = new ArrayList<>();
}
