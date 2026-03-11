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
public class DrawOrderCreateMessage implements Serializable {
    private String msgId;
    private String orderNumber;
    private Long userId;
    private Long addressBookId;
    private Long drawId;
    private Long drawRecordId;
    private Integer targetType;
    private BigDecimal amount;
    private LocalDateTime orderTime;
    private List<OrderCreateItemMessage> items;
}

