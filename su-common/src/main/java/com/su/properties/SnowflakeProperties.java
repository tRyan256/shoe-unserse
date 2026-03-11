package com.su.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "su.snowflake")
@Data
public class SnowflakeProperties {
    private long workerId = 0;
    private long datacenterId = 0;
}

