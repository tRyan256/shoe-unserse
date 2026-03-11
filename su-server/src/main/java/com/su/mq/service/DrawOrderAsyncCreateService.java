package com.su.mq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RedisKeyConstant;
import com.su.entity.AddressBook;
import com.su.entity.ExperienceNotification;
import com.su.entity.OrderDetail;
import com.su.entity.Orders;
import com.su.entity.ShoeSku;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AddressBookMapper;
import com.su.mapper.ExperienceNotificationMapper;
import com.su.mapper.OrderDetailMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mq.message.DrawOrderCreateMessage;
import com.su.mq.message.OrderCreateItemMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.utils.cache.CacheClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DrawOrderAsyncCreateService {
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final ExperienceNotificationMapper experienceNotificationMapper;
    private final ShoeSkuMapper shoeSkuMapper;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;

    public DrawOrderAsyncCreateService(
            OrderMapper orderMapper,
            OrderDetailMapper orderDetailMapper,
            AddressBookMapper addressBookMapper,
            ExperienceNotificationMapper experienceNotificationMapper,
            ShoeSkuMapper shoeSkuMapper,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.addressBookMapper = addressBookMapper;
        this.experienceNotificationMapper = experienceNotificationMapper;
        this.shoeSkuMapper = shoeSkuMapper;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFromMessage(DrawOrderCreateMessage message) {
        if (message == null || message.getOrderNumber() == null || message.getOrderNumber().isBlank()) {
            throw new OrderBusinessException("抽签订单消息缺失");
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

        String address = addressBook.getProvinceName() + addressBook.getCityName()
                + addressBook.getDistrictName() + addressBook.getDetail();

        Orders orders = Orders.builder()
                .number(message.getOrderNumber())
                .orderType(2)
                .orderTime(message.getOrderTime() == null ? LocalDateTime.now() : message.getOrderTime())
                .payStatus(Orders.UN_PAID)
                .status(Orders.PENDING_PAYMENT)
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .userId(message.getUserId())
                .addressBookId(message.getAddressBookId())
                .address(address)
                .amount(message.getAmount())
                .payMethod(1)
                .remark("抽签订单")
                .couponId(null)
                .build();
        orderMapper.insert(orders);

        List<OrderDetail> details = message.getItems().stream().map(i -> toDetail(orders.getId(), i)).toList();
        orderDetailMapper.insertBatch(details);
        createOrderNotification(message.getUserId(), message.getOrderNumber(), "抽签订单已创建，待支付");

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
