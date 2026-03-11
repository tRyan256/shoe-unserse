package com.su.task;

import com.su.constant.RedisKeyConstant;
import com.su.entity.ShoeSkuSize;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockWarmupInitializer implements ApplicationRunner {
    private final ShoeSkuMapper shoeSkuMapper;
    private final ShoeSkuSizeMapper shoeSkuSizeMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public StockWarmupInitializer(ShoeSkuMapper shoeSkuMapper, ShoeSkuSizeMapper shoeSkuSizeMapper, StringRedisTemplate stringRedisTemplate) {
        this.shoeSkuMapper = shoeSkuMapper;
        this.shoeSkuSizeMapper = shoeSkuSizeMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Long> skuIds = shoeSkuMapper.listAllIds();
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        for (Long skuId : skuIds) {
            if (skuId == null) {
                continue;
            }
            List<ShoeSkuSize> sizes = shoeSkuSizeMapper.listBySkuId(skuId);
            if (sizes == null || sizes.isEmpty()) {
                continue;
            }
            String stockKey = RedisKeyConstant.stockSkuKey(skuId);
            for (ShoeSkuSize size : sizes) {
                if (size == null || size.getSize() == null || size.getStock() == null) {
                    continue;
                }
                stringRedisTemplate.opsForHash().put(stockKey, size.getSize(), String.valueOf(size.getStock()));
            }
        }
    }
}

