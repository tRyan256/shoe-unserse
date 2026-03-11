package com.su.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.component.websocket.WebSocketServer;
import com.su.service.WebSocketNotificationService;
import com.su.vo.ExperienceNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * WebSocket通知服务实现类
 * 负责管理WebSocket连接并推送实时通知
 */
@Service
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 推送通知给指定用户
     * 将通知对象序列化为JSON后通过WebSocket发送
     *
     * @param userId 用户ID
     * @param notification 通知内容
     */
    @Override
    public void pushNotification(Long userId, ExperienceNotificationVO notification) {
        if (userId == null || notification == null) {
            log.warn("推送通知失败：userId或notification为空");
            return;
        }

        try {
            // 将通知对象转换为JSON字符串
            String message = objectMapper.writeValueAsString(notification);
            
            // 通过WebSocket发送给指定用户（使用userId作为sid）
            String sid = String.valueOf(userId);
            webSocketServer.sendToClient(sid, message);
            
            log.info("推送通知成功：userId={}, type={}", userId, notification.getType());
        } catch (Exception e) {
            log.error("推送通知失败：userId={}, notification={}", userId, notification, e);
        }
    }

    /**
     * 广播通知给多个用户
     * 遍历用户列表，逐个推送通知
     *
     * @param userIds 用户ID列表
     * @param notification 通知内容
     */
    @Override
    public void broadcastNotification(List<Long> userIds, ExperienceNotificationVO notification) {
        if (userIds == null || userIds.isEmpty() || notification == null) {
            log.warn("广播通知失败：userIds为空或notification为空");
            return;
        }

        log.info("开始广播通知：userCount={}, type={}", userIds.size(), notification.getType());
        
        int successCount = 0;
        for (Long userId : userIds) {
            try {
                pushNotification(userId, notification);
                successCount++;
            } catch (Exception e) {
                log.error("广播通知失败：userId={}", userId, e);
            }
        }
        
        log.info("广播通知完成：总数={}, 成功={}", userIds.size(), successCount);
    }

    /**
     * 检查用户是否在线
     * 通过检查WebSocket连接映射来判断用户是否在线
     *
     * @param userId 用户ID
     * @return true表示在线，false表示离线
     */
    @Override
    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        
        String sid = String.valueOf(userId);
        return webSocketServer.isOnline(sid);
    }
}
