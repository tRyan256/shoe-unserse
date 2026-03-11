package com.su.controller.admin;

import com.su.dto.WarehouseDTO;
import com.su.dto.WarehousePageQueryDTO;
import com.su.dto.WarehouseStockPageQueryDTO;
import com.su.entity.Warehouse;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.WarehouseStockService;
import com.su.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/warehouse")
@Slf4j
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private WarehouseStockService warehouseStockService;

    @PostMapping
    public Result<String> save(@RequestBody WarehouseDTO dto) {
        warehouseService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(WarehousePageQueryDTO dto) {
        PageResult pageResult = warehouseService.page(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }

    @PutMapping
    public Result<String> update(@RequestBody WarehouseDTO dto) {
        warehouseService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        warehouseService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        warehouseService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/stock/page")
    public Result<PageResult> stockPage(WarehouseStockPageQueryDTO dto) {
        return Result.success(warehouseStockService.page(dto));
    }
}
