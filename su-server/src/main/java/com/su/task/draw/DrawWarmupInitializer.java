package com.su.task.draw;

import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import com.su.service.DrawService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DrawWarmupInitializer implements ApplicationRunner {
    private final DrawMapper drawMapper;
    private final DrawService drawService;

    public DrawWarmupInitializer(DrawMapper drawMapper, DrawService drawService) {
        this.drawMapper = drawMapper;
        this.drawService = drawService;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        List<Draw> list = drawMapper.listWarmup(now.minusDays(1), now.plusDays(2));
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Draw draw : list) {
            if (draw == null || draw.getId() == null) {
                continue;
            }
            drawService.warmupDrawCache(draw.getId());
        }
    }
}
