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
public class DrawRecordItem implements Serializable {
    private Long id;
    private Long recordId;
    private Long skuId;
    private String shoeSize;
    private Integer copies;
    private LocalDateTime createTime;
}

