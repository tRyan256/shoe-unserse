package com.su.service;

import com.github.pagehelper.Page;
import com.su.vo.ExperienceNotificationVO;

/**
 * User Notification Service
 */
public interface UserNotificationService {

    /**
     * List notifications for a user with optional type filtering and pagination
     * @param userId User ID
     * @param type Notification type (optional, null for all types)
     * @param page Page number
     * @param size Page size
     * @return Page of notification VOs
     */
    Page<ExperienceNotificationVO> listNotifications(Long userId, Integer type, Integer page, Integer size);

    /**
     * Mark a notification as read
     * @param userId User ID (for authorization check)
     * @param notificationId Notification ID
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * Get unread notification count for a user
     * @param userId User ID
     * @return Unread notification count
     */
    Integer getUnreadCount(Long userId);

    /**
     * Mark all notifications as read for a user
     * @param userId User ID
     */
    void markAllAsRead(Long userId);
}
