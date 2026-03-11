package com.su.dto;

import com.su.entity.BundleShoe;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BundleDTO implements Serializable {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer status;

    private String description;

    private String image;

    private List<BundleShoe> bundleShoes = new ArrayList<>();
}

