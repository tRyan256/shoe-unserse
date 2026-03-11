package com.su.service.impl;

import com.su.constant.StatusConstant;
import com.su.entity.Orders;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.BundleMapper;
import com.su.mapper.UserMapper;
import com.su.service.WorkspaceService;
import com.su.vo.BusinessDataVO;
import com.su.vo.OrderOverViewVO;
import com.su.vo.ShoeOverViewVO;
import com.su.vo.BundleOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoeSpuMapper shoeSpuMapper;
    @Autowired
    private BundleMapper bundleMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);

        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.SIGNED);
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null? 0.0 : turnover;

        Integer validOrderCount = orderMapper.countByMap(map);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        Integer newUsers = userMapper.countByMap(map);

        LocalDateTime yesterdayBegin = begin.minusDays(1);
        LocalDateTime yesterdayEnd = end.minusDays(1);
        Map yesterdayMap = new HashMap();
        yesterdayMap.put("begin", yesterdayBegin);
        yesterdayMap.put("end", yesterdayEnd);

        Integer yesterdayTotalOrderCount = orderMapper.countByMap(yesterdayMap);

        yesterdayMap.put("status", Orders.SIGNED);
        Double yesterdayTurnover = orderMapper.sumByMap(yesterdayMap);
        yesterdayTurnover = yesterdayTurnover == null ? 0.0 : yesterdayTurnover;

        Integer yesterdayValidOrderCount = orderMapper.countByMap(yesterdayMap);
        Integer yesterdayNewUsers = userMapper.countByMap(yesterdayMap);

        Double turnoverTrend = calculateTrend(turnover, yesterdayTurnover);
        Double validOrderCountTrend = calculateTrend(validOrderCount.doubleValue(), yesterdayValidOrderCount.doubleValue());
        Double newUsersTrend = calculateTrend(newUsers.doubleValue(), yesterdayNewUsers.doubleValue());

        map.put("status", Orders.TO_BE_SHIPPED);
        Integer todayWaitingOrders = orderMapper.countByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .turnoverTrend(turnoverTrend)
                .validOrderCountTrend(validOrderCountTrend)
                .newUsersTrend(newUsersTrend)
                .todayWaitingOrders(todayWaitingOrders)
                .build();
    }

    private Double calculateTrend(Double today, Double yesterday) {
        if (yesterday == null || yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        return ((today - yesterday) / yesterday) * 100;
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        Map map = new HashMap();
        map.put("status", Orders.TO_BE_SHIPPED);

        //待接单
        Integer waitingOrders = orderMapper.countByMap(map);

        //已发货
        map.put("status", Orders.SHIPPED);
        Integer deliveredOrders = orderMapper.countByMap(map);

        //已签收
        map.put("status", Orders.SIGNED);
        Integer completedOrders = orderMapper.countByMap(map);

        //已取消
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);

        //全部订单
        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    @Override
    public ShoeOverViewVO getShoeOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = shoeSpuMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = shoeSpuMapper.countByMap(map);

        return ShoeOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @Override
    public BundleOverViewVO getBundleOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = bundleMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = bundleMapper.countByMap(map);

        return BundleOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
