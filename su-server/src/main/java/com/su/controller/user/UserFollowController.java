package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.UserFollowService;
import com.su.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注控制器
 */
@RestController
@RequestMapping("/user/experience/follow")
@Slf4j
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    /**
     * 关注用户
     *
     * @param userId 被关注用户ID
     * @return 成功消息
     */
    @PostMapping("/{userId}")
    public Result<String> followUser(@PathVariable Long userId) {
        Long followerId = BaseContext.getCurrentId();
        log.info("用户关注，followerId={}, followeeId={}", followerId, userId);
        
        userFollowService.followUser(followerId, userId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注用户
     *
     * @param userId 被关注用户ID
     * @return 成功消息
     */
    @DeleteMapping("/{userId}")
    public Result<String> unfollowUser(@PathVariable Long userId) {
        Long followerId = BaseContext.getCurrentId();
        log.info("用户取消关注，followerId={}, followeeId={}", followerId, userId);
        
        userFollowService.unfollowUser(followerId, userId);
        return Result.success("取消关注成功");
    }

    /**
     * 查询关注列表（我关注的人）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/following")
    public Result<PageResult<UserProfileVO>> listFollowing(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询关注列表，userId={}, page={}, size={}", userId, page, size);
        
        PageResult<UserProfileVO> result = userFollowService.listFollowing(userId, page, size);
        return Result.success(result);
    }

    /**
     * 查询粉丝列表（关注我的人）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/followers")
    public Result<PageResult<UserProfileVO>> listFollowers(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询粉丝列表，userId={}, page={}, size={}", userId, page, size);
        
        PageResult<UserProfileVO> result = userFollowService.listFollowers(userId, page, size);
        return Result.success(result);
    }
}
