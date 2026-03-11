package com.su.service;

import com.su.dto.ShoppingCartDTO;
import com.su.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加商品到购物车
     * @param shoppingCartDTO
     */
    void insert(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查询购物车列表
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 从购物车减少商品
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     */
    void clean();
}
