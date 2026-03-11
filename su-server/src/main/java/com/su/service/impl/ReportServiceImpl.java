package com.su.service.impl;

import com.su.dto.GoodsSalesDTO;
import com.su.mapper.ReportMapper;
import com.su.service.ReportService;
import com.su.service.WorkspaceService;
import com.su.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //根据开始和结束时间计算日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)){
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        //将日期列表转化成字符串
        String dateListStr = StringUtils.join(dateList, ",");

        //获取日期对应的营业额
        List<BigDecimal> turnoverList = reportMapper.getTurnoverByDates(dateList);

        //将营业额列表转化成字符串
        String turnoverListStr = turnoverList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return TurnoverReportVO.builder()
                .dateList(dateListStr)
                .turnoverList(turnoverListStr)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)){
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        //将日期列表转化成字符串
        String dateListStr = StringUtils.join(dateList, ",");

        List<Integer> newUserList = reportMapper.getNewUserByDates(dateList);
        String newUserListStr = newUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        List<Integer> totalUserList = reportMapper.getTotalUserByDates(dateList);
        String totalUserListStr = totalUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(newUserListStr)
                .totalUserList(totalUserListStr)
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)){
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        //将日期列表转化成字符串
        String dateListStr = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        //每日订单数
        List<Integer> orderCountList = reportMapper.getOrderCountByDates(dateList);
        String orderCountListStr = orderCountList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        //每日有效订单数
        List<Integer> validOrderCountList = reportMapper.getValidOrderCountByDates(dateList);
        String validOrderCountListStr = StringUtils.join(validOrderCountList, ",");

        //订单总数
        int totalOrderCount = orderCountList.stream()
                .mapToInt(Integer::intValue)
                .sum();

        //有效订单数
        Integer validOrderCount = validOrderCountList.stream()
                .mapToInt(Integer::intValue)
                .sum();

        //订单完成率
        double orderCompletionRate =0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(dateListStr)
                .orderCountList(orderCountListStr)
                .validOrderCountList(validOrderCountListStr)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {


        //获取销量top10商品名称列表和销量列表
        List<GoodsSalesDTO> top10ReportList = reportMapper.getSalesTop10(begin, end);

        //获取名称列表
        String nameListStr = top10ReportList.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));

        //获取销量列表
        String numberListStr = top10ReportList.stream()
                .map(GoodsSalesDTO::getNumber)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameListStr)
                .numberList(numberListStr)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) throws IOException {
        //获取营业数据
        LocalDate beginDate = LocalDate.now().plusDays(-30);
        LocalDate endDate = LocalDate.now().plusDays(-1);
        LocalDateTime begin = beginDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);
        Integer newUsers = businessData.getNewUsers();
        Double turnover = businessData.getTurnover();
        Integer validOrderCount = businessData.getValidOrderCount();
        Double orderCompletionRate = businessData.getOrderCompletionRate();
        Double unitPrice = businessData.getUnitPrice();

        //通过poi将数据输出到Excel文件中

        try (
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
                XSSFWorkbook excel = new XSSFWorkbook(Objects.requireNonNull(in));
                ServletOutputStream out = response.getOutputStream();
        ) {
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            sheet.getRow(3).getCell(6).setCellValue(newUsers);
            sheet.getRow(3).getCell(2).setCellValue(turnover);
            sheet.getRow(4).getCell(2).setCellValue(validOrderCount);
            sheet.getRow(3).getCell(4).setCellValue(orderCompletionRate);
            sheet.getRow(4).getCell(4).setCellValue(unitPrice);
            for(int i=0;i<30;i++){
                LocalDate date = beginDate.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(String.valueOf(date));
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());

            }
            //通过输出流将Excel文件下载到客户端浏览器
            excel.write(out);
        }


    }

    @Override
    public Double getTotalTurnover() {
        Double turnover = reportMapper.getTotalTurnover();
        return turnover == null ? 0.0 : turnover;
    }

    @Override
    public Integer getTotalValidOrderCount() {
        return reportMapper.getTotalValidOrderCount();
    }
}
