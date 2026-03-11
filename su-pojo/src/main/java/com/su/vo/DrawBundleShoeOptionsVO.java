package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawBundleShoeOptionsVO implements Serializable {
    private Long skuId;
    private Integer copies;
    private List<DrawShoeSizeOptionVO> options;
}

