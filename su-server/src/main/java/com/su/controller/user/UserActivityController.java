package com.su.controller.user;

import com.su.result.Result;
import com.su.service.ActivityBannerService;
import com.su.vo.ActivityBannerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/activity")
public class UserActivityController {

    @Autowired
    private ActivityBannerService activityBannerService;

    @GetMapping("/banner")
    public Result<List<ActivityBannerVO>> getBannerActivities() {
        return Result.success(activityBannerService.getBannerActivities());
    }
}
