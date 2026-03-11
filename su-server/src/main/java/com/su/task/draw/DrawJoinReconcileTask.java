package com.su.task.draw;

import com.su.entity.Draw;
import com.su.mapper.DrawMapper;
import com.su.mapper.DrawRecordMapper;
import com.su.service.impl.draw.support.DrawRedisService;
import com.su.service.impl.draw.support.DrawRevealService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DrawJoinReconcileTask {
    private final DrawMapper drawMapper;
    private final DrawRecordMapper drawRecordMapper;
    private final DrawRevealService drawRevealService;
    private final DrawRedisService drawRedisService;
    private final StringRedisTemplate stringRedisTemplate;

    public DrawJoinReconcileTask(
            DrawMapper drawMapper,
            DrawRecordMapper drawRecordMapper,
            DrawRevealService drawRevealService,
            DrawRedisService drawRedisService,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.drawMapper = drawMapper;
        this.drawRecordMapper = drawRecordMapper;
        this.drawRevealService = drawRevealService;
        this.drawRedisService = drawRedisService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Scheduled(initialDelayString = "${SU_DRAW_JOIN_RECONCILE_INITIAL_DELAY_MS:5000}", fixedDelayString = "${SU_DRAW_JOIN_RECONCILE_DELAY_MS:5000}")
    public void reconcile() {
        List<Draw> draws = drawMapper.listActive();
        if (draws == null || draws.isEmpty()) {
            return;
        }
        for (Draw draw : draws) {
            if (draw == null || draw.getId() == null) {
                continue;
            }
            Long redisCount = stringRedisTemplate.opsForSet().size(drawRedisService.participantsKey(draw.getId()));
            if (redisCount == null || redisCount <= 0) {
                continue;
            }
            int dbCount = drawRecordMapper.countByDrawId(draw.getId());
            if (redisCount.intValue() > dbCount) {
                drawRevealService.reconcileJoinRecords(draw.getId());
            }
        }
    }


}

