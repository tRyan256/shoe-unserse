package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.su.utils.cache.CacheClient;
import com.su.context.BaseContext;
import com.su.dto.DrawWinConfirmDTO;
import com.su.dto.DrawWinConfirmItemDTO;
import com.su.dto.DrawDTO;
import com.su.dto.DrawPageQueryDTO;
import com.su.constant.DelayTaskTypeConstant;
import com.su.component.delay.DelayQueueService;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.entity.AddressBook;
import com.su.entity.Bundle;
import com.su.entity.BundleShoe;
import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.entity.DrawRecordItem;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSpu;
import com.su.entity.ShoeSkuSize;
import com.su.entity.ShoppingCart;
import com.su.entity.Orders;
import com.su.service.impl.draw.support.DrawRedisService;
import com.su.service.impl.draw.support.DrawRevealService;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AddressBookMapper;
import com.su.mapper.BundleMapper;
import com.su.mapper.BundleShoeMapper;
import com.su.mapper.DrawMapper;
import com.su.mapper.DrawRecordItemMapper;
import com.su.mapper.DrawRecordMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import com.su.mq.message.DrawJoinPersistMessage;
import com.su.mq.message.DrawOrderCreateMessage;
import com.su.mq.message.OrderCreateItemMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.result.PageResult;
import com.su.service.DrawService;
import com.su.service.StockReservationService;
import com.su.utils.id.OrderNumberGenerator;
import com.su.utils.id.DrawRecordIdGenerator;
import com.su.vo.DrawAdminPageVO;
import com.su.vo.DrawBundleItemPublicVO;
import com.su.vo.DrawBundlePublicVO;
import com.su.vo.DrawBundleShoeOptionsVO;
import com.su.vo.DrawDetailVO;
import com.su.vo.DrawShoePublicVO;
import com.su.vo.DrawShoeSizeOptionVO;
import com.su.vo.DrawWinOptionsVO;
import com.su.vo.DrawWinnerPublicVO;
import com.su.vo.DrawWinnerVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DrawServiceImpl implements DrawService {
    private static final long DRAW_JOIN_MQ_BACKOFF_MS = 5000L;

    private final AtomicLong drawJoinMqSuspendUntil = new AtomicLong(0L);

    @Autowired
    private DrawMapper drawMapper;
    @Autowired
    private DrawRecordMapper drawRecordMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private DrawRedisService drawRedisService;
    @Autowired
    private DrawRevealService drawRevealService;
    @Autowired
    private DelayQueueService delayQueueService;
    @Autowired
    private DrawRecordIdGenerator drawRecordIdGenerator;
    @Autowired
    private ShoeSkuMapper shoeSkuMapper;
    @Autowired
    private ShoeSpuMapper shoeSpuMapper;
    @Autowired
    private ShoeSkuSizeMapper shoeSkuSizeMapper;
    @Autowired
    private BundleMapper bundleMapper;
    @Autowired
    private BundleShoeMapper bundleShoeMapper;
    @Autowired
    private StockReservationService stockReservationService;
    @Autowired
    private OrderNumberGenerator orderNumberGenerator;
    @Autowired
    private DrawRecordItemMapper drawRecordItemMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    @Qualifier("drawDetailLocalCache")
    private Cache<Long, DrawDetailVO> drawDetailLocalCache;    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void save(DrawDTO dto) {
        Draw draw = new Draw();
        BeanUtils.copyProperties(dto, draw);
        LocalDateTime now = LocalDateTime.now();
        if (draw.getStatus() == null) {
            draw.setStatus(0);
        }
        if (draw.getStatus() != null && draw.getStatus() == 0 && draw.getStartTime() != null && !draw.getStartTime().isAfter(now)) {
            draw.setStatus(1);
        }
        draw.setCreateTime(now);
        draw.setUpdateTime(now);
        drawMapper.insert(draw);
        Draw latest = drawMapper.getById(draw.getId());
        if (latest != null) {
            warmupDrawCache(latest.getId());
        }
        scheduleStartIfNeeded(latest);
        scheduleEndRevealIfNeeded(latest);
    }

    @Override
    public PageResult page(DrawPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DrawAdminPageVO> page = drawMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Draw getById(Long id) {
        return drawMapper.getById(id);
    }

    @Override
    public Draw getDrawByIdWithCache(Long id) {
        // 优化：从详情缓存中获取基础信息，避免维护两个key
        DrawDetailVO detailVO = detail(id);
        if (detailVO == null) {
            return null;
        }
        
        // 将DrawDetailVO转换为Draw实体
        Draw draw = new Draw();
        draw.setId(detailVO.getId());
        draw.setTitle(detailVO.getTitle());
        draw.setTargetType(detailVO.getTargetType());
        draw.setSkuId(detailVO.getSkuId());
        draw.setBundleId(detailVO.getBundleId());
        draw.setPrice(detailVO.getPrice());
        draw.setTotalStock(detailVO.getTotalStock());
        draw.setMaxParticipants(detailVO.getMaxParticipants());
        draw.setWinnerCount(detailVO.getWinnerCount());
        draw.setStartTime(detailVO.getStartTime());
        draw.setEndTime(detailVO.getEndTime());
        draw.setDrawTime(detailVO.getDrawTime());
        draw.setStatus(detailVO.getStatus());
        draw.setDescription(detailVO.getDescription());
        return draw;
    }

    @Override
    public void update(DrawDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        Draw existing = drawMapper.getById(dto.getId());
        if (existing == null) {
            throw new OrderBusinessException("抽签活动不存在");
        }
        throw new OrderBusinessException("抽签活动创建后不支持编辑，请取消后重新创建");
    }

    private void scheduleStartIfNeeded(Draw draw) {
        if (draw == null || draw.getId() == null) {
            return;
        }
        if (draw.getStatus() == null || draw.getStatus() != 0) {
            return;
        }
        if (draw.getStartTime() == null) {
            return;
        }
        long startMillis = draw.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        delayQueueService.schedule(DelayTaskTypeConstant.DRAW_START, String.valueOf(draw.getId()), startMillis, null);
    }

    private void scheduleEndRevealIfNeeded(Draw draw) {
        if (draw == null || draw.getId() == null) {
            return;
        }
        if (draw.getStatus() != null && (draw.getStatus() == 2 || draw.getStatus() == 3)) {
            return;
        }
        if (draw.getEndTime() == null) {
            return;
        }
        long endMillis = draw.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        delayQueueService.schedule(DelayTaskTypeConstant.DRAW_REVEAL, String.valueOf(draw.getId()), endMillis, null);
    }

    @Override
    public void deleteById(Long id) {
        drawMapper.deleteById(id);
        cacheClient.evict(RedisKeyConstant.drawDetailKey(id));
        drawRedisService.deleteForDraw(id);
    }

    @Override
    public List<Draw> listActive() {
        return drawMapper.listActive();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void join(Long drawId) {
        if (drawId == null) {
            throw new OrderBusinessException("参数错误");
        }
        Draw draw = getDrawByIdFromHotCache(drawId);
        if (draw == null || !drawRedisService.hasRuntimeCache(drawId)) {
            throw new OrderBusinessException("抽签活动缓存未就绪，请稍后重试");
        }
        if (draw.getStatus() == null || draw.getStatus() != 1) {
            throw new OrderBusinessException("抽签活动未开始");
        }
        LocalDateTime now = LocalDateTime.now();
        if (draw.getStartTime() != null && now.isBefore(draw.getStartTime())) {
            throw new OrderBusinessException("抽签活动未开始");
        }
        if (draw.getEndTime() != null && now.isAfter(draw.getEndTime())) {
            throw new OrderBusinessException("抽签活动已结束");
        }

        Long userId = BaseContext.getCurrentId();
        DrawRecord record = DrawRecord.builder()
                .id(drawRecordIdGenerator.nextId())
                .drawId(draw.getId())
                .userId(userId)
                .addressBookId(null)
                .shoeSize(null)
                .skuId(Objects.equals(draw.getTargetType(), 1) ? draw.getSkuId() : null)
                .status(0)
                .createTime(LocalDateTime.now())
                .build();
        long reserved = drawRedisService.reserveAndCacheJoin(draw, record);
        if (reserved == DrawRedisService.ALREADY_JOINED) {
            throw new OrderBusinessException("已参与该抽签");
        }
        if (reserved == DrawRedisService.FULL) {
            throw new OrderBusinessException("参与人数已满");
        }
        if (reserved == DrawRedisService.INVALID_ARGUMENT) {
            throw new OrderBusinessException("抽签活动缓存未就绪，请稍后重试");
        }

        DrawJoinPersistMessage message = DrawJoinPersistMessage.builder()
                .msgId(draw.getId() + ":" + userId + ":" + record.getId())
                .recordId(record.getId())
                .drawId(draw.getId())
                .userId(userId)
                .addressBookId(record.getAddressBookId())
                .shoeSize(record.getShoeSize())
                .skuId(record.getSkuId())
                .createTime(record.getCreateTime())
                .build();
        boolean sent = sendDrawJoinMessage(message);
        if (!sent) {
            enqueueDrawJoinRetry(message);
        }

        if (reserved == 0L) {
            Long fullDrawId = draw.getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (drawRedisService.markRevealScheduled(fullDrawId)) {
                            delayQueueService.schedule(DelayTaskTypeConstant.DRAW_REVEAL_FULL, String.valueOf(fullDrawId), System.currentTimeMillis(), null);
                        }
                    }
                });
            } else if (drawRedisService.markRevealScheduled(fullDrawId)) {
                delayQueueService.schedule(DelayTaskTypeConstant.DRAW_REVEAL_FULL, String.valueOf(fullDrawId), System.currentTimeMillis(), null);
            }
        }
    }

    @Override
    public List<DrawRecord> myRecords() {
        Long userId = BaseContext.getCurrentId();
        List<DrawRecord> merged = drawRecordMapper.listByUserId(userId);
        if (merged == null) {
            merged = new ArrayList<>();
        }
        Map<Long, DrawRecord> byId = new HashMap<>();
        for (DrawRecord record : merged) {
            if (record != null && record.getId() != null) {
                byId.put(record.getId(), record);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<Draw> activeAndWarmup = drawMapper.listWarmup(now.minusDays(1), now.plusDays(2));
        if (activeAndWarmup == null || activeAndWarmup.isEmpty()) {
            activeAndWarmup = List.of();
        }
        for (Draw draw : activeAndWarmup) {
            if (draw == null || draw.getId() == null) {
                continue;
            }
            DrawRecord cached = drawRedisService.getRecord(draw.getId(), userId);
            if (cached == null || cached.getId() == null || byId.containsKey(cached.getId())) {
                continue;
            }
            merged.add(cached);
            byId.put(cached.getId(), cached);
        }

        merged.sort((a, b) -> {
            if (a == null) {
                return 1;
            }
            if (b == null) {
                return -1;
            }
            if (a.getCreateTime() != null && b.getCreateTime() != null) {
                int timeCmp = b.getCreateTime().compareTo(a.getCreateTime());
                if (timeCmp != 0) {
                    return timeCmp;
                }
            }
            Long aId = a.getId() == null ? 0L : a.getId();
            Long bId = b.getId() == null ? 0L : b.getId();
            return Long.compare(bId, aId);
        });
        return merged;
    }

    @Override
    public DrawRecord myResult(Long drawId) {
        if (drawId == null) {
            return null;
        }
        Long userId = BaseContext.getCurrentId();
        DrawRecord cached = drawRedisService.getRecord(drawId, userId);
        if (cached != null && cached.getStatus() != null) {
            return cached;
        }
        return drawRecordMapper.getByDrawIdAndUserId(drawId, userId);
    }

        @Override
    public DrawDetailVO detail(Long drawId) {
        if (drawId == null) {
            return null;
        }
        if (drawDetailLocalCache != null) {
            DrawDetailVO local = drawDetailLocalCache.getIfPresent(drawId);
            if (local != null) {
                return local;
            }
        }
        String key = RedisKeyConstant.drawDetailKey(drawId);
        String lockKey = RedisKeyConstant.lockKey(key);
        
        // 优化：活动详情使用逻辑过期缓存，适用于预热场景
        // 逻辑TTL较短(10分钟)，但物理TTL较长(2小时)，过期后异步更新
        DrawDetailVO vo = cacheClient.queryWithLogicalExpire(
                key,
                lockKey,
                DrawDetailVO.class,
                () -> buildDrawDetail(drawId),
                Duration.ofMinutes(10),   // 逻辑TTL: 10分钟
                Duration.ofHours(2),      // 物理TTL: 2小时
                Duration.ofMinutes(1),    // 空值TTL: 1分钟
                null,
                null
        );
        if (vo != null && drawDetailLocalCache != null) {
            drawDetailLocalCache.put(drawId, vo);
        }
        return vo;
    }
        String key = RedisKeyConstant.drawDetailKey(drawId);
        String lockKey = RedisKeyConstant.lockKey(key);
        
        // 优化：活动详情使用逻辑过期缓存，适用于预热场景
        // 逻辑TTL较短(10分钟)，但物理TTL较长(2小时)，过期后异步更新
        DrawDetailVO vo = cacheClient.queryWithLogicalExpire(
                key,
                lockKey,
                DrawDetailVO.class,
                () -> buildDrawDetail(drawId),
                Duration.ofMinutes(10),   // 逻辑TTL: 10分钟
                Duration.ofHours(2),      // 物理TTL: 2小时
                Duration.ofMinutes(1),    // 空值TTL: 1分钟
                null,
                null
        );
        return vo;
    }

    private DrawDetailVO buildDrawDetail(Long drawId) {
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            return null;
        }
        DrawDetailVO vo = new DrawDetailVO();
        BeanUtils.copyProperties(draw, vo);

        if (Objects.equals(draw.getTargetType(), 1)) {
            if (draw.getSkuId() == null) {
                return vo;
            }
            ShoeSku sku = shoeSkuMapper.getById(draw.getSkuId());
            if (sku == null) {
                return vo;
            }
            ShoeSpu spu = shoeSpuMapper.getById(sku.getSpuId());
            if (spu == null) {
                return vo;
            }
            List<ShoeSkuSize> sizes = shoeSkuSizeMapper.listBySkuId(sku.getId());
            List<String> sizeList = sizes == null ? List.of() : sizes.stream()
                    .map(ShoeSkuSize::getSize)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            DrawShoePublicVO shoeVO = DrawShoePublicVO.builder()
                    .id(sku.getId())
                    .name(spu.getName())
                    .brand(spu.getBrand())
                    .model(spu.getModel())
                    .color(sku.getColorName())
                    .releaseDate(spu.getReleaseDate())
                    .isLimited(spu.getIsLimited())
                    .price(sku.getPrice())
                    .image(sku.getImage())
                    .description(spu.getDescription())
                    .sizes(sizeList)
                    .build();
            vo.setShoe(shoeVO);
            return vo;
        }

        if (Objects.equals(draw.getTargetType(), 2)) {
            if (draw.getBundleId() == null) {
                return vo;
            }
            Bundle bundle = bundleMapper.getInfoById(draw.getBundleId());
            if (bundle == null) {
                return vo;
            }

            List<BundleShoe> bundleShoes = bundleShoeMapper.getByBundleIds(List.of(bundle.getId()));
            List<ShoeSku> skus = shoeSkuMapper.getByBundleId(bundle.getId());
            Map<Long, ShoeSku> skuMap = new HashMap<>();
            if (skus != null) {
                for (ShoeSku s : skus) {
                    if (s != null && s.getId() != null) {
                        skuMap.put(s.getId(), s);
                    }
                }
            }
            List<Long> skuIds = bundleShoes == null ? List.of() : bundleShoes.stream()
                    .map(BundleShoe::getSkuId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, List<ShoeSkuSize>> sizesBySkuId = new HashMap<>();
            if (!skuIds.isEmpty()) {
                for (Long skuId : skuIds) {
                    List<ShoeSkuSize> sizes = shoeSkuSizeMapper.listBySkuId(skuId);
                    if (sizes != null) {
                        sizesBySkuId.put(skuId, sizes);
                    }
                }
            }

            List<DrawBundleItemPublicVO> items = new ArrayList<>();
            if (bundleShoes != null) {
                for (BundleShoe bs : bundleShoes) {
                    if (bs == null || bs.getSkuId() == null) {
                        continue;
                    }
                    ShoeSku s = skuMap.get(bs.getSkuId());
                    List<String> sizeList = sizesBySkuId.getOrDefault(bs.getSkuId(), List.of()).stream()
                            .map(ShoeSkuSize::getSize)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    items.add(DrawBundleItemPublicVO.builder()
                            .skuId(bs.getSkuId())
                            .name(bs.getName())
                            .image(s == null ? null : s.getImage())
                            .description(s == null ? null : shoeSpuMapper.getById(s.getSpuId()) == null ? null : shoeSpuMapper.getById(s.getSpuId()).getDescription())
                            .price(bs.getPrice())
                            .copies(bs.getCopies())
                            .sizes(sizeList)
                            .build());
                }
            }

            DrawBundlePublicVO bundleVO = DrawBundlePublicVO.builder()
                    .id(bundle.getId())
                    .name(bundle.getName())
                    .price(bundle.getPrice())
                    .image(bundle.getImage())
                    .description(bundle.getDescription())
                    .items(items)
                    .build();
            vo.setBundle(bundleVO);
        }

        return vo;
    }

    @Override
    public DrawWinOptionsVO winOptions(Long drawId) {
        if (drawId == null) {
            return null;
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            throw new OrderBusinessException("抽签活动不存在");
        }
        if (draw.getStatus() == null || draw.getStatus() != 2) {
            throw new OrderBusinessException("抽签活动未开奖");
        }

        Long userId = BaseContext.getCurrentId();
        DrawRecord record = drawRecordMapper.getByDrawIdAndUserId(drawId, userId);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            throw new OrderBusinessException("未中奖");
        }
        if (record.getOrderNo() != null && !record.getOrderNo().isBlank()) {
            throw new OrderBusinessException("已生成订单");
        }

        DrawWinOptionsVO vo = new DrawWinOptionsVO();
        vo.setTargetType(draw.getTargetType());
        vo.setSkuId(draw.getSkuId());
        vo.setBundleId(draw.getBundleId());

        if (Objects.equals(draw.getTargetType(), 1)) {
            vo.setShoeOptions(loadStockOptions(draw.getSkuId()));
        } else if (Objects.equals(draw.getTargetType(), 2)) {
            List<BundleShoe> bundleShoes = draw.getBundleId() == null ? List.of() : bundleShoeMapper.getByBundleIds(List.of(draw.getBundleId()));
            List<DrawBundleShoeOptionsVO> bundleOptions = new ArrayList<>();
            if (bundleShoes != null) {
                for (BundleShoe bs : bundleShoes) {
                    if (bs == null || bs.getSkuId() == null) {
                        continue;
                    }
                    bundleOptions.add(DrawBundleShoeOptionsVO.builder()
                            .skuId(bs.getSkuId())
                            .copies(bs.getCopies())
                            .options(loadStockOptions(bs.getSkuId()))
                            .build());
                }
            }
            vo.setBundleOptions(bundleOptions);
        }
        return vo;
    }

    private List<DrawShoeSizeOptionVO> loadStockOptions(Long skuId) {
        if (skuId == null) {
            return List.of();
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisKeyConstant.stockSkuKey(skuId));
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<DrawShoeSizeOptionVO> list = new ArrayList<>();
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }
            String size = String.valueOf(e.getKey());
            Integer stock = 0;
            try {
                stock = e.getValue() == null ? 0 : Integer.valueOf(String.valueOf(e.getValue()));
            } catch (Exception ignored) {
            }
            list.add(DrawShoeSizeOptionVO.builder().size(size).stock(stock).build());
        }
        list.sort(Comparator.comparing(DrawShoeSizeOptionVO::getSize, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String winConfirm(Long drawId, DrawWinConfirmDTO dto) {
        if (drawId == null) {
            throw new OrderBusinessException("参数错误");
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            throw new OrderBusinessException("抽签活动不存在");
        }
        if (draw.getStatus() == null || draw.getStatus() != 2) {
            throw new OrderBusinessException("抽签活动未开奖");
        }
        if (draw.getDrawTime() != null && LocalDateTime.now().isAfter(draw.getDrawTime().plusHours(24))) {
            throw new OrderBusinessException("中奖资格已过期，请在开奖后24小时内完成购买");
        }

        Long userId = BaseContext.getCurrentId();
        RLock lock = redissonClient.getLock(RedisKeyConstant.drawWinConfirmLockKey(drawId, userId));
        boolean locked;
        try {
            locked = lock.tryLock(0, 15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderBusinessException("请勿重复操作");
        }
        if (!locked) {
            throw new OrderBusinessException("请勿重复操作");
        }
        boolean unlockInFinally = true;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            });
            unlockInFinally = false;
        }
        try {
            DrawRecord record = drawRecordMapper.getByDrawIdAndUserId(drawId, userId);
            if (record == null || record.getStatus() == null || record.getStatus() != 1) {
                throw new OrderBusinessException("未中奖");
            }
            if (record.getOrderNo() != null && !record.getOrderNo().isBlank()) {
                Orders existingOrder = orderMapper.getByNumber(record.getOrderNo());
                if (existingOrder != null) {
                    return record.getOrderNo();
                }
                log.warn("抽签记录关联的订单不存在，drawId={}, userId={}, orderNo={}", drawId, userId, record.getOrderNo());
            }

            Long addressBookId = dto == null ? null : dto.getAddressBookId();
            if (addressBookId == null) {
                addressBookId = record.getAddressBookId();
            }
            AddressBook addressBook = addressBookId == null ? null : addressBookMapper.getById(addressBookId);
            if (addressBook == null || !Objects.equals(addressBook.getUserId(), userId)) {
                throw new OrderBusinessException("请选择收货地址");
            }

            String orderNo = orderNumberGenerator.next();

            List<ShoppingCart> carts = buildWinConfirmCarts(draw, dto);
            stockReservationService.reserveForOrderAllowDisabledAtomic(orderNo, userId, carts);

            if (Objects.equals(draw.getTargetType(), 1)) {
                String shoeSize = dto == null ? null : dto.getShoeSize();
                if (shoeSize == null || shoeSize.isBlank()) {
                    throw new OrderBusinessException("请选择鞋码");
                }
                int updated = drawRecordMapper.updateOrderNoAndShoeSize(record.getId(), orderNo, shoeSize);
                if (updated == 0) {
                    throw new OrderBusinessException("订单已生成");
                }
            } else if (Objects.equals(draw.getTargetType(), 2)) {
                List<DrawRecordItem> items = buildWinConfirmItems(record.getId(), draw.getBundleId(), dto);
                if (!items.isEmpty()) {
                    drawRecordItemMapper.deleteByRecordId(record.getId());
                    drawRecordItemMapper.insertBatch(items);
                }
                int updated = drawRecordMapper.updateStatusAndOrderNo(DrawRecord.builder().id(record.getId()).status(1).orderNo(orderNo).build());
                if (updated == 0) {
                    throw new OrderBusinessException("订单已生成");
                }
            }
            writePreorderProcessing(orderNo, userId);

            DrawOrderCreateMessage message = DrawOrderCreateMessage.builder()
                    .msgId(orderNo)
                    .orderNumber(orderNo)
                    .userId(userId)
                    .addressBookId(addressBook.getId())
                    .drawId(drawId)
                    .drawRecordId(record.getId())
                    .targetType(draw.getTargetType())
                    .amount(draw.getPrice())
                    .orderTime(LocalDateTime.now())
                    .items(carts.stream().map(this::toItemMessage).filter(Objects::nonNull).collect(Collectors.toList()))
                    .build();
            if (!sendDrawOrderMessage(message)) {
                stockReservationService.rollbackReservationByCartList(orderNo, userId, carts);
                writePreorderFailed(orderNo, userId, "下单消息投递失败");
                throw new OrderBusinessException("下单消息投递失败");
            }

            drawRedisService.updateRecordOnWinConfirm(
                    drawId,
                    userId,
                    Objects.equals(draw.getTargetType(), 1) && dto != null ? dto.getShoeSize() : null,
                    orderNo,
                    addressBook.getId()
            );
            return orderNo;
        } finally {
            if (unlockInFinally && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private OrderCreateItemMessage toItemMessage(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }
        return OrderCreateItemMessage.builder()
                .skuId(cart.getSkuId())
                .bundleId(cart.getBundleId())
                .shoeSize(cart.getShoeSize())
                .number(cart.getNumber())
                .amount(cart.getAmount())
                .name(cart.getName())
                .image(cart.getImage())
                .build();
    }

    private boolean sendDrawOrderMessage(DrawOrderCreateMessage message) {
        if (message == null || message.getMsgId() == null || message.getMsgId().isBlank()) {
            return false;
        }
        try {
            CorrelationData correlationData = new CorrelationData(message.getMsgId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.DRAW_ORDER_EXCHANGE,
                    RabbitMqConstant.DRAW_ORDER_CREATE_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, java.util.concurrent.TimeUnit.SECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            return false;
        }
    }

    private void writePreorderProcessing(String orderNumber, Long userId) {
        PreOrderRecord record = PreOrderRecord.builder()
                .orderNumber(orderNumber)
                .status(OrderAsyncStatusConstant.PROCESSING)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
        try {
            cacheClient.set(RedisKeyConstant.preorderKey(orderNumber), objectMapper.writeValueAsString(record), Duration.ofMinutes(30));
        } catch (Exception ignored) {
        }
    }

    private void writePreorderFailed(String orderNumber, Long userId, String error) {
        PreOrderRecord record = PreOrderRecord.builder()
                .orderNumber(orderNumber)
                .status(OrderAsyncStatusConstant.FAILED)
                .userId(userId)
                .error(error)
                .createTime(LocalDateTime.now())
                .build();
        try {
            cacheClient.set(RedisKeyConstant.preorderKey(orderNumber), objectMapper.writeValueAsString(record), Duration.ofMinutes(30));
        } catch (Exception ignored) {
        }
    }

    private List<ShoppingCart> buildWinConfirmCarts(Draw draw, DrawWinConfirmDTO dto) {
        if (draw == null || draw.getTargetType() == null) {
            return List.of();
        }
        List<ShoppingCart> carts = new ArrayList<>();
        if (Objects.equals(draw.getTargetType(), 1)) {
            String shoeSize = dto == null ? null : dto.getShoeSize();
            if (shoeSize == null || shoeSize.isBlank()) {
                throw new OrderBusinessException("请选择鞋码");
            }
            ShoeSku sku = draw.getSkuId() == null ? null : shoeSkuMapper.getById(draw.getSkuId());
            ShoppingCart cart = new ShoppingCart();
            cart.setSkuId(draw.getSkuId());
            cart.setShoeSize(shoeSize);
            cart.setNumber(1);
            cart.setAmount(draw.getPrice());
            cart.setName(draw.getTitle());
            cart.setImage(sku == null ? null : sku.getImage());
            cart.setSelected(1);
            carts.add(cart);
            return carts;
        }

        if (Objects.equals(draw.getTargetType(), 2)) {
            if (draw.getBundleId() == null) {
                throw new OrderBusinessException("抽签组合包不存在");
            }
            List<BundleShoe> bundleShoes = bundleShoeMapper.getByBundleIds(List.of(draw.getBundleId()));
            if (bundleShoes == null || bundleShoes.isEmpty()) {
                throw new OrderBusinessException("组合包信息缺失");
            }
            List<DrawWinConfirmItemDTO> selected = dto == null ? null : dto.getItems();
            Map<Long, String> selectedMap = new HashMap<>();
            if (selected != null) {
                for (DrawWinConfirmItemDTO it : selected) {
                    if (it != null && it.getSkuId() != null && it.getShoeSize() != null) {
                        selectedMap.put(it.getSkuId(), it.getShoeSize());
                    }
                }
            }
            List<ShoeSku> skus = shoeSkuMapper.getByBundleId(draw.getBundleId());
            Map<Long, ShoeSku> skuMap = new HashMap<>();
            if (skus != null) {
                for (ShoeSku s : skus) {
                    if (s != null && s.getId() != null) {
                        skuMap.put(s.getId(), s);
                    }
                }
            }
            int totalUnits = 0;
            for (BundleShoe bs : bundleShoes) {
                if (bs == null) {
                    continue;
                }
                totalUnits += (bs.getCopies() == null ? 1 : bs.getCopies());
            }
            java.math.BigDecimal unitPrice = totalUnits <= 0
                    ? draw.getPrice()
                    : draw.getPrice().divide(java.math.BigDecimal.valueOf(totalUnits), 2, java.math.RoundingMode.HALF_UP);

            for (BundleShoe bs : bundleShoes) {
                if (bs == null || bs.getSkuId() == null) {
                    continue;
                }
                String size = selectedMap.get(bs.getSkuId());
                if (size == null || size.isBlank()) {
                    throw new OrderBusinessException("请选择鞋码");
                }
                ShoeSku sku = skuMap.get(bs.getSkuId());
                ShoppingCart cart = new ShoppingCart();
                cart.setSkuId(bs.getSkuId());
                cart.setBundleId(draw.getBundleId());
                cart.setShoeSize(size);
                cart.setNumber(bs.getCopies() == null ? 1 : bs.getCopies());
                cart.setAmount(unitPrice);
                cart.setName(bs.getName());
                cart.setImage(sku == null ? null : sku.getImage());
                cart.setSelected(1);
                carts.add(cart);
            }
            return carts;
        }

        return carts;
    }

    private List<DrawRecordItem> buildWinConfirmItems(Long recordId, Long bundleId, DrawWinConfirmDTO dto) {
        if (recordId == null || bundleId == null) {
            return List.of();
        }
        List<BundleShoe> bundleShoes = bundleShoeMapper.getByBundleIds(List.of(bundleId));
        if (bundleShoes == null || bundleShoes.isEmpty()) {
            return List.of();
        }
        List<DrawWinConfirmItemDTO> selected = dto == null ? null : dto.getItems();
        Map<Long, String> selectedMap = new HashMap<>();
        if (selected != null) {
            for (DrawWinConfirmItemDTO it : selected) {
                if (it != null && it.getSkuId() != null && it.getShoeSize() != null) {
                    selectedMap.put(it.getSkuId(), it.getShoeSize());
                }
            }
        }
        List<DrawRecordItem> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (BundleShoe bs : bundleShoes) {
            if (bs == null || bs.getSkuId() == null) {
                continue;
            }
            String size = selectedMap.get(bs.getSkuId());
            if (size == null || size.isBlank()) {
                throw new OrderBusinessException("请选择鞋码");
            }
            items.add(DrawRecordItem.builder()
                    .recordId(recordId)
                    .skuId(bs.getSkuId())
                    .shoeSize(size)
                    .copies(bs.getCopies() == null ? 1 : bs.getCopies())
                    .createTime(now)
                    .build());
        }
        return items;
    }

    @Override
    public void giveUp(Long drawId) {
        if (drawId == null) {
            return;
        }
        Long userId = BaseContext.getCurrentId();
        DrawRecord record = drawRecordMapper.getByDrawIdAndUserId(drawId, userId);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            return;
        }
        if (record.getOrderNo() != null && !record.getOrderNo().isBlank()) {
            return;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void manualDraw(Long drawId) {
        drawRevealService.revealManual(drawId);
    }

    @Override
    public void cancel(Long drawId) {
        if (drawId == null) {
            throw new OrderBusinessException("参数错误");
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            throw new OrderBusinessException("抽签活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (draw.getStatus() == null || draw.getStatus() != 0) {
            throw new OrderBusinessException("抽签活动已开始，无法取消");
        }
        if (draw.getStartTime() != null && !draw.getStartTime().isAfter(now)) {
            throw new OrderBusinessException("抽签活动已开始，无法取消");
        }
        int joined = drawRecordMapper.countByDrawId(drawId);
        if (joined > 0) {
            throw new OrderBusinessException("抽签活动已有参与记录，无法取消");
        }
        clearDrawCache(drawId);
        drawMapper.deleteById(drawId);
    }

    @Override
    public List<DrawWinnerVO> winners(Long drawId) {
        if (drawId == null) {
            return List.of();
        }
        return drawRecordMapper.listWinners(drawId);
    }

    @Override
    public List<DrawWinnerPublicVO> winnersPublic(Long drawId) {
        if (drawId == null) {
            return List.of();
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null || draw.getStatus() == null || draw.getStatus() != 2) {
            throw new OrderBusinessException("抽签活动未开奖");
        }
        return drawRecordMapper.listWinnersPublic(drawId);
    }

    private Draw getDrawByIdFromHotCache(Long drawId) {
        if (drawId == null) {
            return null;
        }
        DrawDetailVO detailVO = cacheClient.getLogicalExpireValue(RedisKeyConstant.drawDetailKey(drawId), DrawDetailVO.class);
        if (detailVO == null) {
            return null;
        }
        Draw draw = new Draw();
        draw.setId(detailVO.getId());
        draw.setTitle(detailVO.getTitle());
        draw.setTargetType(detailVO.getTargetType());
        draw.setSkuId(detailVO.getSkuId());
        draw.setBundleId(detailVO.getBundleId());
        draw.setPrice(detailVO.getPrice());
        draw.setTotalStock(detailVO.getTotalStock());
        draw.setMaxParticipants(detailVO.getMaxParticipants());
        draw.setWinnerCount(detailVO.getWinnerCount());
        draw.setStartTime(detailVO.getStartTime());
        draw.setEndTime(detailVO.getEndTime());
        draw.setDrawTime(detailVO.getDrawTime());
        draw.setStatus(detailVO.getStatus());
        draw.setDescription(detailVO.getDescription());
        return draw;
    }

    private boolean sendDrawJoinMessage(DrawJoinPersistMessage message) {
        if (message == null || message.getMsgId() == null || message.getMsgId().isBlank()) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < drawJoinMqSuspendUntil.get()) {
            return false;
        }
        try {
            CorrelationData correlationData = new CorrelationData(message.getMsgId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.DRAW_JOIN_EXCHANGE,
                    RabbitMqConstant.DRAW_JOIN_PERSIST_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
            boolean ack = confirm != null && confirm.isAck();
            if (ack) {
                drawJoinMqSuspendUntil.set(0L);
                return true;
            }
            drawJoinMqSuspendUntil.set(System.currentTimeMillis() + DRAW_JOIN_MQ_BACKOFF_MS);
            return false;
        } catch (Exception e) {
            drawJoinMqSuspendUntil.set(System.currentTimeMillis() + DRAW_JOIN_MQ_BACKOFF_MS);
            return false;
        }
    }

    private void enqueueDrawJoinRetry(DrawJoinPersistMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.drawJoinMqRetryKey(), json);
        } catch (Exception ignored) {
        }
    }

    /**
     * 预热活动缓存，在活动开始前调用
     * 使用逻辑过期机制，预先加载活动详情到Redis
     */
    @Override
    public void warmupDrawCache(Long drawId) {
        if (drawId == null) {
            return;
        }
        Draw draw = drawMapper.getById(drawId);
        if (draw == null) {
            return;
        }

        boolean activeWithRuntimeCache = draw.getStatus() != null
                && draw.getStatus() == 1
                && drawRedisService.hasRuntimeCache(drawId);
        if (!activeWithRuntimeCache) {
            drawRevealService.reconcileJoinRecords(drawId);
            List<DrawRecord> records = drawRecordMapper.listByDrawId(drawId);
            drawRedisService.rebuild(draw, records);
        }

        String detailKey = RedisKeyConstant.drawDetailKey(drawId);
        DrawDetailVO detailVO = buildDrawDetail(drawId);
        if (detailVO != null) {
            cacheClient.setLogicalExpireValue(
                    detailKey,
                    detailVO,
                    Duration.ofMinutes(10),
                    Duration.ofHours(2)
            );
            if (drawDetailLocalCache != null) {
                drawDetailLocalCache.put(drawId, detailVO);
            }
        }
    }

    /**
     * 清除抽签活动缓存
     */
    private void clearDrawCache(Long drawId) {
        if (drawId == null) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.drawDetailKey(drawId));
        if (drawDetailLocalCache != null) {
            drawDetailLocalCache.invalidate(drawId);
        }
        drawRedisService.deleteForDraw(drawId);
    }
}













