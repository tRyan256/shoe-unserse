package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DrawWinConfirmDTO implements Serializable {
    private Long addressBookId;
    private String shoeSize;
    private List<DrawWinConfirmItemDTO> items;
}
