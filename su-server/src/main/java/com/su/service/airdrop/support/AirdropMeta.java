package com.su.service.airdrop.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirdropMeta implements Serializable {
    private Long id;
    private String title;
    private Long couponId;
    private Integer status;
    private Integer remainCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * 关联的优惠券详细信息
     * 直接缓存优惠券信息，避免二次查询
     */
    private CouponMeta coupon;
}

