package com.su.vo;

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
public class OrderSubmitVO implements Serializable {
    //璁㈠崟id
    private Long id;
    //璁㈠崟鍙?
    private String orderNumber;
    //璁㈠崟閲戦
    private BigDecimal orderAmount;
    //涓嬪崟鏃堕棿
    private LocalDateTime orderTime;
}
