package com.su.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.constant.DelayTaskTypeConstant;
import com.su.constant.MessageConstant;
import com.su.constant.OrderAsyncStatusConstant;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.constant.StatusConstant;
import com.su.context.BaseContext;
import com.su.component.delay.DelayQueueService;
import com.su.dto.OrdersCancelDTO;
import com.su.dto.OrdersConfirmDTO;
import com.su.dto.OrdersPageQueryDTO;
import com.su.dto.OrdersPaymentDTO;
import com.su.dto.OrdersRejectionDTO;
import com.su.dto.OrdersSubmitDTO;
import com.su.dto.OrdersSubmitItemDTO;
import com.su.entity.AddressBook;
import com.su.entity.Bundle;
import com.su.entity.Coupon;
import com.su.entity.ExperienceNotification;
import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
import com.su.entity.OrderDetail;
import com.su.entity.Orders;
import com.su.entity.ShoeSku;
import com.su.entity.ShoeSpu;
import com.su.entity.ShoppingCart;
import com.su.entity.User;
import com.su.entity.UserCoupon;
import com.su.exception.AddressBookBusinessException;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AddressBookMapper;
import com.su.mapper.BundleMapper;
import com.su.mapper.CouponMapper;
import com.su.mapper.ExperienceNotificationMapper;
import com.su.mapper.LogisticsMapper;
import com.su.mapper.LogisticsTraceMapper;
import com.su.mapper.OrderDetailMapper;
import com.su.mapper.OrderMapper;
import com.su.mapper.ShoppingCartMapper;
import com.su.mapper.ShoeSkuMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.UserCouponMapper;
import com.su.mapper.UserMapper;
import com.su.mq.message.OrderCreateItemMessage;
import com.su.mq.message.OrderCreateMessage;
import com.su.mq.preorder.PreOrderRecord;
import com.su.utils.cache.CacheClient;
import com.su.utils.id.OrderNumberGenerator;
import com.su.result.PageResult;
import com.su.service.OrderService;
import com.su.service.StockReservationService;
import com.su.vo.OrderAsyncStatusVO;
import com.su.vo.OrderAsyncSubmitVO;
import com.su.vo.OrderPaymentVO;
import com.su.vo.OrderStatisticsVO;
import com.su.vo.OrderVO;
import com.su.vo.UserLogisticsTraceVO;
import com.su.vo.UserLogisticsVO;
import com.su.vo.UserOrderDetailVO;
import com.su.vo.UserOrderItemVO;
import com.su.vo.UserOrderStatisticsVO;
import com.su.vo.UserOrderVO;
import com.su.component.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private BundleMapper bundleMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private ShoeSkuMapper shoeSkuMapper;
    @Autowired
    private ShoeSpuMapper shoeSpuMapper;
    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private LogisticsTraceMapper logisticsTraceMapper;
    @Autowired
    private ExperienceNotificationMapper experienceNotificationMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private StockReservationService stockReservationService;
    @Autowired
    private DelayQueueService delayQueueService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderNumberGenerator orderNumberGenerator;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public OrderAsyncSubmitVO submitAsync(OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        Long userId = BaseContext.getCurrentId();
        RLock submitLock = redissonClient.getLock("lock:order:submit:" + userId);
        boolean locked = submitLock.tryLock(0, 15, TimeUnit.SECONDS);
        if (!locked) {
            throw new OrderBusinessException("请勿重复提交");
        }
        try {
            AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null || !userId.equals(addressBook.getUserId())) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }

            List<ShoppingCart> cartList = buildSubmitCartList(userId, ordersSubmitDTO);
            if (CollectionUtils.isEmpty(cartList)) {
                throw new AddressBookBusinessException("请选择要购买的商品");
            }
            validatePurchaseLimits(userId, cartList);

            BigDecimal cartAmount = BigDecimal.ZERO;
            for (ShoppingCart cart : cartList) {
                BigDecimal unitPrice = cart.getAmount() == null ? BigDecimal.ZERO : cart.getAmount();
                Integer number = cart.getNumber() == null ? 0 : cart.getNumber();
                cartAmount = cartAmount.add(unitPrice.multiply(BigDecimal.valueOf(number)));
            }

            BigDecimal payableAmount = cartAmount;
            if (ordersSubmitDTO.getCouponId() != null) {
                Coupon coupon = couponMapper.getById(ordersSubmitDTO.getCouponId());
                LocalDateTime now = LocalDateTime.now();
                if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
                    throw new OrderBusinessException("优惠券不可用");
                }
                if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
                    throw new OrderBusinessException("优惠券未开始");
                }
                if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                    throw new OrderBusinessException("优惠券已过期");
                }
                if (coupon.getMinAmount() != null && cartAmount.compareTo(coupon.getMinAmount()) < 0) {
                    throw new OrderBusinessException("不满足优惠券使用门槛");
                }

                if (coupon.getType() != null && coupon.getType() == 1) {
                    payableAmount = cartAmount.subtract(coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue());
                } else if (coupon.getType() != null && coupon.getType() == 2) {
                    if (coupon.getValue() == null
                            || coupon.getValue().compareTo(BigDecimal.ZERO) <= 0
                            || coupon.getValue().compareTo(BigDecimal.ONE) > 0) {
                        throw new OrderBusinessException("折扣券参数不合法");
                    }
                    payableAmount = cartAmount.multiply(coupon.getValue()).setScale(2, RoundingMode.HALF_UP);
                } else {
                    throw new OrderBusinessException("优惠券类型不支持");
                }
                if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
                    payableAmount = BigDecimal.ZERO;
                }
            }

            String orderNumber = orderNumberGenerator.next();
            String preorderKey = RedisKeyConstant.preorderKey(orderNumber);

            boolean reserved = false;
            try {
                stockReservationService.reserveForOrder(orderNumber, userId, cartList);
                reserved = true;

                PreOrderRecord record = PreOrderRecord.builder()
                        .orderNumber(orderNumber)
                        .status(OrderAsyncStatusConstant.PROCESSING)
                        .userId(userId)
                        .createTime(LocalDateTime.now())
                        .build();
                // 修改预下单记录的过期时间为10分钟
                cacheClient.set(preorderKey, objectMapper.writeValueAsString(record), Duration.ofMinutes(10));

                OrderCreateMessage message = OrderCreateMessage.builder()
                        .msgId(orderNumber)
                        .orderNumber(orderNumber)
                        .userId(userId)
                        .addressBookId(ordersSubmitDTO.getAddressBookId())
                        .couponId(ordersSubmitDTO.getCouponId())
                        .payMethod(ordersSubmitDTO.getPayMethod())
                        .remark(ordersSubmitDTO.getRemark())
                        .amount(payableAmount)
                        .orderTime(LocalDateTime.now())
                        .items(cartList.stream().map(this::toItemMessage).collect(Collectors.toList()))
                        .build();

                CorrelationData correlationData = new CorrelationData(orderNumber);
                rabbitTemplate.convertAndSend(
                        RabbitMqConstant.ORDER_EXCHANGE,
                        RabbitMqConstant.ORDER_CREATE_ROUTING_KEY,
                        message,
                        m -> {
                            m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return m;
                        },
                        correlationData
                );

                CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
                if (confirm == null || !confirm.isAck()) {
                    throw new OrderBusinessException("下单消息投递失败");
                }

                return OrderAsyncSubmitVO.builder()
                        .orderNumber(orderNumber)
                        .status(OrderAsyncStatusConstant.PROCESSING)
                        .build();
            } catch (Exception ex) {
                PreOrderRecord failed = PreOrderRecord.builder()
                        .orderNumber(orderNumber)
                        .status(OrderAsyncStatusConstant.FAILED)
                        .userId(userId)
                        .error(ex.getMessage())
                        .createTime(LocalDateTime.now())
                        .build();
                try {
                    // 修改预下单失败状态的过期时间为10分钟
                    cacheClient.set(preorderKey, objectMapper.writeValueAsString(failed), Duration.ofMinutes(10));
                } catch (Exception ignored) {
                    log.error("写入预下单失败状态失败，orderNumber={}", orderNumber, ignored);
                }
                if (reserved) {
                    stockReservationService.rollbackReservationByCartList(orderNumber, userId, cartList);
                }
                if (ex instanceof OrderBusinessException) {
                    throw ex;
                }
                throw new OrderBusinessException(ex.getMessage() == null ? "下单失败" : ex.getMessage());
            }
        } finally {
            if (submitLock.isHeldByCurrentThread()) {
                submitLock.unlock();
            }
        }
    }

    @Override
    public OrderAsyncStatusVO getAsyncStatus(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return OrderAsyncStatusVO.builder().orderNumber(orderNumber).status(OrderAsyncStatusConstant.NOT_FOUND).build();
        }
        String json = cacheClient.get(RedisKeyConstant.preorderKey(orderNumber));
        if (json == null) {
            return OrderAsyncStatusVO.builder().orderNumber(orderNumber).status(OrderAsyncStatusConstant.NOT_FOUND).build();
        }
        try {
            PreOrderRecord record = objectMapper.readValue(json, PreOrderRecord.class);
            return OrderAsyncStatusVO.builder()
                    .orderNumber(orderNumber)
                    .status(record.getStatus())
                    .error(record.getError())
                    .build();
        } catch (Exception e) {
            log.warn("解析预下单状态失败，orderNumber={}", orderNumber, e);
            return OrderAsyncStatusVO.builder()
                    .orderNumber(orderNumber)
                    .status(OrderAsyncStatusConstant.FAILED)
                    .error("预下单状态数据异常")
                    .build();
        }
    }

    private OrderCreateItemMessage toItemMessage(ShoppingCart cart) {
        return OrderCreateItemMessage.builder()
                .skuId(cart.getSkuId())
                .bundleId(cart.getBundleId())
                .shoeSize(cart.getShoeSize())
                .number(cart.getNumber())
                .amount(cart.getAmount())
                .name(cart.getName())
                .image(cart.getImage())
                .build();
    }

    private List<ShoppingCart> buildSubmitCartList(Long userId, OrdersSubmitDTO ordersSubmitDTO) {
        if (ordersSubmitDTO != null && !CollectionUtils.isEmpty(ordersSubmitDTO.getItems())) {
            List<ShoppingCart> cartList = new ArrayList<>();
            for (OrdersSubmitItemDTO item : ordersSubmitDTO.getItems()) {
                if (item == null) {
                    continue;
                }
                int quantity = item.getQuantity() == null || item.getQuantity() <= 0 ? 1 : item.getQuantity();

                if (item.getSkuId() != null) {
                    ShoeSku sku = shoeSkuMapper.getById(item.getSkuId());
                    if (sku == null || sku.getStatus() == null || !sku.getStatus().equals(StatusConstant.ENABLE)) {
                        throw new OrderBusinessException("商品规格不存在或已下架");
                    }
                    ShoeSpu spu = shoeSpuMapper.getById(sku.getSpuId());
                    if (spu == null) {
                        throw new OrderBusinessException("商品不存在");
                    }
                    String shoeSize = item.getShoeSize();
                    if (shoeSize == null || shoeSize.isBlank()) {
                        throw new OrderBusinessException("鞋码不能为空");
                    }
                    cartList.add(ShoppingCart.builder()
                            .userId(userId)
                            .spuId(sku.getSpuId())
                            .skuId(sku.getId())
                            .shoeSize(shoeSize)
                            .number(quantity)
                            .amount(sku.getPrice())
                            .name(spu.getName() + " - " + sku.getColorName())
                            .image(sku.getImage())
                            .selected(1)
                            .createTime(LocalDateTime.now())
                            .build());
                    continue;
                }

                if (item.getBundleId() != null) {
                    Bundle bundle = bundleMapper.getInfoById(item.getBundleId());
                    if (bundle == null || bundle.getStatus() == null || !bundle.getStatus().equals(StatusConstant.ENABLE)) {
                        throw new OrderBusinessException("组合包不存在");
                    }
                    String shoeSize = item.getShoeSize();
                    if (shoeSize == null || shoeSize.isBlank()) {
                        throw new OrderBusinessException("鞋码不能为空");
                    }
                    cartList.add(ShoppingCart.builder()
                            .userId(userId)
                            .bundleId(bundle.getId())
                            .shoeSize(shoeSize)
                            .number(quantity)
                            .amount(bundle.getPrice())
                            .name(bundle.getName())
                            .image(bundle.getImage())
                            .selected(1)
                            .createTime(LocalDateTime.now())
                            .build());
                    continue;
                }

                throw new OrderBusinessException("商品参数错误");
            }
            return cartList;
        }

        ShoppingCart shoppingCartQuery = new ShoppingCart();
        shoppingCartQuery.setUserId(userId);
        shoppingCartQuery.setSelected(1);
        return shoppingCartMapper.list(shoppingCartQuery);
    }

    private void validatePurchaseLimits(Long userId, List<ShoppingCart> cartList) {
        if (userId == null || CollectionUtils.isEmpty(cartList)) {
            return;
        }

        for (ShoppingCart cart : cartList) {
            if (cart.getSkuId() == null) {
                continue;
            }

            Long skuId = cart.getSkuId();
            int limit = 5; // 默认限制5单

            // 查询是否是限量商品
            ShoeSku sku = shoeSkuMapper.getById(skuId);
            if (sku != null) {
                ShoeSpu spu = shoeSpuMapper.getById(sku.getSpuId());
                if (spu != null && spu.getIsLimited() != null && spu.getIsLimited() == 1) {
                    limit = 1; // 限量商品限1单
                }
            }

            // 使用分布式锁确保并发安全
            String lockKey = "lock:purchase:limit:" + userId + ":" + skuId;
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
                if (!locked) {
                    throw new OrderBusinessException("系统繁忙，请稍后再试");
                }

                try {
                    // 双重检查：查询已购买数量
                    Integer cnt = orderMapper.countActiveOrderByUserAndSku(userId, skuId);
                    int current = cnt == null ? 0 : cnt;

                    // 检查当前购物车数量
                    int cartQuantity = cart.getNumber() == null ? 0 : cart.getNumber();

                    if (current + cartQuantity > limit) {
                        throw new OrderBusinessException(limit == 1 ?
                                "限量鞋款仅限一人一单" :
                                "同款鞋每人最多购买" + limit + "单，您已购买" + current + "单");
                    }
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OrderBusinessException("系统繁忙，请稍后再试");
            }
        }

        // 检查组合包限制
        validateBundleLimits(userId, cartList);
    }

    private void validateBundleLimits(Long userId, List<ShoppingCart> cartList) {
        Set<Long> bundleIds = cartList.stream()
                .filter(c -> c.getBundleId() != null)
                .map(ShoppingCart::getBundleId)
                .collect(Collectors.toSet());

        for (Long bundleId : bundleIds) {
            String lockKey = "lock:purchase:bundle:" + userId + ":" + bundleId;
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
                if (!locked) {
                    throw new OrderBusinessException("系统繁忙，请稍后再试");
                }

                try {
                    Integer cnt = orderMapper.countActiveOrderByUserAndBundle(userId, bundleId);
                    int current = cnt == null ? 0 : cnt;

                    if (current >= 1) {
                        throw new OrderBusinessException("组合包每人最多购买1单");
                    }
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OrderBusinessException("系统繁忙，请稍后再试");
            }
        }
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return OrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .signType("RSA")
                .packageStr("prepay_id=mock_prepay_id")
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void paySuccess(String outTradeNo) {
        Orders orders = orderMapper.getByNumber(outTradeNo);
        if (orders == null) {
            log.warn("支付回调：订单不存在，orderNumber={}", outTradeNo);
            return;
        }

        // 幂等性检查：已支付的订单直接返回
        if (orders.getPayStatus() != null && orders.getPayStatus().equals(Orders.PAID)) {
            log.info("支付回调：订单已支付，跳过处理，orderNumber={}", outTradeNo);
            return;
        }

        // 检查订单状态是否允许支付
        if (orders.getStatus() == null || !orders.getStatus().equals(Orders.PENDING_PAYMENT)) {
            log.warn("支付回调：订单状态不允许支付，orderNumber={}, status={}", outTradeNo, orders.getStatus());
            return;
        }

        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(Orders.TO_BE_SHIPPED);
        update.setPayStatus(Orders.PAID);
        update.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(update);
        createOrderNotification(orders.getUserId(), outTradeNo, "订单支付成功，商家将尽快发货");

        log.info("支付回调：订单支付成功，orderNumber={}", outTradeNo);

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", 1);
        msg.put("orderId", orders.getId());
        msg.put("content", "新订单：" + orders.getNumber());
        String text = JSONObject.toJSONString(msg);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    webSocketServer.sendToAllClient(text);
                }
            });
        } else {
            webSocketServer.sendToAllClient(text);
        }
    }

    @Override
    public PageResult page(Integer page, Integer pageSize, Integer status, List<Integer> statusList) {
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setStatus(status);
        dto.setStatusList(statusList);
        dto.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> orderPage = orderMapper.pageQuery(dto);
        List<UserOrderVO> list = orderPage.getResult().stream()
                .map(this::buildUserOrderVO)
                .collect(Collectors.toList());
        return new PageResult(orderPage.getTotal(), list);
    }

    @Override
    public UserOrderDetailVO getOrderDetailByNumber(String orderNumber) {
        Orders orders = getUserOrderByNumber(orderNumber);
        return buildUserOrderDetailVO(orders);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelByNumber(String orderNumber) {
        Orders orders = getUserOrderByNumber(orderNumber);
        Long id = orders.getId();
        if (orders.getStatus() == null
                || (orders.getStatus() != Orders.PENDING_PAYMENT && orders.getStatus() != Orders.TO_BE_SHIPPED)) {
            throw new OrderBusinessException("订单状态错误");
        }

        // 获取订单详情，用于恢复购物车
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(List.of(id));

        Orders update = new Orders();
        update.setId(id);
        update.setStatus(Orders.CANCELLED);
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason("用户取消");
        orderMapper.update(update);

        // 回滚库存
        stockReservationService.rollback(orders.getNumber(), orders.getUserId());

        // 恢复购物车
        if (!CollectionUtils.isEmpty(orderDetails)) {
            restoreShoppingCart(orders.getUserId(), orderDetails);
        }
    }

    private void restoreShoppingCart(Long userId, List<OrderDetail> orderDetails) {
        try {
            List<ShoppingCart> carts = orderDetails.stream()
                    .filter(d -> d.getSkuId() != null)
                    .map(d -> {
                        ShoppingCart cart = new ShoppingCart();
                        cart.setUserId(userId);
                        cart.setName(d.getName());
                        cart.setImage(d.getImage());
                        cart.setSkuId(d.getSkuId());
                        cart.setBundleId(d.getBundleId());
                        cart.setShoeSize(d.getShoeSize());
                        cart.setNumber(d.getNumber());
                        cart.setAmount(d.getAmount());
                        cart.setSelected(1); // 默认选中
                        cart.setCreateTime(LocalDateTime.now());
                        return cart;
                    })
                    .collect(Collectors.toList());

            if (!carts.isEmpty()) {
                shoppingCartMapper.insertBatch(carts);
                log.info("订单取消后恢复购物车成功，userId={}, orderId={}, 恢复商品数={}",
                        userId, orderDetails.get(0).getOrderId(), carts.size());
            }
        } catch (Exception e) {
            // 恢复购物车失败不影响取消订单主流程，只记录日志
            log.error("订单取消后恢复购物车失败，userId={}, orderId={}",
                    userId, orderDetails.get(0).getOrderId(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void repetitionByNumber(String orderNumber) {
        Orders orders = getUserOrderByNumber(orderNumber);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(List.of(orders.getId()));
        if (CollectionUtils.isEmpty(orderDetails)) {
            return;
        }
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> carts = orderDetails.stream().map(d -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setName(d.getName());
            cart.setImage(d.getImage());
            cart.setSkuId(d.getSkuId());
            cart.setBundleId(d.getBundleId());
            cart.setShoeSize(d.getShoeSize());
            cart.setNumber(d.getNumber());
            cart.setAmount(d.getAmount());
            cart.setCreateTime(LocalDateTime.now());
            cart.setSelected(1);
            return cart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(carts);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = page.getResult().stream().map(this::buildOrderVO).collect(Collectors.toList());
        return new PageResult(page.getTotal(), list);
    }

    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.statistics();
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        return buildOrderVO(orders);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        if (orders.getStatus() == null || !orders.getStatus().equals(Orders.TO_BE_SHIPPED)) {
            throw new OrderBusinessException("订单状态不支持发货");
        }
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(Orders.SHIPPED);
        orderMapper.update(update);

        Logistics logistics = Logistics.builder()
                .orderNo(orders.getNumber())
                .expressCompany("顺丰")
                .expressNo("SF" + orders.getId())
                .status(Orders.SHIPPED)
                .currentLocation("仓库")
                .receiverName(orders.getConsignee())
                .receiverPhone(orders.getPhone())
                .receiverAddress(orders.getAddress())
                .senderName("鞋宙仓库")
                .senderPhone("")
                .senderAddress("仓库")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        logisticsMapper.insert(logistics);

        LocalDateTime now = LocalDateTime.now();
        insertLogisticsTrace(logistics.getId(), Orders.SHIPPED, "仓库", "订单已出库", "系统模拟", now.minusMinutes(5));
        insertLogisticsTrace(logistics.getId(), Orders.SHIPPED, "仓库", "快件已揽收", "系统模拟", now.minusMinutes(2));
        insertLogisticsTrace(logistics.getId(), Orders.SHIPPED, "仓库", "已发货", "系统", now);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(Orders.CANCELLED);
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(update);
        stockReservationService.rollback(orders.getNumber(), orders.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrders(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(Orders.CANCELLED);
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(update);
        stockReservationService.rollback(orders.getNumber(), orders.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delivery(Long id) {
        updateOrderStatusAndLogistics(id, Orders.IN_TRANSIT, "运输中", "运输中");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void outForDelivery(Long id) {
        updateOrderStatusAndLogistics(id, Orders.OUT_FOR_DELIVERY, "派送中", "派送中");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void complete(Long id) {
        updateOrderStatusAndLogistics(id, Orders.SIGNED, "已签收", "已签收");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void confirmReceiptByNumber(String orderNumber) {
        Orders orders = getUserOrderByNumber(orderNumber);
        if (orders.getStatus() == null) {
            throw new OrderBusinessException("订单状态异常");
        }
        if (Orders.CANCELLED.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单已取消");
        }
        if (Orders.SIGNED.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单已签收，请勿重复操作");
        }
        if (orders.getPayStatus() == null || !Orders.PAID.equals(orders.getPayStatus())) {
            throw new OrderBusinessException("订单未支付");
        }
        if (!Orders.SHIPPED.equals(orders.getStatus())
                && !Orders.IN_TRANSIT.equals(orders.getStatus())
                && !Orders.OUT_FOR_DELIVERY.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单未发货");
        }
        updateOrderStatusAndLogistics(orders.getId(), Orders.SIGNED, "已签收", "用户确认收货");
    }

    @Override
    public void reminderByNumber(String orderNumber) {
        Orders orders = getUserOrderByNumber(orderNumber);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", 2);
        msg.put("orderId", orders.getId());
        msg.put("content", "催单：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSONObject.toJSONString(msg));
    }

    private Orders getUserOrderByNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new OrderBusinessException("订单不存在");
        }
        Orders orders = orderMapper.getByNumber(orderNumber);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        if (orders.getUserId() != null && !orders.getUserId().equals(BaseContext.getCurrentId())) {
            throw new OrderBusinessException("无权操作该订单");
        }
        return orders;
    }

    private UserOrderVO buildUserOrderVO(Orders orders) {
        UserOrderVO vo = new UserOrderVO();
        vo.setNumber(orders.getNumber());
        vo.setOrderType(orders.getOrderType());
        vo.setStatus(orders.getStatus());
        vo.setPayMethod(orders.getPayMethod());
        vo.setPayStatus(orders.getPayStatus());
        vo.setAmount(orders.getAmount());
        vo.setRemark(orders.getRemark());
        vo.setPhone(orders.getPhone());
        vo.setAddress(orders.getAddress());
        vo.setConsignee(orders.getConsignee());
        vo.setOrderTime(orders.getOrderTime());
        vo.setCheckoutTime(orders.getCheckoutTime());
        vo.setCancelReason(orders.getCancelReason());
        vo.setCancelTime(orders.getCancelTime());

        List<OrderDetail> details = orderDetailMapper.getByOrderId(List.of(orders.getId()));
        if (details == null) {
            details = List.of();
        }
        vo.setOrderDetailList(details.stream().map(this::toUserOrderItem).collect(Collectors.toList()));
        vo.setOrderDishes(buildOrderDishes(details));
        return vo;
    }

    private UserOrderDetailVO buildUserOrderDetailVO(Orders orders) {
        UserOrderVO base = buildUserOrderVO(orders);
        UserOrderDetailVO vo = new UserOrderDetailVO();
        BeanUtils.copyProperties(base, vo);

        Logistics logistics = logisticsMapper.getByOrderNo(orders.getNumber());
        if (logistics != null) {
            vo.setLogistics(toUserLogistics(logistics));
            List<LogisticsTrace> traceList = logisticsTraceMapper.listByLogisticsId(logistics.getId());
            List<UserLogisticsTraceVO> traces = traceList == null
                    ? List.of()
                    : traceList.stream().map(this::toUserLogisticsTrace).collect(Collectors.toList());
            vo.setLogisticsTraceList(traces);
        }
        return vo;
    }

    private UserOrderItemVO toUserOrderItem(OrderDetail detail) {
        if (detail == null) {
            return null;
        }
        return UserOrderItemVO.builder()
                .name(detail.getName())
                .spuId(detail.getSpuId())
                .skuId(detail.getSkuId())
                .bundleId(detail.getBundleId())
                .shoeSize(detail.getShoeSize())
                .number(detail.getNumber())
                .amount(detail.getAmount())
                .image(detail.getImage())
                .build();
    }

    private UserLogisticsVO toUserLogistics(Logistics logistics) {
        if (logistics == null) {
            return null;
        }
        return UserLogisticsVO.builder()
                .orderNo(logistics.getOrderNo())
                .expressCompany(logistics.getExpressCompany())
                .expressNo(logistics.getExpressNo())
                .status(logistics.getStatus())
                .currentLocation(logistics.getCurrentLocation())
                .receiverName(logistics.getReceiverName())
                .receiverPhone(logistics.getReceiverPhone())
                .receiverAddress(logistics.getReceiverAddress())
                .senderName(logistics.getSenderName())
                .senderPhone(logistics.getSenderPhone())
                .senderAddress(logistics.getSenderAddress())
                .estimatedTime(logistics.getEstimatedTime())
                .actualTime(logistics.getActualTime())
                .createTime(logistics.getCreateTime())
                .updateTime(logistics.getUpdateTime())
                .build();
    }

    private UserLogisticsTraceVO toUserLogisticsTrace(LogisticsTrace trace) {
        if (trace == null) {
            return null;
        }
        return UserLogisticsTraceVO.builder()
                .status(trace.getStatus())
                .location(trace.getLocation())
                .description(trace.getDescription())
                .operator(trace.getOperator())
                .operateTime(trace.getOperateTime())
                .createTime(trace.getCreateTime())
                .build();
    }

    private OrderVO buildOrderVO(Orders orders) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders, vo);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(List.of(orders.getId()));
        vo.setOrderDetailList(details);
        vo.setOrderDishes(buildOrderDishes(details));

        Logistics logistics = logisticsMapper.getByOrderNo(orders.getNumber());
        if (logistics != null) {
            vo.setLogistics(logistics);
            vo.setLogisticsTraceList(logisticsTraceMapper.listByLogisticsId(logistics.getId()));
        }
        return vo;
    }

    private String buildOrderDishes(List<OrderDetail> details) {
        if (CollectionUtils.isEmpty(details)) {
            return "";
        }
        return details.stream()
                .map(d -> d.getName() + "*" + (d.getNumber() == null ? 0 : d.getNumber()))
                .collect(Collectors.joining(";"));
    }

    private void insertLogisticsTrace(Long logisticsId, Integer status, String location, String description, String operator, LocalDateTime operateTime) {
        LogisticsTrace trace = LogisticsTrace.builder()
                .logisticsId(logisticsId)
                .status(status)
                .location(location)
                .description(description)
                .operator(operator)
                .operateTime(operateTime)
                .createTime(LocalDateTime.now())
                .build();
        logisticsTraceMapper.insert(trace);
    }

    private void updateOrderStatusAndLogistics(Long orderId, Integer status, String location, String description) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        Orders update = new Orders();
        update.setId(orderId);
        update.setStatus(status);
        orderMapper.update(update);

        Logistics logistics = logisticsMapper.getByOrderNo(orders.getNumber());
        if (logistics == null) {
            return;
        }
        Logistics logisticsUpdate = Logistics.builder()
                .id(logistics.getId())
                .status(status)
                .currentLocation(location)
                .updateTime(LocalDateTime.now())
                .build();
        if (status != null && status.equals(Orders.SIGNED)) {
            logisticsUpdate.setActualTime(LocalDateTime.now());
        }
        logisticsMapper.update(logisticsUpdate);

        LogisticsTrace trace = LogisticsTrace.builder()
                .logisticsId(logistics.getId())
                .status(status)
                .location(location)
                .description(description)
                .operator("系统")
                .operateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();
        logisticsTraceMapper.insert(trace);

        if (status != null && (status.equals(Orders.SHIPPED) || status.equals(Orders.IN_TRANSIT) || status.equals(Orders.OUT_FOR_DELIVERY))) {
            delayQueueService.schedule(
                    DelayTaskTypeConstant.ORDER_AUTO_CONFIRM,
                    orders.getNumber(),
                    System.currentTimeMillis() + Duration.ofDays(7).toMillis(),
                    null
            );
        }
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

    @Override
    public UserOrderStatisticsVO userStatistics(Long userId) {
        UserOrderStatisticsVO vo = orderMapper.countByUserId(userId);
        if (vo == null) {
            vo = new UserOrderStatisticsVO();
            vo.setToBePaid(0);
            vo.setToBeShipped(0);
            vo.setToBeReceived(0);
            vo.setToBeReviewed(0);
        }
        return vo;
    }
}
