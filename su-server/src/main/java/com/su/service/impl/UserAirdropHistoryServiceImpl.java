package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.mapper.AirdropRecordMapper;
import com.su.result.PageResult;
import com.su.service.UserAirdropHistoryService;
import com.su.vo.AirdropRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User Airdrop History Service Implementation
 */
@Service
@Slf4j
public class UserAirdropHistoryServiceImpl implements UserAirdropHistoryService {

    @Autowired
    private AirdropRecordMapper airdropRecordMapper;

    @Override
    public PageResult listAirdropRecords(Long userId, Integer page, Integer size) {
        log.info("Listing airdrop records for user: {}, page: {}, size: {}", userId, page, size);

        // Use PageHelper for pagination
        PageHelper.startPage(page, size);

        // Query airdrop records with joined airdrop and coupon details
        // The mapper query already handles:
        // 1. JOIN with airdrop and coupon tables for complete information
        // 2. Sorting by create_time descending (most recent first)
        Page<AirdropRecordVO> pageResult = (Page<AirdropRecordVO>) airdropRecordMapper.listByUserIdWithDetails(userId);

        log.info("Found {} airdrop records for user: {}", pageResult.getTotal(), userId);

        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }
}
