package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.constant.RedisKeyConstant;
import com.su.dto.ExperienceCommentDTO;
import com.su.dto.ExperienceReplyDTO;
import com.su.dto.message.MessageType;
import com.su.dto.message.NotificationMessage;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.entity.ExperienceComment;
import com.su.entity.ExperienceLike;
import com.su.entity.ExperiencePost;
import com.su.exception.CommentNotFoundException;
import com.su.exception.DuplicateLikeException;
import com.su.exception.ExperiencePostNotFoundException;
import com.su.mapper.ExperienceLikeMapper;
import com.su.mapper.ExperienceCommentMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.mq.producer.MessageQueueProducer;
import com.su.result.PageResult;
import com.su.service.ExperienceCommentService;
import com.su.utils.cache.CacheClient;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperienceCommentVO;
import com.su.vo.ExperienceReplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 体验心得评论服务实现类（合并评论和回复）
 */
@Service
@Slf4j
public class ExperienceCommentServiceImpl implements ExperienceCommentService {

    @Autowired
    private ExperienceCommentMapper experienceCommentMapper;

    @Autowired
    private ExperienceLikeMapper experienceLikeMapper;

    @Autowired
    private ExperiencePostMapper experiencePostMapper;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private MessageQueueProducer messageQueueProducer;

