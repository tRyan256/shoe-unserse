package com.su.component.delay;

/**
 * 延迟任务处理器接口
 */
public interface DelayTaskHandler {

    /**
     * 判断是否支持某种类型的延迟任务
     * @param type 任务类型
     * @return 是否支持
     */
    boolean supports(String type);

    /**
     * 处理延迟任务
     * @param taskId 任务ID
     * @param payloadJson 任务载荷JSON
     */
    void handle(String taskId, String payloadJson);
}
