package com.su.mq.consumer;

import com.su.constant.RabbitMqConstant;
import com.su.mq.message.AirdropClaimMessage;
import com.su.mq.service.AirdropClaimPersistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AirdropClaimConsumer {
    private final AirdropClaimPersistService airdropClaimPersistService;

    public AirdropClaimConsumer(AirdropClaimPersistService airdropClaimPersistService) {
        this.airdropClaimPersistService = airdropClaimPersistService;
    }

    @RabbitListener(queues = RabbitMqConstant.AIRDROP_CLAIM_QUEUE, containerFactory = "airdropClaimListenerContainerFactory")
    public void handle(List<AirdropClaimMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("收到空的空投领取消息批次");
            return;
        }
        airdropClaimPersistService.persistBatch(messages);
    }
}
