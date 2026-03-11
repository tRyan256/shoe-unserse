package com.su.service;

import com.su.result.PageResult;
import com.su.vo.UserProfileVO;

/**
 * 用户关注服务接口
 */
public interface UserFollowService {

    /**
     * 关注用户
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    void followUser(Long followerId, Long followeeId);

    /**
     * 取消关注用户
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    void unfollowUser(Long followerId, Long followeeId);

    /**
     * 查询是否已关注
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followeeId);

    /**
     * 查询关注列表（我关注的人）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<UserProfileVO> listFollowing(Long userId, Integer page, Integer size);

    /**
     * 查询粉丝列表（关注我的人）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<UserProfileVO> listFollowers(Long userId, Integer page, Integer size);
}
