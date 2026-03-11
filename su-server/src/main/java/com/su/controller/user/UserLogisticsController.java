package com.su.controller.user;

import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import com.su.result.Result;
import com.su.service.LogisticsService;
import com.su.vo.LogisticsDetailVO;
import com.su.vo.UserLogisticsDetailVO;
import com.su.vo.UserLogisticsTraceVO;
import com.su.vo.UserLogisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/logistics")
public class UserLogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @GetMapping("/{orderNo}")
    public Result<UserLogisticsDetailVO> getByOrderNo(@PathVariable String orderNo) {
        LogisticsDetailVO detail = logisticsService.getDetailByOrderNo(orderNo);
        return Result.success(toUserDetail(detail));
    }

    private UserLogisticsDetailVO toUserDetail(LogisticsDetailVO detail) {
        if (detail == null) {
            return null;
        }
        UserLogisticsVO logistics = toUserLogistics(detail.getLogistics());
        List<UserLogisticsTraceVO> traces = detail.getTraces() == null ? List.of() :
                detail.getTraces().stream().map(this::toUserTrace).toList();
        return UserLogisticsDetailVO.builder()
                .logistics(logistics)
                .traces(traces)
                .build();
    }

    private UserLogisticsVO toUserLogistics(Logistics logistics) {
        if (logistics == null) {
            return null;
        }
        return UserLogisticsVO.builder()
                .orderNo(logistics.getOrderNo())
                .expressCompany(logistics.getExpressCompany())
                .expressNo(logistics.getExpressNo())
                .status(logistics.getStatus())
                .currentLocation(logistics.getCurrentLocation())
                .receiverName(logistics.getReceiverName())
                .receiverPhone(logistics.getReceiverPhone())
                .receiverAddress(logistics.getReceiverAddress())
                .senderName(logistics.getSenderName())
                .senderPhone(logistics.getSenderPhone())
                .senderAddress(logistics.getSenderAddress())
                .estimatedTime(logistics.getEstimatedTime())
                .actualTime(logistics.getActualTime())
                .createTime(logistics.getCreateTime())
                .updateTime(logistics.getUpdateTime())
                .build();
    }

    private UserLogisticsTraceVO toUserTrace(LogisticsTrace trace) {
        if (trace == null) {
            return null;
        }
        return UserLogisticsTraceVO.builder()
                .status(trace.getStatus())
                .location(trace.getLocation())
                .description(trace.getDescription())
                .operator(trace.getOperator())
                .operateTime(trace.getOperateTime())
                .createTime(trace.getCreateTime())
                .build();
    }
}
