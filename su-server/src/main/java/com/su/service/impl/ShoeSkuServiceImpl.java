package com.su.service.impl;

import com.su.constant.RedisKeyConstant;
import com.su.constant.StatusConstant;
import com.su.dto.ShoeSkuDTO;
import com.su.dto.ShoeSkuSizeDTO;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSkuSize;
import com.su.exception.OrderBusinessException;
import com.su.mapper.DrawMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import com.su.service.ShoeSkuService;
import com.su.service.ShoeSpuService;
import com.su.utils.cache.CacheClient;
import com.su.vo.ShoeSkuSizeVO;
import com.su.vo.ShoeSkuVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShoeSkuServiceImpl implements ShoeSkuService {

    @Autowired
    private ShoeSkuMapper shoeSkuMapper;

    @Autowired
    private ShoeSkuSizeMapper shoeSkuSizeMapper;

    @Autowired
    private DrawMapper drawMapper;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ShoeSpuService shoeSpuService;

    @Override
    @Transactional
    public void save(ShoeSkuDTO skuDTO) {
        ShoeSku sku = new ShoeSku();
        BeanUtils.copyProperties(skuDTO, sku);
        if (sku.getStatus() == null) {
            sku.setStatus(StatusConstant.DISABLE);
        }
        if (sku.getIsDefault() == null) {
            sku.setIsDefault(0);
        }
        shoeSkuMapper.insert(sku);

        Long skuId = sku.getId();
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
        
        // 添加到布隆过滤器
        if (skuId != null) {
            RBloomFilter<Long> bloom = redissonClient.getBloomFilter(RedisKeyConstant.BLOOM_SKU_ID);
            bloom.add(skuId);
        }
    }

    @Override
    @Transactional
    public void update(ShoeSkuDTO skuDTO) {
        if (skuDTO == null || skuDTO.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(skuDTO.getId());
        ShoeSku sku = new ShoeSku();
        BeanUtils.copyProperties(skuDTO, sku);
        shoeSkuMapper.update(sku);

        if (skuDTO.getSizes() != null) {
            shoeSkuSizeMapper.deleteBySkuId(skuDTO.getId());
            
            List<ShoeSkuSize> sizes = skuDTO.getSizes().stream()
                    .map(sizeDTO -> ShoeSkuSize.builder()
                            .skuId(skuDTO.getId())
                            .size(sizeDTO.getSize())
                            .stock(sizeDTO.getStock() != null ? sizeDTO.getStock() : 0)
                            .build())
                    .collect(Collectors.toList());
            if (!sizes.isEmpty()) {
                shoeSkuSizeMapper.insertBatch(sizes);
            }
        }
        
        // 同时清除 SPU 相关缓存（详情 + 列表）
        if (skuDTO.getSpuId() != null) {
            shoeSpuService.clearSpuRelatedCache(skuDTO.getSpuId());
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new OrderBusinessException("参数错误");
        }
        ensureEditable(id);
        // 先获取SKU信息，以便清除SPU缓存
        ShoeSku sku = shoeSkuMapper.getById(id);
        Long spuId = sku != null ? sku.getSpuId() : null;
        
        shoeSkuSizeMapper.deleteBySkuId(id);
        shoeSkuMapper.delete(id);
        
        // 清除SPU相关缓存
        if (spuId != null) {
            shoeSpuService.clearSpuRelatedCache(spuId);
        }
    }

    @Override
    public ShoeSkuVO getByIdWithSizes(Long id) {
        if (id == null) {
            return null;
        }
        
        // 管理员端直接查询数据库，不使用缓存
        ShoeSku sku = shoeSkuMapper.getById(id);
        if (sku == null) {
            return null;
        }

        ShoeSkuVO skuVO = new ShoeSkuVO();
        BeanUtils.copyProperties(sku, skuVO);

        List<ShoeSkuSize> sizes = shoeSkuSizeMapper.listBySkuId(id);
        List<ShoeSkuSizeVO> sizeVOs = sizes.stream()
                .map(size -> {
                    ShoeSkuSizeVO sizeVO = new ShoeSkuSizeVO();
                    BeanUtils.copyProperties(size, sizeVO);
                    return sizeVO;
                })
                .collect(Collectors.toList());
        skuVO.setSizes(sizeVOs);

        Integer totalStock = shoeSkuSizeMapper.getTotalStockBySkuId(id);
        skuVO.setStock(totalStock);

        return skuVO;
    }

    @Override
    @Transactional
    public void updateStock(Long skuId, String size, Integer quantity) {
        Integer currentStock = shoeSkuSizeMapper.getStock(skuId, size);
        if (currentStock == null) {
            throw new OrderBusinessException("SKU尺码不存在");
        }
        if (currentStock + quantity < 0) {
            throw new OrderBusinessException("库存不足");
        }
        shoeSkuSizeMapper.updateStock(skuId, size, quantity);
    }

    @Override
    public List<ShoeSkuVO> listBySpuId(Long spuId) {
        List<ShoeSkuVO> skuVOs = shoeSkuMapper.listBySpuIdWithSpuStatus(spuId);
        return skuVOs.stream()
                .map(skuVO -> {
                    Integer totalStock = shoeSkuSizeMapper.getTotalStockBySkuId(skuVO.getId());
                    skuVO.setStock(totalStock);
                    Integer salesCount = shoeSkuMapper.getSalesCountBySkuId(skuVO.getId());
                    skuVO.setSalesCount(salesCount);
                    return skuVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ShoeSkuVO> listAll() {
        List<ShoeSku> skus = shoeSkuMapper.listAll();
        return skus.stream()
                .map(sku -> {
                    ShoeSkuVO skuVO = new ShoeSkuVO();
                    BeanUtils.copyProperties(sku, skuVO);
                    Integer totalStock = shoeSkuSizeMapper.getTotalStockBySkuId(sku.getId());
                    skuVO.setStock(totalStock);
                    Integer salesCount = shoeSkuMapper.getSalesCountBySkuId(sku.getId());
                    skuVO.setSalesCount(salesCount);
                    return skuVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ensureEditable(id);
        shoeSkuMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void setDefaultSku(Long spuId, Long skuId) {
        ensureSpuEditable(spuId);
        shoeSkuMapper.clearDefaultBySpuId(spuId);
        shoeSkuMapper.updateDefault(skuId, 1);
        
        // 清除SPU相关缓存（默认图片变化会影响列表）
        shoeSpuService.clearSpuRelatedCache(spuId);
    }

    private void ensureEditable(Long skuId) {
        if (skuId == null) {
            return;
        }
        int linked = drawMapper.countWarmupBySkuId(skuId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }

    private void ensureSpuEditable(Long spuId) {
        if (spuId == null) {
            return;
        }
        int linked = drawMapper.countWarmupBySpuId(spuId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }
}
