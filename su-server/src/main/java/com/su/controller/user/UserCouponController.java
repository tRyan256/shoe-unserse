package com.su.controller.user;

import com.su.entity.Coupon;
import com.su.result.Result;
import com.su.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/user/coupon")
@Slf4j
public class UserCouponController {

    @Autowired
    private UserCouponService userCouponService;

    @GetMapping("/available")
    public Result<List<Coupon>> available(BigDecimal amount) {
        return Result.success(userCouponService.listAvailable(amount));
    }

    @PostMapping("/claim/{couponId}")
    public Result<String> claim(@PathVariable Long couponId) {
        userCouponService.claim(couponId);
        return Result.success();
    }
}
