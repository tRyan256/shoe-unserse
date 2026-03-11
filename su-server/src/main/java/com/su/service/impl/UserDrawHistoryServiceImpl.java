package com.su.service.impl;

import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.mapper.DrawMapper;
import com.su.result.PageResult;
import com.su.service.DrawService;
import com.su.service.UserDrawHistoryService;
import com.su.vo.DrawRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Draw History Service Implementation
 */
@Service
@Slf4j
public class UserDrawHistoryServiceImpl implements UserDrawHistoryService {
    @Autowired
    private DrawService drawService;
    @Autowired
    private DrawMapper drawMapper;

    @Override
    public PageResult listDrawRecords(Long userId, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : size;
        log.info("Listing draw records for user: {}, page: {}, size: {}", userId, safePage, safeSize);

        // 通过 drawService.myRecords() 合并 DB + Redis 的实时记录，避免“刚参与抽签看不到记录”
        List<DrawRecord> mergedRecords = drawService.myRecords();
        if (mergedRecords == null || mergedRecords.isEmpty()) {
            return new PageResult(0, List.of());
        }

        Map<Long, Draw> drawMap = new HashMap<>();
        for (DrawRecord record : mergedRecords) {
            if (record == null || record.getDrawId() == null || drawMap.containsKey(record.getDrawId())) {
                continue;
            }
            Draw draw = drawMapper.getById(record.getDrawId());
            if (draw != null) {
                drawMap.put(record.getDrawId(), draw);
            }
        }

        List<DrawRecordVO> allItems = new ArrayList<>(mergedRecords.size());
        for (DrawRecord record : mergedRecords) {
            if (record == null || record.getDrawId() == null) {
                continue;
            }
            Draw draw = drawMap.get(record.getDrawId());
            String drawTitle = draw != null ? draw.getTitle() : ("抽签活动 #" + record.getDrawId());
            Integer targetType = draw != null ? draw.getTargetType() : null;
            allItems.add(DrawRecordVO.builder()
                    .id(record.getId())
                    .drawId(record.getDrawId())
                    .drawTitle(drawTitle)
                    .targetType(targetType)
                    .shoeSize(record.getShoeSize())
                    .status(record.getStatus())
                    .orderNo(record.getOrderNo())
                    .createTime(record.getCreateTime())
                    .build());
        }

        int total = allItems.size();
        int fromIndex = (safePage - 1) * safeSize;
        if (fromIndex >= total) {
            return new PageResult(total, List.of());
        }
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<DrawRecordVO> pageItems = allItems.subList(fromIndex, toIndex);

        log.info("Found {} draw records for user: {}", total, userId);
        return new PageResult(total, pageItems);
    }
}
