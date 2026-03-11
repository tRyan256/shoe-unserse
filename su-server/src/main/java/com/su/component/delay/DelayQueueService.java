package com.su.component.delay;

import com.su.constant.DelayQueueConstant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DelayQueueService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> popDueScript;

    public DelayQueueService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.popDueScript = new DefaultRedisScript<>();
        this.popDueScript.setLocation(new ClassPathResource("lua/delay_pop_due.lua"));
        this.popDueScript.setResultType(List.class);
    }

    public void schedule(String type, String bizKey, long executeAtMillis, String payloadJson) {
        String taskId = taskId(type, bizKey);
        if (payloadJson != null) {
            stringRedisTemplate.opsForHash().put(DelayQueueConstant.DELAY_TASK_DATA_KEY, taskId, payloadJson);
        }
        stringRedisTemplate.opsForZSet().add(DelayQueueConstant.DELAY_QUEUE_KEY, taskId, executeAtMillis);
    }

    public List<String> pollDue(long nowMillis, int limit) {
        Object result = stringRedisTemplate.execute(
                popDueScript,
                List.of(DelayQueueConstant.DELAY_QUEUE_KEY),
                String.valueOf(nowMillis),
                String.valueOf(limit)
        );
        if (result == null) {
            return List.of();
        }
        return (List<String>) result;
    }

    public String getPayload(String taskId) {
        Object v = stringRedisTemplate.opsForHash().get(DelayQueueConstant.DELAY_TASK_DATA_KEY, taskId);
        return v == null ? null : String.valueOf(v);
    }

    public void finish(String taskId) {
        stringRedisTemplate.opsForHash().delete(DelayQueueConstant.DELAY_TASK_DATA_KEY, taskId);
    }

    public void reschedule(String taskId, long executeAtMillis) {
        stringRedisTemplate.opsForZSet().add(DelayQueueConstant.DELAY_QUEUE_KEY, taskId, executeAtMillis);
    }

    public static String taskId(String type, String bizKey) {
        return type + ":" + bizKey;
    }

    public static String typeOf(String taskId) {
        int idx = taskId == null ? -1 : taskId.indexOf(':');
        if (idx <= 0) {
            return null;
        }
        return taskId.substring(0, idx);
    }
}

