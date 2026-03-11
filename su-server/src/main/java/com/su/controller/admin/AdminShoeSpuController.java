package com.su.controller.admin;

import com.su.dto.ShoeSpuDTO;
import com.su.dto.ShoeSpuPageQueryDTO;
import com.su.enumeration.ShoeSortType;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.ShoeSpuService;
import com.su.vo.ShoeSpuDetailVO;
import com.su.vo.ShoeSpuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/shoe/spu")
public class AdminShoeSpuController {

    @Autowired
    private ShoeSpuService shoeSpuService;

    @PostMapping
    public Result save(@RequestBody ShoeSpuDTO spuDTO) {
        log.info("新增SPU：{}", spuDTO);
        shoeSpuService.save(spuDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> pageQuery(ShoeSpuPageQueryDTO queryDTO) {
        log.info("分页查询SPU：{}", queryDTO);
        PageResult pageResult = shoeSpuService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除SPU：{}", ids);
        shoeSpuService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ShoeSpuDetailVO> getById(@PathVariable Long id) {
        log.info("根据id查询SPU详情：{}", id);
        ShoeSpuDetailVO detailVO = shoeSpuService.getByIdWithDetails(id);
        return Result.success(detailVO);
    }

    @PutMapping
    public Result update(@RequestBody ShoeSpuDTO spuDTO) {
        log.info("修改SPU：{}", spuDTO);
        shoeSpuService.update(spuDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("启用/停用SPU：id={}, status={}", id, status);
        shoeSpuService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoeSpuVO>> list() {
        log.info("查询所有SPU");
        List<ShoeSpuVO> list = shoeSpuService.listAll((ShoeSortType) null);
        return Result.success(list);
    }
}
