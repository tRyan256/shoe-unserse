package com.su.service;

import com.su.entity.ShoppingCart;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition 探索测试 - 购物车选中状态缺失导致订单包含所有商品
 * 
 * **Validates: Requirements 1.2, 1.3, 1.4**
 * 
 * 此测试验证 Bug Condition：
 * - 购物车表缺少 selected 字段
 * - OrderServiceImpl 查询所有购物车商品，不区分选中状态
 * - 订单包含用户未选中的商品
 * 
 * **关键**: 此测试必须在未修复的代码上失败 - 失败确认 bug 存在
 * **预期结果**: 测试失败（这是正确的 - 证明 bug 存在）
 * 
 * 测试策略：
 * 1. 验证 ShoppingCart 实体类是否有 selected 字段
 * 2. 如果没有 selected 字段，则无法区分选中和未选中的商品
 * 3. 这意味着订单提交时会包含所有购物车商品，而不仅仅是用户选中的商品
 */
class OrderServiceBugConditionTest {

    /**
     * Property 1: Fault Condition - 购物车缺少选中状态导致订单包含所有商品
     * 
     * 测试场景：
     * 1. 用户购物车中有多个商品
     * 2. 用户只想购买其中部分商品（模拟"立即购买"场景）
     * 3. 由于缺少 selected 字段，后端无法区分哪些商品是用户选中的
     * 4. 结果：订单包含了所有购物车商品，而不仅仅是用户选中的商品
     * 
     * **Scoped PBT 方法**: 对于确定性 bug，将属性范围限定为具体失败案例以确保可重现性
     * 
     * 预期行为：ShoppingCart 应该有 selected 字段来标记选中状态
     * 实际行为（Bug）：ShoppingCart 没有 selected 字段
     */
    @Property(tries = 50)
    void bugCondition_shoppingCartMissingSelectedField_cannotDistinguishSelectedItems(
            @ForAll @IntRange(min = 2, max = 5) int totalCartItems,
            @ForAll @IntRange(min = 1, max = 3) int selectedItemsCount
    ) {
        // Ensure selectedItemsCount is less than totalCartItems
        Assume.that(selectedItemsCount < totalCartItems);
        
        // 准备测试数据：购物车中有多个商品
        List<ShoppingCart> allCartItems = new ArrayList<>();
        for (int i = 0; i < totalCartItems; i++) {
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
            allCartItems.add(cart);
        }
        
        // **Bug Condition 验证 1**: 检查 ShoppingCart 是否有 selected 字段
        boolean hasSelectedField = hasField(ShoppingCart.class, "selected");
        
        // 如果没有 selected 字段，则无法标记哪些商品是用户选中的
        // 这意味着在订单提交时，后端无法区分选中和未选中的商品
        assertTrue(hasSelectedField,
                String.format("Bug Condition: ShoppingCart 实体类缺少 'selected' 字段。" +
                        "这导致无法区分用户选中的 %d 个商品和未选中的 %d 个商品。" +
                        "结果：订单会包含所有 %d 个购物车商品，而不仅仅是用户选中的商品。",
                        selectedItemsCount, totalCartItems - selectedItemsCount, totalCartItems));
        
        // **Bug Condition 验证 2**: 如果有 selected 字段，验证是否可以正确过滤
        if (hasSelectedField) {
            // 模拟：用户只选中了部分商品
            List<ShoppingCart> selectedItems = allCartItems.subList(0, selectedItemsCount);
            List<ShoppingCart> unselectedItems = allCartItems.subList(selectedItemsCount, totalCartItems);
            
            // 尝试设置 selected 字段
            for (ShoppingCart item : selectedItems) {
                setFieldValue(item, "selected", 1);
            }
            for (ShoppingCart item : unselectedItems) {
                setFieldValue(item, "selected", 0);
            }
            
            // 模拟后端查询：只获取 selected=1 的商品
            List<ShoppingCart> filteredItems = allCartItems.stream()
                    .filter(item -> {
                        Object selected = getFieldValue(item, "selected");
                        return selected != null && selected.equals(1);
                    })
                    .collect(Collectors.toList());
            
            // 验证：过滤后的商品数量应该等于用户选中的商品数量
            assertEquals(selectedItemsCount, filteredItems.size(),
                    String.format("订单应该只包含 %d 个选中的商品，但实际包含了 %d 个商品",
                            selectedItemsCount, filteredItems.size()));
        }
    }

    /**
     * 检查类是否有指定名称的字段
     */
    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 设置对象的字段值（使用反射）
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            // 字段不存在或无法设置
        }
    }

    /**
     * 获取对象的字段值（使用反射）
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
