package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.dto.WarehouseDTO;
import com.su.dto.WarehousePageQueryDTO;
import com.su.entity.Warehouse;
import com.su.mapper.WarehouseMapper;
import com.su.result.PageResult;
import com.su.service.WarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public void save(WarehouseDTO dto) {
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(dto, warehouse);
        warehouse.setCreateTime(LocalDateTime.now());
        warehouse.setUpdateTime(LocalDateTime.now());
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(0);
        }
        warehouseMapper.insert(warehouse);
    }

    @Override
    public PageResult page(WarehousePageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Warehouse> page = warehouseMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Warehouse getById(Long id) {
        return warehouseMapper.getById(id);
    }

    @Override
    public void update(WarehouseDTO dto) {
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(dto, warehouse);
        warehouse.setUpdateTime(LocalDateTime.now());
        warehouseMapper.update(warehouse);
    }

    @Override
    public void deleteById(Long id) {
        warehouseMapper.deleteById(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setStatus(status);
        warehouse.setUpdateTime(LocalDateTime.now());
        warehouseMapper.update(warehouse);
    }
}

