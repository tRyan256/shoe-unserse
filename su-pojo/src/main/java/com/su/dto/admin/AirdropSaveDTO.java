package com.su.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空投活动保存DTO
 * 用于创建和更新空投活动
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirdropSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空投标题
     */
    @NotBlank(message = "空投标题不能为空")
    private String title;

    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

    /**
     * 总数量
     */
    @NotNull(message = "总数量不能为空")
    private Integer totalCount;

    /**
     * 剩余数量
     */
    private Integer remainCount;

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
     * 状态 0:未开始 1:进行中 2:已结束 3:已取消
     */
    private Integer status;
}
