package com.su.controller.admin;

import com.su.dto.DrawDTO;
import com.su.dto.DrawPageQueryDTO;
import com.su.entity.Draw;
import com.su.entity.ShoeSku;
import com.su.mapper.ShoeSkuMapper;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.DrawService;
import com.su.vo.DrawDetailAdminVO;
import com.su.vo.DrawWinnerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/draw")
@Slf4j
public class AdminDrawController {

    @Autowired
    private DrawService drawService;

    @Autowired
    private ShoeSkuMapper shoeSkuMapper;

    @PostMapping
    public Result<String> save(@RequestBody DrawDTO dto) {
        drawService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DrawPageQueryDTO dto) {
        return Result.success(drawService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<DrawDetailAdminVO> getById(@PathVariable Long id) {
        Draw draw = drawService.getById(id);
        if (draw == null) {
            return Result.success(null);
        }

        DrawDetailAdminVO vo = new DrawDetailAdminVO();
        BeanUtils.copyProperties(draw, vo);

        // 如果是鞋款类型，根据skuId查询spuId
        if (draw.getTargetType() != null && draw.getTargetType() == 1 && draw.getSkuId() != null) {
            ShoeSku sku = shoeSkuMapper.getById(draw.getSkuId());
            if (sku != null) {
                vo.setSpuId(sku.getSpuId());
            }
        }

        return Result.success(vo);
    }

    @PutMapping
    public Result<String> update(@RequestBody DrawDTO dto) {
        drawService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        drawService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/manualDraw/{id}")
    public Result<String> manualDraw(@PathVariable Long id) {
        drawService.manualDraw(id);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        drawService.cancel(id);
        return Result.success();
    }

    @GetMapping("/winners/{id}")
    public Result<List<DrawWinnerVO>> winners(@PathVariable Long id) {
        return Result.success(drawService.winners(id));
    }
}
