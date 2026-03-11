package com.su.task;

import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import com.su.service.DrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class DrawDetailWarmupTask {
    private final DrawMapper drawMapper;
    private final DrawService drawService;

    public DrawDetailWarmupTask(DrawMapper drawMapper, DrawService drawService) {
        this.drawMapper = drawMapper;
        this.drawService = drawService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void warmup() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(2);
        LocalDateTime windowStart = now.minusDays(1);
        List<Draw> list = drawMapper.listWarmup(windowStart, windowEnd);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Draw draw : list) {
            if (draw == null || draw.getId() == null) {
                continue;
            }
            try {
                drawService.warmupDrawCache(draw.getId());
            } catch (Exception e) {
                log.error("warmup draw failed: {}", draw.getId(), e);
            }
        }
    }
}
