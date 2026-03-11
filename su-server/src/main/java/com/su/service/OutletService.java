package com.su.service;

import com.su.dto.OutletDTO;
import com.su.dto.OutletPageQueryDTO;
import com.su.entity.Outlet;
import com.su.result.PageResult;
import com.su.vo.OutletNearbyVO;

import java.util.List;

public interface OutletService {

    /**
     * 新增门店
     * @param dto
     */
    void save(OutletDTO dto);

    /**
     * 分页查询门店
     * @param dto
     * @return
     */
    PageResult page(OutletPageQueryDTO dto);

    /**
     * 根据id查询门店
     * @param id
     * @return
     */
    Outlet getById(Long id);

    /**
     * 查询附近门店
     * @param longitude 经度
     * @param latitude 纬度
     * @param radiusMeters 搜索半径（米）
     * @param limit 返回数量限制
     * @return
     */
    List<OutletNearbyVO> nearby(Double longitude, Double latitude, Double radiusMeters, Integer limit);

    /**
     * 修改门店
     * @param dto
     */
    void update(OutletDTO dto);

    /**
     * 根据id删除门店
     * @param id
     */
    void deleteById(Long id);

    /**
     * 启用或禁用门店
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
