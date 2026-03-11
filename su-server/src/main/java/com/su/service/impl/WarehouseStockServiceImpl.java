package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.dto.WarehouseStockPageQueryDTO;
import com.su.mapper.ShoeSkuSizeMapper;
import com.su.result.PageResult;
import com.su.service.WarehouseStockService;
import com.su.vo.WarehouseStockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseStockServiceImpl implements WarehouseStockService {
    @Autowired
    private ShoeSkuSizeMapper shoeSkuSizeMapper;

    @Override
    public PageResult page(WarehouseStockPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<WarehouseStockVO> page = shoeSkuSizeMapper.pageStock(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }
}

