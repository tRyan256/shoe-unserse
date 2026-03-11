package com.su.service;

import com.su.dto.admin.BatchOperationResult;
import com.su.dto.admin.ExperienceCommentAdminQueryDTO;
import com.su.dto.admin.ExperiencePostAdminQueryDTO;
import com.su.result.PageResult;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.admin.ExperienceReplyAdminVO;

import java.util.List;

/**
 * 管理员端体验心得管理服务接口
 */
public interface AdminExperienceService {

    /**
     * 分页查询心得列表（包含商品信息）
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageResult<ExperiencePostAdminVO> pageQueryPosts(ExperiencePostAdminQueryDTO queryDTO);

    /**
     * 更新心得隐藏状态
     *
     * @param id     心得ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    void updatePostHiddenStatus(Long id, Integer hidden);

    /**
     * 批量更新心得隐藏状态
     *
     * @param ids    心得ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    BatchOperationResult batchUpdatePostsHiddenStatus(java.util.List<Long> ids, Integer hidden);

    /**
     * 分页查询评论列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageResult<ExperienceCommentAdminVO> pageQueryComments(ExperienceCommentAdminQueryDTO queryDTO);

    /**
     * 更新评论隐藏状态
     *
     * @param id     评论ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    void updateCommentHiddenStatus(Long id, Integer hidden);

    /**
     * 批量更新评论隐藏状态
     *
     * @param ids    评论ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    BatchOperationResult batchUpdateCommentsHiddenStatus(java.util.List<Long> ids, Integer hidden);

    /**
     * 根据评论ID查询回复列表
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    List<ExperienceReplyAdminVO> listRepliesByCommentId(Long commentId);

    /**
     * 更新回复隐藏状态
     *
     * @param id     回复ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    void updateReplyHiddenStatus(Long id, Integer hidden);

    /**
     * 批量更新回复隐藏状态
     *
     * @param ids    回复ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    BatchOperationResult batchUpdateRepliesHiddenStatus(java.util.List<Long> ids, Integer hidden);

    /**
     * 删除心得
     *
     * @param id 心得ID
     */
    void deletePost(Long id);

    /**
     * 批量删除心得
     *
     * @param ids 心得ID列表
     * @return 批量操作结果统计
     */
    BatchOperationResult batchDeletePosts(List<Long> ids);

    /**
     * 删除评论（同时删除关联回复）
     *
     * @param id 评论ID
     */
    void deleteComment(Long id);

    /**
     * 批量删除评论（同时删除关联回复）
     *
     * @param ids 评论ID列表
     * @return 批量操作结果统计
     */
    BatchOperationResult batchDeleteComments(List<Long> ids);

    /**
     * 删除回复
     *
     * @param id 回复ID
     */
    void deleteReply(Long id);

    /**
     * 批量删除回复
     *
     * @param ids 回复ID列表
     * @return 批量操作结果统计
     */
    BatchOperationResult batchDeleteReplies(List<Long> ids);
}
