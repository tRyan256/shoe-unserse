package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShoeSpuPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String brand;

    private Integer status;

    private Integer isLimited;

    private Long categoryId;

    private List<Long> categoryIds;

    private Integer page;

    private Integer pageSize;
}
