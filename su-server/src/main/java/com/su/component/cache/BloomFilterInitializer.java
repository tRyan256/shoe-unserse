package com.su.component.cache;

import com.su.constant.RedisKeyConstant;
import com.su.mapper.CouponMapper;
import com.su.mapper.OutletMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSpuMapper;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BloomFilterInitializer implements ApplicationRunner {
    private final RedissonClient redissonClient;
    private final ShoeSpuMapper shoeSpuMapper;
    private final ShoeSkuMapper shoeSkuMapper;
    private final OutletMapper outletMapper;
    private final CouponMapper couponMapper;

    public BloomFilterInitializer(RedissonClient redissonClient, ShoeSpuMapper shoeSpuMapper,
                                  ShoeSkuMapper shoeSkuMapper,
                                  OutletMapper outletMapper, CouponMapper couponMapper) {
        this.redissonClient = redissonClient;
        this.shoeSpuMapper = shoeSpuMapper;
        this.shoeSkuMapper = shoeSkuMapper;
        this.outletMapper = outletMapper;
        this.couponMapper = couponMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        // SPU-SKU 表
        initBloom(RedisKeyConstant.BLOOM_SPU_ID, shoeSpuMapper.listAllIds());
        initBloom(RedisKeyConstant.BLOOM_SKU_ID, shoeSkuMapper.listAllIds());
        // 其他表
        initBloom(RedisKeyConstant.BLOOM_OUTLET_ID, outletMapper.listAllIds());
        initBloom(RedisKeyConstant.BLOOM_COUPON_ID, couponMapper.listAllIds());
    }

    private void initBloom(String name, List<Long> ids) {
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(name);
        long expected = ids == null || ids.isEmpty() ? 1000L : Math.max(1000L, ids.size() * 2L);
        bloom.tryInit(expected, 0.01);
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                if (id != null) {
                    bloom.add(id);
                }
            }
        }
    }
}
