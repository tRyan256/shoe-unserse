package com.su.controller.admin;

import com.su.result.Result;
import com.su.service.WorkspaceService;
import com.su.vo.BusinessDataVO;
import com.su.vo.OrderOverViewVO;
import com.su.vo.ShoeOverViewVO;
import com.su.vo.BundleOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 工作台
 */
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 工作台今日数据查询
     * @return
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> orderOverView(){
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * 查询菜品总览
     * @return
     */
    @GetMapping("/overviewShoes")
    public Result<ShoeOverViewVO> shoeOverView(){
        return Result.success(workspaceService.getShoeOverView());
    }

    /**
     * 查询套餐总览
     * @return
     */
    @GetMapping("/overviewBundles")
    public Result<BundleOverViewVO> bundleOverView(){
        return Result.success(workspaceService.getBundleOverView());
    }
}
