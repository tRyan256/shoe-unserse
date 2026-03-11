package com.su.service;

import com.su.result.PageResult;

/**
 * User Airdrop History Service
 */
public interface UserAirdropHistoryService {

    /**
     * List user airdrop records with pagination
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Paginated list of airdrop records with complete airdrop and coupon information, sorted by create_time desc
     */
    PageResult listAirdropRecords(Long userId, Integer page, Integer size);
}
