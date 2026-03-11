package com.su.service;

import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import com.su.entity.Orders;
import com.su.mapper.LogisticsMapper;
import com.su.mapper.LogisticsTraceMapper;
import com.su.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderAutoConfirmService {
    private final OrderMapper orderMapper;
    private final LogisticsMapper logisticsMapper;
    private final LogisticsTraceMapper logisticsTraceMapper;

    public OrderAutoConfirmService(OrderMapper orderMapper, LogisticsMapper logisticsMapper, LogisticsTraceMapper logisticsTraceMapper) {
        this.orderMapper = orderMapper;
        this.logisticsMapper = logisticsMapper;
        this.logisticsTraceMapper = logisticsTraceMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmByOrderNumber(String orderNumber, String description, String operator) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        Orders orders = orderMapper.getByNumber(orderNumber);
        if (orders == null || orders.getId() == null) {
            return;
        }
        if (orders.getStatus() == null) {
            return;
        }
        if (Orders.SIGNED.equals(orders.getStatus()) || Orders.CANCELLED.equals(orders.getStatus())) {
            return;
        }
        int updated = orderMapper.markSignedIfEligible(orders.getId());
        if (updated <= 0) {
            return;
        }

        Logistics logistics = logisticsMapper.getByOrderNo(orderNumber);
        if (logistics == null || logistics.getId() == null) {
            return;
        }
        Logistics logisticsUpdate = Logistics.builder()
                .id(logistics.getId())
                .status(Orders.SIGNED)
                .currentLocation("签收")
                .updateTime(LocalDateTime.now())
                .actualTime(LocalDateTime.now())
                .build();
        logisticsMapper.update(logisticsUpdate);

        LogisticsTrace trace = LogisticsTrace.builder()
                .logisticsId(logistics.getId())
                .status(Orders.SIGNED)
                .location("签收")
                .description(description)
                .operator(operator)
                .operateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();
        logisticsTraceMapper.insert(trace);
    }
}

