package com.su.service;

import com.su.entity.DrawRecord;
import com.su.entity.Orders;
import com.su.mapper.DrawRecordMapper;
import com.su.mapper.OrderMapper;
import com.su.service.impl.draw.support.DrawRedisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderTimeoutCancelService {
    private final OrderMapper orderMapper;
    private final StockReservationService stockReservationService;
    private final DrawRecordMapper drawRecordMapper;
    private final DrawRedisService drawRedisService;

    public OrderTimeoutCancelService(OrderMapper orderMapper, StockReservationService stockReservationService, DrawRecordMapper drawRecordMapper, DrawRedisService drawRedisService) {
        this.orderMapper = orderMapper;
        this.stockReservationService = stockReservationService;
        this.drawRecordMapper = drawRecordMapper;
        this.drawRedisService = drawRedisService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutPaymentAndRollback(Orders orders) {
        if (orders == null || orders.getId() == null || orders.getUserId() == null || orders.getNumber() == null || orders.getNumber().isBlank()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = orderMapper.updateStatusIfMatch(
                orders.getId(),
                Orders.PENDING_PAYMENT,
                Orders.CANCELLED,
                "支付超时",
                now
        );
        if (updated <= 0) {
            return;
        }
        stockReservationService.rollback(orders.getNumber(), orders.getUserId());
        if (orders.getOrderType() != null && orders.getOrderType() == 2) {
            DrawRecord record = drawRecordMapper.getByOrderNo(orders.getNumber());
            if (record != null && record.getDrawId() != null && record.getUserId() != null) {
                drawRedisService.updateRecordOnTimeoutRollback(record.getDrawId(), record.getUserId());
            }
            drawRecordMapper.clearOrderNoByOrderNo(orders.getNumber());
        }
    }
}


