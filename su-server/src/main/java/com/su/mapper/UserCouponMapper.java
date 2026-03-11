package com.su.mapper;

import com.su.entity.Coupon;
import com.su.entity.UserCoupon;
import com.su.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserCouponMapper {
    void insert(UserCoupon userCoupon);

    List<UserCoupon> listByUserId(Long userId);

    UserCoupon findAvailable(Long userId, Long couponId);

    int markUsed(UserCoupon userCoupon);

    List<Coupon> listAvailableCoupons(Long userId, BigDecimal amount);

    UserCoupon getUnusedByUserIdAndCouponId(Long userId, Long couponId);

    /**
     * List user coupons with joined coupon details for user profile display
     * @param userId User ID
     * @param status Coupon status filter (optional): 0=unused, 1=used, 2=expired
     * @return List of UserCouponVO with complete coupon information
     */
    List<UserCouponVO> listByUserIdWithDetails(@Param("userId") Long userId, @Param("status") Integer status);
}
