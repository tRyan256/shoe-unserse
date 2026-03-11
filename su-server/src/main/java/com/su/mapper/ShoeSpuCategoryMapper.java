package com.su.mapper;

import com.su.entity.ShoeSpuCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoeSpuCategoryMapper {

    void insert(ShoeSpuCategory shoeSpuCategory);

    void insertBatch(List<ShoeSpuCategory> list);

    void deleteBySpuId(Long spuId);

    void deleteBySpuIds(List<Long> spuIds);

    @Select("select category_id from shoe_spu_category where spu_id = #{spuId}")
    List<Long> getCategoryIdsBySpuId(Long spuId);

    @Select("select spu_id from shoe_spu_category where category_id = #{categoryId}")
    List<Long> getSpuIdsByCategoryId(Long categoryId);

    List<Long> getSpuIdsByCategoryIds(List<Long> categoryIds);

    @Select("select count(*) from shoe_spu_category where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
}
