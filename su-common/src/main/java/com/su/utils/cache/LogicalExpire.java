package com.su.utils.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逻辑过期时间包装类，用于缓存击穿解决方案
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogicalExpire<T> {
    /**
     * 缓存数据
     */
    private T data;

    /**
     * 逻辑过期时间戳（毫秒）
     */
    private Long expireAtEpochMilli;
}
