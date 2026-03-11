package com.su.task.geo;

import com.su.constant.RedisKeyConstant;
import com.su.entity.Outlet;
import com.su.mapper.OutletMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OutletGeoInitializer implements ApplicationRunner {
    private final OutletMapper outletMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public OutletGeoInitializer(OutletMapper outletMapper, StringRedisTemplate stringRedisTemplate) {
        this.outletMapper = outletMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Outlet> outlets = outletMapper.listEnabledWithLocation();
        if (outlets == null || outlets.isEmpty()) {
            return;
        }
        for (Outlet outlet : outlets) {
            if (outlet == null || outlet.getId() == null) {
                continue;
            }
            BigDecimal lon = outlet.getLongitude();
            BigDecimal lat = outlet.getLatitude();
            if (lon == null || lat == null) {
                continue;
            }
            stringRedisTemplate.opsForGeo().add(
                    RedisKeyConstant.OUTLET_GEO,
                    new Point(lon.doubleValue(), lat.doubleValue()),
                    String.valueOf(outlet.getId())
            );
        }
    }
}

