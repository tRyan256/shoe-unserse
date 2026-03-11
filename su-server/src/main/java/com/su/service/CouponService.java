package com.su.service;

import com.su.dto.CouponDTO;
import com.su.dto.CouponPageQueryDTO;
import com.su.entity.Coupon;
import com.su.result.PageResult;

import java.util.List;

public interface CouponService {

    /**
     * 新增优惠券
     * @param dto
     */
    void save(CouponDTO dto);

    /**
     * 分页查询优惠券
     * @param dto
     * @return
     */
    PageResult page(CouponPageQueryDTO dto);

    /**
     * 根据id查询优惠券
     * @param id
     * @return
     */
    Coupon getById(Long id);

    /**
     * 修改优惠券
     * @param dto
     */
    void update(CouponDTO dto);

    /**
     * 根据id删除优惠券
     * @param id
     */
    void deleteById(Long id);

    /**
     * 启用或禁用优惠券
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询启用的优惠券列表
     * @return
     */
    List<Coupon> listEnabled();
}
