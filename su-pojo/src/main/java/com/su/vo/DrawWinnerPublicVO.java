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
public class DrawWinnerPublicVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String userAvatar;
    private String shoeSize;
    private LocalDateTime joinTime;
}

