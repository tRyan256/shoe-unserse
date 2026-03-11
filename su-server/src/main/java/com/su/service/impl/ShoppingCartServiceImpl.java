package com.su.service.impl;

import com.su.context.BaseContext;
import com.su.constant.StatusConstant;
import com.su.dto.ShoppingCartDTO;
import com.su.entity.ShoppingCart;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSpu;
import com.su.entity.Bundle;
import com.su.exception.OrderBusinessException;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.BundleMapper;
import com.su.mapper.ShoppingCartMapper;
import com.su.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private ShoeSkuMapper shoeSkuMapper;
    @Autowired
    private ShoeSpuMapper shoeSpuMapper;
    @Autowired
    private BundleMapper bundleMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insert(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        
        // Handle selected field: use passed value or default to 1 (selected)
        if (shoppingCart.getSelected() == null) {
            shoppingCart.setSelected(1);
        }
        
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list != null && !list.isEmpty()){
            ShoppingCart cart = list.get(0);
            cart.setNumber((cart.getNumber() == null ? 0 : cart.getNumber()) + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else{
            if(shoppingCart.getSkuId() != null){
                ShoeSku sku = shoeSkuMapper.getById(shoppingCart.getSkuId());
                if (sku == null || sku.getStatus() == null || !sku.getStatus().equals(StatusConstant.ENABLE)) {
                    throw new OrderBusinessException("商品规格不存在或已下架");
                }
                ShoeSpu spu = shoeSpuMapper.getById(sku.getSpuId());
                if (spu == null) {
                    throw new OrderBusinessException("商品不存在");
                }
                shoppingCart.setSpuId(sku.getSpuId());
                shoppingCart.setName(spu.getName() + " - " + sku.getColorName());
                shoppingCart.setImage(sku.getImage());
                shoppingCart.setAmount(sku.getPrice());
            }else{
                Bundle infoById = bundleMapper.getInfoById(shoppingCart.getBundleId());
                if (infoById == null || infoById.getStatus() == null || !infoById.getStatus().equals(StatusConstant.ENABLE)) {
                    throw new OrderBusinessException("组合包不存在");
                }
                shoppingCart.setName(infoById.getName());
                shoppingCart.setImage(infoById.getImage());
                shoppingCart.setAmount(infoById.getPrice());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }


    }


    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.list(shoppingCart);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(!CollectionUtils.isEmpty(list)){
            ShoppingCart cart = list.get(0);
            if(cart.getNumber() > 1){
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }else{
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }

    @Override
    public void clean() {
        Long currentId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(currentId);
    }


}
