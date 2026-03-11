package com.su.mq.consumer;

import com.su.constant.RabbitMqConstant;
import com.su.mq.message.DrawJoinPersistMessage;
import com.su.mq.service.DrawJoinPersistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DrawJoinConsumer {
    private final DrawJoinPersistService drawJoinPersistService;

    public DrawJoinConsumer(DrawJoinPersistService drawJoinPersistService) {
        this.drawJoinPersistService = drawJoinPersistService;
    }

    @RabbitListener(queues = RabbitMqConstant.DRAW_JOIN_PERSIST_QUEUE, containerFactory = "drawJoinListenerContainerFactory")
    public void handle(List<DrawJoinPersistMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("收到空的抽签参与持久化消息批次");
            return;
        }
        drawJoinPersistService.persistBatch(messages);
    }
}
