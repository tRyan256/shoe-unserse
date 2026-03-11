package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 璁㈠崟姒傝鏁版嵁
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO implements Serializable {
    //寰呮帴鍗曟暟閲?
    private Integer waitingOrders;

    //寰呮淳閫佹暟閲?
    private Integer deliveredOrders;

    //宸插畬鎴愭暟閲?
    private Integer completedOrders;

    //宸插彇娑堟暟閲?
    private Integer cancelledOrders;

    //鍏ㄩ儴璁㈠崟
    private Integer allOrders;
}
