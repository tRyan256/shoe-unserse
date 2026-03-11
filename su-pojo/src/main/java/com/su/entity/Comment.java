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
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spuId;

    private Long userId;

    private String content;

    private String images;

    private Integer rating;

    private Integer status;

    private LocalDateTime createTime;
}
