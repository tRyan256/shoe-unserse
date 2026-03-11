package com.su.service;

import com.su.dto.ShoeSpuDTO;
import com.su.dto.ShoeSpuPageQueryDTO;
import com.su.enumeration.ShoeSortType;
import com.su.result.PageResult;
import com.su.vo.ShoeSpuDetailVO;
import com.su.vo.ShoeSpuVO;

import java.util.List;

public interface ShoeSpuService {

    void save(ShoeSpuDTO spuDTO);

    void update(ShoeSpuDTO spuDTO);

    void delete(List<Long> ids);

    void startOrStop(Integer status, Long id);

    PageResult pageQuery(ShoeSpuPageQueryDTO queryDTO);

    ShoeSpuDetailVO getByIdWithDetails(Long id);

    List<ShoeSpuVO> listByCategoryIds(List<Long> categoryIds, ShoeSortType sortType);

    List<ShoeSpuVO> listAll(ShoeSortType sortType);

    List<ShoeSpuVO> searchByKeyword(String keyword, ShoeSortType sortType);

    void clearCache();

    /**
     * 清除指定SPU的相关缓存（详情 + 相关分类列表）
     * @param spuId SPU ID
     */
    void clearSpuRelatedCache(Long spuId);
}
