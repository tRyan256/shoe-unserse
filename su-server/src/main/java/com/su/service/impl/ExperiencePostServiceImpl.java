package com.su.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.constant.RedisKeyConstant;
import com.su.dto.ExperiencePostDTO;
import com.su.dto.message.MessageType;
import com.su.dto.message.NotificationMessage;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.entity.ExperiencePost;
import com.su.entity.ExperienceLike;
import com.su.entity.UserFollow;
import com.su.exception.DuplicateLikeException;
import com.su.exception.ExperiencePostNotFoundException;
import com.su.exception.ProductNotFoundException;
import com.su.mapper.BundleMapper;
import com.su.mapper.ExperienceLikeMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.UserFollowMapper;
import com.su.mapper.UserMapper;
import com.su.mq.producer.MessageQueueProducer;
import com.su.result.PageResult;
import com.su.service.ExperiencePostService;
import com.su.utils.cache.CacheClient;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.ExperiencePostDetailVO;
import com.su.vo.ExperiencePostVO;
import com.su.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 体验心得服务实现类
 */
@Service
@Slf4j
public class ExperiencePostServiceImpl implements ExperiencePostService {

    @Autowired
    private ExperiencePostMapper experiencePostMapper;

    @Autowired
    private ExperienceLikeMapper experienceLikeMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoeSpuMapper shoeSpuMapper;

    @Autowired
    private ShoeSkuMapper shoeSkuMapper;

    @Autowired
    private BundleMapper bundleMapper;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageQueueProducer messageQueueProducer;

    /**
     * 创建体验心得
     *
     * @param dto    体验心得DTO
     * @param userId 用户ID
     * @return 体验心得VO
     */
    @Override
    @Transactional
    public ExperiencePostVO createPost(ExperiencePostDTO dto, Long userId) {
        // 1. 验证内容长度（由@Valid注解自动验证，这里做二次检查）
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("心得内容不能为空");
        }
        if (dto.getContent().length() > 5000) {
            throw new IllegalArgumentException("心得内容长度不能超过5000字符");
        }

        // 2. 验证商品引用是否存在
        validateProductReference(dto.getProductType(), dto.getProductId());

        // 3. 验证图片数量
        if (dto.getImages() != null && dto.getImages().size() > 9) {
            throw new IllegalArgumentException("图片数量不能超过9张");
        }

        // 4. 构建ExperiencePost实体
        ExperiencePost post = ExperiencePost.builder()
                .userId(userId)
                .content(dto.getContent())
                .productType(dto.getProductType())
                .productId(dto.getProductId())
                .images(convertImagesToJson(dto.getImages()))
                .likeCount(0)
                .commentCount(0)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 5. 保存心得到数据库
        experiencePostMapper.insert(post);
        log.info("创建体验心得成功，postId={}, userId={}", post.getId(), userId);

        // 6. 发送统计更新消息（异步更新用户发布数）
        String statisticsId = "post_create_" + userId + "_" + post.getId() + "_" + System.currentTimeMillis();
        StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                .type(MessageType.POST_CREATE)
                .userId(userId)
                .postId(post.getId())
                .timestamp(System.currentTimeMillis())
                .messageId(statisticsId)
                .build();
        
        boolean statsSent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
        if (!statsSent) {
            log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", statisticsId);
        }

        // 7. 删除相关缓存（心得列表缓存）
        evictPostListCache(dto.getProductType(), dto.getProductId());

        // 8. 构建返回VO
        ExperiencePostVO vo = new ExperiencePostVO();
        BeanUtils.copyProperties(post, vo);
        vo.setImages(dto.getImages());
        vo.setIsLiked(false);
        vo.setIsFollowed(false);

