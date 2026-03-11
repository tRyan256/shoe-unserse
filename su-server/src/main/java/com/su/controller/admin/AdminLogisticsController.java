package com.su.controller.admin;

import com.su.result.Result;
import com.su.service.LogisticsService;
import com.su.vo.LogisticsDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/logistics")
@Slf4j
public class AdminLogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @GetMapping("/{orderNo}")
    public Result<LogisticsDetailVO> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(logisticsService.getDetailByOrderNo(orderNo));
    }
}

