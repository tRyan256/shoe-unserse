package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawAdminPageVO implements Serializable {
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
    private String targetName;
    private String skuColorName;
    private Long participantCount;
}

