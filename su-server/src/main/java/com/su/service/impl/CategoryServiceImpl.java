package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.su.constant.MessageConstant;
import com.su.constant.RedisKeyConstant;
import com.su.constant.StatusConstant;
import com.su.context.BaseContext;
import com.su.dto.CategoryDTO;
import com.su.dto.CategoryPageQueryDTO;
import com.su.entity.Category;
import com.su.exception.DeletionNotAllowedException;
import com.su.mapper.CategoryMapper;
import com.su.mapper.ShoeSpuCategoryMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.BundleShoeMapper;
import com.su.result.PageResult;
import com.su.service.CategoryService;
import com.su.utils.cache.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ShoeSpuCategoryMapper shoeSpuCategoryMapper;
    @Autowired
    private ShoeSpuMapper shoeSpuMapper;
    @Autowired
    private BundleShoeMapper bundleShoeMapper;
    @Autowired
    private Cache<Integer, List<Category>> categoryLocalCache;
    @Autowired
    private CacheClient cacheClient;

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.DISABLE);
        categoryMapper.insert(category);
        clearCategoryListCache();
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new DeletionNotAllowedException("分类id不能为空");
        }

        Category category = categoryMapper.getById(id);
        if (category == null) {
            throw new DeletionNotAllowedException("分类不存在");
        }

        if (StatusConstant.ENABLE.equals(category.getStatus())) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_ENABLE_DELETE_NOT_ALLOWED);
        }

        Integer count = shoeSpuCategoryMapper.countByCategoryId(id);
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SHOE);
        }

        count = bundleShoeMapper.countBundlesByCategoryId(id);
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_BUNDLE);
        }

        List<Long> spuIds = shoeSpuCategoryMapper.getSpuIdsByCategoryIds(Collections.singletonList(id));
        clearSpuDetailCache(spuIds);
        clearSpuListCache(id);

        categoryMapper.deleteById(id);
        clearCategoryListCache();
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category oldCategory = categoryMapper.getById(categoryDTO.getId());

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);

        if (oldCategory != null && !oldCategory.getName().equals(categoryDTO.getName())) {
            List<Long> spuIds = shoeSpuCategoryMapper.getSpuIdsByCategoryIds(
                Collections.singletonList(categoryDTO.getId())
            );
            clearSpuDetailCache(spuIds);
        }

        clearSpuListCache(categoryDTO.getId());
        clearCategoryListCache();
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
        clearSpuListCache(id);
        clearCategoryListCache();
    }

    @Override
    public List<Category> list(Integer type) {
        Integer cacheKey = type == null ? -1 : type;
        List<Category> cached = categoryLocalCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<Category> fromDb = categoryMapper.list(type);
        List<Category> result = fromDb == null ? List.of() : fromDb;
        categoryLocalCache.put(cacheKey, result);
        return result;
    }

    @Override
    public List<Category> listAll(Integer type) {
        return categoryMapper.listAll(type);
    }

    private void clearCategoryListCache() {
        categoryLocalCache.invalidateAll();
    }

    private void clearSpuDetailCache(List<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return;
        }
        List<String> keys = spuIds.stream()
                .map(RedisKeyConstant::spuDetailKey)
                .toList();
        cacheClient.evictBatch(keys);
    }

    private void clearSpuListCache(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.spuListByCategoryKey(categoryId));
        cacheClient.evict(RedisKeyConstant.spuListByCategoryKey(null));
    }
}
