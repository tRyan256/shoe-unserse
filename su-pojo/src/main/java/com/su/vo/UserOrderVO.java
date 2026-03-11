package com.su.vo;

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
public class UserOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 订单号
    private String number;

    // 订单类型
    private Integer orderType;

    // 订单状态
    private Integer status;

    // 支付方式
    private Integer payMethod;

    // 支付状态
    private Integer payStatus;

    // 订单金额
    private BigDecimal amount;

    // 订单备注
    private String remark;

    // 联系方式
    private String phone;

    // 收货地址
    private String address;

    // 收货人
    private String consignee;

    // 下单时间
    private LocalDateTime orderTime;

    // 支付时间
    private LocalDateTime checkoutTime;

    // 取消原因
    private String cancelReason;

    // 取消时间
    private LocalDateTime cancelTime;

    // 订单菜品信息
    private String orderDishes;

    // 订单详情
    private List<UserOrderItemVO> orderDetailList;
}
