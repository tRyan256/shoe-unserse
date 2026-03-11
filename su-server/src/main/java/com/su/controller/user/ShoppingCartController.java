package com.su.controller.user;


import com.su.dto.ShoppingCartDTO;
import com.su.entity.ShoppingCart;
import com.su.result.Result;
import com.su.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody ShoppingCartDTO shoppingCartDTO){

        log.info("添加购物车：{}", shoppingCartDTO);
        shoppingCartService.insert(shoppingCartDTO);
        return Result.success();
    }


    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车");
        return Result.success(shoppingCartService.list());
    }

    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一个商品，商品：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    public Result<String> clean(){
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
