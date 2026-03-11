package com.su.service.impl;

import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import com.su.mapper.LogisticsMapper;
import com.su.mapper.LogisticsTraceMapper;
import com.su.service.LogisticsService;
import com.su.vo.LogisticsDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private LogisticsTraceMapper logisticsTraceMapper;

    @Override
    public LogisticsDetailVO getDetailByOrderNo(String orderNo) {
        Logistics logistics = logisticsMapper.getByOrderNo(orderNo);
        List<LogisticsTrace> traces = Collections.emptyList();
        if (logistics != null && logistics.getId() != null) {
            traces = logisticsTraceMapper.listByLogisticsId(logistics.getId());
        }
        return LogisticsDetailVO.builder()
                .logistics(logistics)
                .traces(traces)
                .build();
    }
}

