package com.su.mq.service;

import com.su.entity.DrawRecord;
import com.su.mapper.DrawRecordMapper;
import com.su.mq.message.DrawJoinPersistMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DrawJoinPersistService {
    private static final int BATCH_SIZE = 200;

    private final DrawRecordMapper drawRecordMapper;

    public DrawJoinPersistService(DrawRecordMapper drawRecordMapper) {
        this.drawRecordMapper = drawRecordMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void persist(DrawJoinPersistMessage message) {
        persistBatch(message == null ? List.of() : List.of(message));
    }

    @Transactional(rollbackFor = Exception.class)
    public void persistBatch(List<DrawJoinPersistMessage> messages) {
        List<DrawRecord> records = toRecords(messages);
        if (records.isEmpty()) {
            return;
        }
        for (int i = 0; i < records.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, records.size());
            drawRecordMapper.insertBatchIgnore(records.subList(i, end));
        }
    }

    private List<DrawRecord> toRecords(List<DrawJoinPersistMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        Map<Long, DrawRecord> deduped = new LinkedHashMap<>();
        for (DrawJoinPersistMessage message : messages) {
            DrawRecord record = toRecord(message);
            if (record == null || record.getId() == null) {
                continue;
            }
            deduped.putIfAbsent(record.getId(), record);
        }
        return new ArrayList<>(deduped.values());
    }

    private DrawRecord toRecord(DrawJoinPersistMessage message) {
        if (message == null || message.getRecordId() == null || message.getDrawId() == null || message.getUserId() == null) {
            return null;
        }
        return DrawRecord.builder()
                .id(message.getRecordId())
                .drawId(message.getDrawId())
                .userId(message.getUserId())
                .addressBookId(message.getAddressBookId())
                .shoeSize(message.getShoeSize())
                .skuId(message.getSkuId())
                .status(0)
                .orderNo(null)
                .createTime(message.getCreateTime() == null ? LocalDateTime.now() : message.getCreateTime())
                .build();
    }
}
