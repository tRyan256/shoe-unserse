package com.su.controller.admin;

import com.su.dto.CouponDTO;
import com.su.dto.CouponPageQueryDTO;
import com.su.entity.Coupon;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/coupon")
@Slf4j
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    public Result<String> save(@RequestBody CouponDTO dto) {
        couponService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(CouponPageQueryDTO dto) {
        return Result.success(couponService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<Coupon> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @PutMapping
    public Result<String> update(@RequestBody CouponDTO dto) {
        couponService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        couponService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        couponService.startOrStop(status, id);
        return Result.success();
    }
}

