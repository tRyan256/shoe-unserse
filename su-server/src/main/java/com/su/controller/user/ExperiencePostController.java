package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.dto.ExperiencePostDTO;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.ExperienceCommentService;
import com.su.service.ExperiencePostService;
import com.su.vo.ExperienceCommentVO;
import com.su.vo.ExperiencePostDetailVO;
import com.su.vo.ExperiencePostVO;
import com.su.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 体验心得控制器
 */
@RestController
@RequestMapping("/user/experience/posts")
@Slf4j
public class ExperiencePostController {

    @Autowired
    private ExperiencePostService experiencePostService;

    @Autowired
    private ExperienceCommentService experienceCommentService;

    /**
     * 发布体验心得
     *
     * @param dto 体验心得DTO
     * @return 体验心得VO
     */
    @PostMapping
    public Result<ExperiencePostVO> createPost(@Valid @RequestBody ExperiencePostDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户发布体验心得，userId={}, productType={}, productId={}", 
                userId, dto.getProductType(), dto.getProductId());
        
        ExperiencePostVO vo = experiencePostService.createPost(dto, userId);
        return Result.success(vo);
    }

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
    @GetMapping
    public Result<PageResult<ExperiencePostVO>> listPosts(
            @RequestParam Integer productType,
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        log.info("查询心得列表，productType={}, productId={}, sortBy={}, page={}, size={}", 
                productType, productId, sortBy, page, size);
        
        PageResult<ExperiencePostVO> result = experiencePostService.listPosts(
                productType, productId, sortBy, page, size);
        return Result.success(result);
    }

    /**
     * 查询所有公开心得列表
     *
     * @param sortBy 排序方式 time:按时间 time_asc:按时间升序 like:按点赞数
     * @param keyword 搜索关键词（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/all")
    public Result<PageResult<ExperiencePostVO>> listAllPosts(
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询所有公开心得列表，currentUserId={}, sortBy={}, keyword={}, page={}, size={}", currentUserId, sortBy, keyword, page, size);
        
        PageResult<ExperiencePostVO> result = experiencePostService.listAllPosts(currentUserId, sortBy, keyword, page, size);
        return Result.success(result);
    }

    /**
     * 查询关注用户的心得列表
     *
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/following")
    public Result<PageResult<ExperiencePostVO>> listFollowingPosts(
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询关注用户的心得列表，userId={}, sortBy={}, page={}, size={}", userId, sortBy, page, size);
        
        PageResult<ExperiencePostVO> result = experiencePostService.listFollowingPosts(userId, sortBy, page, size);
        return Result.success(result);
    }

    /**
     * 查询点赞过的心得列表
     *
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/liked")
    public Result<PageResult<ExperiencePostVO>> listLikedPosts(
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询点赞过的心得列表，userId={}, sortBy={}, page={}, size={}", userId, sortBy, page, size);
        
        PageResult<ExperiencePostVO> result = experiencePostService.listLikedPosts(userId, sortBy, page, size);
        return Result.success(result);
    }

    /**
     * 查询心得详情
     *
     * @param id 心得ID
     * @return 心得详情VO
     */
    @GetMapping("/{id}")
    public Result<ExperiencePostDetailVO> getPostDetail(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询心得详情，postId={}, currentUserId={}", id, currentUserId);
        
        ExperiencePostDetailVO vo = experiencePostService.getPostDetail(id, currentUserId);
        return Result.success(vo);
    }

    /**
     * 点赞心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @PostMapping("/{id}/like")
    public Result<String> likePost(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户点赞心得，postId={}, userId={}", id, userId);
        
        experiencePostService.likePost(id, userId);
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}/like")
    public Result<String> unlikePost(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户取消点赞心得，postId={}, userId={}", id, userId);
        
        experiencePostService.unlikePost(id, userId);
        return Result.success("取消点赞成功");
    }

    /**
     * 查询点赞心得的用户列表
     *
     * @param id 心得ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/{id}/likes")
    public Result<PageResult<UserProfileVO>> listLikeUsers(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询点赞心得的用户列表，postId={}, currentUserId={}, page={}, size={}", id, currentUserId, page, size);
        
        PageResult<UserProfileVO> result = experiencePostService.listLikeUsers(id, currentUserId, page, size);
        return Result.success(result);
    }
/**
     * 修改心得（只能修改内容和图片）
     *
     * @param id 心得ID
     * @param dto 体验心得DTO（只使用content和images字段）
     * @return 体验心得VO
     */
    @PutMapping("/{id}")
    public Result<ExperiencePostVO> updatePost(@PathVariable Long id, @RequestBody ExperiencePostDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户修改心得，postId={}, userId={}", id, userId);
        
        ExperiencePostVO vo = experiencePostService.updatePost(id, userId, dto);
        return Result.success(vo);
    }

    /**
     * 删除心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户删除心得，postId={}, userId={}", id, userId);
        
        experiencePostService.deletePost(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 用户隐藏自己的心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @PutMapping("/{id}/hide")
    public Result<String> hidePostByUser(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户隐藏心得，postId={}, userId={}", id, userId);
        
        experiencePostService.hidePostByUser(id, userId);
        return Result.success("隐藏成功");
    }

    /**
     * 用户取消隐藏自己的心得
     *
     * @param id 心得ID
     * @return 成功消息
     */
    @PutMapping("/{id}/unhide")
    public Result<String> unhidePostByUser(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户取消隐藏心得，postId={}, userId={}", id, userId);
        
        experiencePostService.unhidePostByUser(id, userId);
        return Result.success("取消隐藏成功");
    }

    /**
     * 查询评论列表
     *
     * @param postId 心得ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/{postId}/comments")
    public Result<PageResult<ExperienceCommentVO>> listComments(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询评论列表，postId={}, page={}, size={}, currentUserId={}", 
                postId, page, size, currentUserId);
        
        PageResult<ExperienceCommentVO> result = experienceCommentService.listComments(
                postId, page, size, currentUserId);
        return Result.success(result);
    }
}


