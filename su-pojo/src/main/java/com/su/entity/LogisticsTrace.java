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
public class LogisticsTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long logisticsId;

    private Integer status;

    private String location;

    private String description;

    private String operator;

    private LocalDateTime operateTime;

    private LocalDateTime createTime;
}

