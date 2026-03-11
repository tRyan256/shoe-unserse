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
public class DrawDetailAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long skuId;

    private Long spuId;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
