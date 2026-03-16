package com.su.service;

import com.su.constant.RedisKeyConstant;
import com.su.entity.User;
import com.su.mapper.UserMapper;
import com.su.service.impl.UserServiceImpl;
import com.su.utils.cache.CacheClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceStatisticsTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CacheClient cacheClient;

    @InjectMocks
    private UserServiceImpl userService;

    private Long testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testUser = User.builder()
                .id(testUserId)
                .name("测试用户")
                .phone("13800138000")
                .followerCount(10)
                .followingCount(5)
                .likedCount(20)
                .postCount(3)
                .build();
    }

    @Test
    void testUpdateFollowerCount_Success() {
        userService.updateFollowerCount(testUserId, 1);

        verify(userMapper, times(1)).updateFollowerCount(testUserId, 1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateFollowerCount_Decrement() {
        userService.updateFollowerCount(testUserId, -1);

        verify(userMapper, times(1)).updateFollowerCount(testUserId, -1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateFollowerCount_InvalidParameters() {
        userService.updateFollowerCount(null, 1);
        userService.updateFollowerCount(testUserId, null);
        userService.updateFollowerCount(testUserId, 0);

        verify(userMapper, never()).updateFollowerCount(anyLong(), anyInt());
        verify(cacheClient, never()).evict(anyString());
    }

    @Test
    void testUpdateFollowingCount_Success() {
        userService.updateFollowingCount(testUserId, 1);

        verify(userMapper, times(1)).updateFollowingCount(testUserId, 1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateFollowingCount_Decrement() {
        userService.updateFollowingCount(testUserId, -1);

        verify(userMapper, times(1)).updateFollowingCount(testUserId, -1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateFollowingCount_InvalidParameters() {
        userService.updateFollowingCount(null, 1);
        userService.updateFollowingCount(testUserId, null);
        userService.updateFollowingCount(testUserId, 0);

        verify(userMapper, never()).updateFollowingCount(anyLong(), anyInt());
        verify(cacheClient, never()).evict(anyString());
    }

    @Test
    void testUpdateLikedCount_Success() {
        userService.updateLikedCount(testUserId, 1);

        verify(userMapper, times(1)).updateLikedCount(testUserId, 1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateLikedCount_Decrement() {
        userService.updateLikedCount(testUserId, -1);

        verify(userMapper, times(1)).updateLikedCount(testUserId, -1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdateLikedCount_InvalidParameters() {
        userService.updateLikedCount(null, 1);
        userService.updateLikedCount(testUserId, null);
        userService.updateLikedCount(testUserId, 0);

        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
        verify(cacheClient, never()).evict(anyString());
    }

    @Test
    void testUpdatePostCount_Success() {
        userService.updatePostCount(testUserId, 1);

        verify(userMapper, times(1)).updatePostCount(testUserId, 1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdatePostCount_Decrement() {
        userService.updatePostCount(testUserId, -1);

        verify(userMapper, times(1)).updatePostCount(testUserId, -1);
        verify(cacheClient, times(1)).evict(anyString());
    }

    @Test
    void testUpdatePostCount_InvalidParameters() {
        userService.updatePostCount(null, 1);
        userService.updatePostCount(testUserId, null);
        userService.updatePostCount(testUserId, 0);

        verify(userMapper, never()).updatePostCount(anyLong(), anyInt());
        verify(cacheClient, never()).evict(anyString());
    }

    @Test
    void testGetById_Success() {
        when(userMapper.getById(testUserId)).thenReturn(testUser);

        User result = userService.getById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals(10, result.getFollowerCount());
        assertEquals(5, result.getFollowingCount());
        assertEquals(20, result.getLikedCount());
        assertEquals(3, result.getPostCount());
    }

    @Test
    void testGetById_NotFound() {
        when(userMapper.getById(testUserId)).thenReturn(null);

        User result = userService.getById(testUserId);

        assertNull(result);
    }
}

