package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AirdropPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String title;
    private Integer status;
}

