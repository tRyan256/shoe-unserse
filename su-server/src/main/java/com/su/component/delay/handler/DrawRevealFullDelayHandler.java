package com.su.component.delay.handler;

import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import com.su.service.impl.draw.support.DrawRevealService;
import org.springframework.stereotype.Component;

@Component
public class DrawRevealFullDelayHandler implements DelayTaskHandler {
    private final DrawMapper drawMapper;
    private final DrawRevealService drawRevealService;

    public DrawRevealFullDelayHandler(DrawMapper drawMapper, DrawRevealService drawRevealService) {
        this.drawMapper = drawMapper;
        this.drawRevealService = drawRevealService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.DRAW_REVEAL_FULL.equals(type);
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
        if (draw.getStatus() == null || draw.getStatus() != 1) {
            return;
        }
        drawRevealService.revealManual(drawId);
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
