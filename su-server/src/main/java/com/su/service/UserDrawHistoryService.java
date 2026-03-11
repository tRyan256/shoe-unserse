package com.su.service;

import com.su.result.PageResult;

/**
 * User Draw History Service
 */
public interface UserDrawHistoryService {

    /**
     * List user draw records with pagination
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return Paginated list of draw records with complete draw information, sorted by create_time desc
     */
    PageResult listDrawRecords(Long userId, Integer page, Integer size);
}
