package com.su.service;

import com.su.dto.admin.AirdropSaveDTO;
import com.su.dto.admin.BundleSaveDTO;
import com.su.dto.admin.DrawSaveDTO;
import com.su.dto.admin.ShoeSaveDTO;
import com.su.entity.Airdrop;
import com.su.entity.Bundle;
import com.su.entity.Draw;
import com.su.vo.DrawWinnerVO;
import com.su.vo.ShoeSpuAdminVO;

import java.util.List;

/**
 * 管理员商品管理服务接口
 * 提供商品（鞋款、组合包、抽签、空投）的管理功能，确保缓存一致性
 */
public interface AdminProductService {

    /**
     * 保存鞋款（支持多分类）
     * 
     * @param dto 鞋款保存DTO
     */
    void saveShoeSpu(ShoeSaveDTO dto);

    /**
     * 更新鞋款（支持多分类）
     * 先删除旧的分类关联，再插入新的分类关联
     * 
     * @param id 鞋款ID
     * @param dto 鞋款保存DTO
     */
    void updateShoeSpu(Long id, ShoeSaveDTO dto);

    /**
     * 查询鞋款详情（包含所有分类）
     * 
     * @param id 鞋款ID
     * @return 鞋款详情VO
     */
    ShoeSpuAdminVO getShoeSpu(Long id);

    /**
     * 更新组合包
     * 更新组合包信息和包含的商品项，事务提交后更新缓存
     * 
     * @param id 组合包ID
     * @param dto 组合包保存DTO
     */
    void updateBundle(Long id, BundleSaveDTO dto);

    /**
     * 查询组合包列表
     * 
     * @return 组合包列表
     */
    List<Bundle> getBundleList();

    /**
     * 切换组合包启用/停用状态
     * 切换状态后更新缓存
     * 
     * @param id 组合包ID
     */
    void toggleBundleStatus(Long id);

    /**
     * 更新抽签活动
     * 更新抽签信息，事务提交后更新缓存
     * 
     * @param id 抽签活动ID
     * @param dto 抽签保存DTO
     */
    void updateDraw(Long id, DrawSaveDTO dto);

    /**
     * 查询抽签列表
     * 
     * @return 抽签活动列表
     */
    List<Draw> getDrawList();

    /**
     * 查询中奖名单
     * 
     * @param drawId 抽签活动ID
     * @return 中奖用户列表
     */
    List<DrawWinnerVO> getDrawWinners(Long drawId);

    /**
     * 更新空投活动
     * 更新空投信息和关联的优惠券信息，事务提交后更新缓存（包括嵌入的优惠券信息）
     * 
     * @param id 空投活动ID
     * @param dto 空投保存DTO
     */
    void updateAirdrop(Long id, AirdropSaveDTO dto);

    /**
     * 查询空投列表
     * 返回所有空投活动，包括嵌入的优惠券信息
     * 
     * @return 空投活动列表
     */
    List<Airdrop> getAirdropList();

    /**
     * 取消空投活动
     * 更新空投状态为已取消，并清理相关缓存
     * 
     * @param id 空投活动ID
     */
    void cancelAirdrop(Long id);
}
