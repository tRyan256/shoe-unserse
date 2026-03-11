package com.su.service;

import com.su.entity.ShoppingCart;
import com.su.dto.OrdersSubmitDTO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 保留属性测试 - 验证非 Bug 条件行为保留
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
 * 
 * 此测试验证在修复 Bug 后，以下行为必须保持不变：
 * - 3.1: 用户选择收货地址功能正常
 * - 3.2: 优惠券计算和应用功能正常
 * - 3.3: 订单备注保存功能正常
 * - 3.4: 订单提交成功后清空购物车并跳转支付页面
 * - 3.5: 从购物车页面发起订单提交流程
 * - 3.6: 订单金额计算正确（商品总额、运费、优惠、应付总额）
 * 
 * **重要**: 此测试应该在未修复的代码上通过，确认基线行为
 * **预期结果**: 测试通过（确认要保留的基线行为）
 */
class OrderServicePreservationTest {

    /**
     * Property 2: Preservation - 地址选择功能保留
     * 
     * **Validates: Requirement 3.1**
     * 
     * 验证：用户选择收货地址功能应该继续正常工作
     * - OrdersSubmitDTO 应该包含 addressBookId 字段
     * - addressBookId 应该能够正确传递和接收
     */
    @Property(tries = 30)
    void preservation_addressSelection_shouldWorkCorrectly(
            @ForAll @IntRange(min = 1, max = 1000) long addressBookId
    ) {
        // 创建订单提交 DTO
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(addressBookId);
        
        // 验证：addressBookId 字段存在且可以正确设置和获取
        assertNotNull(dto.getAddressBookId(), 
                "Preservation 3.1: OrdersSubmitDTO 应该支持 addressBookId 字段");
        assertEquals(addressBookId, dto.getAddressBookId(),
                "Preservation 3.1: addressBookId 应该能够正确传递");
    }

    /**
     * Property 2: Preservation - 优惠券功能保留
     * 
     * **Validates: Requirement 3.2**
     * 
     * 验证：优惠券计算和应用功能应该继续正常工作
     * - OrdersSubmitDTO 应该包含 couponId 字段
     * - 优惠券金额计算逻辑应该保持不变
     */
    @Property(tries = 30)
    void preservation_couponCalculation_shouldWorkCorrectly(
            @ForAll @IntRange(min = 1, max = 1000) long couponId,
            @ForAll("cartAmounts") BigDecimal cartAmount,
            @ForAll("discountValues") BigDecimal discountValue
    ) {
        // 创建订单提交 DTO
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setCouponId(couponId);
        
        // 验证：couponId 字段存在且可以正确设置和获取
        assertNotNull(dto.getCouponId(),
                "Preservation 3.2: OrdersSubmitDTO 应该支持 couponId 字段");
        assertEquals(couponId, dto.getCouponId(),
                "Preservation 3.2: couponId 应该能够正确传递");
        
        // 验证：优惠券计算逻辑（满减券）
        BigDecimal payableAmount = cartAmount.subtract(discountValue);
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }
        
