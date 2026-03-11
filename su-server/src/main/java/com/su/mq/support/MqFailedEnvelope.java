package com.su.mq.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqFailedEnvelope {
    private String type;
    private String exchange;
    private String routingKey;
    private String correlationId;
    private Integer replyCode;
    private String replyText;
    private String bodyBase64;
    private Integer attempts;
    private String lastError;
    private LocalDateTime createTime;
}

