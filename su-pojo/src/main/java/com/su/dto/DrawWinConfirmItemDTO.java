package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DrawWinConfirmItemDTO implements Serializable {
    private Long skuId;
    private String shoeSize;
}

