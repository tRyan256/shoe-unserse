package com.su.service;

import com.su.dto.ExperiencePostDTO;
import com.su.result.PageResult;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.ExperiencePostDetailVO;
import com.su.vo.ExperiencePostVO;
import com.su.vo.UserProfileVO;

/**
 * 体验心得服务接口
 */
public interface ExperiencePostService {

    /**
     * 创建体验心得
     *
     * @param dto 体验心得DTO
     * @param userId 用户ID
     * @return 体验心得VO
     */
    ExperiencePostVO createPost(ExperiencePostDTO dto, Long userId);

    /**
     * 查询心得列表
     *
     * @param productType 商品类型 1:SPU 2:组合包
     * @param productId 商品ID
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostVO> listPosts(Integer productType, Long productId, String sortBy, Integer page, Integer size);

    /**
     * 查询心得详情
     *
     * @param postId 心得ID
     * @param currentUserId 当前用户ID（可为null）
     * @return 心得详情VO
     */
    ExperiencePostDetailVO getPostDetail(Long postId, Long currentUserId);

    /**
     * 隐藏心得
     *
     * @param postId 心得ID
     */
    void hidePost(Long postId);

    /**
     * 取消隐藏心得
     *
     * @param postId 心得ID
     */
    void unhidePost(Long postId);

    /**
     * 点赞心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 查询用户发布的心得列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostVO> listPostsByUserId(Long userId, Integer page, Integer size);

    /**
     * 管理员分页查询心得列表（不过滤隐藏状态）
     *
     * @param productType 商品类型
     * @param productId 商品ID
     * @param hidden 隐藏状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostAdminVO> pageQueryAdmin(Integer productType, Long productId, Integer hidden, Integer page, Integer size);

    /**
     * 查询点赞心得的用户列表
     *
     * @param postId 心得ID
     * @param currentUserId 当前用户ID（可为null）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<UserProfileVO> listLikeUsers(Long postId, Long currentUserId, Integer page, Integer size);

    /**
     * 查询关注用户的心得列表
     *
     * @param userId 当前用户ID
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostVO> listFollowingPosts(Long userId, String sortBy, Integer page, Integer size);

    /**
     * 查询用户点赞过的心得列表
     *
     * @param userId 用户ID
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostVO> listLikedPosts(Long userId, String sortBy, Integer page, Integer size);

    /**
     * 查询所有公开心得列表
     *
     * @param currentUserId 当前用户ID（可为null）
     * @param sortBy 排序方式 time:按时间 time_asc:按时间升序 like:按点赞数
     * @param keyword 搜索关键词（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperiencePostVO> listAllPosts(Long currentUserId, String sortBy, String keyword, Integer page, Integer size);

    /**
     * 修改心得（只能修改内容和图片）
     *
     * @param postId 心得ID
     * @param userId 用户ID
     * @param dto 体验心得DTO（只使用content和images字段）
     * @return 体验心得VO
     */
    ExperiencePostVO updatePost(Long postId, Long userId, ExperiencePostDTO dto);

    /**
     * 删除心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);

    /**
     * 用户隐藏自己的心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    void hidePostByUser(Long postId, Long userId);

    /**
     * 用户取消隐藏自己的心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    void unhidePostByUser(Long postId, Long userId);
}
