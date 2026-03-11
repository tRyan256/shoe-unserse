package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.utils.cache.CacheClient;
import com.su.constant.RedisKeyConstant;
import com.su.dto.OutletDTO;
import com.su.dto.OutletPageQueryDTO;
import com.su.entity.Outlet;
import com.su.mapper.OutletMapper;
import com.su.result.PageResult;
import com.su.service.OutletService;
import com.su.vo.OutletNearbyVO;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutletServiceImpl implements OutletService {

    @Autowired
    private OutletMapper outletMapper;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void save(OutletDTO dto) {
        Outlet outlet = new Outlet();
        BeanUtils.copyProperties(dto, outlet);
        outlet.setCreateTime(LocalDateTime.now());
        outlet.setUpdateTime(LocalDateTime.now());
        if (outlet.getStatus() == null) {
            outlet.setStatus(0);
        }
        outletMapper.insert(outlet);
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_OUTLET_ID);
        if (outlet.getId() != null) {
            bloom.add(outlet.getId());
        }
        updateGeo(outlet);
    }

    @Override
    public PageResult page(OutletPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Outlet> page = outletMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Outlet getById(Long id) {
        if (id == null) {
            return null;
        }
        String key = RedisKeyConstant.outletDetailKey(id);
        String lockKey = RedisKeyConstant.lockKey(key);
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_OUTLET_ID);
        return cacheClient.queryWithSimpleTTL(
                key,
                lockKey,
                Outlet.class,
                () -> outletMapper.getById(id),
                Duration.ofMinutes(30),
                Duration.ofSeconds(60),
                bloom,
                id
        );
    }

    @Override
    public List<OutletNearbyVO> nearby(Double longitude, Double latitude, Double radiusMeters, Integer limit) {
        if (longitude == null || latitude == null) {
            return List.of();
        }
        double radius = radiusMeters == null ? 5000D : radiusMeters;
        int size = limit == null ? 20 : limit;
        if (size <= 0) {
            return List.of();
        }

        Circle circle = new Circle(new Point(longitude, latitude), new Distance(radius));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(size);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(RedisKeyConstant.OUTLET_GEO, circle, args);
        if (results == null || results.getContent() == null || results.getContent().isEmpty()) {
            return List.of();
        }

        List<OutletNearbyVO> list = new ArrayList<>();
        for (var geoResult : results.getContent()) {
            if (geoResult == null || geoResult.getContent() == null || geoResult.getContent().getName() == null) {
                continue;
            }
            Long outletId;
            try {
                outletId = Long.valueOf(geoResult.getContent().getName());
            } catch (Exception e) {
                continue;
            }
            Outlet outlet = getById(outletId);
            if (outlet == null) {
                continue;
            }
            OutletNearbyVO vo = new OutletNearbyVO();
            BeanUtils.copyProperties(outlet, vo);
            if (geoResult.getDistance() != null) {
                vo.setDistanceMeters(geoResult.getDistance().getValue());
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public void update(OutletDTO dto) {
        Outlet outlet = new Outlet();
        BeanUtils.copyProperties(dto, outlet);
        outlet.setUpdateTime(LocalDateTime.now());
        outletMapper.update(outlet);
        if (dto.getId() != null) {
            cacheClient.evict(RedisKeyConstant.outletDetailKey(dto.getId()));
            Outlet latest = outletMapper.getById(dto.getId());
            if (latest != null) {
                updateGeo(latest);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        outletMapper.deleteById(id);
        if (id != null) {
            cacheClient.evict(RedisKeyConstant.outletDetailKey(id));
            stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.OUTLET_GEO, String.valueOf(id));
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Outlet outlet = new Outlet();
        outlet.setId(id);
        outlet.setStatus(status);
        outlet.setUpdateTime(LocalDateTime.now());
        outletMapper.update(outlet);
        if (id != null) {
            cacheClient.evict(RedisKeyConstant.outletDetailKey(id));
            Outlet latest = outletMapper.getById(id);
            if (latest != null) {
                updateGeo(latest);
            }
        }
    }

    private void updateGeo(Outlet outlet) {
        if (outlet == null || outlet.getId() == null) {
            return;
        }
        Integer status = outlet.getStatus();
        BigDecimal lon = outlet.getLongitude();
        BigDecimal lat = outlet.getLatitude();
        if (status != null && status == 1 && lon != null && lat != null) {
            stringRedisTemplate.opsForGeo().add(
                    RedisKeyConstant.OUTLET_GEO,
                    new Point(lon.doubleValue(), lat.doubleValue()),
                    String.valueOf(outlet.getId())
            );
        } else {
            stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.OUTLET_GEO, String.valueOf(outlet.getId()));
        }
    }
}
