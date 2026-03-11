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
public class DrawWinnerVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long userId;
    private String userName;
    private String userPhone;
    private String shoeSize;
    private String orderNo;
    private LocalDateTime joinTime;
    private String prizeName;
    private LocalDateTime winTime;
    private Integer status;
}

