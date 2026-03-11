package com.su.component.delay.handler;

import com.su.component.delay.DelayQueueService;
import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.mapper.AirdropMapper;
import com.su.mapper.CouponMapper;
import com.su.service.airdrop.support.AirdropRedisService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class AirdropEndDelayHandler implements DelayTaskHandler {
    private final AirdropMapper airdropMapper;
    private final CouponMapper couponMapper;
    private final AirdropRedisService airdropRedisService;
    private final DelayQueueService delayQueueService;

    public AirdropEndDelayHandler(
            AirdropMapper airdropMapper,
            CouponMapper couponMapper,
            AirdropRedisService airdropRedisService,
            DelayQueueService delayQueueService
    ) {
        this.airdropMapper = airdropMapper;
        this.couponMapper = couponMapper;
        this.airdropRedisService = airdropRedisService;
        this.delayQueueService = delayQueueService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.AIRDROP_END.equals(type);
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
        if (airdrop.getStatus() != null && (airdrop.getStatus() == 2 || airdrop.getStatus() == 3)) {
            return;
        }
        if (airdrop.getEndTime() == null) {
            return;
        }
        long endMillis = airdrop.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long nowMillis = System.currentTimeMillis();
        if (nowMillis < endMillis) {
            delayQueueService.schedule(DelayTaskTypeConstant.AIRDROP_END, String.valueOf(airdropId), endMillis, null);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Airdrop update = new Airdrop();
        update.setId(airdropId);
        update.setStatus(2);
        update.setUpdateTime(now);
        airdropMapper.update(update);

        Coupon coupon = null;
        if (airdrop.getCouponId() != null) {
            coupon = couponMapper.getById(airdrop.getCouponId());
        }
        Airdrop latest = airdropMapper.getById(airdropId);
        if (latest != null) {
            airdropRedisService.warmupAirdropMeta(latest, coupon);
        }
        airdropRedisService.deleteStockAndUsers(airdropId);
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
