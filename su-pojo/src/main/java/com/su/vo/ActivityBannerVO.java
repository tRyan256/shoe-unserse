package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBannerVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private String title;
    private String description;
    private String image;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime drawTime;
    private String bannerStatus;
}
