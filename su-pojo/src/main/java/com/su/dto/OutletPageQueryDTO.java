package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutletPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String name;
    private String code;
    private Integer type;
    private Integer status;
    private String province;
    private String city;
}

