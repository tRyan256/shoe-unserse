package com.su.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateMessage implements Serializable {
    private String msgId;
    private String orderNumber;
    private Long userId;
    private Long addressBookId;
    private Long couponId;
    private Long warehouseId;
    private Integer payMethod;
    private String remark;
    private BigDecimal amount;
    private LocalDateTime orderTime;
    private List<OrderCreateItemMessage> items;
}

