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
public class DrawJoinPersistMessage implements Serializable {
    private String msgId;
    private Long recordId;
    private Long drawId;
    private Long userId;
    private Long addressBookId;
    private String shoeSize;
    private Long skuId;
    private LocalDateTime createTime;
}
