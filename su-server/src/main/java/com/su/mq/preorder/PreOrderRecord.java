package com.su.mq.preorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreOrderRecord {
    private String orderNumber;
    private String status;
    private Long userId;
    private String error;
    private LocalDateTime createTime;
}
