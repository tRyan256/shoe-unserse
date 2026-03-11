package com.su.controller.admin;

import com.su.properties.BaiduMapProperties;
import com.su.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminMapController")
@RequestMapping("/admin/map")
public class AdminMapController {
    private final BaiduMapProperties baiduMapProperties;

    public AdminMapController(BaiduMapProperties baiduMapProperties) {
        this.baiduMapProperties = baiduMapProperties;
    }

    @GetMapping("/ak")
    public Result<String> ak() {
        String ak = baiduMapProperties == null ? null : baiduMapProperties.getAk();
        if (ak == null || ak.isBlank()) {
            return Result.error("Missing Baidu Map AK");
        }
        return Result.success(ak);
    }
}

