package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawShoePublicVO implements Serializable {
    private Long id;
    private String name;
    private String brand;
    private String model;
    private String color;
    private LocalDate releaseDate;
    private Integer isLimited;
    private BigDecimal price;
    private String image;
    private String description;
    private List<String> sizes;
}

