package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WarehouseDTO implements Serializable {
    private Long id;
    private String name;
    private String code;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String phone;
    private String province;
    private String city;
    private String district;
    private Integer status;
    private Integer capacity;
}

