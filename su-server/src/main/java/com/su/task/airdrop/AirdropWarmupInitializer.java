package com.su.task.airdrop;

import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.mapper.AirdropMapper;
import com.su.mapper.CouponMapper;
import com.su.service.airdrop.support.AirdropRedisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AirdropWarmupInitializer implements ApplicationRunner {
    private final AirdropMapper airdropMapper;
    private final CouponMapper couponMapper;
    private final AirdropRedisService airdropRedisService;

    public AirdropWarmupInitializer(
            AirdropMapper airdropMapper,
            CouponMapper couponMapper,
            AirdropRedisService airdropRedisService
    ) {
        this.airdropMapper = airdropMapper;
        this.couponMapper = couponMapper;
        this.airdropRedisService = airdropRedisService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 使用 listWarmup 查询需要预热的活动（活动开始前2天到结束后1天）
        List<Airdrop> list = airdropMapper.listWarmup();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Airdrop airdrop : list) {
            airdropRedisService.initIfAbsent(airdrop);
            // 查询优惠券信息并一起预热
            Coupon coupon = couponMapper.getById(airdrop.getCouponId());
            airdropRedisService.warmupAirdropMeta(airdrop, coupon);
        }
    }
}
