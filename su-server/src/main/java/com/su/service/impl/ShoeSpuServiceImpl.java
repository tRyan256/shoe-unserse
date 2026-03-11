package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.constant.MessageConstant;
import com.su.constant.RedisKeyConstant;
import com.su.constant.StatusConstant;
import com.su.context.BaseContext;
import com.su.dto.ShoeSkuDTO;
import com.su.dto.ShoeSkuSizeDTO;
import com.su.dto.ShoeSpuDTO;
import com.su.dto.ShoeSpuPageQueryDTO;
import com.su.entity.Category;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSkuSize;
import com.su.entity.ShoeSpu;
import com.su.entity.ShoeSpuCategory;
import com.su.enumeration.ShoeSortType;
import com.su.exception.DeletionNotAllowedException;
import com.su.exception.OrderBusinessException;
import com.su.mapper.DrawMapper;
import com.su.mapper.CategoryMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import com.su.mapper.ShoeSpuCategoryMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.result.PageResult;
import com.su.service.ShoeSpuService;
import com.su.utils.cache.CacheClient;
import com.su.vo.ShoeSkuSizeVO;
import com.su.vo.ShoeSkuVO;
import com.su.vo.ShoeSpuDetailVO;
import com.su.vo.ShoeSpuVO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShoeSpuServiceImpl implements ShoeSpuService {

    private static final String NULL_CACHE_VALUE = "__NULL__";

    @Autowired
    private ShoeSpuMapper shoeSpuMapper;

    @Autowired
    private ShoeSkuMapper shoeSkuMapper;

    @Autowired
    private ShoeSkuSizeMapper shoeSkuSizeMapper;

    @Autowired
    private ShoeSpuCategoryMapper shoeSpuCategoryMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DrawMapper drawMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(ShoeSpuDTO spuDTO) {
        ShoeSpu spu = new ShoeSpu();
        BeanUtils.copyProperties(spuDTO, spu);
        spu.setStatus(StatusConstant.DISABLE);
        shoeSpuMapper.insert(spu);

        Long spuId = spu.getId();

        if (spuId != null) {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_SPU_ID);
            bloom.add(spuId);
        }

        if (spuDTO.getCategoryIds() != null && !spuDTO.getCategoryIds().isEmpty()) {
            List<ShoeSpuCategory> categories = spuDTO.getCategoryIds().stream()
                    .map(categoryId -> ShoeSpuCategory.builder()
                            .spuId(spuId)
                            .categoryId(categoryId)
                            .createTime(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            shoeSpuCategoryMapper.insertBatch(categories);
        }

        if (spuDTO.getSkus() != null && !spuDTO.getSkus().isEmpty()) {
            saveSkus(spuId, spuDTO.getSkus());
        }

        // 使用精确缓存清除
        clearSpuRelatedCache(spuId);
    }

    private void saveSkus(Long spuId, List<ShoeSkuDTO> skuDTOs) {
        boolean hasDefault = false;
        for (ShoeSkuDTO skuDTO : skuDTOs) {
            ShoeSku sku = new ShoeSku();
            BeanUtils.copyProperties(skuDTO, sku);
            sku.setSpuId(spuId);
            
            if (sku.getIsDefault() == null) {
                sku.setIsDefault(0);
            }
            if (sku.getIsDefault() == 1) {
                hasDefault = true;
            }
            sku.setStatus(StatusConstant.DISABLE);
            shoeSkuMapper.insert(sku);

            Long skuId = sku.getId();
            if (skuId != null) {
                RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_SKU_ID);
                bloom.add(skuId);
            }

            if (skuDTO.getSizes() != null && !skuDTO.getSizes().isEmpty()) {
                List<ShoeSkuSize> sizes = skuDTO.getSizes().stream()
                        .map(sizeDTO -> ShoeSkuSize.builder()
                                .skuId(skuId)
                                .size(sizeDTO.getSize())
                                .stock(sizeDTO.getStock() != null ? sizeDTO.getStock() : 0)
                                .build())
                        .collect(Collectors.toList());
                shoeSkuSizeMapper.insertBatch(sizes);
            }
        }

        if (!hasDefault && !skuDTOs.isEmpty()) {
            List<ShoeSku> skus = shoeSkuMapper.listBySpuId(spuId);
            if (!skus.isEmpty()) {
                ShoeSku firstSku = skus.get(0);
                firstSku.setIsDefault(1);
                shoeSkuMapper.update(firstSku);
            }
        }
    }

    @Override
    @Transactional
    public void update(ShoeSpuDTO spuDTO) {
        if (spuDTO == null || spuDTO.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(spuDTO.getId());
        ShoeSpu spu = new ShoeSpu();
        BeanUtils.copyProperties(spuDTO, spu);
        shoeSpuMapper.update(spu);

        Long spuId = spuDTO.getId();

        shoeSpuCategoryMapper.deleteBySpuId(spuId);
        if (spuDTO.getCategoryIds() != null && !spuDTO.getCategoryIds().isEmpty()) {
            List<ShoeSpuCategory> categories = spuDTO.getCategoryIds().stream()
                    .map(categoryId -> ShoeSpuCategory.builder()
                            .spuId(spuId)
                            .categoryId(categoryId)
                            .createTime(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            shoeSpuCategoryMapper.insertBatch(categories);
        }

        // 使用精确缓存清除
        clearSpuRelatedCache(spuId);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        for (Long id : ids) {
            ensureEditable(id);
            ShoeSpuVO spu = shoeSpuMapper.getVOById(id);
            if (spu != null && spu.getTotalStock() != null && spu.getTotalStock() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.SPU_HAS_STOCK);
            }
        }

        for (Long id : ids) {
            List<ShoeSku> skus = shoeSkuMapper.listBySpuId(id);
            for (ShoeSku sku : skus) {
                shoeSkuSizeMapper.deleteBySkuId(sku.getId());
            }
            shoeSkuMapper.deleteBySpuId(id);
            shoeSpuCategoryMapper.deleteBySpuId(id);
            
            // 清除每个SPU的相关缓存
            clearSpuRelatedCache(id);
        }

        shoeSpuMapper.delete(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        ensureEditable(id);
        ShoeSpu spu = ShoeSpu.builder()
                .id(id)
                .status(status)
                .build();
        shoeSpuMapper.update(spu);
        
        // 使用精确缓存清除
        clearSpuRelatedCache(id);
    }

    @Override
    public PageResult pageQuery(ShoeSpuPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<ShoeSpuVO> page = shoeSpuMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public ShoeSpuDetailVO getByIdWithDetails(Long id) {
        if (id == null) {
            return null;
        }
        
        String key = RedisKeyConstant.spuDetailKey(id);
        String lockKey = RedisKeyConstant.lockKey(key);
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_SPU_ID);
        
        // 使用 CacheClient 查询 SPU 详情缓存，TTL 1小时
        return cacheClient.queryWithSimpleTTL(
                key,
                lockKey,
                ShoeSpuDetailVO.class,
                () -> getSpuDetailFromDb(id),
                Duration.ofHours(1),
                Duration.ofMinutes(5),
                bloom,
                id
        );
    }

    private ShoeSpuDetailVO getSpuDetailFromDb(Long id) {
        ShoeSpuVO spuVO = shoeSpuMapper.getVOById(id);
        if (spuVO == null) {
            return null;
        }

        ShoeSpuDetailVO detailVO = new ShoeSpuDetailVO();
        BeanUtils.copyProperties(spuVO, detailVO);

        List<Long> categoryIds = shoeSpuCategoryMapper.getCategoryIdsBySpuId(id);
        detailVO.setCategoryIds(categoryIds != null ? categoryIds : new ArrayList<>());

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.getByIds(categoryIds);
            List<String> categoryNames = categories == null ? new ArrayList<>() : categories.stream()
                    .map(Category::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            detailVO.setCategoryNames(categoryNames);
        }

        List<ShoeSku> skus = shoeSkuMapper.listEnabledBySpuId(id);
        List<ShoeSkuVO> skuVOs = new ArrayList<>();
        for (ShoeSku sku : skus) {
            ShoeSkuVO skuVO = new ShoeSkuVO();
            BeanUtils.copyProperties(sku, skuVO);

            List<ShoeSkuSize> sizes = shoeSkuSizeMapper.listBySkuId(sku.getId());
            List<ShoeSkuSizeVO> sizeVOs = sizes.stream()
                    .map(size -> {
                        ShoeSkuSizeVO sizeVO = new ShoeSkuSizeVO();
                        BeanUtils.copyProperties(size, sizeVO);
                        return sizeVO;
                    })
                    .collect(Collectors.toList());
            skuVO.setSizes(sizeVOs);

            int totalStock = sizes.stream()
                    .map(ShoeSkuSize::getStock)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            skuVO.setStock(totalStock);

            skuVOs.add(skuVO);
        }
        detailVO.setSkus(skuVOs);

        if (detailVO.getDefaultImage() == null && !skuVOs.isEmpty()) {
            ShoeSkuVO defaultSku = skuVOs.stream()
                    .filter(s -> s.getIsDefault() != null && s.getIsDefault() == 1)
                    .findFirst()
                    .orElse(skuVOs.get(0));
            detailVO.setDefaultImage(defaultSku.getImage());
        }

        return detailVO;
    }

    @Override
    public List<ShoeSpuVO> listByCategoryIds(List<Long> categoryIds, ShoeSortType sortType) {
        // 情况1：无分类筛选，查询全部
        if (categoryIds == null || categoryIds.isEmpty()) {
            return listAll(sortType);
        }
        
        // 情况2：单个分类筛选
        if (categoryIds.size() == 1) {
            return listBySingleCategory(categoryIds.get(0), sortType);
        }
        
        // 情况3：多个分类筛选 - 分别处理每个分类的缓存
        return listByMultipleCategories(categoryIds, sortType);
    }

    /**
     * 单分类查询（使用缓存）
     */
    private List<ShoeSpuVO> listBySingleCategory(Long categoryId, ShoeSortType sortType) {
        String key = RedisKeyConstant.spuListByCategoryKey(categoryId);
        String lockKey = RedisKeyConstant.lockKey(key);
        
        JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, ShoeSpuVO.class);
        
        List<ShoeSpuVO> list = cacheClient.queryWithSimpleTTL(
            key,
            lockKey,
            listType,
            () -> shoeSpuMapper.listByCategoryIds(List.of(categoryId)),
            Duration.ofHours(1),
            Duration.ofMinutes(5),
            null,
            null
        );
        
        return sortSpuList(list, sortType);
    }

    /**
     * 多分类查询（分别处理缓存命中/未命中）
     * 优化策略：先检查每个分类的缓存，有缓存的直接使用，没有缓存的从数据库查询后回写
     */
    private List<ShoeSpuVO> listByMultipleCategories(List<Long> categoryIds, ShoeSortType sortType) {
        // 用于存储最终结果（使用 Map 按 SPU ID 去重，因为同一SPU可能属于多个分类）
        Map<Long, ShoeSpuVO> resultMap = new LinkedHashMap<>();
        
        // 分类缓存未命中的列表
        List<Long> missedCategoryIds = new ArrayList<>();
        
        JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, ShoeSpuVO.class);
        
        // 1. 检查每个分类的缓存
        for (Long categoryId : categoryIds) {
            String key = RedisKeyConstant.spuListByCategoryKey(categoryId);
            String cached = cacheClient.get(key);
            
            if (cached != null && !NULL_CACHE_VALUE.equals(cached)) {
                // 缓存命中
                try {
                    List<ShoeSpuVO> cachedList = objectMapper.readValue(cached, listType);
                    for (ShoeSpuVO spu : cachedList) {
                        resultMap.put(spu.getId(), spu);
                    }
                    log.debug("分类 {} 缓存命中，获取 {} 个SPU", categoryId, cachedList.size());
                } catch (Exception e) {
                    log.warn("解析分类 {} 缓存失败，将从数据库查询", categoryId, e);
                    missedCategoryIds.add(categoryId);
                }
            } else {
                // 缓存未命中
                missedCategoryIds.add(categoryId);
            }
        }
        
        log.info("多分类查询：共 {} 个分类，缓存命中 {} 个，未命中 {} 个", 
                 categoryIds.size(), categoryIds.size() - missedCategoryIds.size(), missedCategoryIds.size());
        
        // 2. 对于未命中的分类，从数据库查询并回写缓存
        if (!missedCategoryIds.isEmpty()) {
            // 为每个未命中的分类单独查询并回写缓存
            for (Long categoryId : missedCategoryIds) {
                List<ShoeSpuVO> categoryList = shoeSpuMapper.listByCategoryIds(List.of(categoryId));
                
                // 添加到结果Map（去重）
                for (ShoeSpuVO spu : categoryList) {
                    resultMap.put(spu.getId(), spu);
                }
                
                // 回写缓存
                String key = RedisKeyConstant.spuListByCategoryKey(categoryId);
                cacheClient.set(key, categoryList, Duration.ofHours(1));
                log.debug("分类 {} 缓存回写，共 {} 个SPU", categoryId, categoryList.size());
            }
        }
        
        // 3. 返回排序后的结果
        List<ShoeSpuVO> result = new ArrayList<>(resultMap.values());
        return sortSpuList(result, sortType);
    }

    @Override
    public List<ShoeSpuVO> listAll(ShoeSortType sortType) {
        String key = RedisKeyConstant.spuListByCategoryKey(null);
        String lockKey = RedisKeyConstant.lockKey(key);
        
        JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, ShoeSpuVO.class);
        
        List<ShoeSpuVO> list = cacheClient.queryWithSimpleTTL(
            key,
            lockKey,
            listType,
            () -> shoeSpuMapper.listAll(),
            Duration.ofHours(1),
            Duration.ofMinutes(5),
            null,
            null
        );
        
        return sortSpuList(list, sortType);
    }

    @Override
    public List<ShoeSpuVO> searchByKeyword(String keyword, ShoeSortType sortType) {
        List<ShoeSpuVO> list = shoeSpuMapper.searchByKeyword(keyword);
        return sortSpuList(list, sortType);
    }

    private List<ShoeSpuVO> sortSpuList(List<ShoeSpuVO> list, ShoeSortType sortType) {
        if (list == null || list.isEmpty()) {
            return list == null ? new ArrayList<>() : list;
        }

        if (sortType == null) {
            sortType = ShoeSortType.NEWEST;
        }

        switch (sortType) {
            case PRICE_ASC:
                list.sort(Comparator.comparing(ShoeSpuVO::getMinPrice, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case PRICE_DESC:
                list.sort(Comparator.comparing(ShoeSpuVO::getMinPrice, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case SALES:
                list.sort(Comparator.comparing(ShoeSpuVO::getSalesCount, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case NEWEST:
            default:
                list.sort(Comparator.comparing(ShoeSpuVO::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
        }
        return list;
    }

    @Override
    public void clearCache() {
        // 使用unlink异步删除，避免阻塞Redis
        // 注意：这是全量清除方法，仅在必要时使用
        // 推荐使用 clearSpuRelatedCache(spuId) 进行精确清除
        try (var cursor = stringRedisTemplate.scan(org.springframework.data.redis.core.ScanOptions.scanOptions().match("spu:*").count(1000).build())) {
            cursor.forEachRemaining(key -> {
                cacheClient.evict(key);
            });
        } catch (Exception e) {
            log.error("扫描SPU缓存失败", e);
        }
    }

    /**
     * 清除单个SPU详情缓存
     */
    private void clearSpuDetailCache(Long spuId) {
        if (spuId == null) {
            return;
        }
        String key = RedisKeyConstant.spuDetailKey(spuId);
        cacheClient.evict(key);
        log.debug("清除SPU详情缓存: {}", key);
    }

    /**
     * 清除指定分类的SPU列表缓存
     */
    private void clearSpuListCache(Long categoryId) {
        String key = RedisKeyConstant.spuListByCategoryKey(categoryId);
        cacheClient.evict(key);
        log.debug("清除SPU列表缓存: {}", key);
    }

    /**
     * 清除全部SPU列表缓存
     */
    private void clearAllSpuListCache() {
        String key = RedisKeyConstant.spuListByCategoryKey(null);
        cacheClient.evict(key);
        log.debug("清除全部SPU列表缓存: {}", key);
    }

    /**
     * 清除SPU相关的所有缓存（详情 + 相关分类列表）
     */
    @Override
    public void clearSpuRelatedCache(Long spuId) {
        // 清除详情缓存
        clearSpuDetailCache(spuId);
        
        // 清除全部列表缓存
        clearAllSpuListCache();
        
        // 清除相关分类的列表缓存
        List<Long> categoryIds = shoeSpuCategoryMapper.getCategoryIdsBySpuId(spuId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                clearSpuListCache(categoryId);
            }
        }
    }

    /**
     * 系统启动时初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        try {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_SPU_ID);
            
            // 如果布隆过滤器不存在，则初始化
            if (!bloom.isExists()) {
                // 预期元素数量：10000，误判率：0.01
                bloom.tryInit(10000, 0.01);
                
                // 加载所有SPU ID到布隆过滤器
                List<Long> spuIds = shoeSpuMapper.listAllIds();
                for (Long spuId : spuIds) {
                    bloom.add(spuId);
                }
                
                log.info("SPU布隆过滤器初始化完成，加载了 {} 个SPU ID", spuIds.size());
            } else {
                log.info("SPU布隆过滤器已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.error("SPU布隆过滤器初始化失败", e);
        }
    }

    private void ensureEditable(Long spuId) {
        if (spuId == null) {
            return;
        }
        int linked = drawMapper.countWarmupBySpuId(spuId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }
}
