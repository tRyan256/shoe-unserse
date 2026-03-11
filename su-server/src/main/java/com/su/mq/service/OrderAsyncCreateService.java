package com.su.mq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RedisKeyConstant;
import com.su.entity.AddressBook;
import com.su.entity.ExperienceNotification;
import com.su.entity.OrderDetail;
import com.su.entity.Orders;
import com.su.entity.ShoeSku;
import com.su.entity.UserCoupon;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AddressBookMapper;
import com.su.mapper.ExperienceNotificationMapper;
import com.su.mapper.OrderDetailMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoppingCartMapper;
import com.su.mapper.UserCouponMapper;
import com.su.mq.message.OrderCreateItemMessage;
import com.su.mq.message.OrderCreateMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.utils.cache.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderAsyncCreateService {
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final UserCouponMapper userCouponMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final ExperienceNotificationMapper experienceNotificationMapper;
    private final ShoeSkuMapper shoeSkuMapper;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;

    public OrderAsyncCreateService(
            OrderMapper orderMapper,
            OrderDetailMapper orderDetailMapper,
            AddressBookMapper addressBookMapper,
            UserCouponMapper userCouponMapper,
            ShoppingCartMapper shoppingCartMapper,
            ExperienceNotificationMapper experienceNotificationMapper,
            ShoeSkuMapper shoeSkuMapper,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.addressBookMapper = addressBookMapper;
        this.userCouponMapper = userCouponMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.experienceNotificationMapper = experienceNotificationMapper;
        this.shoeSkuMapper = shoeSkuMapper;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFromMessage(OrderCreateMessage message) {
        if (message == null || message.getOrderNumber() == null || message.getOrderNumber().isBlank()) {
            throw new OrderBusinessException("订单消息缺失");
        }
        Orders exist = orderMapper.getByNumber(message.getOrderNumber());
        if (exist != null && exist.getId() != null) {
            registerAfterCommit(message.getOrderNumber(), message.getUserId(), null);
            return exist.getId();
        }

        AddressBook addressBook = addressBookMapper.getById(message.getAddressBookId());
        if (addressBook == null || message.getUserId() == null || !message.getUserId().equals(addressBook.getUserId())) {
            throw new OrderBusinessException("地址信息异常");
        }
        if (CollectionUtils.isEmpty(message.getItems())) {
            throw new OrderBusinessException("订单明细为空");
        }

        StringBuilder addressBuilder = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            addressBuilder.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            addressBuilder.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            addressBuilder.append(addressBook.getDistrictName());
        }
        if (addressBook.getDetail() != null) {
            addressBuilder.append(addressBook.getDetail());
        }
        String address = addressBuilder.toString();

        Orders orders = Orders.builder()
                .number(message.getOrderNumber())
                .orderTime(message.getOrderTime() == null ? LocalDateTime.now() : message.getOrderTime())
                .payStatus(Orders.UN_PAID)
                .status(Orders.PENDING_PAYMENT)
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .userId(message.getUserId())
                .addressBookId(message.getAddressBookId())
                .address(address)
                .amount(message.getAmount())
                .remark(message.getRemark())
                .payMethod(message.getPayMethod())
                .couponId(message.getCouponId())
                .build();
        orderMapper.insert(orders);

        List<OrderDetail> details = message.getItems().stream().map(i -> toDetail(orders.getId(), i)).toList();
        orderDetailMapper.insertBatch(details);

        if (message.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.findAvailable(message.getUserId(), message.getCouponId());
            if (userCoupon == null) {
                throw new OrderBusinessException("未找到可用优惠券");
            }
            userCoupon.setStatus(1);
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderId(orders.getId());
            int updated = userCouponMapper.markUsed(userCoupon);
            if (updated == 0) {
                throw new OrderBusinessException("优惠券已被使用");
            }
        }

        shoppingCartMapper.cleanSelected(message.getUserId());
        createOrderNotification(message.getUserId(), message.getOrderNumber(), "订单已创建，待支付");

        registerAfterCommit(message.getOrderNumber(), message.getUserId(), null);
        return orders.getId();
    }

    private void createOrderNotification(Long userId, String orderNumber, String content) {
        if (userId == null) {
            return;
        }
        ExperienceNotification notification = ExperienceNotification.builder()
                .userId(userId)
                .type(ExperienceNotification.TYPE_ORDER)
                .sourceUserId(userId)
                .content(content + "（订单号：" + orderNumber + "）")
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        experienceNotificationMapper.insert(notification);
    }

    private void registerAfterCommit(String orderNumber, Long userId, String error) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            updatePreorder(orderNumber, userId, error);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                updatePreorder(orderNumber, userId, error);
            }
        });
    }

    private void updatePreorder(String orderNumber, Long userId, String error) {
        PreOrderRecord record = PreOrderRecord.builder()
                .orderNumber(orderNumber)
                .status(error == null ? OrderAsyncStatusConstant.SUCCESS : OrderAsyncStatusConstant.FAILED)
                .userId(userId)
                .error(error)
                .createTime(LocalDateTime.now())
                .build();
        try {
            cacheClient.set(RedisKeyConstant.preorderKey(orderNumber), objectMapper.writeValueAsString(record), Duration.ofMinutes(30));
        } catch (Exception ignored) {
            log.error("更新预下单状态失败，orderNumber={}, status={}", orderNumber, record.getStatus(), ignored);
        }
    }

    private OrderDetail toDetail(Long orderId, OrderCreateItemMessage item) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setSkuId(item.getSkuId());
        if (item.getSkuId() != null) {
            ShoeSku sku = shoeSkuMapper.getById(item.getSkuId());
            if (sku != null) {
                detail.setSpuId(sku.getSpuId());
            }
        }
        detail.setBundleId(item.getBundleId());
        detail.setShoeSize(item.getShoeSize());
        detail.setNumber(item.getNumber());
        detail.setAmount(item.getAmount());
        detail.setName(item.getName());
        detail.setImage(item.getImage());
        return detail;
    }

}
