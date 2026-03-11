package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VO for draw record response with joined draw details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawRecordVO implements Serializable {
    private Long id;
    private Long drawId;
    private String drawTitle;
    private Integer targetType;
    private String shoeSize;
    private Integer status;
    private String orderNo;
    private LocalDateTime createTime;
}
