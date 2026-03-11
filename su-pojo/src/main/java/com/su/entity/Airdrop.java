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
public class Airdrop implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private Long couponId;

    private Integer totalCount;

    private Integer remainCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

