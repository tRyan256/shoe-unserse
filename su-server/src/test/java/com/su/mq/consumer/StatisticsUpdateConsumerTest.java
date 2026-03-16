package com.su.mq.consumer;

import com.su.dto.message.MessageType;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.mapper.UserMapper;
import com.su.utils.cache.CacheClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsUpdateConsumerTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private CacheClient cacheClient;
    
    private StatisticsUpdateConsumer consumer;
    
    @BeforeEach
    void setUp() {
        consumer = new StatisticsUpdateConsumer(userMapper, cacheClient);
    }
    
    @Test
    void testHandleMessage_PostLike_Success() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.POST_LIKE)
                .userId(1L)
                .targetUserId(2L)
                .postId(100L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(cacheClient, times(1)).exists(anyString());
        verify(userMapper, times(1)).updateLikedCount(2L, 1);
        verify(cacheClient, times(1)).evict(anyString());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_PostUnlike_Success() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.POST_UNLIKE)
                .userId(1L)
                .targetUserId(2L)
                .postId(100L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, times(1)).updateLikedCount(2L, -1);
        verify(cacheClient, times(1)).evict(anyString());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_UserFollow_Success() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.USER_FOLLOW)
                .userId(1L)
                .targetUserId(2L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, times(1)).updateFollowerCount(2L, 1);
        verify(userMapper, times(1)).updateFollowingCount(1L, 1);
        verify(cacheClient, times(2)).evict(anyString());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_UserUnfollow_Success() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.USER_UNFOLLOW)
                .userId(1L)
                .targetUserId(2L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, times(1)).updateFollowerCount(2L, -1);
        verify(userMapper, times(1)).updateFollowingCount(1L, -1);
        verify(cacheClient, times(2)).evict(anyString());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_Idempotency_MessageAlreadyProcessed() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.POST_LIKE)
                .userId(1L)
                .targetUserId(2L)
                .postId(100L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(true);
        
        consumer.handleMessage(message);
        
        verify(cacheClient, times(1)).exists(anyString());
        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
        verify(userMapper, never()).updateFollowerCount(anyLong(), anyInt());
        verify(userMapper, never()).updateFollowingCount(anyLong(), anyInt());
        verify(cacheClient, never()).set(anyString(), anyString(), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_NullMessage() {
        consumer.handleMessage(null);
        
        verify(cacheClient, never()).exists(anyString());
        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
    }
    
    @Test
    void testHandleMessage_NullMessageId() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .type(MessageType.POST_LIKE)
                .userId(1L)
                .targetUserId(2L)
                .postId(100L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        consumer.handleMessage(message);
        
        verify(cacheClient, never()).exists(anyString());
        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
    }
    
    @Test
    void testHandleMessage_PostLike_MissingTargetUserId() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.POST_LIKE)
                .userId(1L)
                .postId(100L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_UserFollow_MissingUserId() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.USER_FOLLOW)
                .targetUserId(2L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, never()).updateFollowerCount(anyLong(), anyInt());
        verify(userMapper, never()).updateFollowingCount(anyLong(), anyInt());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
    
    @Test
    void testHandleMessage_NullMessageType() {
        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(1L)
                .targetUserId(2L)
                .timestamp(System.currentTimeMillis())
                .build();
        
        when(cacheClient.exists(anyString())).thenReturn(false);
        
        consumer.handleMessage(message);
        
        verify(userMapper, never()).updateLikedCount(anyLong(), anyInt());
        verify(userMapper, never()).updateFollowerCount(anyLong(), anyInt());
        verify(userMapper, never()).updateFollowingCount(anyLong(), anyInt());
        verify(cacheClient, times(1)).set(anyString(), eq("1"), any(Duration.class));
    }
}

