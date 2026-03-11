package com.su.mq.consumer;

import com.su.constant.RabbitMqConstant;
import com.su.mq.message.OrderCreateMessage;
import com.su.mq.service.OrderAsyncCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreateConsumer {
    private final OrderAsyncCreateService orderAsyncCreateService;

    public OrderCreateConsumer(OrderAsyncCreateService orderAsyncCreateService) {
        this.orderAsyncCreateService = orderAsyncCreateService;
    }

    @RabbitListener(queues = RabbitMqConstant.ORDER_CREATE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handle(OrderCreateMessage message) {
        if (message == null) {
            log.warn("收到空的订单创建消息");
            return;
        }
        try {
            log.info("处理订单创建消息，orderNumber={}", message.getOrderNumber());
            orderAsyncCreateService.createFromMessage(message);
        } catch (Exception e) {
            log.error("订单创建消息处理失败，orderNumber={}", message.getOrderNumber(), e);
            throw e;
        }
    }
}

