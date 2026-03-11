package com.su.vo;

import com.su.entity.OrderDetail;
import com.su.entity.Orders;
import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderVO extends Orders implements Serializable {

    //订单菜品信息
    private String orderDishes;

    //订单详情
    private List<OrderDetail> orderDetailList;

    private Logistics logistics;

    private List<LogisticsTrace> logisticsTraceList;

}
