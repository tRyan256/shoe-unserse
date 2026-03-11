package com.su.service;

import com.su.dto.AirdropDTO;
import com.su.dto.AirdropPageQueryDTO;
import com.su.entity.Airdrop;
import com.su.result.PageResult;

public interface AirdropService {

    /**
     * 新增空投活动
     * @param dto
     */
    void save(AirdropDTO dto);

    /**
     * 分页查询空投活动
     * @param dto
     * @return
     */
    PageResult page(AirdropPageQueryDTO dto);

    /**
     * 根据id查询空投活动
     * @param id
     * @return
     */
    Airdrop getById(Long id);

    /**
     * 修改空投活动
     * @param dto
     */
    void update(AirdropDTO dto);

    /**
     * 根据id删除空投活动
     * @param id
     */
    void deleteById(Long id);

    /**
     * 取消空投活动
     * @param airdropId
     */
    void cancel(Long airdropId);

    /**
     * 领取空投
     * @param airdropId
     */
    void receive(Long airdropId);

    void warmupAirdropCache(Long airdropId);
}
