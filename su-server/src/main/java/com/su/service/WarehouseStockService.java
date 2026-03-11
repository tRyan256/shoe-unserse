package com.su.service;

import com.su.dto.WarehouseStockPageQueryDTO;
import com.su.result.PageResult;

public interface WarehouseStockService {

    /**
     * 分页查询仓库库存
     * @param dto
     * @return
     */
    PageResult page(WarehouseStockPageQueryDTO dto);
}
