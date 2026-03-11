package com.su.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Orders;
import com.su.entity.ShoppingCart;
import com.su.mapper.OrderMapper;
import com.su.mq.message.DrawOrderCreateMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.service.StockReservationService;
import com.su.utils.cache.CacheClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DrawOrderCreateDlqConsumer {
    private final StockReservationService stockReservationService;
    private final OrderMapper orderMapper;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;

    public DrawOrderCreateDlqConsumer(
            StockReservationService stockReservationService,
            OrderMapper orderMapper,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.stockReservationService = stockReservationService;
        this.orderMapper = orderMapper;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConstant.DRAW_ORDER_CREATE_DLQ, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DrawOrderCreateMessage message) {
        if (message == null || message.getOrderNumber() == null || message.getOrderNumber().isBlank()) {
            return;
        }
        Orders exist = orderMapper.getByNumber(message.getOrderNumber());
        if (exist != null && exist.getId() != null) {
            return;
        }

        String existing = cacheClient.get(RedisKeyConstant.preorderKey(message.getOrderNumber()));
        if (existing != null) {
            try {
                PreOrderRecord record = objectMapper.readValue(existing, PreOrderRecord.class);
                if (OrderAsyncStatusConstant.SUCCESS.equals(record.getStatus())) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        if (message.getUserId() != null) {
            stockReservationService.rollbackReservationByCartList(message.getOrderNumber(), message.getUserId(), toCartList(message));
        }
        PreOrderRecord failed = PreOrderRecord.builder()
                .orderNumber(message.getOrderNumber())
                .status(OrderAsyncStatusConstant.FAILED)
                .userId(message.getUserId())
                .error("抽签订单创建失败")
                .createTime(LocalDateTime.now())
                .build();
        try {
            cacheClient.set(RedisKeyConstant.preorderKey(message.getOrderNumber()), objectMapper.writeValueAsString(failed), Duration.ofMinutes(30));
        } catch (Exception ignored) {
        }
    }

    private List<ShoppingCart> toCartList(DrawOrderCreateMessage message) {
        if (message == null || message.getItems() == null || message.getItems().isEmpty()) {
            return List.of();
        }
        List<ShoppingCart> list = new ArrayList<>();
        for (var it : message.getItems()) {
            if (it == null) {
                continue;
            }
            ShoppingCart cart = new ShoppingCart();
            cart.setSkuId(it.getSkuId());
            cart.setBundleId(it.getBundleId());
            cart.setShoeSize(it.getShoeSize());
            cart.setNumber(it.getNumber());
            list.add(cart);
        }
        return list;
    }
}
