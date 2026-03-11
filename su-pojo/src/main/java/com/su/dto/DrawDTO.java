package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DrawDTO implements Serializable {
    private Long id;
    private Long skuId;
    private Long bundleId;
    private String title;
    private Integer targetType;
    private Integer totalStock;
    private Integer maxParticipants;
    private Integer winnerCount;
    private BigDecimal price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime drawTime;
    private Integer status;
    private String description;
}

