package com.su.service;

import com.su.dto.ShoeSkuDTO;
import com.su.vo.ShoeSkuVO;

import java.util.List;

public interface ShoeSkuService {

    void save(ShoeSkuDTO skuDTO);

    void update(ShoeSkuDTO skuDTO);

    void delete(Long id);

    ShoeSkuVO getByIdWithSizes(Long id);

    void updateStock(Long skuId, String size, Integer quantity);

    List<ShoeSkuVO> listBySpuId(Long spuId);

    List<ShoeSkuVO> listAll();

    void updateStatus(Long id, Integer status);

    void setDefaultSku(Long spuId, Long skuId);
}
