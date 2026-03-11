package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShoeSpuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String brand;

    private String model;

    private String description;

    private Integer isLimited;

    private LocalDate releaseDate;

    private Integer status;

    private List<Long> categoryIds = new ArrayList<>();

    private List<ShoeSkuDTO> skus = new ArrayList<>();
}
