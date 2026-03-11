package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.utils.cache.CacheClient;
import com.su.constant.MessageConstant;
import com.su.constant.RedisKeyConstant;
import com.su.constant.StatusConstant;
import com.su.dto.BundleDTO;
import com.su.dto.BundlePageQueryDTO;
import com.su.entity.Bundle;
import com.su.entity.BundleShoe;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSkuSize;
import com.su.exception.DeletionNotAllowedException;
import com.su.exception.SetmealEnableFailedException;
import com.su.exception.OrderBusinessException;
import com.su.mapper.BundleMapper;
import com.su.mapper.BundleShoeMapper;
import com.su.mapper.DrawMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import com.su.result.PageResult;
import com.su.service.BundleService;
import com.su.vo.ShoeItemVO;
import com.su.vo.BundleVO;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BundleServiceImpl implements BundleService {
    @Autowired
    private BundleMapper bundleMapper;
    @Autowired
    private BundleShoeMapper bundleShoeMapper;
    @Autowired
    private DrawMapper drawMapper;
    @Autowired
    private ShoeSkuMapper shoeSkuMapper;
    @Autowired
    private ShoeSkuSizeMapper shoeSkuSizeMapper;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 系统启动时初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        try {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_BUNDLE_ID);
            
            // 如果布隆过滤器不存在，则初始化
            if (!bloom.isExists()) {
                // 预期元素数量：1000，误判率：0.01
                bloom.tryInit(1000, 0.01);
                
                // 加载所有组合包ID到布隆过滤器
                List<Long> bundleIds = bundleMapper.getAllIds();
                for (Long bundleId : bundleIds) {
                    bloom.add(bundleId);
                }
                
                log.info("组合包布隆过滤器初始化完成，加载了 {} 个组合包ID", bundleIds.size());
            } else {
                log.info("组合包布隆过滤器已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.error("组合包布隆过滤器初始化失败", e);
        }
    }

    @Override
    public PageResult pageQuery(BundlePageQueryDTO bundlePageQueryDTO) {
        PageHelper.startPage(bundlePageQueryDTO.getPage(), bundlePageQueryDTO.getPageSize());
        Page<BundleVO> page = bundleMapper.pageQuery(bundlePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(BundleDTO bundleDTO) {
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(bundleDTO, bundle);
        bundle.setStatus(StatusConstant.DISABLE);
        bundleMapper.insert(bundle);
        List<BundleShoe> bundleShoes = bundleDTO.getBundleShoes();

        if (bundleShoes != null && !bundleShoes.isEmpty()) {
            bundleShoes.forEach(bundleShoe -> bundleShoe.setBundleId(bundle.getId()));
            bundleShoeMapper.insertBatch(bundleShoes);
        }
        
        // 添加到布隆过滤器
        Long bundleId = bundle.getId();
        if (bundleId != null) {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_BUNDLE_ID);
            bloom.add(bundleId);
        }
        
        clearBundleListCache();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<Long> ids) {
        for (Long id : ids) {
            ensureEditable(id);
        }
        Integer count = bundleMapper.getSellingCount(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        bundleMapper.delete(ids);
        bundleShoeMapper.deleteByBundleId(ids);
        
        // 清除每个组合包的相关缓存
        for (Long id : ids) {
            clearBundleRelatedCache(id);
        }
    }

    @Override
    public BundleVO getById(Long id) {
        if (id == null) {
            return null;
        }
        
        String key = RedisKeyConstant.bundleDetailKey(id);
        String lockKey = RedisKeyConstant.lockKey(key);
        RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_BUNDLE_ID);
        
        // 使用 CacheClient 查询组合包详情缓存，TTL 1小时
        return cacheClient.queryWithSimpleTTL(
                key,
                lockKey,
                BundleVO.class,
                () -> getBundleDetailFromDb(id),
                Duration.ofHours(1),
                Duration.ofMinutes(5),
                bloom,
                id
        );
    }

    private BundleVO getBundleDetailFromDb(Long id) {
        Bundle bundle = bundleMapper.getInfoById(id);
        if (bundle == null) {
            return null;
        }
        
        BundleVO bundleVO = new BundleVO();
        BeanUtils.copyProperties(bundle, bundleVO);
        
        // 查询组合包商品关系
        List<BundleShoe> bundleShoes = bundleShoeMapper.getByBundleIds(Collections.singletonList(id));
        bundleVO.setBundleShoes(bundleShoes);
        
        // 查询商品详细信息
        List<ShoeItemVO> shoeItems = bundleMapper.getShoeItemByBundleId(id);
        
        // 为每个商品填充尺码信息
        if (shoeItems != null && !shoeItems.isEmpty()) {
            for (ShoeItemVO item : shoeItems) {
                if (item.getId() != null) {
                    List<ShoeSkuSize> skuSizes = shoeSkuSizeMapper.listBySkuId(item.getId());
                    if (skuSizes != null && !skuSizes.isEmpty()) {
                        List<ShoeItemVO.ShoeSizeItem> sizes = skuSizes.stream()
                                .map(s -> ShoeItemVO.ShoeSizeItem.builder()
                                        .id(s.getId())
                                        .size(s.getSize())
                                        .stock(s.getStock())
                                        .build())
                                .toList();
                        item.setSizes(sizes);
                    }
                }
            }
        }
        
        bundleVO.setShoeItems(shoeItems);
        bundleVO.setShoeCount(shoeItems != null ? shoeItems.size() : 0);
        
        return bundleVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(BundleDTO bundleDTO) {
        if (bundleDTO == null || bundleDTO.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(bundleDTO.getId());
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(bundleDTO, bundle);
        bundleMapper.update(bundle);

        bundleShoeMapper.deleteByBundleId(Collections.singletonList(bundleDTO.getId()));
        List<BundleShoe> bundleShoes = bundleDTO.getBundleShoes();
        if (bundleShoes != null && !bundleShoes.isEmpty()) {
            bundleShoes.forEach(bundleShoe -> bundleShoe.setBundleId(bundle.getId()));
            bundleShoeMapper.insertBatch(bundleShoes);
        }
        
        // 使用精确缓存清除
        clearBundleRelatedCache(bundleDTO.getId());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        ensureEditable(id);
        if (Objects.equals(status, StatusConstant.ENABLE)) {
            List<ShoeSku> skus = shoeSkuMapper.getByBundleId(id);
            if (skus != null && !skus.isEmpty()) {
                skus.forEach(sku -> {
                    if (Objects.equals(sku.getStatus(), StatusConstant.DISABLE)) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        bundleMapper.startOrStop(status, id);
        
        // 使用精确缓存清除
        clearBundleRelatedCache(id);
    }

    @Override
    public List<Bundle> list(Bundle bundle) {
        if (bundle != null
                && Objects.equals(bundle.getStatus(), StatusConstant.ENABLE)
                && (bundle.getName() == null || bundle.getName().isBlank())) {
            String key = RedisKeyConstant.bundleListKey();
            String lockKey = RedisKeyConstant.lockKey(key);
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Bundle.class);
            List<Bundle> list = cacheClient.queryWithSimpleTTL(
                    key,
                    lockKey,
                    listType,
                    () -> {
                        List<Bundle> fromDb = bundleMapper.list(bundle);
                        return fromDb == null ? List.of() : fromDb;
                    },
                    Duration.ofSeconds(60),
                    Duration.ofSeconds(60),
                    null,
                    null
            );
            return list == null ? List.of() : list;
        }
        List<Bundle> list = bundleMapper.list(bundle);
        return list == null ? List.of() : list;
    }

    @Override
    public List<ShoeItemVO> getShoeItemById(Long id) {
        return bundleMapper.getShoeItemByBundleId(id);
    }

    private void clearBundleListCache() {
        cacheClient.evict(RedisKeyConstant.bundleListKey());
    }

    /**
     * 清除单个组合包详情缓存
     */
    private void clearBundleDetailCache(Long bundleId) {
        if (bundleId == null) {
            return;
        }
        String key = RedisKeyConstant.bundleDetailKey(bundleId);
        cacheClient.evict(key);
        log.debug("清除组合包详情缓存: {}", key);
    }

    /**
     * 清除组合包相关的所有缓存（详情 + 列表）
     */
    private void clearBundleRelatedCache(Long bundleId) {
        clearBundleDetailCache(bundleId);
        clearBundleListCache();
    }

    private void ensureEditable(Long bundleId) {
        if (bundleId == null) {
            return;
        }
        int linked = drawMapper.countWarmupByBundleId(bundleId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }
}
