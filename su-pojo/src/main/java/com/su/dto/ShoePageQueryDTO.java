package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShoePageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    private String brand;

    private List<Integer> categoryIds;  // 多分类筛选

    private Integer status;
}

