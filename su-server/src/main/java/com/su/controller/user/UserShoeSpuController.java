package com.su.controller.user;

import com.su.constant.StatusConstant;
import com.su.enumeration.ShoeSortType;
import com.su.exception.OrderBusinessException;
import com.su.result.Result;
import com.su.service.ShoeSkuService;
import com.su.service.ShoeSpuService;
import com.su.vo.ShoeSkuVO;
import com.su.vo.ShoeSpuDetailVO;
import com.su.vo.ShoeSpuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoe")
public class UserShoeSpuController {

    @Autowired
    private ShoeSpuService shoeSpuService;

    @Autowired
    private ShoeSkuService shoeSkuService;

    @GetMapping("/list")
    public Result<List<ShoeSpuVO>> list(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String keyword) {

        log.info("查询SPU列表：categoryIds={}, sort={}, keyword={}", categoryIds, sort, keyword);

        ShoeSortType sortType = ShoeSortType.fromCode(sort);
        List<ShoeSpuVO> result;

        if (keyword != null && !keyword.trim().isEmpty()) {
            result = shoeSpuService.searchByKeyword(keyword.trim(), sortType);
        } else if (categoryIds != null && !categoryIds.isEmpty()) {
            result = shoeSpuService.listByCategoryIds(categoryIds, sortType);
        } else {
            result = shoeSpuService.listAll(sortType);
        }

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ShoeSpuDetailVO> getById(@PathVariable Long id) {
        log.info("查询SPU详情：{}", id);
        ShoeSpuDetailVO detailVO = shoeSpuService.getByIdWithDetails(id);
        if (detailVO == null || detailVO.getStatus() == null || !detailVO.getStatus().equals(StatusConstant.ENABLE)) {
            throw new OrderBusinessException("鞋款不存在");
        }
        return Result.success(detailVO);
    }

    @GetMapping("/sku/{skuId}")
    public Result<ShoeSkuVO> getSkuById(@PathVariable Long skuId) {
        log.info("查询SKU详情：{}", skuId);
        ShoeSkuVO skuVO = shoeSkuService.getByIdWithSizes(skuId);
        if (skuVO == null || skuVO.getStatus() == null || !skuVO.getStatus().equals(StatusConstant.ENABLE)) {
            throw new OrderBusinessException("SKU不存在");
        }
        return Result.success(skuVO);
    }
}
