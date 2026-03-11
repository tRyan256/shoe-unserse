package com.su.service;

import com.su.dto.BundleDTO;
import com.su.dto.BundlePageQueryDTO;
import com.su.entity.Bundle;
import com.su.result.PageResult;
import com.su.vo.ShoeItemVO;
import com.su.vo.BundleVO;

import java.util.List;

public interface BundleService {

    /**
     * 分页查询组合包
     * @param bundlePageQueryDTO
     * @return
     */
    PageResult pageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    /**
     * 新增组合包
     * @param bundleDTO
     */
    void save(BundleDTO bundleDTO);

    /**
     * 批量删除组合包
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据id查询组合包
     * @param id
     * @return
     */
    BundleVO getById(Long id);

    /**
     * 修改组合包
     * @param bundleDTO
     */
    void update(BundleDTO bundleDTO);

    /**
     * 启用或停售组合包
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询组合包列表
     * @param bundle
     * @return
     */
    List<Bundle> list(Bundle bundle);

    /**
     * 根据组合包id查询鞋款项列表
     * @param id
     * @return
     */
    List<ShoeItemVO> getShoeItemById(Long id);
}
