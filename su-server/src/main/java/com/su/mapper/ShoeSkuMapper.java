package com.su.mapper;

import com.su.entity.ShoeSku;
import com.su.vo.ShoeSkuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoeSkuMapper {

    void insert(ShoeSku shoeSku);

    void insertBatch(List<ShoeSku> list);

    void update(ShoeSku shoeSku);

    void delete(Long id);

    void deleteBySpuId(Long spuId);

    void deleteBySpuIds(List<Long> spuIds);

    @Select("select * from shoe_sku where id = #{id}")
    ShoeSku getById(Long id);

    @Select("select * from shoe_sku where spu_id = #{spuId} order by is_default desc, id asc")
    List<ShoeSku> listBySpuId(Long spuId);

    @Select("select * from shoe_sku where spu_id = #{spuId} and status = 1 order by is_default desc, id asc")
    List<ShoeSku> listEnabledBySpuId(Long spuId);

    @Select("select * from shoe_sku where spu_id = #{spuId} and is_default = 1 limit 1")
    ShoeSku getDefaultBySpuId(Long spuId);

    @Update("update shoe_sku set is_default = #{isDefault} where id = #{id}")
    void updateDefault(@Param("id") Long id, @Param("isDefault") Integer isDefault);

    @Update("update shoe_sku set is_default = 0 where spu_id = #{spuId}")
    void clearDefaultBySpuId(@Param("spuId") Long spuId);

    @Update("update shoe_sku set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("select id from shoe_sku")
    List<Long> listAllIds();

    @Select("select * from shoe_sku order by spu_id, is_default desc, id asc")
    List<ShoeSku> listAll();

    List<ShoeSku> getByBundleId(Long bundleId);

    @Select("SELECT COALESCE(SUM(od.number), 0) FROM order_detail od " +
            "INNER JOIN orders o ON od.order_id = o.id " +
            "WHERE od.sku_id = #{skuId} AND o.status != 7")
    Integer getSalesCountBySkuId(Long skuId);

    List<ShoeSkuVO> listBySpuIdWithSpuStatus(Long spuId);
}
