package com.su.task;

import com.su.entity.Orders;
import com.su.mapper.OrderMapper;
import com.su.service.OrderAutoConfirmService;
import com.su.service.OrderTimeoutCancelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderTimeoutCancelService orderTimeoutCancelService;
    @Autowired
    private OrderAutoConfirmService orderAutoConfirmService;

    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        // 修改：从30分钟改为10分钟
        List<Orders> ordersList = orderMapper.processTimeoutOrder(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(10));
        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }
        for (Orders orders : ordersList) {
            orderTimeoutCancelService.cancelTimeoutPaymentAndRollback(orders);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("开始进行派送中的订单状态转换");
        List<Orders> ordersList = orderMapper.processTimeoutOrder(Orders.OUT_FOR_DELIVERY, LocalDateTime.now().plusDays(-7));
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orderAutoConfirmService.autoConfirmByOrderNumber(orders.getNumber(), "系统自动确认收货", "系统");
            }
        }
    }
}
