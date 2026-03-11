package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.OrdersPageQueryDTO;
import com.su.entity.Orders;
import com.su.vo.OrderStatisticsVO;
import com.su.vo.UserOrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {


    void insert(Orders order);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    OrderStatisticsVO statistics();

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> processTimeoutOrder(Integer status, LocalDateTime orderTime);

    void updateList(List<Orders> ordersList2);

    @Update("update orders set status = #{toStatus}, cancel_reason = #{cancelReason}, cancel_time = #{cancelTime} where id = #{id} and status = #{fromStatus}")
    int updateStatusIfMatch(@Param("id") Long id,
                            @Param("fromStatus") Integer fromStatus,
                            @Param("toStatus") Integer toStatus,
                            @Param("cancelReason") String cancelReason,
                            @Param("cancelTime") LocalDateTime cancelTime);

    @Update("update orders set status = 6 where id = #{id} and status in (3,4,5)")
    int markSignedIfEligible(@Param("id") Long id);

    @Update("update orders set status = 8 where id = #{id} and user_id = #{userId} and status = 6")
    int markReviewedIfEligible(@Param("id") Long id, @Param("userId") Long userId);


    Integer countByMap(Map map);

    Double sumByMap(Map map);

    Integer countActiveOrderByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);

    Integer countActiveOrderByUserAndBundle(@Param("userId") Long userId, @Param("bundleId") Long bundleId);

    Integer countSignedOrderByUserAndSpu(@Param("userId") Long userId, @Param("spuId") Long spuId);

    UserOrderStatisticsVO countByUserId(@Param("userId") Long userId);
}
