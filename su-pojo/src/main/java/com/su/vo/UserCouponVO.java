package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VO for user coupon response with joined coupon details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO implements Serializable {
    private Long id;
    private Long couponId;
    private String couponName;
    private Integer couponType;
    private BigDecimal couponValue;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime useTime;
    private Long orderId;
    private LocalDateTime createTime;
}
