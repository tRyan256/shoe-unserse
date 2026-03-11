package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO implements Serializable {
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}

