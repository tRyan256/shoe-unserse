package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员端体验心得响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperiencePostAdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String contentSummary;

    private String content;

    private Integer productType;

    private String productTypeName;

    private Long productId;

    private String productName;

    private String productImage;

    private List<String> images;

    private Integer likeCount;

    private Integer commentCount;

    private Integer hidden;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
