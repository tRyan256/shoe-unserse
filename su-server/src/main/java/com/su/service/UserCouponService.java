package com.su.service;

import com.su.entity.Coupon;
import com.su.entity.UserCoupon;
import com.su.result.PageResult;
import com.su.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * User Coupon Service
 */
public interface UserCouponService {

    /**
     * List user coupons with pagination and optional status filtering
     * @param userId User ID
     * @param status Coupon status filter (optional): 0=unused, 1=used, 2=expired
     * @param page Page number
     * @param size Page size
     * @return Paginated list of user coupons with complete coupon information
     */
    PageResult listCoupons(Long userId, Integer status, Integer page, Integer size);

    /**
     * List all coupons for current user
     * @return List of user coupons
     */
    List<UserCoupon> listMyCoupons();

    /**
     * List available coupons for a given amount
     * @param amount Order amount
     * @return List of available coupons
     */
    List<Coupon> listAvailable(BigDecimal amount);

    /**
     * Claim a coupon
     * @param couponId Coupon ID
     */
    void claim(Long couponId);
}
