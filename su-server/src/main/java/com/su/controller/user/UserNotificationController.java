package com.su.controller.user;

import com.github.pagehelper.Page;
import com.su.context.BaseContext;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.UserNotificationService;
import com.su.vo.ExperienceNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * User Notification Controller
 * Handles message center notification operations
 */
@RestController
@RequestMapping("/user/profile/notifications")
@Slf4j
public class UserNotificationController {

    @Autowired
    private UserNotificationService userNotificationService;

    /**
     * Get notifications for current user with optional type filtering and pagination
     *
     * @param type Notification type (optional, null for all types)
     * @param page Page number (default: 1)
     * @param size Page size (default: 20)
     * @return Paginated list of notifications
     */
    @GetMapping
    public Result<PageResult<ExperienceNotificationVO>> listNotifications(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting notifications for user: {}, type: {}, page: {}, size: {}", userId, type, page, size);

        Page<ExperienceNotificationVO> notificationPage = userNotificationService.listNotifications(userId, type, page, size);
        PageResult<ExperienceNotificationVO> pageResult = new PageResult<>(notificationPage.getTotal(), notificationPage.getResult());
        
        return Result.success(pageResult);
    }

    /**
     * Mark a notification as read
     *
     * @param id Notification ID
     * @return Success result
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("Marking notification {} as read for user: {}", id, userId);

        userNotificationService.markAsRead(userId, id);
        return Result.success();
    }

    /**
     * Get unread notification count for current user
     *
     * @return Unread notification count
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting unread notification count for user: {}", userId);

        Integer count = userNotificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * Mark all notifications as read for current user
     *
     * @return Success result
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = BaseContext.getCurrentId();
        log.info("Marking all notifications as read for user: {}", userId);

        userNotificationService.markAllAsRead(userId);
        return Result.success();
    }
}
