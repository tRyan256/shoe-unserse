package com.su.controller.admin;

import com.su.dto.AirdropDTO;
import com.su.dto.AirdropPageQueryDTO;
import com.su.entity.Airdrop;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.AirdropService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/airdrop")
@Slf4j
public class AdminAirdropController {

    @Autowired
    private AirdropService airdropService;

    @PostMapping
    public Result<String> save(@RequestBody AirdropDTO dto) {
        airdropService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(AirdropPageQueryDTO dto) {
        return Result.success(airdropService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<Airdrop> getById(@PathVariable Long id) {
        return Result.success(airdropService.getById(id));
    }

    @PutMapping
    public Result<String> update(@RequestBody AirdropDTO dto) {
        airdropService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        airdropService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        airdropService.cancel(id);
        return Result.success();
    }
}
