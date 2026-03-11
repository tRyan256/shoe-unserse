package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.DrawPageQueryDTO;
import com.su.entity.Draw;
import com.su.vo.DrawAdminPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DrawMapper {
    void insert(Draw draw);

    void update(Draw draw);

    void deleteById(Long id);

    @Select("select * from draw where id = #{id}")
    Draw getById(Long id);

    Page<DrawAdminPageVO> pageQuery(DrawPageQueryDTO dto);

    List<Draw> listActive();

    List<Draw> listWarmup(@Param("windowStart") LocalDateTime windowStart, @Param("windowEnd") LocalDateTime windowEnd);

    @Select("select count(1) from draw " +
            "where target_type = 1 and sku_id = #{skuId} " +
            "and status <> 3 " +
            "and (start_time is null or start_time <= date_add(now(), interval 2 day)) " +
            "and ((end_time is not null and end_time >= date_sub(now(), interval 2 day)) " +
            "or (end_time is null and status in (0, 1)))")
    int countWarmupBySkuId(@Param("skuId") Long skuId);

    @Select("select count(1) from draw " +
            "where target_type = 2 and bundle_id = #{bundleId} " +
            "and status <> 3 " +
            "and (start_time is null or start_time <= date_add(now(), interval 2 day)) " +
            "and ((end_time is not null and end_time >= date_sub(now(), interval 2 day)) " +
            "or (end_time is null and status in (0, 1)))")
    int countWarmupByBundleId(@Param("bundleId") Long bundleId);

    @Select("select count(1) from draw d " +
            "join shoe_sku s on d.sku_id = s.id " +
            "where d.target_type = 1 and s.spu_id = #{spuId} " +
            "and d.status <> 3 " +
            "and (d.start_time is null or d.start_time <= date_add(now(), interval 2 day)) " +
            "and ((d.end_time is not null and d.end_time >= date_sub(now(), interval 2 day)) " +
            "or (d.end_time is null and d.status in (0, 1)))")
    int countWarmupBySpuId(@Param("spuId") Long spuId);
}
