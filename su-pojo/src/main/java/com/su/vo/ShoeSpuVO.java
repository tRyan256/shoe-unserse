package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeSpuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String brand;

    private String model;

    private String description;

    private Integer isLimited;

    private LocalDate releaseDate;

    private Integer status;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer colorCount;

    private Integer totalStock;

    private Integer salesCount;

    private String colorNames;

    private String categoryNames;

    private String defaultImage;

    private LocalDateTime createTime;
}
