package com.su.controller.user;


import com.su.constant.RedisKeyConstant;
import com.su.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
public class UserShopController {

    @Autowired
    private RedisTemplate redisTemplate;



    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(RedisKeyConstant.shopStatusKey());
        log.info("获取店铺状态：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
