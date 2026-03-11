package com.su.vo;

import com.su.entity.BundleShoe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleVO implements Serializable {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer status;

    private String description;

    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer shoeCount;

    @Builder.Default
    private List<BundleShoe> bundleShoes = new ArrayList<>();

    @Builder.Default
    private List<ShoeItemVO> shoeItems = new ArrayList<>();
}

