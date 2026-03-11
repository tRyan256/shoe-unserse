package com.su.controller.admin;

import com.su.dto.admin.BatchOperationResult;
import com.su.dto.admin.ExperienceCommentAdminQueryDTO;
import com.su.dto.admin.ExperiencePostAdminQueryDTO;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.AdminExperienceService;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.admin.ExperienceReplyAdminVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端体验心得管理控制器
 */
@RestController
@RequestMapping("/admin/experience")
@Slf4j
public class AdminExperienceController {

    @Autowired
    private AdminExperienceService adminExperienceService;

    /**
     * 分页查询心得列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @GetMapping("/posts")
    public Result<PageResult<ExperiencePostAdminVO>> pageQueryPosts(@Valid ExperiencePostAdminQueryDTO queryDTO) {
        log.info("管理员分页查询心得列表，queryDTO={}", queryDTO);
        
        PageResult<ExperiencePostAdminVO> result = adminExperienceService.pageQueryPosts(queryDTO);
        return Result.success(result);
    }

    /**
     * 删除心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @DeleteMapping("/posts/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        log.info("管理员删除心得，postId={}", id);
        
        adminExperienceService.deletePost(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除心得
     *
     * @param ids 心得ID列表
     * @return 批量操作结果
     */
    @DeleteMapping("/posts/batch")
    public Result<BatchOperationResult> batchDeletePosts(@RequestBody List<Long> ids) {
        log.info("管理员批量删除心得，ids={}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("心得ID列表不能为空");
        }
        
        BatchOperationResult result = adminExperienceService.batchDeletePosts(ids);
        return Result.success(result);
    }

    /**
     * 分页查询评论列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @GetMapping("/comments")
    public Result<PageResult<ExperienceCommentAdminVO>> pageQueryComments(@Valid ExperienceCommentAdminQueryDTO queryDTO) {
        log.info("管理员分页查询评论列表，queryDTO={}", queryDTO);
        
        PageResult<ExperienceCommentAdminVO> result = adminExperienceService.pageQueryComments(queryDTO);
        return Result.success(result);
    }

    /**
     * 删除评论（同时删除关联回复）
     *
     * @param id 评论ID
     * @return 成功消息
     */
    @DeleteMapping("/comments/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        log.info("管理员删除评论，commentId={}", id);
        
        adminExperienceService.deleteComment(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除评论（同时删除关联回复）
     *
     * @param ids 评论ID列表
     * @return 批量操作结果
     */
    @DeleteMapping("/comments/batch")
    public Result<BatchOperationResult> batchDeleteComments(@RequestBody List<Long> ids) {
        log.info("管理员批量删除评论，ids={}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("评论ID列表不能为空");
        }
        
        BatchOperationResult result = adminExperienceService.batchDeleteComments(ids);
        return Result.success(result);
    }

    /**
     * 根据评论ID查询回复列表
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<ExperienceReplyAdminVO>> listRepliesByCommentId(@PathVariable Long commentId) {
        log.info("管理员查询评论回复列表，commentId={}", commentId);
        
        List<ExperienceReplyAdminVO> replies = adminExperienceService.listRepliesByCommentId(commentId);
        return Result.success(replies);
    }

    /**
     * 删除回复
     *
     * @param id 回复ID
     * @return 成功消息
     */
    @DeleteMapping("/replies/{id}")
    public Result<String> deleteReply(@PathVariable Long id) {
        log.info("管理员删除回复，replyId={}", id);
        
        adminExperienceService.deleteReply(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除回复
     *
     * @param ids 回复ID列表
     * @return 批量操作结果
     */
    @DeleteMapping("/replies/batch")
    public Result<BatchOperationResult> batchDeleteReplies(@RequestBody List<Long> ids) {
        log.info("管理员批量删除回复，ids={}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("回复ID列表不能为空");
        }
        
        BatchOperationResult result = adminExperienceService.batchDeleteReplies(ids);
        return Result.success(result);
    }
}
