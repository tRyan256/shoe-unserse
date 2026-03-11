package com.su.controller.admin;

import com.su.dto.admin.AirdropSaveDTO;
import com.su.dto.admin.BundleSaveDTO;
import com.su.dto.admin.DrawSaveDTO;
import com.su.dto.admin.ShoeSaveDTO;
import com.su.result.Result;
import com.su.service.AdminProductService;
import com.su.vo.ShoeSpuAdminVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端商品管理控制器
 * 提供商品（鞋款、组合包、抽签、空投）的管理接口
 */
@RestController
@RequestMapping("/admin/product")
@Slf4j
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    /**
     * 保存鞋款（支持多分类）
     *
     * @param dto 鞋款保存DTO
     * @return 成功消息
     */
    @PostMapping("/shoe")
    public Result<String> saveShoeSpu(@Valid @RequestBody ShoeSaveDTO dto) {
        log.info("管理员保存鞋款，dto={}", dto);
        
        adminProductService.saveShoeSpu(dto);
        return Result.success("保存成功");
    }

    /**
     * 更新鞋款（支持多分类）
     *
     * @param id 鞋款ID
     * @param dto 鞋款保存DTO
     * @return 成功消息
     */
    @PutMapping("/shoe/{id}")
    public Result<String> updateShoeSpu(@PathVariable Long id, @Valid @RequestBody ShoeSaveDTO dto) {
        log.info("管理员更新鞋款，id={}, dto={}", id, dto);
        
        adminProductService.updateShoeSpu(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 查询鞋款详情（包含所有分类）
     *
     * @param id 鞋款ID
     * @return 鞋款详情VO
     */
    @GetMapping("/shoe/{id}")
    public Result<ShoeSpuAdminVO> getShoeSpu(@PathVariable Long id) {
        log.info("管理员查询鞋款详情，id={}", id);
        
        ShoeSpuAdminVO vo = adminProductService.getShoeSpu(id);
        if (vo == null) {
            return Result.error("鞋款不存在");
        }
        return Result.success(vo);
    }

    /**
     * 更新组合包
     *
     * @param id 组合包ID
     * @param dto 组合包保存DTO
     * @return 成功消息
     */
    @PutMapping("/bundle/{id}")
    public Result<String> updateBundle(@PathVariable Long id, @Valid @RequestBody BundleSaveDTO dto) {
        log.info("管理员更新组合包，id={}, dto={}", id, dto);
        
        adminProductService.updateBundle(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 更新抽签活动
     *
     * @param id 抽签活动ID
     * @param dto 抽签保存DTO
     * @return 成功消息
     */
    @PutMapping("/draw/{id}")
    public Result<String> updateDraw(@PathVariable Long id, @Valid @RequestBody DrawSaveDTO dto) {
        log.info("管理员更新抽签活动，id={}, dto={}", id, dto);
        
        adminProductService.updateDraw(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 更新空投活动
     *
     * @param id 空投活动ID
     * @param dto 空投保存DTO
     * @return 成功消息
     */
    @PutMapping("/airdrop/{id}")
    public Result<String> updateAirdrop(@PathVariable Long id, @Valid @RequestBody AirdropSaveDTO dto) {
        log.info("管理员更新空投活动，id={}, dto={}", id, dto);
        
        adminProductService.updateAirdrop(id, dto);
        return Result.success("更新成功");
    }
}
