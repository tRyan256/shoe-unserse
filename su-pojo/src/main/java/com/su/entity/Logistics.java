package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Logistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private String expressCompany;

    private String expressNo;

    private Integer status;

    private String currentLocation;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String senderName;

    private String senderPhone;

    private String senderAddress;

    private LocalDateTime estimatedTime;

    private LocalDateTime actualTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

