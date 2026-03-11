package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.entity.User;
import com.su.mapper.UserMapper;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.ExperiencePostService;
import com.su.service.UserFollowService;
import com.su.vo.ExperiencePostVO;
import com.su.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/experience/users")
@Slf4j
public class UserProfileController {

    @Autowired
    private ExperiencePostService experiencePostService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{userId}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询用户资料，userId={}, currentUserId={}", userId, currentUserId);

        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Boolean isFollowed = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowed = userFollowService.isFollowing(currentUserId, userId);
        }

        UserProfileVO vo = UserProfileVO.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userAvatar(user.getAvatar())
                .followerCount(user.getFollowerCount() != null ? user.getFollowerCount() : 0)
                .followingCount(user.getFollowingCount() != null ? user.getFollowingCount() : 0)
                .likedCount(user.getLikedCount() != null ? user.getLikedCount() : 0)
                .postCount(user.getPostCount() != null ? user.getPostCount() : 0)
                .isFollowed(isFollowed)
                .build();

        return Result.success(vo);
    }

    @GetMapping("/{userId}/posts")
    public Result<PageResult<ExperiencePostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        log.info("查询用户发布的心得，userId={}, page={}, size={}", userId, page, size);

        PageResult<ExperiencePostVO> result = experiencePostService.listPostsByUserId(userId, page, size);
        return Result.success(result);
    }
}
