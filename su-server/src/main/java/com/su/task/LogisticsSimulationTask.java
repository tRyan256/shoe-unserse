package com.su.task;

import com.su.entity.Logistics;
import com.su.service.impl.logistics.support.LogisticsSimulationService;
import com.su.mapper.LogisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class LogisticsSimulationTask {
    private final LogisticsMapper logisticsMapper;
    private final LogisticsSimulationService logisticsSimulationService;

    public LogisticsSimulationTask(LogisticsMapper logisticsMapper, LogisticsSimulationService logisticsSimulationService) {
        this.logisticsMapper = logisticsMapper;
        this.logisticsSimulationService = logisticsSimulationService;
    }

    @Scheduled(fixedDelay = 86_400_000)
    public void simulate() {
        List<Logistics> list = logisticsMapper.listNeedSimulate(LocalDateTime.now().minusDays(1), 200);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Logistics logistics : list) {
            try {
                logisticsSimulationService.simulateNext(logistics);
            } catch (Exception e) {
                log.error("simulate logistics failed: {}", logistics == null ? null : logistics.getId(), e);
            }
        }
    }
}

