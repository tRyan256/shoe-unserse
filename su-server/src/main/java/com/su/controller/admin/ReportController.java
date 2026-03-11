package com.su.controller.admin;


import com.su.result.Result;
import com.su.service.ReportService;
import com.su.vo.OrderReportVO;
import com.su.vo.SalesTop10ReportVO;
import com.su.vo.TurnoverReportVO;
import com.su.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RequestMapping("/admin/report")
@RestController
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额统计：{}到{}", begin, end);
        return Result.success(reportService.turnoverStatistics(begin, end));

    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户统计：{}到{}", begin, end);
        return Result.success(reportService.userStatistics(begin, end));
    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计：{}到{}", begin, end);
        return Result.success(reportService.ordersStatistics(begin, end));
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("查询top10商品：{}到{}", begin, end);
        return Result.success(reportService.top10(begin, end));
    }

    @GetMapping("/export")
    public void exportBusinessData(HttpServletResponse response) throws IOException {
        log.info("导出数据");
        reportService.exportBusinessData(response);
    }

    @GetMapping("/totalTurnover")
    public Result<Double> totalTurnover() {
        log.info("查询总营业额");
        return Result.success(reportService.getTotalTurnover());
    }

    @GetMapping("/totalValidOrderCount")
    public Result<Integer> totalValidOrderCount() {
        log.info("查询总有效订单数");
        return Result.success(reportService.getTotalValidOrderCount());
    }

}
