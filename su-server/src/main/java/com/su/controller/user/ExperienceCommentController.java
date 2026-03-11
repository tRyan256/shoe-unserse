package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.dto.ExperienceCommentDTO;
import com.su.dto.ExperienceReplyDTO;
import com.su.result.Result;
import com.su.service.ExperienceCommentService;
import com.su.vo.ExperienceCommentVO;
import com.su.vo.ExperienceReplyVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 体验心得评论控制器
 */
@RestController
@RequestMapping("/user/experience/comments")
@Slf4j
public class ExperienceCommentController {

    @Autowired
    private ExperienceCommentService experienceCommentService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<ExperienceCommentVO> createComment(@Valid @RequestBody ExperienceCommentDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户发表评论，userId={}, postId={}", userId, dto.getPostId());

        ExperienceCommentVO vo = experienceCommentService.createComment(dto, userId);
        return Result.success(vo);
    }

    /**
     * 回复评论
     */
    @PostMapping("/{commentId}/reply")
    public Result<ExperienceReplyVO> replyComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ExperienceReplyDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户回复评论，userId={}, commentId={}", userId, commentId);

        dto.setCommentId(commentId);
        ExperienceReplyVO vo = experienceCommentService.createReply(dto, userId);
        return Result.success(vo);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}/like")
    public Result<String> likeComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户点赞评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.likeComment(commentId, userId);
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{commentId}/like")
    public Result<String> unlikeComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户取消点赞评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.unlikeComment(commentId, userId);
        return Result.success("取消点赞成功");
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/{commentId}")
    public Result<String> deleteComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户删除自己的评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.deleteOwnComment(commentId, userId);
        return Result.success("删除成功");
    }

    /**
     * 隐藏自己的评论
     */
    @PutMapping("/{commentId}/hide")
    public Result<String> hideComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户隐藏自己的评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.hideOwnComment(commentId, userId);
        return Result.success("隐藏成功");
    }

    /**
     * 取消隐藏自己的评论
     */
    @PutMapping("/{commentId}/unhide")
    public Result<String> unhideComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户取消隐藏自己的评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.unhideOwnComment(commentId, userId);
        return Result.success("取消隐藏成功");
    }

    /**
     * 修改自己的评论
     */
    @PutMapping("/{commentId}")
    public Result<String> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户修改自己的评论，commentId={}, userId={}", commentId, userId);

        experienceCommentService.updateOwnComment(commentId, content, userId);
        return Result.success("修改成功");
    }

    /**
     * 删除自己的回复
     */
    @DeleteMapping("/replies/{replyId}")
    public Result<String> deleteReply(@PathVariable Long replyId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户删除自己的回复，replyId={}, userId={}", replyId, userId);

        experienceCommentService.deleteOwnReply(replyId, userId);
        return Result.success("删除成功");
    }

    /**
     * 隐藏自己的回复
     */
    @PutMapping("/replies/{replyId}/hide")
    public Result<String> hideReply(@PathVariable Long replyId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户隐藏自己的回复，replyId={}, userId={}", replyId, userId);

        experienceCommentService.hideOwnReply(replyId, userId);
        return Result.success("隐藏成功");
    }

    /**
     * 取消隐藏自己的回复
     */
    @PutMapping("/replies/{replyId}/unhide")
    public Result<String> unhideReply(@PathVariable Long replyId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户取消隐藏自己的回复，replyId={}, userId={}", replyId, userId);

        experienceCommentService.unhideOwnReply(replyId, userId);
        return Result.success("取消隐藏成功");
    }

    /**
     * 修改自己的回复
     */
    @PutMapping("/replies/{replyId}")
    public Result<String> updateReply(
            @PathVariable Long replyId,
            @RequestBody String content) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户修改自己的回复，replyId={}, userId={}", replyId, userId);

        experienceCommentService.updateOwnReply(replyId, content, userId);
        return Result.success("修改成功");
    }
}