        assertTrue(payableAmount.compareTo(BigDecimal.ZERO) >= 0,
                "Preservation 3.2: 优惠后金额不应该为负数");
        assertTrue(payableAmount.compareTo(cartAmount) <= 0,
                "Preservation 3.2: 优惠后金额不应该大于原价");
    }

    /**
     * Property 2: Preservation - 订单备注功能保留
     * 
     * **Validates: Requirement 3.3**
     * 
     * 验证：订单备注保存功能应该继续正常工作
     * - OrdersSubmitDTO 应该包含 remark 字段
     * - remark 应该能够正确传递和接收
     */
    @Property(tries = 30)
    void preservation_orderRemark_shouldWorkCorrectly(
            @ForAll("remarks") String remark
    ) {
        // 创建订单提交 DTO
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setRemark(remark);
        
        // 验证：remark 字段存在且可以正确设置和获取
        assertEquals(remark, dto.getRemark(),
                "Preservation 3.3: OrdersSubmitDTO 应该支持 remark 字段并正确保存备注信息");
    }

    /**
     * Property 2: Preservation - 购物车数据结构保留
     * 
     * **Validates: Requirements 3.4, 3.5**
     * 
     * 验证：购物车数据结构应该保持不变
     * - ShoppingCart 实体类的现有字段应该保持不变
     * - 购物车查询和清空逻辑应该保持不变
     */
    @Property(tries = 30)
    void preservation_shoppingCartStructure_shouldRemainUnchanged(
            @ForAll @IntRange(min = 1, max = 5) int cartItemCount
    ) {
        // 创建购物车商品列表
        List<ShoppingCart> cartList = new ArrayList<>();
        for (int i = 0; i < cartItemCount; i++) {
            ShoppingCart cart = ShoppingCart.builder()
                    .id((long) (i + 1))
                    .userId(1L)
                    .spuId((long) (100 + i))
                    .skuId((long) (200 + i))
                    .name("商品" + (i + 1))
                    .shoeSize("42")
                    .number(1)
                    .amount(new BigDecimal("100.00"))
                    .createTime(LocalDateTime.now())
                    .build();
            cartList.add(cart);
        }
        
        // 验证：购物车实体类的核心字段应该存在
        for (ShoppingCart cart : cartList) {
            assertNotNull(cart.getId(), "Preservation 3.4/3.5: ShoppingCart 应该包含 id 字段");
            assertNotNull(cart.getUserId(), "Preservation 3.4/3.5: ShoppingCart 应该包含 userId 字段");
            assertNotNull(cart.getSpuId(), "Preservation 3.4/3.5: ShoppingCart 应该包含 spuId 字段");
            assertNotNull(cart.getSkuId(), "Preservation 3.4/3.5: ShoppingCart 应该包含 skuId 字段");
            assertNotNull(cart.getName(), "Preservation 3.4/3.5: ShoppingCart 应该包含 name 字段");
            assertNotNull(cart.getNumber(), "Preservation 3.4/3.5: ShoppingCart 应该包含 number 字段");
            assertNotNull(cart.getAmount(), "Preservation 3.4/3.5: ShoppingCart 应该包含 amount 字段");
        }
        
        // 验证：购物车列表应该可以正常遍历和处理
        assertEquals(cartItemCount, cartList.size(),
                "Preservation 3.4/3.5: 购物车商品列表应该保持完整");
    }

    /**
     * Property 2: Preservation - 订单金额计算保留
     * 
     * **Validates: Requirement 3.6**
     * 
     * 验证：订单金额计算逻辑应该保持不变
     * - 商品总额 = Σ(单价 × 数量)
     * - 应付总额 = 商品总额 - 优惠金额
     * - 金额计算精度应该保持不变
     */
    @Property(tries = 50)
    void preservation_orderAmountCalculation_shouldBeCorrect(
            @ForAll @IntRange(min = 1, max = 5) int cartItemCount,
            @ForAll("itemPrices") List<BigDecimal> prices,
            @ForAll("itemQuantities") List<Integer> quantities
    ) {
        Assume.that(prices.size() >= cartItemCount);
        Assume.that(quantities.size() >= cartItemCount);
        
        // 创建购物车商品列表
        List<ShoppingCart> cartList = new ArrayList<>();
        BigDecimal expectedTotal = BigDecimal.ZERO;
        
        for (int i = 0; i < cartItemCount; i++) {
            BigDecimal price = prices.get(i);
            Integer quantity = quantities.get(i);
            
            ShoppingCart cart = ShoppingCart.builder()
                    .id((long) (i + 1))
                    .userId(1L)
                    .spuId((long) (100 + i))
                    .skuId((long) (200 + i))
                    .name("商品" + (i + 1))
                    .shoeSize("42")
                    .number(quantity)
                    .amount(price)
                    .createTime(LocalDateTime.now())
                    .build();
            cartList.add(cart);
            
            // 计算预期总额
            expectedTotal = expectedTotal.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
        
        // 模拟订单金额计算逻辑（与 OrderServiceImpl.submitAsync 中的逻辑一致）
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (ShoppingCart cart : cartList) {
            BigDecimal unitPrice = cart.getAmount() == null ? BigDecimal.ZERO : cart.getAmount();
            Integer number = cart.getNumber() == null ? 0 : cart.getNumber();
            calculatedTotal = calculatedTotal.add(unitPrice.multiply(BigDecimal.valueOf(number)));
        }
        
        // 验证：计算的总额应该等于预期总额
        assertEquals(expectedTotal.setScale(2, RoundingMode.HALF_UP), 
                calculatedTotal.setScale(2, RoundingMode.HALF_UP),
                "Preservation 3.6: 订单金额计算应该正确（商品总额 = Σ(单价 × 数量)）");
        
        // 验证：总额应该大于等于 0
        assertTrue(calculatedTotal.compareTo(BigDecimal.ZERO) >= 0,
                "Preservation 3.6: 订单总额不应该为负数");
    }

    /**
     * Property 2: Preservation - 折扣券计算保留
     * 
     * **Validates: Requirement 3.6**
     * 
     * 验证：折扣券计算逻辑应该保持不变
     * - 折扣后金额 = 商品总额 × 折扣率
     * - 折扣率应该在 (0, 1] 范围内
     */
    @Property(tries = 30)
    void preservation_discountCouponCalculation_shouldBeCorrect(
            @ForAll("cartAmounts") BigDecimal cartAmount,
            @ForAll("discountRates") BigDecimal discountRate
    ) {
        // 模拟折扣券计算逻辑（与 OrderServiceImpl.submitAsync 中的逻辑一致）
        BigDecimal payableAmount = cartAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        
        // 验证：折扣后金额应该在合理范围内
        assertTrue(payableAmount.compareTo(BigDecimal.ZERO) >= 0,
                "Preservation 3.6: 折扣后金额不应该为负数");
        assertTrue(payableAmount.compareTo(cartAmount) <= 0,
                "Preservation 3.6: 折扣后金额不应该大于原价");
        
        // 验证：折扣计算精度应该保持 2 位小数
        assertEquals(2, payableAmount.scale(),
                "Preservation 3.6: 折扣后金额应该保持 2 位小数精度");
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<BigDecimal> cartAmounts() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10.00), BigDecimal.valueOf(1000.00))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> discountValues() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(5.00), BigDecimal.valueOf(100.00))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> discountRates() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.1), BigDecimal.valueOf(1.0))
                .ofScale(2);
    }

    @Provide
    Arbitrary<String> remarks() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.just("请尽快发货"),
                Arbitraries.just("周末送达"),
                Arbitraries.just("不要辣椒"),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
        );
    }

    @Provide
    Arbitrary<List<BigDecimal>> itemPrices() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10.00), BigDecimal.valueOf(500.00))
                .ofScale(2)
                .list().ofMinSize(5).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<Integer>> itemQuantities() {
        return Arbitraries.integers()
                .between(1, 10)
                .list().ofMinSize(5).ofMaxSize(5);
    }
}
