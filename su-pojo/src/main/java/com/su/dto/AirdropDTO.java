package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AirdropDTO implements Serializable {
    private Long id;
    private String title;
    private Long couponId;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}

