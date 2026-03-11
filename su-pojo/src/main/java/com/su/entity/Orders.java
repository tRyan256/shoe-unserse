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
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待发货 3已发货 4运输中 5派送中 6已签收 7已取消 8已评价
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_SHIPPED = 2;
    public static final Integer SHIPPED = 3;
    public static final Integer IN_TRANSIT = 4;
    public static final Integer OUT_FOR_DELIVERY = 5;
    public static final Integer SIGNED = 6;
    public static final Integer CANCELLED = 7;
    public static final Integer REVIEWED = 8;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    private Long id;

    //订单号
    private String number;

    //订单类型 1普通订单 2抽签订单
    private Integer orderType;

    //订单状态 1待付款 2待发货 3已发货 4运输中 5派送中 6已签收 7已取消 8已评价
    private Integer status;

    //下单用户id
    private Long userId;

    //优惠券ID
    private Long couponId;

    //发货仓库ID
    private Long warehouseId;

    //地址id
    private Long addressBookId;

    //下单时间
    private LocalDateTime orderTime;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    private Integer payMethod;

    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

    //实收金额
    private BigDecimal amount;

    //备注
    private String remark;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    //订单取消原因
    private String cancelReason;

    //订单取消时间
    private LocalDateTime cancelTime;
}
