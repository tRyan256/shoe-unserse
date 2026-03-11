package com.su.service;

import com.su.utils.cache.CacheClient;
import com.su.constant.RedisKeyConstant;
import com.su.entity.OrderDetail;
import com.su.entity.Orders;
import com.su.entity.ShoeSku;
import com.su.entity.ShoppingCart;
import com.su.exception.OrderBusinessException;
import com.su.mapper.OrderDetailMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSkuSizeMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StockReservationService {
    private final ShoeSkuMapper shoeSkuMapper;
    private final ShoeSkuSizeMapper shoeSkuSizeMapper;
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheClient cacheClient;

    public StockReservationService(
            ShoeSkuMapper shoeSkuMapper,
            ShoeSkuSizeMapper shoeSkuSizeMapper,
            OrderMapper orderMapper,
            OrderDetailMapper orderDetailMapper,
            StringRedisTemplate stringRedisTemplate,
            CacheClient cacheClient
    ) {
        this.shoeSkuMapper = shoeSkuMapper;
        this.shoeSkuSizeMapper = shoeSkuSizeMapper;
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheClient = cacheClient;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrder(String orderNumber, Long userId, List<ShoppingCart> cartList) {
        reserveForOrderInternal(orderNumber, userId, cartList, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrderAllowDisabled(String orderNumber, Long userId, List<ShoppingCart> cartList) {
        reserveForOrderInternal(orderNumber, userId, cartList, true);
    }

    private void reserveForOrderInternal(String orderNumber, Long userId, List<ShoppingCart> cartList, boolean allowDisabled) {
        if (orderNumber == null || orderNumber.isBlank() || userId == null || CollectionUtils.isEmpty(cartList)) {
            return;
        }
        String lockKey = RedisKeyConstant.stockReserveLockKey(orderNumber);
        String token = cacheClient.tryLock(lockKey, Duration.ofSeconds(30));
        if (token == null) {
            throw new OrderBusinessException("下单处理中，请稍后重试");
        }
        String reserveKey = RedisKeyConstant.stockReserveKey(orderNumber);
        try {
            String existing = cacheClient.get(reserveKey);
            if (existing != null) {
                return;
            }
            cacheClient.set(reserveKey, String.valueOf(userId), Duration.ofDays(1));
            try {
                for (ShoppingCart cart : cartList) {
                    if (cart == null || cart.getSkuId() == null) {
                        continue;
                    }
                    Integer number = cart.getNumber();
                    if (number == null || number <= 0) {
                        continue;
                    }
                    String size = cart.getShoeSize();
                    if (size == null || size.isBlank()) {
                        throw new OrderBusinessException("鞋码不能为空");
                    }

                    Long skuId = cart.getSkuId();
                    ShoeSku sku = shoeSkuMapper.getById(skuId);
                    if (sku == null) {
                        throw new OrderBusinessException("商品规格不存在");
                    }
                    if (!allowDisabled && (sku.getStatus() == null || sku.getStatus() != 1)) {
                        throw new OrderBusinessException("商品已下架");
                    }

                    int updated = shoeSkuSizeMapper.decrementStock(skuId, size, number);
                    if (updated == 0) {
                        throw new OrderBusinessException("库存不足");
                    }
                }
            } catch (Exception e) {
                cacheClient.evict(reserveKey);
                throw e;
            }
        } finally {
            cacheClient.unlock(lockKey, token);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrderAllowDisabledAtomic(String orderNumber, Long userId, List<ShoppingCart> cartList) {
        reserveForOrderInternal(orderNumber, userId, cartList, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrderAtomic(String orderNumber, Long userId, List<ShoppingCart> cartList) {
        reserveForOrderInternal(orderNumber, userId, cartList, false);
    }

    public void rollbackByCartList(Long userId, List<ShoppingCart> cartList) {
        if (userId == null || CollectionUtils.isEmpty(cartList)) {
            return;
        }
        for (ShoppingCart cart : cartList) {
            if (cart == null || cart.getSkuId() == null) {
                continue;
            }
            Integer number = cart.getNumber();
            if (number == null || number <= 0) {
                continue;
            }
            String size = cart.getShoeSize();
            if (size == null || size.isBlank()) {
                continue;
            }
            shoeSkuSizeMapper.incrementStock(cart.getSkuId(), size, number);
        }
    }

    public void rollbackReservationByCartList(String orderNumber, Long userId, List<ShoppingCart> cartList) {
        if (!beginRollback(orderNumber)) {
            return;
        }
        try {
            rollbackByCartList(userId, cartList);
            clearReservation(orderNumber);
            finalizeRollback(orderNumber, true);
        } catch (RuntimeException e) {
            finalizeRollback(orderNumber, false);
            throw e;
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                registerInflightCleanup(orderNumber);
            } else {
                releaseInflight(orderNumber);
            }
        }
    }

    public void clearReservation(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.stockReserveKey(orderNumber));
    }

    public void rollback(String orderNumber, Long userId) {
        if (orderNumber == null || orderNumber.isBlank() || userId == null) {
            return;
        }
        if (!beginRollback(orderNumber)) {
            return;
        }
        try {
        Orders orders = orderMapper.getByNumber(orderNumber);
        if (orders == null || orders.getId() == null) {
            clearReservation(orderNumber);
            finalizeRollback(orderNumber, false);
            return;
        }
        if (orders.getUserId() != null && !orders.getUserId().equals(userId)) {
            finalizeRollback(orderNumber, false);
            return;
        }
        List<OrderDetail> details = orderDetailMapper.getByOrderId(List.of(orders.getId()));
        if (CollectionUtils.isEmpty(details)) {
            clearReservation(orderNumber);
            finalizeRollback(orderNumber, false);
            return;
        }
        for (OrderDetail d : details) {
            if (d == null || d.getSkuId() == null) {
                continue;
            }
            Integer number = d.getNumber();
            if (number == null || number <= 0) {
                continue;
            }
            String size = d.getShoeSize();
            if (size == null || size.isBlank()) {
                continue;
            }
            shoeSkuSizeMapper.incrementStock(d.getSkuId(), size, number);
        }
        clearReservation(orderNumber);
        finalizeRollback(orderNumber, true);
        } catch (RuntimeException e) {
            finalizeRollback(orderNumber, false);
            throw e;
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                registerInflightCleanup(orderNumber);
            } else {
                releaseInflight(orderNumber);
            }
        }
    }

    private boolean beginRollback(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return false;
        }
        try {
            Boolean done = cacheClient.exists(RedisKeyConstant.stockRollbackDoneKey(orderNumber));
            if (Boolean.TRUE.equals(done)) {
                return false;
            }
            Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(
                    RedisKeyConstant.stockRollbackInflightKey(orderNumber),
                    String.valueOf(System.currentTimeMillis()),
                    Duration.ofMinutes(5)
            );
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.error("库存回滚检查异常, orderNumber={}", orderNumber, e);
            return false;
        }
    }

    private void finalizeRollback(String orderNumber, boolean done) {
        if (!done || orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    markRollbackDone(orderNumber);
                }
            });
            return;
        }
        markRollbackDone(orderNumber);
    }

    private void registerInflightCleanup(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                releaseInflight(orderNumber);
            }
        });
    }

    private void markRollbackDone(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().setIfAbsent(
                    RedisKeyConstant.stockRollbackDoneKey(orderNumber),
                    String.valueOf(LocalDateTime.now()),
                    Duration.ofDays(30)
            );
        } catch (Exception ignored) {
        }
    }

    private void releaseInflight(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return;
        }
        cacheClient.evict(RedisKeyConstant.stockRollbackInflightKey(orderNumber));
    }
}
