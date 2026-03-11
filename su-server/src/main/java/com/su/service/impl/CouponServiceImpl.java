package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.utils.cache.CacheClient;
import com.su.constant.RedisKeyConstant;
import com.su.dto.CouponDTO;
import com.su.dto.CouponPageQueryDTO;
import com.su.entity.Coupon;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AirdropMapper;
import com.su.mapper.CouponMapper;
import com.su.result.PageResult;
import com.su.service.CouponService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private AirdropMapper airdropMapper;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void save(CouponDTO dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        if (coupon.getStatus() == null) {
            coupon.setStatus(0);
        }
        couponMapper.insert(coupon);
        
        // 添加到布隆过滤器
        if (coupon.getId() != null) {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_COUPON_ID);
            bloom.add(coupon.getId());
        }
    }

    @Override
    public PageResult page(CouponPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Coupon> page = couponMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Coupon getById(Long id) {
        if (id == null) {
            return null;
        }
        String key = RedisKeyConstant.couponDetailKey(id);
        String lockKey = RedisKeyConstant.lockKey(key);
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_COUPON_ID);
        
        // 使用互斥锁方法，解决缓存击穿和穿透问题
        return cacheClient.queryWithSimpleTTL(
                key,
                lockKey,
                Coupon.class,
                () -> couponMapper.getById(id),
                Duration.ofHours(1),     // TTL 1小时
                Duration.ofMinutes(5),   // 空值TTL 5分钟
                bloom,
                id
        );
    }

    @Override
    public void update(CouponDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(dto.getId());
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.update(coupon);
        
        // 清除缓存
        if (dto.getId() != null) {
            cacheClient.evict(RedisKeyConstant.couponDetailKey(dto.getId()));
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(id);
        couponMapper.deleteById(id);
        
        // 清除缓存
        if (id != null) {
            cacheClient.evict(RedisKeyConstant.couponDetailKey(id));
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(id);
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.update(coupon);
        
        // 清除缓存
        if (id != null) {
            cacheClient.evict(RedisKeyConstant.couponDetailKey(id));
        }
    }

    @Override
    public List<Coupon> listEnabled() {
        return couponMapper.listEnabled();
    }

    private void ensureEditable(Long couponId) {
        if (couponId == null) {
            return;
        }
        int linked = airdropMapper.countWarmupByCouponId(couponId);
        if (linked > 0) {
            throw new OrderBusinessException("该优惠券关联空投活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }
}
