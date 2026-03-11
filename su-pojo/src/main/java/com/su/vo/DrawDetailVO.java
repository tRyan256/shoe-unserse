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
public class DrawDetailVO implements Serializable {
    private Long id;
    private String title;
    private Integer targetType;
    private Long skuId;
    private Long bundleId;
    private BigDecimal price;
    private Integer totalStock;
    private Integer maxParticipants;
    private Integer winnerCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime drawTime;
    private Integer status;
    private String description;
    private DrawShoePublicVO shoe;
    private DrawBundlePublicVO bundle;
}

