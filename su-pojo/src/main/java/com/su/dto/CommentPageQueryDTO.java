package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Long spuId;
    private Long userId;
    private String spuName;
    private String userName;
    private Integer rating;
    private Integer status;
}
