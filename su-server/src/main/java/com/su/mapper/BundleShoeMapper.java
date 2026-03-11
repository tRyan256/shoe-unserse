package com.su.mapper;

import com.su.entity.BundleShoe;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BundleShoeMapper {

    void insertBatch(List<BundleShoe> bundleShoes);

    void deleteByBundleId(List<Long> bundleIds);

    List<BundleShoe> getByBundleIds(List<Long> bundleIds);

    List<Long> getBundleIdsBySkuIds(List<Long> skuIds);

    Integer countBundlesByCategoryId(Long categoryId);
}
