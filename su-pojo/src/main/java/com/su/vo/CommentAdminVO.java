package com.su.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentAdminVO implements Serializable {
    private Long id;
    private Long spuId;
    private String spuName;
    private String spuImage;
    private Long userId;
    private String userName;
    private String userPhone;
    private String userAvatar;
    private String content;
    private String images;
    private Integer rating;
    private Integer status;
    private LocalDateTime createTime;
}

