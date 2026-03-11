package com.su.service.impl.draw.support;

import com.su.entity.DrawRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawJoinCacheRecord {
    private Long recordId;
    private Long drawId;
    private Long userId;
    private Long skuId;
    private Long addressBookId;
    private String shoeSize;
    private Integer status;
    private String orderNo;
    private LocalDateTime joinTime;

    public static DrawJoinCacheRecord from(DrawRecord record) {
        if (record == null) {
            return null;
        }
        return DrawJoinCacheRecord.builder()
                .recordId(record.getId())
                .drawId(record.getDrawId())
                .userId(record.getUserId())
                .skuId(record.getSkuId())
                .addressBookId(record.getAddressBookId())
                .shoeSize(record.getShoeSize())
                .status(record.getStatus())
                .orderNo(record.getOrderNo())
                .joinTime(record.getCreateTime())
                .build();
    }

    public DrawRecord toDrawRecord() {
        return DrawRecord.builder()
                .id(recordId)
                .drawId(drawId)
                .userId(userId)
                .skuId(skuId)
                .addressBookId(addressBookId)
                .shoeSize(shoeSize)
                .status(status)
                .orderNo(orderNo)
                .createTime(joinTime)
                .build();
    }
}
