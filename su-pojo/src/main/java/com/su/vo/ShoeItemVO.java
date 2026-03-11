package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeItemVO implements Serializable {

    private Long id;

    private String name;

    private Integer copies;

    private String image;

    private String description;

    private BigDecimal price;

    private String brand;

    private String colorName;

    private List<ShoeSizeItem> sizes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShoeSizeItem implements Serializable {
        private Long id;
        private String size;
        private Integer stock;
    }
}

