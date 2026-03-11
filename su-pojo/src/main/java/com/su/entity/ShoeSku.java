package com.su.entity;

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
public class ShoeSku implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spuId;

    private String colorName;

    private BigDecimal price;

    private String image;

    private Integer isDefault;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
