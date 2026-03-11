package com.su.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MyCommentVO implements Serializable {
    private Long id;
    private Long spuId;
    private String spuName;
    private String spuImage;
    private String content;
    private String images;
    private Integer rating;
    private LocalDateTime createTime;
}
