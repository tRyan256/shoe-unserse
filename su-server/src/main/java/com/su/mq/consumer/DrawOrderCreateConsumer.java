package com.su.mq.consumer;

import com.su.constant.RabbitMqConstant;
import com.su.mq.message.DrawOrderCreateMessage;
import com.su.mq.service.DrawOrderAsyncCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DrawOrderCreateConsumer {
    private final DrawOrderAsyncCreateService drawOrderAsyncCreateService;

    public DrawOrderCreateConsumer(DrawOrderAsyncCreateService drawOrderAsyncCreateService) {
        this.drawOrderAsyncCreateService = drawOrderAsyncCreateService;
    }

    @RabbitListener(queues = RabbitMqConstant.DRAW_ORDER_CREATE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DrawOrderCreateMessage message) {
        if (message == null) {
            log.warn("收到空的抽签订单创建消息");
            return;
        }
        try {
            log.info("处理抽签订单创建消息，orderNumber={}", message.getOrderNumber());
            drawOrderAsyncCreateService.createFromMessage(message);
        } catch (Exception e) {
            log.error("抽签订单创建消息处理失败，orderNumber={}", message.getOrderNumber(), e);
            throw e;
        }
    }
}

