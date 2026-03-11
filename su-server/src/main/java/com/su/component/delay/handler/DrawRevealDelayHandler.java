package com.su.component.delay.handler;

import com.su.component.delay.DelayQueueService;
import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.service.impl.draw.support.DrawRevealService;
import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class DrawRevealDelayHandler implements DelayTaskHandler {
    private final DrawMapper drawMapper;
    private final DrawRevealService drawRevealService;
    private final DelayQueueService delayQueueService;

    public DrawRevealDelayHandler(DrawMapper drawMapper, DrawRevealService drawRevealService, DelayQueueService delayQueueService) {
        this.drawMapper = drawMapper;
        this.drawRevealService = drawRevealService;
        this.delayQueueService = delayQueueService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.DRAW_REVEAL.equals(type);
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
        if (draw.getStatus() != null && (draw.getStatus() == 2 || draw.getStatus() == 3)) {
            return;
        }
        if (draw.getEndTime() == null) {
            return;
        }
        if (draw.getEndTime() != null) {
            long endMillis = draw.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (System.currentTimeMillis() < endMillis) {
                delayQueueService.schedule(DelayTaskTypeConstant.DRAW_REVEAL, String.valueOf(drawId), endMillis, null);
                return;
            }
        }
        if (draw.getStatus() != null && draw.getStatus() == 1) {
            drawRevealService.revealBySystem(drawId);
        }
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
