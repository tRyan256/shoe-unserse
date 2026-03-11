package com.su.constant;

public class RedisKeyConstant {
    // 布隆过滤器
    public static final String BLOOM_SPU_ID = "bloom:spu:id";
    public static final String BLOOM_SKU_ID = "bloom:sku:id";
    public static final String BLOOM_BUNDLE_ID = "bloom:bundle:id";
    public static final String BLOOM_OUTLET_ID = "bloom:outlet:id";
    public static final String BLOOM_COUPON_ID = "bloom:coupon:id";
    public static final String OUTLET_GEO = "outlet:geo";

    public static String loginCodeKey(String phone) {
        return "login:code:" + phone;
    }

    public static final Long LOGIN_CODE_TTL = 5L;

    // SPU 相关缓存键
    public static String spuDetailKey(Long spuId) {
        return "spu:detail:" + spuId;
    }

    public static String spuListByCategoryKey(Long categoryId) {
        return "spu:list:category:" + (categoryId == null ? "all" : categoryId);
    }

    public static String outletDetailKey(Long outletId) {
        return "outlet:detail:" + outletId;
    }

    public static String couponDetailKey(Long couponId) {
        return "coupon:detail:" + couponId;
    }

    // Draw 抽签活动相关缓存键（统一使用 drawId 作用域）
    public static String drawDetailKey(Long drawId) {
        return "cache:draw:detail:" + drawId;
    }

    public static String drawQuotaRemainingKey(Long drawId) {
        return "draw:" + drawId + ":quota:remaining";
    }

    public static String drawParticipantsKey(Long drawId) {
        return "draw:" + drawId + ":participants";
    }

    public static String drawRecordsKey(Long drawId) {
        return "draw:" + drawId + ":records";
    }

    public static String drawWinnersKey(Long drawId) {
        return "draw:" + drawId + ":winners";
    }

    public static String drawStateKey(Long drawId) {
        return "draw:" + drawId + ":state";
    }


    // Draw 历史 key，仅用于重建前迁移/清理
    public static String legacyDrawRemainingKey(Long drawId) {
        return "draw:remaining:" + drawId;
    }

    public static String legacyDrawParticipantsKey(Long drawId) {
        return "draw:participants:" + drawId;
    }

    public static String legacyDrawJoinUsersKey(Long drawId) {
        return "draw:join:users:" + drawId;
    }

    public static String legacyDrawJoinRecordKey(Long drawId, Long userId) {
        return "draw:join:record:" + drawId + ":" + userId;
    }

    public static String legacyDrawResultKey(Long drawId) {
        return "draw:result:" + drawId;
    }

    public static String legacyDrawRecordKey(Long recordId) {
        return "draw:record:" + recordId;
    }

    public static String legacyDrawRecordKeyPrefix() {
        return "draw:record:";
    }

    public static String legacyDrawWinnerUsersKey(Long drawId) {
        return "draw:winner:users:" + drawId;
    }

    public static String legacyDrawProcessedKey(Long drawId) {
        return "draw:processed:" + drawId;
    }

    public static String legacyDrawRevealScheduledKey(Long drawId) {
        return "draw:reveal:scheduled:" + drawId;
    }

    public static String legacyDrawUserRecordsKey(Long userId) {
        return "draw:user:records:" + userId;
    }

    public static String lockKey(String key) {
        return "lock:" + key;
    }

    public static String airdropStockKey(Long airdropId) {
        return "airdrop:stock:" + airdropId;
    }

    public static String airdropUsersKey(Long airdropId) {
        return "airdrop:users:" + airdropId;
    }

    public static String airdropMetaKey(Long airdropId) {
        return "airdrop:meta:" + airdropId;
    }


    public static String airdropClaimMqRetryKey() {
        return "airdrop:claim:mq:retry";
    }

    public static String airdropClaimDlqKey() {
        return "airdrop:claim:dlq";
    }

    public static String drawJoinMqRetryKey() {
        return "draw:join:mq:retry";
    }

