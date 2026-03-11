package com.su.service;

import com.su.vo.LogisticsDetailVO;

public interface LogisticsService {

    /**
     * 根据订单号查询物流详情
     * @param orderNo
     * @return
     */
    LogisticsDetailVO getDetailByOrderNo(String orderNo);
}
