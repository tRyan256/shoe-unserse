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
public class DrawWinOptionsVO implements Serializable {
    private Integer targetType;
    private Long skuId;
    private Long bundleId;
    private List<DrawShoeSizeOptionVO> shoeOptions;
    private List<DrawBundleShoeOptionsVO> bundleOptions;
}

