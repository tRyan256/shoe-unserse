package com.su.service;

import com.su.dto.ExperienceCommentDTO;
import com.su.dto.ExperienceReplyDTO;
import com.su.result.PageResult;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperienceCommentVO;
import com.su.vo.ExperienceReplyVO;

/**
 * 体验心得评论服务接口
 */
public interface ExperienceCommentService {

    /**
     * 创建评论
     * 验证内容、创建评论、增加计数、发送消息
     *
     * @param dto 评论DTO
     * @param userId 用户ID
     * @return 评论VO
     */
    ExperienceCommentVO createComment(ExperienceCommentDTO dto, Long userId);

    /**
     * 创建回复
     * 验证内容、创建回复、标记作者回复、增加计数、发送消息
     *
     * @param dto 回复DTO
     * @param userId 用户ID
     * @return 回复VO
     */
    ExperienceReplyVO createReply(ExperienceReplyDTO dto, Long userId);

    /**
     * 点赞评论
     * 创建点赞记录、增加计数、发送消息
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     * 删除点赞记录、减少计数
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unlikeComment(Long commentId, Long userId);

    /**
     * 查询评论列表
     * 支持分页、嵌套回复、过滤隐藏内容
     *
     * @param postId 心得ID
     * @param page 页码
     * @param size 每页大小
     * @param currentUserId 当前用户ID（可为null）
     * @return 分页结果
     */
    PageResult<ExperienceCommentVO> listComments(Long postId, Integer page, Integer size, Long currentUserId);

    /**
     * 隐藏评论
     *
     * @param commentId 评论ID
     */
    void hideComment(Long commentId);

    /**
     * 取消隐藏评论
     *
     * @param commentId 评论ID
     */
    void unhideComment(Long commentId);

    /**
     * 管理员分页查询评论列表（不过滤隐藏状态）
     *
     * @param postId 心得ID
     * @param hidden 隐藏状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ExperienceCommentAdminVO> pageQueryAdmin(Long postId, Integer hidden, Integer page, Integer size);

    /**
     * 用户删除自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteOwnComment(Long commentId, Long userId);

    /**
     * 用户隐藏自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void hideOwnComment(Long commentId, Long userId);

    /**
     * 用户取消隐藏自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unhideOwnComment(Long commentId, Long userId);

    /**
     * 用户修改自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param content 新内容
     * @param userId 用户ID
     */
    void updateOwnComment(Long commentId, String content, Long userId);

    /**
     * 用户删除自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId 用户ID
     */
    void deleteOwnReply(Long replyId, Long userId);

    /**
     * 用户隐藏自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId 用户ID
     */
    void hideOwnReply(Long replyId, Long userId);

    /**
     * 用户取消隐藏自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId 用户ID
     */
    void unhideOwnReply(Long replyId, Long userId);

    /**
     * 用户修改自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param content 新内容
     * @param userId 用户ID
     */
    void updateOwnReply(Long replyId, String content, Long userId);
}
