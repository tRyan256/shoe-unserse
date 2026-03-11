package com.su.controller.user;


import com.su.dto.OrdersPaymentDTO;
import com.su.dto.OrdersSubmitDTO;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.OrderService;
import com.su.vo.OrderAsyncStatusVO;
import com.su.vo.OrderAsyncSubmitVO;
import com.su.vo.OrderPaymentVO;
import com.su.vo.UserOrderDetailVO;
import com.su.vo.UserOrderStatisticsVO;
import com.su.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderAsyncSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        log.info("用户下单：{}", ordersSubmitDTO);
        return Result.success(orderService.submitAsync(ordersSubmitDTO));
    }

    @GetMapping("/submit/status/{orderNumber}")
    public Result<OrderAsyncStatusVO> submitStatus(@PathVariable String orderNumber) {
        return Result.success(orderService.getAsyncStatus(orderNumber));
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 订单状态
     * @param statusList 订单状态列表（多状态查询）
     * @return 订单分页结果
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> page(Integer page, Integer pageSize, Integer status,
                                    @RequestParam(required = false) List<Integer> statusList){
        log.info("查询历史订单：status={}, statusList={}", status, statusList);
        PageResult pageResult = orderService.page(page, pageSize, status, statusList);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{orderNumber}")
    public Result<UserOrderDetailVO> getOrderDetailByNumber(@PathVariable String orderNumber){
        log.info("查询订单详情：{}", orderNumber);
        UserOrderDetailVO orderVO = orderService.getOrderDetailByNumber(orderNumber);
        return Result.success(orderVO);
    }


    @PutMapping("/cancel/{orderNumber}")
    public Result<String> cancel(@PathVariable String orderNumber){
        log.info("取消订单：{}", orderNumber);
        orderService.cancelByNumber(orderNumber);
        return Result.success();
    }

    @PutMapping("/confirm/{orderNumber}")
    public Result<String> confirm(@PathVariable String orderNumber) {
        log.info("确认收货：{}", orderNumber);
        orderService.confirmReceiptByNumber(orderNumber);
        return Result.success();
    }

    @PostMapping("/repetition/{orderNumber}")
    public Result<String> repetition(@PathVariable String orderNumber){
        log.info("再来一单：{}", orderNumber);
        orderService.repetitionByNumber(orderNumber);
        return Result.success();
    }

    @GetMapping("/reminder/{orderNumber}")
    public Result<String> reminder(@PathVariable String orderNumber){
        log.info("订单催单：{}", orderNumber);
        orderService.reminderByNumber(orderNumber);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<UserOrderStatisticsVO> statistics() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户订单统计：userId={}", userId);
        UserOrderStatisticsVO vo = orderService.userStatistics(userId);
        return Result.success(vo);
    }

}
