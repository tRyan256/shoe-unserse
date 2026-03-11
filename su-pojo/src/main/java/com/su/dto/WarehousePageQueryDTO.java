package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarehousePageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String name;
    private String code;
    private Integer status;
    private String province;
    private String city;
}

