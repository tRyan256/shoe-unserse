package com.su.task.airdrop;

import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.mapper.AirdropMapper;
import com.su.mapper.CouponMapper;
import com.su.service.airdrop.support.AirdropRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AirdropWarmupTask {
    private final AirdropMapper airdropMapper;
    private final CouponMapper couponMapper;
    private final AirdropRedisService airdropRedisService;

    public AirdropWarmupTask(
            AirdropMapper airdropMapper,
            CouponMapper couponMapper,
            AirdropRedisService airdropRedisService
    ) {
        this.airdropMapper = airdropMapper;
        this.couponMapper = couponMapper;
        this.airdropRedisService = airdropRedisService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void warmup() {
        List<Airdrop> list = airdropMapper.listWarmup();
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Airdrop airdrop : list) {
            if (airdrop == null || airdrop.getId() == null) {
                continue;
            }

            try {
                Coupon coupon = null;
                if (airdrop.getCouponId() != null) {
                    coupon = couponMapper.getById(airdrop.getCouponId());
                }
                airdropRedisService.warmupAirdropMeta(airdrop, coupon);
                airdropRedisService.initIfAbsent(airdrop);
                log.debug("warmup airdrop success: {}", airdrop.getId());
            } catch (Exception e) {
                log.error("warmup airdrop failed: {}", airdrop.getId(), e);
            }
        }
    }
}
