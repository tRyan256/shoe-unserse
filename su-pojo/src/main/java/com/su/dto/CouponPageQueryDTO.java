package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String name;
    private Integer type;
    private Integer status;
}

