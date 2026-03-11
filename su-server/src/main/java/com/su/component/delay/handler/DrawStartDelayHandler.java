package com.su.component.delay.handler;

import com.su.component.delay.DelayQueueService;
import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import com.su.service.DrawService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DrawStartDelayHandler implements DelayTaskHandler {
    private final DrawMapper drawMapper;
    private final DelayQueueService delayQueueService;
    private final DrawService drawService;

    public DrawStartDelayHandler(DrawMapper drawMapper, DelayQueueService delayQueueService, DrawService drawService) {
        this.drawMapper = drawMapper;
        this.delayQueueService = delayQueueService;
        this.drawService = drawService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.DRAW_START.equals(type);
    }

    @Override
    public void handle(String taskId, String payloadJson) {
        Long drawId = parseLong(bizKey(taskId));
        if (drawId == null) {
            return;
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            return;
        }
        if (draw.getStatus() == null || draw.getStatus() != 0) {
            return;
        }
        if (draw.getStartTime() == null) {
            return;
        }
        long startMillis = draw.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long nowMillis = System.currentTimeMillis();
        if (nowMillis < startMillis) {
            delayQueueService.schedule(DelayTaskTypeConstant.DRAW_START, String.valueOf(drawId), startMillis, null);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Draw update = new Draw();
        update.setId(drawId);
        update.setStatus(1);
        update.setUpdateTime(now);
        drawMapper.update(update);
        drawService.warmupDrawCache(drawId);
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
