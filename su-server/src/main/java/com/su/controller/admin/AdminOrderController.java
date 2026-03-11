package com.su.controller.admin;


import com.su.dto.OrdersCancelDTO;
import com.su.dto.OrdersConfirmDTO;
import com.su.dto.OrdersConfirmBatchDTO;
import com.su.dto.OrdersPageQueryDTO;
import com.su.dto.OrdersRejectionDTO;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.OrderService;
import com.su.vo.OrderBatchConfirmVO;
import com.su.vo.OrderStatisticsVO;
import com.su.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
@Slf4j
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("条件搜索订单：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 发货
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("订单确认：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/confirmBatch")
    public Result<List<OrderBatchConfirmVO>> confirmBatch(@RequestBody OrdersConfirmBatchDTO dto) {
        List<Long> ids = dto == null ? null : dto.getIds();
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<OrderBatchConfirmVO> results = new ArrayList<>(ids.size());
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            try {
                OrdersConfirmDTO one = new OrdersConfirmDTO();
                one.setId(id);
                orderService.confirm(one);
                results.add(OrderBatchConfirmVO.builder().id(id).success(true).message(null).build());
            } catch (Exception e) {
                String msg = e.getMessage();
                results.add(OrderBatchConfirmVO.builder().id(id).success(false).message(msg).build());
            }
        }
        return Result.success(results);
    }

    @PutMapping("/rejection")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancelOrders(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 运输中
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result<String> delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/outForDelivery/{id}")
    public Result<String> outForDelivery(@PathVariable("id") Long id) {
        orderService.outForDelivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result<String> complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
