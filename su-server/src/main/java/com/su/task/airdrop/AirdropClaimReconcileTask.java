package com.su.task.airdrop;

import com.su.constant.RedisKeyConstant;
import com.su.entity.Airdrop;
import com.su.mapper.AirdropMapper;
import com.su.mapper.AirdropRecordMapper;
import com.su.mq.message.AirdropClaimMessage;
import com.su.mq.service.AirdropClaimPersistService;
import com.su.service.airdrop.support.AirdropMeta;
import com.su.service.airdrop.support.AirdropRedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AirdropClaimReconcileTask {
    private static final int BATCH_SIZE = 200;

    private final AirdropMapper airdropMapper;
    private final AirdropRecordMapper airdropRecordMapper;
    private final AirdropClaimPersistService airdropClaimPersistService;
    private final AirdropRedisService airdropRedisService;
    private final StringRedisTemplate stringRedisTemplate;

    public AirdropClaimReconcileTask(
            AirdropMapper airdropMapper,
            AirdropRecordMapper airdropRecordMapper,
            AirdropClaimPersistService airdropClaimPersistService,
            AirdropRedisService airdropRedisService,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.airdropMapper = airdropMapper;
        this.airdropRecordMapper = airdropRecordMapper;
        this.airdropClaimPersistService = airdropClaimPersistService;
        this.airdropRedisService = airdropRedisService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Scheduled(cron = "30 */2 * * * ?")
    public void reconcile() {
        List<Airdrop> airdrops = airdropMapper.listWarmup();
        if (airdrops == null || airdrops.isEmpty()) {
            return;
        }
        for (Airdrop airdrop : airdrops) {
            if (airdrop == null || airdrop.getId() == null) {
                continue;
            }
            Long redisCount = stringRedisTemplate.opsForSet().size(RedisKeyConstant.airdropUsersKey(airdrop.getId()));
            if (redisCount == null || redisCount <= 0) {
                continue;
            }
            int dbCount = airdropRecordMapper.countByAirdropId(airdrop.getId());
            if (redisCount.intValue() <= dbCount) {
                continue;
            }
            AirdropMeta meta = airdropRedisService.getMeta(airdrop.getId());
            if (meta == null || meta.getCouponId() == null) {
                continue;
            }
            Set<String> userIds = stringRedisTemplate.opsForSet().members(RedisKeyConstant.airdropUsersKey(airdrop.getId()));
            if (userIds == null || userIds.isEmpty()) {
                continue;
            }
            LocalDateTime claimTime = LocalDateTime.now();
            List<AirdropClaimMessage> pending = new ArrayList<>(userIds.size());
            for (String userIdStr : userIds) {
                try {
                    Long userId = Long.valueOf(userIdStr);
                    pending.add(AirdropClaimMessage.builder()
                            .msgId(airdrop.getId() + ":" + userId)
                            .airdropId(airdrop.getId())
                            .userId(userId)
                            .couponId(meta.getCouponId())
                            .claimTime(claimTime)
                            .build());
                } catch (Exception ignored) {
                }
            }
            for (int i = 0; i < pending.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, pending.size());
                airdropClaimPersistService.persistBatch(pending.subList(i, end));
            }
        }
    }
}
