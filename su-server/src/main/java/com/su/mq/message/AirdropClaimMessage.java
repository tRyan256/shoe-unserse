package com.su.mq.message;

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
public class AirdropClaimMessage implements Serializable {
    private String msgId;
    private Long airdropId;
    private Long userId;
    private Long couponId;
    private LocalDateTime claimTime;
}