    public static String drawJoinDlqKey() {
        return "draw:join:dlq";
    }

    public static String shopStatusKey() {
        return "SHOP_STATUS";
    }

    public static String bundleListKey() {
        return "bundle:list";
    }

    public static String bundleDetailKey(Long bundleId) {
        return "bundle:detail:" + bundleId;
    }

    public static String mqReturnsKey() {
        return "mq:returns";
    }

    public static String mqConfirmNackKey() {
        return "mq:confirm:nack";
    }

    public static String mqDlqKey() {
        return "mq:dlq";
    }

    public static String stockReserveKey(String orderNumber) {
        return "stock:reserve:" + orderNumber;
    }

    public static String stockReserveLockKey(String orderNumber) {
        return "lock:stock:reserve:" + orderNumber;
    }

    public static String stockRollbackInflightKey(String orderNumber) {
        return "stock:rollback:inflight:" + orderNumber;
    }

    public static String stockRollbackDoneKey(String orderNumber) {
        return "stock:rollback:done:" + orderNumber;
    }

    // 库存相关缓存键
    public static String stockSkuKey(Long skuId) {
        return "stock:sku:" + skuId;
    }

    // 预订单相关缓存键
    public static String preorderKey(String orderNumber) {
        return "preorder:" + orderNumber;
    }

    // Draw 锁相关缓存键
    public static String drawWinConfirmLockKey(Long drawId, Long userId) {
        return "lock:draw:winConfirm:" + drawId + ":" + userId;
    }

    public static String drawRevealLockKey(Long drawId) {
        return "lock:draw:" + drawId;
    }

    // ==================== 体验心得系统缓存键 ====================

    /**
     * 心得详情缓存键
     * @param postId 心得ID
     * @return 缓存键
     */
    public static String experiencePostKey(Long postId) {
        return "experience:post:" + postId;
    }

    /**
     * 心得列表缓存键（按商品）
     * @param productType 商品类型 1:SPU 2:组合包
     * @param productId 商品ID
     * @param sortBy 排序方式 (time/like)
     * @return 缓存键
     */
    public static String experiencePostListKey(Integer productType, Long productId, String sortBy) {
        return "experience:post:list:" + productType + ":" + productId + ":" + sortBy;
    }

    /**
     * 用户统计缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String userStatisticsKey(Long userId) {
        return "user:stats:" + userId;
    }

    /**
     * 心得点赞状态缓存键
     * @param postId 心得ID
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String postLikeStatusKey(Long postId, Long userId) {
        return "experience:like:post:" + postId + ":" + userId;
    }

    /**
     * 评论点赞状态缓存键
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String commentLikeStatusKey(Long commentId, Long userId) {
        return "experience:like:comment:" + commentId + ":" + userId;
    }

    /**
     * 关注状态缓存键
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 缓存键
     */
    public static String followStatusKey(Long followerId, Long followeeId) {
        return "experience:follow:" + followerId + ":" + followeeId;
    }

    /**
     * 评论列表缓存键
     * @param postId 心得ID
     * @return 缓存键
     */
    public static String commentListKey(Long postId) {
        return "experience:comment:list:" + postId;
    }

    /**
     * 经验系统消息处理记录缓存键
     * 用于记录发送失败的消息，便于后续补偿重试
     * @param messageType 消息类型（statistics/notification）
     * @param messageId 消息ID
     * @return 缓存键
     */
    public static String experienceFailedMessageKey(String messageType, String messageId) {
        return "experience:failed:message:" + messageType + ":" + messageId;
    }

    /**
     * 经验系统消息处理记录缓存键（幂等性处理）
     * @param messageId 消息ID
     * @return 缓存键
     */
    public static String experienceMessageProcessedKey(String messageId) {
        return "experience:message:processed:" + messageId;
    }

    /**
     * 经验系统通知处理记录缓存键（幂等性处理）
     * @param messageId 消息ID
     * @return 缓存键
     */
    public static String experienceNotificationProcessedKey(String messageId) {
        return "experience:notification:processed:" + messageId;
    }
}
