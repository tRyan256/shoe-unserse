package com.su.service;

import com.su.vo.ExperienceNotificationVO;

import java.util.List;

/**
 * WebSocket通知服务接口
 * 负责通过WebSocket推送实时通知给用户
 */
public interface WebSocketNotificationService {

    /**
     * 推送通知给指定用户
     * 如果用户在线，通过WebSocket推送；如果用户离线，不做处理（由消费者存储到数据库）
     *
     * @param userId 用户ID
     * @param notification 通知内容
     */
    void pushNotification(Long userId, ExperienceNotificationVO notification);

    /**
     * 广播通知给多个用户
     *
     * @param userIds 用户ID列表
     * @param notification 通知内容
     */
    void broadcastNotification(List<Long> userIds, ExperienceNotificationVO notification);

    /**
     * 检查用户是否在线（是否有WebSocket连接）
     *
     * @param userId 用户ID
     * @return true表示在线，false表示离线
     */
    boolean isUserOnline(Long userId);
}
