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
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Integer type;

    private BigDecimal value;

    private BigDecimal minAmount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

