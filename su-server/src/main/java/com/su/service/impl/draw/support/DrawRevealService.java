package com.su.service.impl.draw.support;

import com.su.constant.RedisKeyConstant;
import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.exception.OrderBusinessException;
import com.su.mapper.DrawMapper;
import com.su.mapper.DrawRecordMapper;
import com.su.utils.cache.CacheClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DrawRevealService {
    private final DrawMapper drawMapper;
    private final DrawRecordMapper drawRecordMapper;
    private final DrawRedisService drawRedisService;
    private final CacheClient cacheClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> revealScript;

    public DrawRevealService(
            DrawMapper drawMapper,
            DrawRecordMapper drawRecordMapper,
            DrawRedisService drawRedisService,
            CacheClient cacheClient,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.drawMapper = drawMapper;
        this.drawRecordMapper = drawRecordMapper;
        this.drawRedisService = drawRedisService;
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.revealScript = new DefaultRedisScript<>();
        this.revealScript.setLocation(new ClassPathResource("lua/draw_reveal.lua"));
        this.revealScript.setResultType(List.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void revealManual(Long drawId) {
        revealInternal(drawId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void revealBySystem(Long drawId) {
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            return;
        }
        if (draw.getEndTime() != null && LocalDateTime.now().isBefore(draw.getEndTime())) {
            return;
        }
        revealInternal(drawId);
    }

    private void revealInternal(Long drawId) {
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            throw new OrderBusinessException("抽签活动不存在");
        }
        if (draw.getStatus() != null && draw.getStatus() == 2) {
            return;
        }
        if (draw.getStatus() == null || draw.getStatus() != 1) {
            throw new OrderBusinessException("抽签活动未开始");
        }
        if (draw.getWinnerCount() == null || draw.getWinnerCount() <= 0) {
            throw new OrderBusinessException("中奖人数不合法");
        }

        String lockKey = RedisKeyConstant.drawRevealLockKey(drawId);
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(30));
        if (token == null) {
            return;
        }
        try {
            Draw latest = drawMapper.getById(drawId);
            if (latest == null) {
                return;
            }
            if (latest.getStatus() != null && latest.getStatus() == 2) {
                return;
            }
            if (latest.getStatus() == null || latest.getStatus() != 1) {
                return;
            }

            syncJoinRecordsFromRedisToDb(latest);
            List<DrawRecord> pending = drawRecordMapper.listPendingByDrawId(drawId);
            if (pending == null || pending.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                latest.setStatus(2);
                latest.setDrawTime(now);
                if (latest.getEndTime() == null || latest.getEndTime().isAfter(now)) {
                    latest.setEndTime(now);
                }
                latest.setUpdateTime(now);
                drawMapper.update(latest);
                drawRedisService.markProcessed(drawId);
                cleanupAfterReveal(latest);
                return;
            }

            int winnersToPick = latest.getWinnerCount();
            if (latest.getTotalStock() != null) {
                winnersToPick = Math.min(winnersToPick, latest.getTotalStock());
            }
            if (winnersToPick <= 0) {
                throw new OrderBusinessException("库存不足");
            }

            Object result = stringRedisTemplate.execute(
                    revealScript,
                    List.of(drawRedisService.participantsKey(drawId), drawRedisService.winnerUsersKey(drawId)),
                    String.valueOf(winnersToPick)
            );
            List<String> winnerUserIds = result == null ? List.of() : (List<String>) result;
            Set<Long> winnerUserIdSet = new HashSet<>();
            for (String userIdStr : winnerUserIds) {
                Long userId = parseLong(userIdStr);
                if (userId != null) {
                    winnerUserIdSet.add(userId);
                }
            }

            List<Long> fallbackCandidates = new ArrayList<>();
            for (DrawRecord record : pending) {
                if (record == null || record.getUserId() == null) {
                    continue;
                }
                if (!winnerUserIdSet.contains(record.getUserId())) {
                    fallbackCandidates.add(record.getUserId());
                }
            }
            if (winnerUserIdSet.size() < winnersToPick && !fallbackCandidates.isEmpty()) {
                java.util.Collections.shuffle(fallbackCandidates);
                int need = winnersToPick - winnerUserIdSet.size();
                for (Long userId : fallbackCandidates) {
                    if (need <= 0) {
                        break;
                    }
                    if (userId != null && winnerUserIdSet.add(userId)) {
                        need--;
                    }
                }
            }

            List<Long> winnerRecordIds = new ArrayList<>();
            for (DrawRecord record : pending) {
                if (record == null || record.getUserId() == null) {
                    continue;
                }
                if (winnerUserIdSet.contains(record.getUserId()) && record.getId() != null) {
                    winnerRecordIds.add(record.getId());
                }
            }

            drawRecordMapper.markLosers(drawId, winnerRecordIds);

            for (DrawRecord record : pending) {
                if (record == null || record.getUserId() == null) {
                    continue;
                }
                if (winnerUserIdSet.contains(record.getUserId())) {
                    record.setStatus(1);
                    drawRecordMapper.updateStatusAndOrderNo(record);
                    drawRedisService.updateRecordOnReveal(drawId, record.getUserId(), 1);
                } else {
                    drawRedisService.updateRecordOnReveal(drawId, record.getUserId(), 2);
                }
            }

            LocalDateTime now = LocalDateTime.now();
            latest.setStatus(2);
            latest.setDrawTime(now);
            if (latest.getEndTime() == null || latest.getEndTime().isAfter(now)) {
                latest.setEndTime(now);
            }
            latest.setUpdateTime(now);
            drawMapper.update(latest);
            drawRedisService.markProcessed(drawId);
            cleanupAfterReveal(latest);
        } finally {
            cacheClient.unlock(lockKey, token);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int reconcileJoinRecords(Long drawId) {
        if (drawId == null) {
            return 0;
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            return 0;
        }
        return syncJoinRecordsFromRedisToDb(draw).size();
    }

    private List<DrawRecord> syncJoinRecordsFromRedisToDb(Draw draw) {
        if (draw == null || draw.getId() == null) {
            return List.of();
        }
        List<DrawRecord> records = drawRedisService.loadCachedRecordsForSync(draw);
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        for (int i = 0; i < records.size(); i += 1000) {
            int end = Math.min(i + 1000, records.size());
            drawRecordMapper.insertBatchIgnore(records.subList(i, end));
        }
        return records;
    }

    private void cleanupAfterReveal(Draw draw) {
        if (draw == null || draw.getId() == null) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.drawDetailKey(draw.getId()));
        drawRedisService.deleteLegacyForDraw(draw.getId());
    }

    private Long parseLong(Object value) {
        try {
            return value == null ? null : Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
