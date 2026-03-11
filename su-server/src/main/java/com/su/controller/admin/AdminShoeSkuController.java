package com.su.controller.admin;

import com.su.dto.ShoeSkuDTO;
import com.su.result.Result;
import com.su.service.ShoeSkuService;
import com.su.vo.ShoeSkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/shoe/sku")
public class AdminShoeSkuController {

    @Autowired
    private ShoeSkuService shoeSkuService;

    @PostMapping
    public Result save(@RequestBody ShoeSkuDTO skuDTO) {
        log.info("新增SKU：{}", skuDTO);
        shoeSkuService.save(skuDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ShoeSkuVO> getById(@PathVariable Long id) {
        log.info("根据id查询SKU详情：{}", id);
        ShoeSkuVO skuVO = shoeSkuService.getByIdWithSizes(id);
        return Result.success(skuVO);
    }

    @PutMapping
    public Result update(@RequestBody ShoeSkuDTO skuDTO) {
        log.info("修改SKU：{}", skuDTO);
        shoeSkuService.update(skuDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除SKU：{}", id);
        shoeSkuService.delete(id);
        return Result.success();
    }

    @GetMapping("/list/{spuId}")
    public Result listBySpuId(@PathVariable Long spuId) {
        log.info("根据SPU id查询SKU列表：{}", spuId);
        return Result.success(shoeSkuService.listBySpuId(spuId));
    }

    @GetMapping("/list")
    public Result listAll() {
        log.info("查询所有SKU列表");
        return Result.success(shoeSkuService.listAll());
    }

    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("更新SKU状态：{}, {}", id, status);
        shoeSkuService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/default/{skuId}")
    public Result setDefault(@PathVariable Long skuId, @RequestParam Long spuId) {
        log.info("设置默认SKU：spuId={}, skuId={}", spuId, skuId);
        shoeSkuService.setDefaultSku(spuId, skuId);
        return Result.success();
    }
}
