package com.su.service;

import com.su.vo.OrderReportVO;
import com.su.vo.SalesTop10ReportVO;
import com.su.vo.TurnoverReportVO;
import com.su.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名Top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     * @param response
     * @throws IOException
     */
    void exportBusinessData(HttpServletResponse response) throws IOException;

    /**
     * 获取总营业额（所有已签收订单）
     */
    Double getTotalTurnover();

    /**
     * 获取总有效订单数（所有已签收订单）
     */
    Integer getTotalValidOrderCount();
}
