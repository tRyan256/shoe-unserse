package com.su.service;

import com.su.vo.BusinessDataVO;
import com.su.vo.BundleOverViewVO;
import com.su.vo.OrderOverViewVO;
import com.su.vo.ShoeOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询鞋款总览
     * @return
     */
    ShoeOverViewVO getShoeOverView();

    /**
     * 查询组合包总览
     * @return
     */
    BundleOverViewVO getBundleOverView();
}
