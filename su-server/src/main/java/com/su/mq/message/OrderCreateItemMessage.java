package com.su.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建消息中的商品项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateItemMessage implements Serializable {
    private Long skuId;
    private Long bundleId;
    private String shoeSize;
    private Integer number;
    private BigDecimal amount;
    private String name;
    private String image;
}
