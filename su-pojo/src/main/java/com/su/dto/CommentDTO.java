package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentDTO implements Serializable {
    private Long spuId;
    private Long orderId;
    private String orderNumber;
    private String content;
    private String images;
    private Integer rating;
}
