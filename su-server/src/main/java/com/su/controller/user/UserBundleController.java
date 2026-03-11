package com.su.controller.user;

import com.su.constant.StatusConstant;
import com.su.entity.Bundle;
import com.su.exception.OrderBusinessException;
import com.su.result.Result;
import com.su.service.BundleService;
import com.su.vo.BundleVO;
import com.su.vo.ShoeItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userBundleController")
@RequestMapping("/user/bundle")
public class UserBundleController {
    @Autowired
    private BundleService bundleService;

    @GetMapping("/list")
    public Result<List<Bundle>> list() {
        Bundle bundle = new Bundle();
        bundle.setStatus(StatusConstant.ENABLE);

        List<Bundle> list = bundleService.list(bundle);
        return Result.success(list);
    }

    @GetMapping("/shoe/{id}")
    public Result<List<ShoeItemVO>> shoeList(@PathVariable("id") Long id) {
        BundleVO bundle = bundleService.getById(id);
        if (bundle == null || bundle.getStatus() == null || !bundle.getStatus().equals(StatusConstant.ENABLE)) {
            throw new OrderBusinessException("组合包不存在");
        }
        // 直接从缓存的BundleVO中获取商品信息
        return Result.success(bundle.getShoeItems());
    }

    @GetMapping("/detail/{id}")
    public Result<BundleVO> detail(@PathVariable("id") Long id) {
        BundleVO bundle = bundleService.getById(id);
        if (bundle == null || bundle.getStatus() == null || !bundle.getStatus().equals(StatusConstant.ENABLE)) {
            throw new OrderBusinessException("组合包不存在");
        }
        return Result.success(bundle);
    }
}
