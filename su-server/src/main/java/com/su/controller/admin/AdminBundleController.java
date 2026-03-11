package com.su.controller.admin;

import com.su.constant.StatusConstant;
import com.su.dto.BundleDTO;
import com.su.dto.BundlePageQueryDTO;
import com.su.entity.Bundle;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.BundleService;
import com.su.vo.BundleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/bundle")
public class AdminBundleController {

    @Autowired
    private BundleService bundleService;

    @GetMapping("/page")
    public Result<PageResult> page(BundlePageQueryDTO bundlePageQueryDTO) {
        log.info("分页查询：{}", bundlePageQueryDTO);
        PageResult pageResult = bundleService.pageQuery(bundlePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<BundleDTO> save(@RequestBody BundleDTO bundleDTO) {
        log.info("新增组合包：{}", bundleDTO);
        bundleService.save(bundleDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除：{}", ids);
        bundleService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<BundleVO> getById(@PathVariable Long id) {
        log.info("根据id查询组合包：{}", id);
        BundleVO bundleVO = bundleService.getById(id);
        return Result.success(bundleVO);
    }

    @GetMapping("/list")
    public Result<List<Bundle>> list(@RequestParam(required = false, defaultValue = "false") Boolean includeDisabled) {
        log.info("查询组合包列表：includeDisabled={}", includeDisabled);
        Bundle.BundleBuilder builder = Bundle.builder();
        if (includeDisabled == null || !includeDisabled) {
            builder.status(StatusConstant.ENABLE);
        }
        List<Bundle> list = bundleService.list(builder.build());
        return Result.success(list);
    }

    @PutMapping
    public Result<String> update(@RequestBody BundleDTO bundleDTO) {
        bundleService.update(bundleDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        bundleService.startOrStop(status, id);
        return Result.success();
    }
}
