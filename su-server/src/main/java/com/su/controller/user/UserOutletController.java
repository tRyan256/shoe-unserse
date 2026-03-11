package com.su.controller.user;

import com.su.result.Result;
import com.su.service.OutletService;
import com.su.vo.OutletNearbyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/outlet")
public class UserOutletController {
    @Autowired
    private OutletService outletService;

    @GetMapping("/nearby")
    public Result<List<OutletNearbyVO>> nearby(Double longitude, Double latitude, Double radiusMeters, Integer limit) {
        return Result.success(outletService.nearby(longitude, latitude, radiusMeters, limit));
    }
}

