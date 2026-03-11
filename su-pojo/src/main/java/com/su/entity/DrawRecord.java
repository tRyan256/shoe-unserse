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
public class DrawRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long drawId;

    private Long userId;

    private Long addressBookId;

    private String shoeSize;

    private Long skuId;

    private Integer status;

    private String orderNo;

    private LocalDateTime createTime;
}
