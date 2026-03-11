package com.su.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrdersPaymentDTO implements Serializable {
    //璁㈠崟鍙?
    private String orderNumber;

    //浠樻鏂瑰紡
    private Integer payMethod;

}
