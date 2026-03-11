package com.su.service.airdrop.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券元数据
 * 包含Coupon表的完整字段信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponMeta implements Serializable {
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

