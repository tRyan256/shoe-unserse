package com.su.mapper;


import com.su.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);

    @Insert("insert into shopping_cart (name, user_id, spu_id, sku_id, bundle_id, shoe_size, number, amount, image, create_time, selected) " +
            "values (#{name}, #{userId}, #{spuId}, #{skuId}, #{bundleId}, #{shoeSize}, #{number}, #{amount}, #{image}, #{createTime}, #{selected})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void clean(Long currentId);

    @Delete("delete from shopping_cart where user_id = #{currentId} and selected = 1")
    void cleanSelected(Long currentId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
