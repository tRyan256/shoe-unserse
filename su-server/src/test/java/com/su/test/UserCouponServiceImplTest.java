package com.su.test;

import com.su.context.BaseContext;
import com.su.entity.Coupon;
import com.su.exception.OrderBusinessException;
import com.su.mapper.CouponMapper;
import com.su.mapper.UserCouponMapper;
import com.su.service.impl.UserCouponServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserCouponServiceImplTest {
    @AfterEach
    void cleanup() {
        BaseContext.removeCurrentId();
    }

    @Test
    void claim_rejects_disabled_coupon() {
        BaseContext.setCurrentId(1L);
        UserCouponMapper userCouponMapper = mock(UserCouponMapper.class);
        CouponMapper couponMapper = mock(CouponMapper.class);
        UserCouponServiceImpl svc = new UserCouponServiceImpl();
        try {
            java.lang.reflect.Field f1 = UserCouponServiceImpl.class.getDeclaredField("userCouponMapper");
            f1.setAccessible(true);
            f1.set(svc, userCouponMapper);
            java.lang.reflect.Field f2 = UserCouponServiceImpl.class.getDeclaredField("couponMapper");
            f2.setAccessible(true);
            f2.set(svc, couponMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Coupon coupon = new Coupon();
        coupon.setId(10L);
        coupon.setStatus(0);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(1));
        when(couponMapper.getById(10L)).thenReturn(coupon);

        assertThrows(OrderBusinessException.class, () -> svc.claim(10L));
    }

    @Test
    void claim_accepts_enabled_coupon() {
        BaseContext.setCurrentId(1L);
        UserCouponMapper userCouponMapper = mock(UserCouponMapper.class);
        CouponMapper couponMapper = mock(CouponMapper.class);
        UserCouponServiceImpl svc = new UserCouponServiceImpl();
        try {
            java.lang.reflect.Field f1 = UserCouponServiceImpl.class.getDeclaredField("userCouponMapper");
            f1.setAccessible(true);
            f1.set(svc, userCouponMapper);
            java.lang.reflect.Field f2 = UserCouponServiceImpl.class.getDeclaredField("couponMapper");
            f2.setAccessible(true);
            f2.set(svc, couponMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Coupon coupon = new Coupon();
        coupon.setId(10L);
        coupon.setStatus(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(1));
        when(couponMapper.getById(10L)).thenReturn(coupon);
        when(userCouponMapper.getUnusedByUserIdAndCouponId(1L, 10L)).thenReturn(null);

        assertDoesNotThrow(() -> svc.claim(10L));
    }
}
