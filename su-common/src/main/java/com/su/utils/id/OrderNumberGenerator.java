package com.su.utils.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.su.properties.SnowflakeProperties;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {
    private final Snowflake snowflake;

    public OrderNumberGenerator(SnowflakeProperties properties) {
        long workerId = properties == null ? 0 : normalize(properties.getWorkerId());
        long datacenterId = properties == null ? 0 : normalize(properties.getDatacenterId());
        this.snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    public String next() {
        return snowflake.nextIdStr();
    }

    private long normalize(long value) {
        if (value < 0) {
            return 0;
        }
        if (value > 31) {
            return value % 32;
        }
        return value;
    }
}

