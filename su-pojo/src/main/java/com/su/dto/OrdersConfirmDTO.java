package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersConfirmDTO implements Serializable {

    private Long id;
    //璁㈠崟鐘舵€?1寰呬粯娆?2寰呮帴鍗?3 宸叉帴鍗?4 娲鹃€佷腑 5 宸插畬鎴?6 宸插彇娑?7 閫€娆?
    private Integer status;

}
