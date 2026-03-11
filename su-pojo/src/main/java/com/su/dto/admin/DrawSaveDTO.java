package com.su.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抽签活动保存DTO
 * 用于创建和更新抽签活动
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 抽签标题
     */
    @NotBlank(message = "抽签标题不能为空")
    private String title;

    /**
     * SKU ID（当目标类型为鞋款时）
     */
    private Long skuId;

    /**
     * 组合包ID（当目标类型为组合包时）
     */
    private Long bundleId;

    /**
     * 目标类型 1:鞋款 2:组合包
     */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /**
     * 总库存
     */
    @NotNull(message = "总库存不能为空")
    private Integer totalStock;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 中奖人数
     */
    @NotNull(message = "中奖人数不能为空")
    private Integer winnerCount;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 开奖时间
     */
    private LocalDateTime drawTime;

    /**
     * 状态 0:未开始 1:进行中 2:已结束 3:已开奖
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
