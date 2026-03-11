package com.su.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Orders;
import com.su.entity.ShoppingCart;
import com.su.mapper.OrderMapper;
import com.su.mq.message.OrderCreateItemMessage;
import com.su.mq.message.OrderCreateMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.service.StockReservationService;
import com.su.utils.cache.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderCreateDlqConsumer {
    private final StockReservationService stockReservationService;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    public OrderCreateDlqConsumer(
            StockReservationService stockReservationService,
            CacheClient cacheClient,
            ObjectMapper objectMapper,
            OrderMapper orderMapper
    ) {
        this.stockReservationService = stockReservationService;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
        this.orderMapper = orderMapper;
    }

    @RabbitListener(queues = RabbitMqConstant.ORDER_CREATE_DLQ, containerFactory = "rabbitListenerContainerFactory")
    public void handle(OrderCreateMessage message) {
        if (message == null || message.getOrderNumber() == null || message.getOrderNumber().isBlank()) {
            return;
        }
        String key = RedisKeyConstant.preorderKey(message.getOrderNumber());
        String existing = cacheClient.get(key);
        if (existing != null) {
            try {
                PreOrderRecord record = objectMapper.readValue(existing, PreOrderRecord.class);
                if (OrderAsyncStatusConstant.SUCCESS.equals(record.getStatus())) {
                    return;
                }
            } catch (Exception ignored) {
                log.warn("解析预下单记录失败，orderNumber={}", message.getOrderNumber(), ignored);
                Orders orders = orderMapper.getByNumber(message.getOrderNumber());
                if (orders != null && orders.getId() != null) {
                    return;
                }
            }
        }

        if (message.getUserId() != null) {
            if (message.getItems() != null && !message.getItems().isEmpty()) {
                stockReservationService.rollbackReservationByCartList(message.getOrderNumber(), message.getUserId(), toCartList(message.getItems()));
            } else {
                stockReservationService.rollback(message.getOrderNumber(), message.getUserId());
            }
        }

        PreOrderRecord failed = PreOrderRecord.builder()
                .orderNumber(message.getOrderNumber())
                .status(OrderAsyncStatusConstant.FAILED)
                .userId(message.getUserId())
                .error("订单创建失败")
                .createTime(LocalDateTime.now())
                .build();
        try {
            cacheClient.set(key, objectMapper.writeValueAsString(failed), Duration.ofMinutes(30));
        } catch (Exception ignored) {
            log.error("写入预下单失败状态失败，orderNumber={}", message.getOrderNumber(), ignored);
        }
    }

    private List<ShoppingCart> toCartList(List<OrderCreateItemMessage> items) {
        return items.stream().map(this::toCart).toList();
    }

    private ShoppingCart toCart(OrderCreateItemMessage item) {
        ShoppingCart cart = new ShoppingCart();
        cart.setSkuId(item.getSkuId());
        cart.setBundleId(item.getBundleId());
        cart.setShoeSize(item.getShoeSize());
        cart.setNumber(item.getNumber());
        return cart;
    }
}
