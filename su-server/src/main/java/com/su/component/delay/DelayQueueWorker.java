package com.su.component.delay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DelayQueueWorker {
    private final DelayQueueService delayQueueService;
    private final List<DelayTaskHandler> handlers;

    public DelayQueueWorker(DelayQueueService delayQueueService, List<DelayTaskHandler> handlers) {
        this.delayQueueService = delayQueueService;
        this.handlers = handlers;
    }

    @Scheduled(fixedDelay = 1000)
    public void run() {
        List<String> taskIds = delayQueueService.pollDue(System.currentTimeMillis(), 100);
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }
        for (String taskId : taskIds) {
            boolean success = false;
            try {
                String type = DelayQueueService.typeOf(taskId);
                DelayTaskHandler handler = findHandler(type);
                if (handler == null) {
                    success = true;
                    continue;
                }
                String payload = delayQueueService.getPayload(taskId);
                handler.handle(taskId, payload);
                success = true;
            } catch (Exception e) {
                log.error("delay task failed: {}", taskId, e);
                delayQueueService.reschedule(taskId, System.currentTimeMillis() + 10_000);
            } finally {
                if (success) {
                    delayQueueService.finish(taskId);
                }
            }
        }
    }

    private DelayTaskHandler findHandler(String type) {
        if (type == null || handlers == null || handlers.isEmpty()) {
            return null;
        }
        for (DelayTaskHandler handler : handlers) {
            if (handler != null && handler.supports(type)) {
                return handler;
            }
        }
        return null;
    }
}

