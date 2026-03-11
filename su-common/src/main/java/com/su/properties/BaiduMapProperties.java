package com.su.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "su.baidu.map")
@Data
public class BaiduMapProperties {
    private String ak;
}

