package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DrawPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String title;
    private Integer status;
    private Integer targetType;
}