        return vo;
    }

    /**
     * 查询心得列表
     *
     * @param productType 商品类型 1:SPU 2:组合包
     * @param productId   商品ID
     * @param sortBy      排序方式 time:按时间 like:按点赞数
     * @param page        页码
     * @param size        每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ExperiencePostVO> listPosts(Integer productType, Long productId, String sortBy, Integer page, Integer size) {
        // 1. 参数验证
        if (productType == null || productId == null) {
            throw new IllegalArgumentException("商品类型和商品ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 10 || size > 50) {
            size = 20; // 默认每页20条
        }
        if (sortBy == null || (!sortBy.equals("time") && !sortBy.equals("like"))) {
            sortBy = "time"; // 默认按时间排序
        }
        
        // 将参数声明为final，以便在lambda表达式中使用
        final Integer finalProductType = productType;
        final Long finalProductId = productId;
        final String finalSortBy = sortBy;
        final Integer finalSize = size;

        // 2. 仅第一页使用缓存（使用CacheClient的queryWithSimpleTTL）
        if (page == 1) {
            String cacheKey = RedisKeyConstant.experiencePostListKey(finalProductType, finalProductId, finalSortBy);
            String lockKey = RedisKeyConstant.lockKey(cacheKey);
            
            // 使用JavaType处理List类型
            com.fasterxml.jackson.databind.JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ExperiencePostVO.class);
            
            List<ExperiencePostVO> list = cacheClient.queryWithSimpleTTL(
                    cacheKey,
                    lockKey,
                    listType,
                    () -> queryPostListFromDb(finalProductType, finalProductId, finalSortBy, finalSize),
                    Duration.ofMinutes(10),  // 缓存10分钟
                    Duration.ofMinutes(2),   // 空值缓存2分钟
                    null,
                    null
            );
            
            if (list == null) {
                list = new ArrayList<>();
            }
            
            return new PageResult<>(list.size(), list);
        }

        // 3. 非第一页直接查询数据库
        PageHelper.startPage(page, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listByProduct(finalProductType, finalProductId, finalSortBy);

        // 4. 设置默认状态（图片已通过JsonListTypeHandler自动转换）
        List<ExperiencePostVO> list = pageResult.getResult();
        for (ExperiencePostVO vo : list) {
            // 确保images不为null
            if (vo.getImages() == null) {
                vo.setImages(new ArrayList<>());
            }
            vo.setIsLiked(false);
            vo.setIsFollowed(false);
        }

        return new PageResult<>(pageResult.getTotal(), list);
    }

    /**
     * 从数据库查询心得列表（供缓存回调使用）
     */
    private List<ExperiencePostVO> queryPostListFromDb(Integer productType, Long productId, String sortBy, Integer size) {
        PageHelper.startPage(1, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listByProduct(productType, productId, sortBy);

        List<ExperiencePostVO> list = pageResult.getResult();
        for (ExperiencePostVO vo : list) {
            // 确保images不为null（图片已通过JsonListTypeHandler自动转换）
            if (vo.getImages() == null) {
                vo.setImages(new ArrayList<>());
            }
            vo.setIsLiked(false);
            vo.setIsFollowed(false);
        }

        return list;
    }

    /**
     * 查询心得详情
     *
     * @param postId        心得ID
     * @param currentUserId 当前用户ID（可为null）
     * @return 心得详情VO
     */
    @Override
    public ExperiencePostDetailVO getPostDetail(Long postId, Long currentUserId) {
        // 1. 参数验证
        if (postId == null) {
            throw new IllegalArgumentException("心得ID不能为空");
        }

        // 2. 使用CacheClient的queryWithSimpleTTL查询（直接缓存VO）
        String cacheKey = RedisKeyConstant.experiencePostKey(postId);
        String lockKey = RedisKeyConstant.lockKey(cacheKey);
        
        ExperiencePostDetailVO vo = cacheClient.queryWithSimpleTTL(
                cacheKey,
                lockKey,
                ExperiencePostDetailVO.class,
                () -> buildPostDetailVOForCache(postId),
                Duration.ofMinutes(30),  // 缓存30分钟
                Duration.ofMinutes(5),   // 空值缓存5分钟
                null,
                null
        );

        if (vo == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        // 3. 设置用户相关状态（不缓存，每次查询时重新计算）
        if (currentUserId != null) {
            ExperienceLike like = experienceLikeMapper.getPostLike(postId, currentUserId);
            vo.setIsLiked(like != null);

            UserFollow follow = userFollowMapper.getByFollowerIdAndFolloweeId(currentUserId, vo.getUserId());
            vo.setIsFollowed(follow != null);
        } else {
            vo.setIsLiked(false);
            vo.setIsFollowed(false);
        }

        return vo;
    }

    /**
     * 构建用于缓存的心得详情VO（包含用户信息和商品信息，但不包含用户状态）
     *
     * @param postId 心得ID
     * @return 心得详情VO
     */
    private ExperiencePostDetailVO buildPostDetailVOForCache(Long postId) {
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            return null;
        }

        ExperiencePostDetailVO vo = new ExperiencePostDetailVO();
        BeanUtils.copyProperties(post, vo);

        // 转换图片JSON为列表
        vo.setImages(convertJsonToImages(post.getImages()));

        // 查询用户信息
        com.su.entity.User user = userMapper.getById(post.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserAvatar(user.getAvatar());
        }

        // 查询商品信息
        if (post.getProductType() != null && post.getProductId() != null) {
            if (post.getProductType() == 1) {
                com.su.entity.ShoeSpu shoeSpu = shoeSpuMapper.getById(post.getProductId());
                if (shoeSpu != null) {
                    vo.setProductName(shoeSpu.getName());
                    // 通过默认SKU获取图片
                    com.su.entity.ShoeSku defaultSku = shoeSkuMapper.getDefaultBySpuId(post.getProductId());
                    if (defaultSku != null) {
                        vo.setProductImage(defaultSku.getImage());
                    }
                }
            } else if (post.getProductType() == 2) {
                com.su.entity.Bundle bundle = bundleMapper.getInfoById(post.getProductId());
                if (bundle != null) {
                    vo.setProductName(bundle.getName());
                    vo.setProductImage(bundle.getImage());
                }
            }
        }

        // isLiked 和 isFollowed 不缓存，每次查询时重新计算
        vo.setIsLiked(false);
        vo.setIsFollowed(false);

        return vo;
    }

    /**
     * 隐藏心得
     *
     * @param postId 心得ID
     */
    @Override
    @Transactional
    public void hidePost(Long postId) {
        // 1. 参数验证
        if (postId == null) {
            throw new IllegalArgumentException("心得ID不能为空");
        }

        // 2. 查询心得是否存在
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        // 3. 更新隐藏状态
        experiencePostMapper.updateHidden(postId, 1);
        log.info("隐藏心得成功，postId={}", postId);

        // 4. 删除缓存
        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());
    }

    /**
     * 取消隐藏心得
     *
     * @param postId 心得ID
     */
    @Override
    @Transactional
    public void unhidePost(Long postId) {
        // 1. 参数验证
        if (postId == null) {
            throw new IllegalArgumentException("心得ID不能为空");
        }

        // 2. 查询心得是否存在
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        // 3. 更新隐藏状态
        experiencePostMapper.updateHidden(postId, 0);
        log.info("取消隐藏心得成功，postId={}", postId);

        // 4. 删除缓存
        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());
    }

    /**
     * 点赞心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        // 1. 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        // 2. 查询心得是否存在
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        // 3. 使用互斥锁检查是否已点赞（防止并发重复点赞）
        String cacheKey = RedisKeyConstant.postLikeStatusKey(postId, userId);
        String lockKey = RedisKeyConstant.lockKey("like:" + postId + ":" + userId);
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(3));
        
        if (token == null) {
            throw new DuplicateLikeException("操作过于频繁，请稍后再试");
        }
        
        try {
            // 检查缓存中的点赞状态
            String cached = cacheClient.get(cacheKey);
            if ("1".equals(cached)) {
                throw new DuplicateLikeException("已点赞");
            }

            // 从数据库检查是否已点赞
            ExperienceLike existingLike = experienceLikeMapper.getPostLike(postId, userId);
            if (existingLike != null) {
                // 更新缓存
                cacheClient.set(cacheKey, "1", Duration.ofHours(1));
                throw new DuplicateLikeException("已点赞");
            }

            // 4. 创建点赞记录
            ExperienceLike like = ExperienceLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            experienceLikeMapper.insertPostLike(like);
            log.info("创建点赞记录成功，postId={}, userId={}", postId, userId);

            // 5. 增加心得点赞计数
            experiencePostMapper.updateLikeCount(postId, 1);
            log.info("增加心得点赞计数，postId={}", postId);

            // 6. 更新Redis缓存点赞状态（1小时）
            cacheClient.set(cacheKey, "1", Duration.ofHours(1));
            log.debug("缓存点赞状态，key={}", cacheKey);

            // 7. 删除心得详情和列表缓存
            evictPostCache(postId);
            evictPostListCache(post.getProductType(), post.getProductId());

            // 8. 发送统计更新消息（异步更新作者被点赞总数）
            String messageId = "like_" + postId + "_" + userId + "_" + System.currentTimeMillis();
            StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                    .type(MessageType.POST_LIKE)
                    .userId(userId)
                    .targetUserId(post.getUserId())
                    .postId(postId)
                    .timestamp(System.currentTimeMillis())
                    .messageId(messageId)
                    .build();
            
            boolean sent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
            if (!sent) {
                log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", messageId);
            }

            // 9. 发送通知消息（通知心得作者）
            if (!userId.equals(post.getUserId())) { // 不给自己发通知
                String notificationId = "notify_like_" + postId + "_" + userId + "_" + System.currentTimeMillis();
                NotificationMessage notificationMessage = NotificationMessage.builder()
                        .type(MessageType.POST_LIKED)
                        .receiverId(post.getUserId())
                        .senderId(userId)
                        .postId(postId)
                        .content("有用户点赞了你的心得")
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
     * 取消点赞心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        // 1. 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        // 2. 查询心得是否存在
        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        // 3. 检查是否已点赞
        ExperienceLike existingLike = experienceLikeMapper.getPostLike(postId, userId);
        if (existingLike == null) {
            log.warn("取消点赞失败，用户未点赞该心得，postId={}, userId={}", postId, userId);
            return; // 幂等性处理：如果未点赞，直接返回成功
        }

        // 4. 删除点赞记录
        experienceLikeMapper.deletePostLike(postId, userId);
        log.info("删除点赞记录成功，postId={}, userId={}", postId, userId);

        // 5. 减少心得点赞计数
        experiencePostMapper.updateLikeCount(postId, -1);
        log.info("减少心得点赞计数，postId={}", postId);

        // 6. 删除Redis缓存点赞状态
        String cacheKey = RedisKeyConstant.postLikeStatusKey(postId, userId);
        try {
            cacheClient.evict(cacheKey);
            log.debug("删除点赞状态缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("删除点赞状态缓存失败", e);
        }

        // 7. 删除心得详情和列表缓存
        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());

        // 8. 发送统计更新消息（异步更新作者被点赞总数）
        String messageId = "unlike_" + postId + "_" + userId + "_" + System.currentTimeMillis();
        StatisticsUpdateMessage statisticsMessage = StatisticsUpdateMessage.builder()
                .type(MessageType.POST_UNLIKE)
                .userId(userId)
                .targetUserId(post.getUserId())
                .postId(postId)
                .timestamp(System.currentTimeMillis())
                .messageId(messageId)
                .build();
        
        boolean sent = messageQueueProducer.sendStatisticsUpdateMessage(statisticsMessage);
        if (!sent) {
            log.warn("统计更新消息发送失败，将通过补偿机制处理，messageId={}", messageId);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证商品引用是否存在
     *
     * @param productType 商品类型 1:SPU 2:组合包
     * @param productId   商品ID
     */
    private void validateProductReference(Integer productType, Long productId) {
        if (productType == null || productId == null) {
            throw new IllegalArgumentException("商品类型和商品ID不能为空");
        }

        boolean exists = false;
        if (productType == 1) {
            // 验证SPU是否存在
            exists = shoeSpuMapper.getById(productId) != null;
        } else if (productType == 2) {
            // 验证组合包是否存在
            exists = bundleMapper.getInfoById(productId) != null;
        } else {
            throw new IllegalArgumentException("无效的商品类型");
        }

        if (!exists) {
            throw new ProductNotFoundException("商品不存在");
        }
    }
    /**
     * 将图片列表转换为JSON字符串
     *
     * @param images 图片列表
     * @return JSON字符串
     */
    private String convertImagesToJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.error("转换图片列表为JSON失败", e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为图片列表
     *
     * @param json JSON字符串
     * @return 图片列表
     */
    private List<String> convertJsonToImages(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("转换JSON为图片列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 删除心得详情缓存
     *
     * @param postId 心得ID
     */
    private void evictPostCache(Long postId) {
        String cacheKey = RedisKeyConstant.experiencePostKey(postId);
        cacheClient.evict(cacheKey);
        log.debug("删除心得详情缓存，key={}", cacheKey);
    }

    /**
     * 删除心得列表缓存
     *
     * @param productType 商品类型
     * @param productId   商品ID
     */
    private void evictPostListCache(Integer productType, Long productId) {
        // 删除按时间排序的缓存
        String timeKey = RedisKeyConstant.experiencePostListKey(productType, productId, "time");
        cacheClient.evict(timeKey);
        log.debug("删除心得列表缓存，key={}", timeKey);

        // 删除按点赞数排序的缓存
        String likeKey = RedisKeyConstant.experiencePostListKey(productType, productId, "like");
        cacheClient.evict(likeKey);
        log.debug("删除心得列表缓存，key={}", likeKey);
    }

    /**
     * 查询用户发布的心得列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ExperiencePostVO> listPostsByUserId(Long userId, Integer page, Integer size) {
        // 1. 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 20;
        }

        log.info("查询用户发布的心得列表，userId={}, page={}, size={}", userId, page, size);

        // 2. 使用PageHelper进行分页查询
        PageHelper.startPage(page, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listByUserId(userId);

        // 3. 构建分页结果
        return new PageResult<>(pageResult.getTotal(), pageResult.getResult());
    }

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
    @Override
    public PageResult<ExperiencePostAdminVO> pageQueryAdmin(Integer productType, Long productId, Integer hidden, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }

        log.info("管理员分页查询心得列表，productType={}, productId={}, hidden={}, page={}, size={}", 
                productType, productId, hidden, page, size);

        PageHelper.startPage(page, size);
        Page<ExperiencePostAdminVO> pageResult = experiencePostMapper.pageQueryAdmin(productType, productId, hidden);

        return new PageResult<>(pageResult.getTotal(), pageResult.getResult());
    }

    /**
     * 查询点赞心得的用户列表
     *
     * @param postId 心得ID
     * @param currentUserId 当前用户ID（可为null）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<UserProfileVO> listLikeUsers(Long postId, Long currentUserId, Integer page, Integer size) {
        if (postId == null) {
            throw new IllegalArgumentException("心得ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 20;
        }

        log.info("查询点赞心得的用户列表，postId={}, currentUserId={}, page={}, size={}", postId, currentUserId, page, size);

        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        PageHelper.startPage(page, size);
        Page<UserProfileVO> pageResult = experienceLikeMapper.listPostLikeUsers(postId);

        List<UserProfileVO> list = pageResult.getResult();
        if (currentUserId != null && !list.isEmpty()) {
            List<Long> followedIds = userFollowMapper.listFolloweeIds(currentUserId);
            for (UserProfileVO vo : list) {
                vo.setIsFollowed(followedIds.contains(vo.getUserId()));
            }
        } else {
            for (UserProfileVO vo : list) {
                vo.setIsFollowed(false);
            }
        }

        return new PageResult<>(pageResult.getTotal(), list);
    }

    /**
     * 查询关注用户的心得列表
     *
     * @param userId 当前用户ID
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ExperiencePostVO> listFollowingPosts(Long userId, String sortBy, Integer page, Integer size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 20;
        }

        log.info("查询关注用户的心得列表，userId={}, sortBy={}, page={}, size={}", userId, sortBy, page, size);

        List<Long> followeeIds = userFollowMapper.listFolloweeIds(userId);
        if (followeeIds == null || followeeIds.isEmpty()) {
            return new PageResult<>(0L, Collections.emptyList());
        }

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "time";
        }

        PageHelper.startPage(page, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listByFollowing(followeeIds, sortBy);

        List<ExperiencePostVO> list = pageResult.getResult();
        for (ExperiencePostVO vo : list) {
            vo.setIsLiked(false);
            vo.setIsFollowed(true);
        }

        return new PageResult<>(pageResult.getTotal(), list);
    }

    /**
     * 查询用户点赞过的心得列表
     *
     * @param userId 用户ID
     * @param sortBy 排序方式 time:按时间 like:按点赞数
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ExperiencePostVO> listLikedPosts(Long userId, String sortBy, Integer page, Integer size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 20;
        }

        log.info("查询用户点赞过的心得列表，userId={}, sortBy={}, page={}, size={}", userId, sortBy, page, size);

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "time";
        }

        PageHelper.startPage(page, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listLikedByUser(userId, sortBy);

        List<ExperiencePostVO> list = pageResult.getResult();
        List<Long> followedIds = userFollowMapper.listFolloweeIds(userId);
        for (ExperiencePostVO vo : list) {
            vo.setIsLiked(true);
            vo.setIsFollowed(followedIds != null && followedIds.contains(vo.getUserId()));
        }

        return new PageResult<>(pageResult.getTotal(), list);
    }

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
    @Override
    public PageResult<ExperiencePostVO> listAllPosts(Long currentUserId, String sortBy, String keyword, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 20;
        }

        log.info("查询所有公开心得列表，currentUserId={}, sortBy={}, keyword={}, page={}, size={}", currentUserId, sortBy, keyword, page, size);

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "time";
        }

        PageHelper.startPage(page, size);
        Page<ExperiencePostVO> pageResult = experiencePostMapper.listAllPublic(sortBy, keyword);

        List<ExperiencePostVO> list = pageResult.getResult();
        
        if (currentUserId != null && !list.isEmpty()) {
            List<Long> followedIds = userFollowMapper.listFolloweeIds(currentUserId);
            for (ExperiencePostVO vo : list) {
                ExperienceLike like = experienceLikeMapper.getPostLike(vo.getId(), currentUserId);
                vo.setIsLiked(like != null);
                vo.setIsFollowed(followedIds != null && followedIds.contains(vo.getUserId()));
            }
        } else {
            for (ExperiencePostVO vo : list) {
                vo.setIsLiked(false);
                vo.setIsFollowed(false);
            }
        }

        return new PageResult<>(pageResult.getTotal(), list);
    }

    /**
     * 修改心得（只能修改内容和图片）
     *
     * @param postId 心得ID
     * @param userId 用户ID
     * @param dto 体验心得DTO（只使用content和images字段）
     * @return 体验心得VO
     */
    @Override
    @Transactional
    public ExperiencePostVO updatePost(Long postId, Long userId, ExperiencePostDTO dto) {
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能修改自己的心得");
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("心得内容不能为空");
        }
        if (dto.getContent().length() > 5000) {
            throw new IllegalArgumentException("心得内容长度不能超过5000字符");
        }

        if (dto.getImages() != null && dto.getImages().size() > 9) {
            throw new IllegalArgumentException("图片数量不能超过9张");
        }

        post.setContent(dto.getContent());
        post.setImages(convertImagesToJson(dto.getImages()));
        post.setUpdateTime(LocalDateTime.now());
        experiencePostMapper.update(post);
        log.info("修改心得成功，postId={}, userId={}", postId, userId);

        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());

        ExperiencePostVO vo = new ExperiencePostVO();
        BeanUtils.copyProperties(post, vo);
        vo.setImages(dto.getImages());
        vo.setIsLiked(false);
        vo.setIsFollowed(false);

        return vo;
    }

    /**
     * 删除心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己的心得");
        }

        experiencePostMapper.deleteById(postId);
        log.info("删除心得成功，postId={}, userId={}", postId, userId);

        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());
    }

    /**
     * 用户隐藏自己的心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void hidePostByUser(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能隐藏自己的心得");
        }

        experiencePostMapper.updateHidden(postId, 1);
        log.info("用户隐藏心得成功，postId={}, userId={}", postId, userId);

        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());
    }

    /**
     * 用户取消隐藏自己的心得
     *
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void unhidePostByUser(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("心得ID和用户ID不能为空");
        }

        ExperiencePost post = experiencePostMapper.getById(postId);
        if (post == null) {
            throw new ExperiencePostNotFoundException("心得不存在");
        }

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能取消隐藏自己的心得");
        }

        experiencePostMapper.updateHidden(postId, 0);
        log.info("用户取消隐藏心得成功，postId={}, userId={}", postId, userId);

        evictPostCache(postId);
        evictPostListCache(post.getProductType(), post.getProductId());
    }

}

