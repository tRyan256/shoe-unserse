package com.su.service;

import com.su.dto.WarehouseDTO;
import com.su.dto.WarehousePageQueryDTO;
import com.su.entity.Warehouse;
import com.su.result.PageResult;

public interface WarehouseService {

    /**
     * 新增仓库
     * @param dto
     */
    void save(WarehouseDTO dto);

    /**
     * 分页查询仓库
     * @param dto
     * @return
     */
    PageResult page(WarehousePageQueryDTO dto);

    /**
     * 根据id查询仓库
     * @param id
     * @return
     */
    Warehouse getById(Long id);

    /**
     * 修改仓库
     * @param dto
     */
    void update(WarehouseDTO dto);

    /**
     * 根据id删除仓库
     * @param id
     */
    void deleteById(Long id);

    /**
     * 启用或禁用仓库
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
