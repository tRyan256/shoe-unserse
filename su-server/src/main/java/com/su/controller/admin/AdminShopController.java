package com.su.controller.admin;


import com.su.constant.RedisKeyConstant;
import com.su.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class AdminShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    public Result<String> setStatus(@PathVariable Integer status){
        log.info("设置店铺状态：{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(RedisKeyConstant.shopStatusKey(),status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(RedisKeyConstant.shopStatusKey());
        log.info("获取店铺状态：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
