package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.annotation.AutoFill;
import com.su.dto.BundlePageQueryDTO;
import com.su.entity.Bundle;
import com.su.enumeration.OperationType;
import com.su.vo.ShoeItemVO;
import com.su.vo.BundleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface BundleMapper {

    Page<BundleVO> pageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void insert(Bundle bundle);

    Integer getSellingCount(List<Long> ids);

    void delete(List<Long> ids);

    @Select("select * from bundle where id = #{id}")
    Bundle getInfoById(Long id);

    List<Bundle> getByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Bundle bundle);

    @Update("update bundle set status = #{status} where id=#{id}")
    void startOrStop(Integer status, Long id);

    List<Bundle> list(Bundle bundle);

    @Select("select bs.sku_id as id, bs.name, bs.copies, COALESCE(sk.image, '') as image, spu.description, " +
            "COALESCE(sk.price, bs.price) as price, spu.brand, sk.color_name as colorName " +
            "from bundle_shoe bs " +
            "left join shoe_sku sk on bs.sku_id = sk.id " +
            "left join shoe_spu spu on sk.spu_id = spu.id " +
            "where bs.bundle_id = #{bundleId}")
    List<ShoeItemVO> getShoeItemByBundleId(Long bundleId);

    @Select("select id from bundle")
    List<Long> getAllIds();

    Integer countByMap(Map map);
}