    /**
     * 创建评论
     *
     * @param dto    评论DTO
     * @param userId 用户ID
     * @return 评论VO
     */
    @Override
    @Transactional
    public ExperienceCommentVO createComment(ExperienceCommentDTO dto, Long userId) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            throw new IllegalArgumentException("评论内容长度不能超过500字符");
        }

        ExperiencePost post = experiencePostMapper.getById(dto.getPostId());
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        ExperienceComment comment = ExperienceComment.builder()
                .postId(dto.getPostId())
                .parentId(0L)
                .userId(userId)
                .content(dto.getContent())
                .targetUserId(0L)
                .targetReplyId(null)
                .replyCount(0)
                .likeCount(0)
                .isAuthor(0)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .build();

        experienceCommentMapper.insert(comment);
        log.info("创建评论成功，commentId={}, postId={}, userId={}", comment.getId(), dto.getPostId(), userId);

        experiencePostMapper.updateCommentCount(dto.getPostId(), 1);
        log.info("增加心得评论计数，postId={}", dto.getPostId());

        evictCommentListCache(dto.getPostId());
        evictPostCache(dto.getPostId());
        evictPostListCache(dto.getPostId());

        if (!userId.equals(post.getUserId())) {
            String notificationId = "notify_comment_" + dto.getPostId() + "_" + userId + "_" + System.currentTimeMillis();
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .type(MessageType.POST_COMMENTED)
                    .receiverId(post.getUserId())
                    .senderId(userId)
                    .postId(dto.getPostId())
                    .commentId(comment.getId())
                    .content("有用户评论了你的心得")
                    .timestamp(System.currentTimeMillis())
                    .messageId(notificationId)
                    .build();

            boolean notificationSent = messageQueueProducer.sendNotificationMessage(notificationMessage);
            if (!notificationSent) {
                log.warn("通知消息发送失败，将通过补偿机制处理，messageId={}", notificationId);
            }
        }

        ExperienceCommentVO vo = new ExperienceCommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setIsLiked(false);
        vo.setReplies(new ArrayList<>());

        return vo;
    }

    /**
     * 创建回复
     *
     * @param dto    回复DTO
     * @param userId 用户ID
     * @return 回复VO
     */
    @Override
    @Transactional
    public ExperienceReplyVO createReply(ExperienceReplyDTO dto, Long userId) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            throw new IllegalArgumentException("回复内容长度不能超过500字符");
        }

        ExperienceComment comment = experienceCommentMapper.getById(dto.getCommentId());
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        if (comment.getParentId() != null && comment.getParentId() > 0) {
            throw new IllegalArgumentException("不能回复回复，请回复一级评论");
        }

        ExperiencePost post = experiencePostMapper.getById(comment.getPostId());
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        Integer isAuthor = userId.equals(post.getUserId()) ? 1 : 0;

        ExperienceComment reply = ExperienceComment.builder()
                .postId(comment.getPostId())
                .parentId(dto.getCommentId())
                .userId(userId)
                .content(dto.getContent())
                .targetUserId(dto.getTargetUserId())
                .targetReplyId(dto.getTargetReplyId())
                .replyCount(0)
                .likeCount(0)
                .isAuthor(isAuthor)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .build();

        experienceCommentMapper.insertReply(reply);
        log.info("创建回复成功，replyId={}, commentId={}, userId={}, isAuthor={}", 
                reply.getId(), dto.getCommentId(), userId, isAuthor);

        experienceCommentMapper.updateReplyCount(dto.getCommentId(), 1);
        log.info("增加评论回复计数，commentId={}", dto.getCommentId());

        evictCommentListCache(comment.getPostId());
        evictPostCache(comment.getPostId());
        evictPostListCache(comment.getPostId());

        if (!userId.equals(dto.getTargetUserId())) {
            String notificationId = "notify_reply_" + dto.getCommentId() + "_" + userId + "_" + System.currentTimeMillis();
            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .type(MessageType.COMMENT_REPLIED)
                    .receiverId(dto.getTargetUserId())
                    .senderId(userId)
                    .postId(comment.getPostId())
                    .commentId(dto.getCommentId())
                    .replyId(reply.getId())
                    .content("有用户回复了你的评论")
                    .timestamp(System.currentTimeMillis())
                    .messageId(notificationId)
                    .build();

            boolean notificationSent = messageQueueProducer.sendNotificationMessage(notificationMessage);
            if (!notificationSent) {
                log.warn("通知消息发送失败，将通过补偿机制处理，messageId={}", notificationId);
            }
        }

        ExperienceReplyVO vo = new ExperienceReplyVO();
        BeanUtils.copyProperties(reply, vo);
        vo.setCommentId(dto.getCommentId());

        return vo;
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        String cacheKey = RedisKeyConstant.commentLikeStatusKey(commentId, userId);
        String lockKey = RedisKeyConstant.lockKey("comment:like:" + commentId + ":" + userId);
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(3));
        
        if (token == null) {
            throw new DuplicateLikeException("操作过于频繁，请稍后再试");
        }
        
        try {
            String cached = cacheClient.get(cacheKey);
            if ("1".equals(cached)) {
                throw new DuplicateLikeException("已点赞该评论");
            }

            ExperienceLike existingLike = experienceLikeMapper.getCommentLike(commentId, userId);
            if (existingLike != null) {
                cacheClient.set(cacheKey, "1", Duration.ofHours(1));
                throw new DuplicateLikeException("已点赞该评论");
            }

            ExperienceLike like = ExperienceLike.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            experienceLikeMapper.insertCommentLike(like);
            log.info("创建评论点赞记录成功，commentId={}, userId={}", commentId, userId);

            experienceCommentMapper.updateLikeCount(commentId, 1);
            log.info("增加评论点赞计数，commentId={}", commentId);

            cacheClient.set(cacheKey, "1", Duration.ofHours(1));
            log.debug("缓存评论点赞状态，key={}", cacheKey);

            evictCommentListCache(comment.getPostId());

            if (!userId.equals(comment.getUserId())) {
                String statisticsId = "stats_comment_like_" + commentId + "_" + userId + "_" + System.currentTimeMillis();
                StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                        .type(MessageType.COMMENT_LIKE)
                        .userId(userId)
                        .targetUserId(comment.getUserId())
                        .commentId(commentId)
                        .timestamp(System.currentTimeMillis())
                        .messageId(statisticsId)
                        .build();
                
                boolean statsSent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
                if (!statsSent) {
                    log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", statisticsId);
                }

                String notificationId = "notify_comment_like_" + commentId + "_" + userId + "_" + System.currentTimeMillis();
                NotificationMessage notificationMessage = NotificationMessage.builder()
                        .type(MessageType.COMMENT_LIKED)
                        .receiverId(comment.getUserId())
                        .senderId(userId)
                        .postId(comment.getPostId())
                        .commentId(commentId)
                        .content("有用户点赞了你的评论")
                        .timestamp(System.currentTimeMillis())
                        .messageId(notificationId)
                        .build();

                boolean notificationSent = messageQueueProducer.sendNotificationMessage(notificationMessage);
                if (!notificationSent) {
                    log.warn("通知消息发送失败，将通过补偿机制处理，messageId={}", notificationId);
                }
            }
        } finally {
            cacheClient.unlock(lockKey, token);
        }
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        ExperienceLike existingLike = experienceLikeMapper.getCommentLike(commentId, userId);
        if (existingLike == null) {
            log.warn("取消点赞失败，用户未点赞该评论，commentId={}, userId={}", commentId, userId);
            return;
        }

        experienceLikeMapper.deleteCommentLike(commentId, userId);
        log.info("删除评论点赞记录成功，commentId={}, userId={}", commentId, userId);

        experienceCommentMapper.updateLikeCount(commentId, -1);
        log.info("减少评论点赞计数，commentId={}", commentId);

        String cacheKey = RedisKeyConstant.commentLikeStatusKey(commentId, userId);
        try {
            cacheClient.evict(cacheKey);
            log.debug("删除评论点赞状态缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("删除评论点赞状态缓存失败", e);
        }

        evictCommentListCache(comment.getPostId());

        if (!userId.equals(comment.getUserId())) {
            String statisticsId = "stats_comment_unlike_" + commentId + "_" + userId + "_" + System.currentTimeMillis();
            StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                    .type(MessageType.COMMENT_UNLIKE)
                    .userId(userId)
                    .targetUserId(comment.getUserId())
                    .commentId(commentId)
                    .timestamp(System.currentTimeMillis())
                    .messageId(statisticsId)
                    .build();
            
            boolean statsSent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
            if (!statsSent) {
                log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", statisticsId);
            }
        }
    }

    /**
     * 查询评论列表
     *
     * @param postId        心得ID
     * @param page          页码
     * @param size          每页大小
     * @param currentUserId 当前用户ID（可为null）
     * @return 分页结果
     */
    @Override
    public PageResult<ExperienceCommentVO> listComments(Long postId, Integer page, Integer size, Long currentUserId) {
        if (postId == null) {
            throw new IllegalArgumentException("心得ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 10 || size > 50) {
            size = 20;
        }

        PageHelper.startPage(page, size);
        Page<ExperienceCommentVO> pageResult = experienceCommentMapper.listByPostId(postId, currentUserId);

        List<ExperienceCommentVO> commentList = pageResult.getResult();

        if (commentList.isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        List<Long> commentIds = commentList.stream()
                .map(ExperienceCommentVO::getId)
                .collect(Collectors.toList());

        List<ExperienceReplyVO> allReplies = experienceCommentMapper.listRepliesByParentIds(commentIds, currentUserId);

        Map<Long, List<ExperienceReplyVO>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(ExperienceReplyVO::getCommentId));

        for (ExperienceCommentVO comment : commentList) {
            List<ExperienceReplyVO> commentReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
            comment.setReplies(commentReplies);

            if (currentUserId != null) {
                ExperienceLike like = experienceLikeMapper.getCommentLike(
                        comment.getId(), currentUserId);
                comment.setIsLiked(like != null);
                
                for (ExperienceReplyVO reply : commentReplies) {
                    ExperienceLike replyLike = experienceLikeMapper.getCommentLike(
                            reply.getId(), currentUserId);
                    reply.setIsLiked(replyLike != null);
                }
            } else {
                comment.setIsLiked(false);
                for (ExperienceReplyVO reply : commentReplies) {
                    reply.setIsLiked(false);
                }
            }
        }

        return new PageResult<>(pageResult.getTotal(), commentList);
    }

    /**
     * 隐藏评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional
    public void hideComment(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        experienceCommentMapper.updateHidden(commentId, 1);
        log.info("隐藏评论成功，commentId={}", commentId);

        evictCommentListCache(comment.getPostId());
    }

    /**
     * 取消隐藏评论
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional
    public void unhideComment(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        experienceCommentMapper.updateHidden(commentId, 0);
        log.info("取消隐藏评论成功，commentId={}", commentId);

        evictCommentListCache(comment.getPostId());
    }

    private void evictCommentListCache(Long postId) {
        String cacheKey = RedisKeyConstant.commentListKey(postId);
        try {
            cacheClient.evict(cacheKey);
            log.debug("删除评论列表缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("删除评论列表缓存失败", e);
        }
    }

    /**
     * 删除心得详情缓存
     *
     * @param postId 心得ID
     */
    private void evictPostCache(Long postId) {
        String cacheKey = RedisKeyConstant.experiencePostKey(postId);
        try {
            cacheClient.evict(cacheKey);
            log.debug("删除心得详情缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("删除心得详情缓存失败", e);
        }
    }

    /**
     * 删除心得列表缓存
     *
     * @param postId 心得ID
     */
    private void evictPostListCache(Long postId) {
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post != null && post.getProductType() != null && post.getProductId() != null) {
            try {
                // 删除按时间排序的缓存
                String timeKey = RedisKeyConstant.experiencePostListKey(post.getProductType(), post.getProductId(), "time");
                cacheClient.evict(timeKey);
                log.debug("删除心得列表缓存，key={}", timeKey);

                // 删除按点赞数排序的缓存
                String likeKey = RedisKeyConstant.experiencePostListKey(post.getProductType(), post.getProductId(), "like");
                cacheClient.evict(likeKey);
                log.debug("删除心得列表缓存，key={}", likeKey);
            } catch (Exception e) {
                log.warn("删除心得列表缓存失败", e);
            }
        }
    }

    @Override
    public PageResult<ExperienceCommentAdminVO> pageQueryAdmin(Long postId, Integer hidden, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }

        log.info("管理员分页查询评论列表，postId={}, hidden={}, page={}, size={}", postId, hidden, page, size);

        PageHelper.startPage(page, size);
        Page<ExperienceCommentAdminVO> pageResult = experienceCommentMapper.pageQueryAdmin(postId, hidden);

        return new PageResult<>(pageResult.getTotal(), pageResult.getResult());
    }

    /**
     * 用户删除自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void deleteOwnComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        if (!userId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权删除他人的评论");
        }

        if (comment.getParentId() != null && comment.getParentId() > 0) {
            throw new IllegalArgumentException("请使用回复删除接口");
        }

        experienceCommentMapper.deleteRepliesByParentId(commentId);
        experienceCommentMapper.deleteById(commentId);
        log.info("用户删除自己的评论成功，commentId={}, userId={}", commentId, userId);

        experiencePostMapper.updateCommentCount(comment.getPostId(), -1);
        evictCommentListCache(comment.getPostId());
        evictPostCache(comment.getPostId());
        evictPostListCache(comment.getPostId());
    }

    /**
     * 用户隐藏自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void hideOwnComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        if (!userId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权隐藏他人的评论");
        }

        experienceCommentMapper.updateHidden(commentId, 1);
        log.info("用户隐藏自己的评论成功，commentId={}, userId={}", commentId, userId);

        evictCommentListCache(comment.getPostId());
    }

    /**
     * 用户取消隐藏自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void unhideOwnComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        if (!userId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权取消隐藏他人的评论");
        }

        experienceCommentMapper.updateHidden(commentId, 0);
        log.info("用户取消隐藏自己的评论成功，commentId={}, userId={}", commentId, userId);

        evictCommentListCache(comment.getPostId());
    }

    /**
     * 用户修改自己的评论（需验证所有权）
     *
     * @param commentId 评论ID
     * @param content   新内容
     * @param userId    用户ID
     */
    @Override
    @Transactional
    public void updateOwnComment(Long commentId, String content, Long userId) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("评论内容长度不能超过500字符");
        }

        ExperienceComment comment = experienceCommentMapper.getById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException("评论不存在");
        }

        if (!userId.equals(comment.getUserId())) {
            throw new IllegalArgumentException("无权修改他人的评论");
        }

        experienceCommentMapper.updateContent(commentId, content);
        log.info("用户修改自己的评论成功，commentId={}, userId={}", commentId, userId);

        evictCommentListCache(comment.getPostId());
    }

    /**
     * 用户删除自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId  用户ID
     */
    @Override
    @Transactional
    public void deleteOwnReply(Long replyId, Long userId) {
        if (replyId == null || userId == null) {
            throw new IllegalArgumentException("回复ID和用户ID不能为空");
        }

        ExperienceComment reply = experienceCommentMapper.getById(replyId);
        if (reply == null) {
            throw new CommentNotFoundException("回复不存在");
        }

        if (!userId.equals(reply.getUserId())) {
            throw new IllegalArgumentException("无权删除他人的回复");
        }

        if (reply.getParentId() == null || reply.getParentId() == 0) {
            throw new IllegalArgumentException("请使用评论删除接口");
        }

        experienceCommentMapper.deleteById(replyId);
        log.info("用户删除自己的回复成功，replyId={}, userId={}", replyId, userId);

        experienceCommentMapper.updateReplyCount(reply.getParentId(), -1);
        evictCommentListCache(reply.getPostId());
        evictPostCache(reply.getPostId());
        evictPostListCache(reply.getPostId());
    }

    /**
     * 用户隐藏自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId  用户ID
     */
    @Override
    @Transactional
    public void hideOwnReply(Long replyId, Long userId) {
        if (replyId == null || userId == null) {
            throw new IllegalArgumentException("回复ID和用户ID不能为空");
        }

        ExperienceComment reply = experienceCommentMapper.getById(replyId);
        if (reply == null) {
            throw new CommentNotFoundException("回复不存在");
        }

        if (!userId.equals(reply.getUserId())) {
            throw new IllegalArgumentException("无权隐藏他人的回复");
        }

        experienceCommentMapper.updateHidden(replyId, 1);
        log.info("用户隐藏自己的回复成功，replyId={}, userId={}", replyId, userId);

        evictCommentListCache(reply.getPostId());
    }

    /**
     * 用户取消隐藏自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param userId  用户ID
     */
    @Override
    @Transactional
    public void unhideOwnReply(Long replyId, Long userId) {
        if (replyId == null || userId == null) {
            throw new IllegalArgumentException("回复ID和用户ID不能为空");
        }

        ExperienceComment reply = experienceCommentMapper.getById(replyId);
        if (reply == null) {
            throw new CommentNotFoundException("回复不存在");
        }

        if (!userId.equals(reply.getUserId())) {
            throw new IllegalArgumentException("无权取消隐藏他人的回复");
        }

        experienceCommentMapper.updateHidden(replyId, 0);
        log.info("用户取消隐藏自己的回复成功，replyId={}, userId={}", replyId, userId);

        evictCommentListCache(reply.getPostId());
    }

    /**
     * 用户修改自己的回复（需验证所有权）
     *
     * @param replyId 回复ID
     * @param content 新内容
     * @param userId  用户ID
     */
    @Override
    @Transactional
    public void updateOwnReply(Long replyId, String content, Long userId) {
        if (replyId == null || userId == null) {
            throw new IllegalArgumentException("回复ID和用户ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("回复内容长度不能超过500字符");
        }

        ExperienceComment reply = experienceCommentMapper.getById(replyId);
        if (reply == null) {
            throw new CommentNotFoundException("回复不存在");
        }

        if (!userId.equals(reply.getUserId())) {
            throw new IllegalArgumentException("无权修改他人的回复");
        }

        experienceCommentMapper.updateContent(replyId, content);
        log.info("用户修改自己的回复成功，replyId={}, userId={}", replyId, userId);

        evictCommentListCache(reply.getPostId());
    }
}
