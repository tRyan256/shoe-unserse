package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.CouponPageQueryDTO;
import com.su.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CouponMapper {
    void insert(Coupon coupon);

    void update(Coupon coupon);

    void deleteById(Long id);

    @Select("select * from coupon where id = #{id}")
    Coupon getById(Long id);

    Page<Coupon> pageQuery(CouponPageQueryDTO dto);

    List<Coupon> listEnabled();

    @Select("select id from coupon")
    List<Long> listAllIds();
}

