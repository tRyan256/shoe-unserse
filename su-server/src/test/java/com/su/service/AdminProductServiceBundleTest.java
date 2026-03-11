package com.su.service;

import com.su.dto.admin.BundleSaveDTO;
import com.su.entity.Bundle;
import com.su.mapper.BundleMapper;
import com.su.mapper.BundleShoeMapper;
import com.su.service.impl.AdminProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminProductService组合包管理方法测试
 */
@ExtendWith(MockitoExtension.class)
class AdminProductServiceBundleTest {

    @Mock
    private BundleMapper bundleMapper;

    @Mock
    private BundleShoeMapper bundleShoeMapper;

    @InjectMocks
    private AdminProductServiceImpl adminProductService;

    private BundleSaveDTO bundleSaveDTO;
    private Bundle bundle;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        bundleSaveDTO = BundleSaveDTO.builder()
                .name("测试组合包")
                .price(new BigDecimal("999.00"))
                .description("测试描述")
                .image("test.jpg")
                .status(1)
                .items(Arrays.asList(
                        BundleSaveDTO.BundleItemDTO.builder()
                                .skuId(1L)
                                .name("商品1")
                                .price(new BigDecimal("500.00"))
                                .copies(2)
                                .build(),
                        BundleSaveDTO.BundleItemDTO.builder()
                                .skuId(2L)
                                .name("商品2")
                                .price(new BigDecimal("300.00"))
                                .copies(1)
                                .build()
                ))
                .build();

        bundle = Bundle.builder()
                .id(1L)
                .name("测试组合包")
                .price(new BigDecimal("999.00"))
                .description("测试描述")
                .image("test.jpg")
                .status(1)
                .build();
    }

    @Test
    void testUpdateBundle() {
        // Given
        Long bundleId = 1L;

        // When
        adminProductService.updateBundle(bundleId, bundleSaveDTO);

        // Then
        verify(bundleMapper, times(1)).update(any(Bundle.class));
        verify(bundleShoeMapper, times(1)).deleteByBundleId(Collections.singletonList(bundleId));
        verify(bundleShoeMapper, times(1)).insertBatch(anyList());
    }

    @Test
    void testUpdateBundleWithoutItems() {
        // Given
        Long bundleId = 1L;
        BundleSaveDTO dtoWithoutItems = BundleSaveDTO.builder()
                .name("测试组合包")
                .price(new BigDecimal("999.00"))
                .description("测试描述")
                .image("test.jpg")
                .status(1)
                .items(null)
                .build();

        // When
        adminProductService.updateBundle(bundleId, dtoWithoutItems);

        // Then
        verify(bundleMapper, times(1)).update(any(Bundle.class));
        verify(bundleShoeMapper, times(1)).deleteByBundleId(Collections.singletonList(bundleId));
        verify(bundleShoeMapper, never()).insertBatch(anyList());
    }

    @Test
    void testGetBundleList() {
        // Given
        List<Bundle> expectedBundles = Arrays.asList(bundle);
        when(bundleMapper.list(null)).thenReturn(expectedBundles);

        // When
        List<Bundle> result = adminProductService.getBundleList();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试组合包", result.get(0).getName());
        verify(bundleMapper, times(1)).list(null);
    }

    @Test
    void testToggleBundleStatus() {
        // Given
        Long bundleId = 1L;
        Bundle existingBundle = Bundle.builder()
                .id(bundleId)
                .name("测试组合包")
                .status(0)
                .build();
        when(bundleMapper.getInfoById(bundleId)).thenReturn(existingBundle);

        // When
        adminProductService.toggleBundleStatus(bundleId);

        // Then
        verify(bundleMapper, times(1)).getInfoById(bundleId);
        verify(bundleMapper, times(1)).startOrStop(1, bundleId);
    }

    @Test
    void testToggleBundleStatusFromEnabledToDisabled() {
        // Given
        Long bundleId = 1L;
        Bundle existingBundle = Bundle.builder()
                .id(bundleId)
                .name("测试组合包")
                .status(1)
                .build();
        when(bundleMapper.getInfoById(bundleId)).thenReturn(existingBundle);

        // When
        adminProductService.toggleBundleStatus(bundleId);

        // Then
        verify(bundleMapper, times(1)).getInfoById(bundleId);
        verify(bundleMapper, times(1)).startOrStop(0, bundleId);
    }

    @Test
    void testToggleBundleStatusBundleNotFound() {
        // Given
        Long bundleId = 1L;
        when(bundleMapper.getInfoById(bundleId)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminProductService.toggleBundleStatus(bundleId);
        });

        assertTrue(exception.getMessage().contains("组合包不存在"));
        verify(bundleMapper, times(1)).getInfoById(bundleId);
        verify(bundleMapper, never()).startOrStop(anyInt(), anyLong());
    }
}
