package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletNearbyVO implements Serializable {
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
    private Double distanceMeters;
}

