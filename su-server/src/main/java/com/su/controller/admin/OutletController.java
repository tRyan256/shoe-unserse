package com.su.controller.admin;

import com.su.dto.OutletDTO;
import com.su.dto.OutletPageQueryDTO;
import com.su.entity.Outlet;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.OutletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/outlet")
@Slf4j
public class OutletController {

    @Autowired
    private OutletService outletService;

    @PostMapping
    public Result<String> save(@RequestBody OutletDTO dto) {
        outletService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(OutletPageQueryDTO dto) {
        PageResult pageResult = outletService.page(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Outlet> getById(@PathVariable Long id) {
        return Result.success(outletService.getById(id));
    }

    @PutMapping
    public Result<String> update(@RequestBody OutletDTO dto) {
        outletService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        outletService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        outletService.startOrStop(status, id);
        return Result.success();
    }
}

