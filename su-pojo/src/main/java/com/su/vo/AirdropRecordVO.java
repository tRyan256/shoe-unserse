package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VO for airdrop record response with joined airdrop and coupon details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirdropRecordVO implements Serializable {
    private Long id;
    private Long airdropId;
    private String airdropTitle;
    private String couponName;
    private BigDecimal couponValue;
    private LocalDateTime createTime;
}
