package com.su.component.delay.handler;

import com.su.component.delay.DelayQueueService;
import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.entity.Airdrop;
import com.su.mapper.AirdropMapper;
import com.su.service.AirdropService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class AirdropStartDelayHandler implements DelayTaskHandler {
    private final AirdropMapper airdropMapper;
    private final DelayQueueService delayQueueService;
    private final AirdropService airdropService;

    public AirdropStartDelayHandler(
            AirdropMapper airdropMapper,
            DelayQueueService delayQueueService,
            AirdropService airdropService
    ) {
        this.airdropMapper = airdropMapper;
        this.delayQueueService = delayQueueService;
        this.airdropService = airdropService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.AIRDROP_START.equals(type);
    }

    @Override
    public void handle(String taskId, String payloadJson) {
        Long airdropId = parseLong(bizKey(taskId));
        if (airdropId == null) {
            return;
        }
        Airdrop airdrop = airdropMapper.getById(airdropId);
        if (airdrop == null) {
            return;
        }
        if (airdrop.getStatus() == null || airdrop.getStatus() != 0) {
            return;
        }
        if (airdrop.getStartTime() == null) {
            return;
        }
        long startMillis = airdrop.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long nowMillis = System.currentTimeMillis();
        if (nowMillis < startMillis) {
            delayQueueService.schedule(DelayTaskTypeConstant.AIRDROP_START, String.valueOf(airdropId), startMillis, null);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Airdrop update = new Airdrop();
        update.setId(airdropId);
        update.setStatus(1);
        update.setUpdateTime(now);
        airdropMapper.update(update);
        airdropService.warmupAirdropCache(airdropId);
    }

    private String bizKey(String taskId) {
        int idx = taskId == null ? -1 : taskId.indexOf(':');
        return idx < 0 ? taskId : taskId.substring(idx + 1);
    }

    private Long parseLong(String s) {
        try {
            return s == null ? null : Long.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }
}
