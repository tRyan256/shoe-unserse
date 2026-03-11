package com.su.service;

import com.su.dto.OrdersCancelDTO;
import com.su.dto.OrdersConfirmDTO;
import com.su.dto.OrdersPageQueryDTO;
import com.su.dto.OrdersPaymentDTO;
import com.su.dto.OrdersRejectionDTO;
import com.su.dto.OrdersSubmitDTO;
import com.su.result.PageResult;
import com.su.vo.OrderAsyncStatusVO;
import com.su.vo.OrderAsyncSubmitVO;
import com.su.vo.OrderPaymentVO;
import com.su.vo.OrderStatisticsVO;
import com.su.vo.OrderVO;
import com.su.vo.UserOrderDetailVO;
import com.su.vo.UserOrderStatisticsVO;

import java.util.List;

public interface OrderService {

    /**
     * 提交订单（异步）
     * @param ordersSubmitDTO
     * @return
     * @throws Exception
     */
    OrderAsyncSubmitVO submitAsync(OrdersSubmitDTO ordersSubmitDTO) throws Exception;

    /**
     * 获取异步订单状态
     * @param orderNumber
     * @return
     */
    OrderAsyncStatusVO getAsyncStatus(String orderNumber);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     * @throws Exception
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功回调
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 分页查询订单（用户端）
     * @param page
     * @param pageSize
     * @param status
     * @param statusList
     * @return
     */
    PageResult page(Integer page, Integer pageSize, Integer status, List<Integer> statusList);

    /**
     * 根据订单号查询订单详情（用户端）
     * @param orderNumber
     * @return
     */
    UserOrderDetailVO getOrderDetailByNumber(String orderNumber);

    /**
     * 用户取消订单
     * @param orderNumber
     */
    void cancelByNumber(String orderNumber);

    /**
     * 再来一单
     * @param orderNumber
     */
    void repetitionByNumber(String orderNumber);

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各状态订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 查询订单详情（管理端）
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单（管理端）
     * @param ordersCancelDTO
     * @throws Exception
     */
    void cancelOrders(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 派送中
     * @param id
     */
    void delivery(Long id);

    /**
     * 派送中
     * @param id
     */
    void outForDelivery(Long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 用户确认收货
     * @param orderNumber
     */
    void confirmReceiptByNumber(String orderNumber);

    /**
     * 催单
     * @param orderNumber
     */
    void reminderByNumber(String orderNumber);

    /**
     * 用户订单统计
     * @param userId
     * @return
     */
    UserOrderStatisticsVO userStatistics(Long userId);
}
