package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/profile")
@Slf4j
public class UserProfileCouponController {

    @Autowired
    private UserCouponService userCouponService;

    /**
     * Get user coupons with pagination and optional status filtering
     * For user profile management - displays coupons grouped by status
     * Endpoint: GET /user/profile/coupons
     *
     * @param status Coupon status filter (optional): 0=unused, 1=used, 2=expired
     * @param page Page number (default: 1)
     * @param size Page size (default: 20)
     * @return Paginated list of user coupons with complete coupon information
     */
    @GetMapping("/coupons")
    public Result<PageResult> listCouponsForProfile(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting coupons for user profile: userId={}, status={}, page={}, size={}", 
                userId, status, page, size);

        PageResult pageResult = userCouponService.listCoupons(userId, status, page, size);
        return Result.success(pageResult);
    }
}
