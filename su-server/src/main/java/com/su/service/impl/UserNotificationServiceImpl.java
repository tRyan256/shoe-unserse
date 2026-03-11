package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.entity.ExperienceNotification;
import com.su.exception.ForbiddenException;
import com.su.exception.ServiceException;
import com.su.mapper.ExperienceNotificationMapper;
import com.su.service.UserNotificationService;
import com.su.vo.ExperienceNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User Notification Service Implementation
 */
@Service
@Slf4j
public class UserNotificationServiceImpl implements UserNotificationService {

    @Autowired
    private ExperienceNotificationMapper notificationMapper;

    @Override
    public Page<ExperienceNotificationVO> listNotifications(Long userId, Integer type, Integer page, Integer size) {
        log.info("Listing notifications for user: {}, type: {}, page: {}, size: {}", userId, type, page, size);

        // Set up pagination
        PageHelper.startPage(page, size);

        // Query notifications with JOIN to get user, post, and comment data
        Page<ExperienceNotificationVO> notifications = notificationMapper.listNotificationVOsByUserId(userId, type);

        log.info("Found {} notifications for user: {}", notifications.getTotal(), userId);
        return notifications;
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);

        // Check if notification exists and belongs to user (authorization check)
        ExperienceNotification notification = notificationMapper.getById(notificationId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenException("无权操作此通知");
        }

        // Mark as read
        notificationMapper.markAsRead(notificationId);

        log.info("Notification {} marked as read for user: {}", notificationId, userId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        log.info("Getting unread notification count for user: {}", userId);

        Integer count = notificationMapper.countUnreadByUserId(userId);

        log.info("User {} has {} unread notifications", userId, count);
        return count;
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        notificationMapper.markAllAsReadByUserId(userId);

        log.info("All notifications marked as read for user: {}", userId);
    }
}
