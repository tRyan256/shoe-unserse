package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.WarehouseStockPageQueryDTO;
import com.su.entity.ShoeSkuSize;
import com.su.vo.WarehouseStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoeSkuSizeMapper {

    void insert(ShoeSkuSize shoeSkuSize);

    void insertBatch(List<ShoeSkuSize> list);

    void deleteBySkuId(Long skuId);

    void deleteBySkuIds(List<Long> skuIds);

    @Select("select * from shoe_sku_size where sku_id = #{skuId} order by size asc")
    List<ShoeSkuSize> listBySkuId(Long skuId);

    @Update("update shoe_sku_size set stock = stock + #{quantity} where sku_id = #{skuId} and size = #{size}")
    void updateStock(@Param("skuId") Long skuId, @Param("size") String size, @Param("quantity") Integer quantity);

    @Select("select stock from shoe_sku_size where sku_id = #{skuId} and size = #{size}")
    Integer getStock(@Param("skuId") Long skuId, @Param("size") String size);

    @Select("select COALESCE(sum(stock), 0) from shoe_sku_size where sku_id = #{skuId}")
    Integer getTotalStockBySkuId(Long skuId);

    int decrementStock(@Param("skuId") Long skuId, @Param("size") String size, @Param("quantity") Integer quantity);

    int incrementStock(@Param("skuId") Long skuId, @Param("size") String size, @Param("quantity") Integer quantity);

    @Select("select * from shoe_sku_size where sku_id = #{skuId} and size = #{size}")
    ShoeSkuSize getBySkuIdAndSize(@Param("skuId") Long skuId, @Param("size") String size);

    Page<WarehouseStockVO> pageStock(WarehouseStockPageQueryDTO dto);
}
