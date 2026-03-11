package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.context.BaseContext;
import com.su.entity.Coupon;
import com.su.entity.UserCoupon;
import com.su.exception.OrderBusinessException;
import com.su.mapper.CouponMapper;
import com.su.mapper.UserCouponMapper;
import com.su.result.PageResult;
import com.su.service.UserCouponService;
import com.su.vo.UserCouponVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Coupon Service Implementation
 */
@Service
@Slf4j
public class UserCouponServiceImpl implements UserCouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public PageResult listCoupons(Long userId, Integer status, Integer page, Integer size) {
        log.info("Listing coupons for user: {}, status: {}, page: {}, size: {}", userId, status, page, size);

        // Use PageHelper for pagination
        PageHelper.startPage(page, size);

        // Query coupons with joined coupon details
        // The mapper query already handles:
        // 1. JOIN with coupon table for complete information
        // 2. Status filtering (if status is provided)
        // 3. Ordering: status ASC (unused=0, used=1, expired=2), then id DESC
        Page<UserCouponVO> pageResult = (Page<UserCouponVO>) userCouponMapper.listByUserIdWithDetails(userId, status);

        log.info("Found {} coupons for user: {}", pageResult.getTotal(), userId);

        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    public List<UserCoupon> listMyCoupons() {
        Long userId = BaseContext.getCurrentId();
        log.info("Listing all coupons for user: {}", userId);
        return userCouponMapper.listByUserId(userId);
    }

    @Override
    public List<Coupon> listAvailable(BigDecimal amount) {
        Long userId = BaseContext.getCurrentId();
        log.info("Listing available coupons for user: {}, amount: {}", userId, amount);
        return userCouponMapper.listAvailableCoupons(userId, amount);
    }

    @Override
    public void claim(Long couponId) {
        Long userId = BaseContext.getCurrentId();
        log.info("User {} claiming coupon: {}", userId, couponId);

        // Validate coupon exists and is enabled
        Coupon coupon = couponMapper.getById(couponId);
        if (coupon == null) {
            throw new OrderBusinessException("优惠券不存在");
        }

        if (coupon.getStatus() != 1) {
            throw new OrderBusinessException("优惠券未启用");
        }

        // Check if coupon is within validity period
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new OrderBusinessException("优惠券未到使用时间");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new OrderBusinessException("优惠券已过期");
        }

        // Check if user already has this coupon
        UserCoupon existingUserCoupon = userCouponMapper.getUnusedByUserIdAndCouponId(userId, couponId);
        if (existingUserCoupon != null) {
            throw new OrderBusinessException("您已领取过该优惠券");
        }

        // Create user coupon record
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // unused
        userCoupon.setCreateTime(LocalDateTime.now());

        userCouponMapper.insert(userCoupon);

        log.info("User {} successfully claimed coupon: {}", userId, couponId);
    }
}
