package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outlet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String code;

    private Integer type;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String address;

    private String phone;

    private String businessHours;

    private String province;

    private String city;

    private String district;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

