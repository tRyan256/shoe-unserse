package com.su.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentVO implements Serializable {
    private Long id;
    private Long spuId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String images;
    private Integer rating;
    private LocalDateTime createTime;
}
