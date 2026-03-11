package com.su.service.impl.logistics.support;

import com.su.utils.cache.CacheClient;
import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import com.su.entity.Orders;
import com.su.mapper.LogisticsMapper;
import com.su.mapper.LogisticsTraceMapper;
import com.su.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LogisticsSimulationService {
    private final LogisticsMapper logisticsMapper;
    private final LogisticsTraceMapper logisticsTraceMapper;
    private final OrderMapper orderMapper;
    private final CacheClient cacheClient;

    public LogisticsSimulationService(
            LogisticsMapper logisticsMapper,
            LogisticsTraceMapper logisticsTraceMapper,
            OrderMapper orderMapper,
            CacheClient cacheClient
    ) {
        this.logisticsMapper = logisticsMapper;
        this.logisticsTraceMapper = logisticsTraceMapper;
        this.orderMapper = orderMapper;
        this.cacheClient = cacheClient;
    }

    public void simulateNext(Logistics logistics) {
        if (logistics == null || logistics.getId() == null || logistics.getOrderNo() == null) {
            return;
        }
        String lockKey = "lock:logistics:sim:" + logistics.getId();
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(5));
        if (token == null) {
            return;
        }
        try {
            Logistics latest = logisticsMapper.getByOrderNo(logistics.getOrderNo());
            if (latest == null) {
                return;
            }
            Integer status = latest.getStatus();
            if (status == null || Orders.SIGNED.equals(status)) {
                return;
            }

            Orders orders = orderMapper.getByNumber(latest.getOrderNo());
            if (orders == null || (orders.getStatus() != null && Orders.CANCELLED.equals(orders.getStatus()))) {
                return;
            }

            Integer nextStatus = null;
            String location = null;
            String description = null;
            if (Orders.SHIPPED.equals(status)) {
                nextStatus = Orders.IN_TRANSIT;
                location = "运输中";
                description = "包裹已到达中转站";
            } else if (Orders.IN_TRANSIT.equals(status)) {
                nextStatus = Orders.OUT_FOR_DELIVERY;
                location = "派送中";
                description = "快递员正在派送";
            } else if (Orders.OUT_FOR_DELIVERY.equals(status)) {
                nextStatus = Orders.SIGNED;
                location = "签收";
                description = "已签收";
            }
            if (nextStatus == null) {
                return;
            }

            Orders updateOrder = new Orders();
            updateOrder.setId(orders.getId());
            updateOrder.setStatus(nextStatus);
            orderMapper.update(updateOrder);

            Logistics logisticsUpdate = Logistics.builder()
                    .id(latest.getId())
                    .status(nextStatus)
                    .currentLocation(location)
                    .updateTime(LocalDateTime.now())
                    .build();
            if (Orders.SIGNED.equals(nextStatus)) {
                logisticsUpdate.setActualTime(LocalDateTime.now());
            }
            logisticsMapper.update(logisticsUpdate);

            LogisticsTrace trace = LogisticsTrace.builder()
                    .logisticsId(latest.getId())
                    .status(nextStatus)
                    .location(location)
                    .description(description)
                    .operator("系统模拟")
                    .operateTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .build();
            logisticsTraceMapper.insert(trace);
        } finally {
            cacheClient.unlock(lockKey, token);
        }
    }
}

