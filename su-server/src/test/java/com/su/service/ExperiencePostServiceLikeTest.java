package com.su.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.dto.message.MessageType;
import com.su.dto.message.NotificationMessage;
import com.su.dto.message.StatisticsUpdateMessage;
import com.su.entity.ExperiencePost;
import com.su.entity.ExperienceLike;
import com.su.exception.DuplicateLikeException;
import com.su.exception.ExperiencePostNotFoundException;
import com.su.mapper.BundleMapper;
import com.su.mapper.ExperienceLikeMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.mapper.UserFollowMapper;
import com.su.mq.producer.MessageQueueProducer;
import com.su.service.impl.ExperiencePostServiceImpl;
import com.su.utils.cache.CacheClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperiencePostServiceLikeTest {

    @Mock
    private ExperiencePostMapper experiencePostMapper;

    @Mock
    private ExperienceLikeMapper experienceLikeMapper;

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private ShoeSpuMapper shoeSpuMapper;

    @Mock
    private BundleMapper bundleMapper;

    @Mock
    private CacheClient cacheClient;

    @Mock
    private MessageQueueProducer messageQueueProducer;

    private ObjectMapper objectMapper;

    private ExperiencePostServiceImpl experiencePostService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        experiencePostService = new ExperiencePostServiceImpl();
        
        try {
            java.lang.reflect.Field postMapperField = ExperiencePostServiceImpl.class.getDeclaredField("experiencePostMapper");
            postMapperField.setAccessible(true);
            postMapperField.set(experiencePostService, experiencePostMapper);
            
            java.lang.reflect.Field likeMapperField = ExperiencePostServiceImpl.class.getDeclaredField("experienceLikeMapper");
            likeMapperField.setAccessible(true);
            likeMapperField.set(experiencePostService, experienceLikeMapper);
            
            java.lang.reflect.Field cacheField = ExperiencePostServiceImpl.class.getDeclaredField("cacheClient");
            cacheField.setAccessible(true);
            cacheField.set(experiencePostService, cacheClient);
            
            java.lang.reflect.Field mqField = ExperiencePostServiceImpl.class.getDeclaredField("messageQueueProducer");
            mqField.setAccessible(true);
            mqField.set(experiencePostService, messageQueueProducer);
            
            java.lang.reflect.Field objectMapperField = ExperiencePostServiceImpl.class.getDeclaredField("objectMapper");
            objectMapperField.setAccessible(true);
            objectMapperField.set(experiencePostService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testLikePost_Success() {
        Long postId = 1L;
        Long userId = 100L;
        Long authorId = 200L;
        
        ExperiencePost post = ExperiencePost.builder()
                .id(postId)
                .userId(authorId)
                .content("测试心得")
                .productType(1)
                .productId(1L)
                .likeCount(0)
                .build();
        
        when(experiencePostMapper.getById(postId)).thenReturn(post);
        when(experienceLikeMapper.getPostLike(postId, userId)).thenReturn(null);
        when(messageQueueProducer.sendStatisticsUpdateMessage(any())).thenReturn(true);
        when(messageQueueProducer.sendNotificationMessage(any())).thenReturn(true);
        
        String lockToken = UUID.randomUUID().toString();
        when(cacheClient.tryLock(anyString(), any(Duration.class))).thenReturn(lockToken);
        when(cacheClient.get(anyString())).thenReturn(null);
        doNothing().when(cacheClient).set(anyString(), anyString(), any(Duration.class));
        doNothing().when(cacheClient).evict(anyString());
        when(cacheClient.unlock(anyString(), anyString())).thenReturn(true);
        
        experiencePostService.likePost(postId, userId);
        
        ArgumentCaptor<ExperienceLike> likeCaptor = ArgumentCaptor.forClass(ExperienceLike.class);
        verify(experienceLikeMapper, times(1)).insertPostLike(likeCaptor.capture());
        ExperienceLike capturedLike = likeCaptor.getValue();
        assertEquals(postId, capturedLike.getPostId());
        assertEquals(userId, capturedLike.getUserId());
        
        verify(experiencePostMapper, times(1)).updateLikeCount(postId, 1);
        
        ArgumentCaptor<StatisticsUpdateMessage> statsCaptor = ArgumentCaptor.forClass(StatisticsUpdateMessage.class);
        verify(messageQueueProducer, times(1)).sendStatisticsUpdateMessage(statsCaptor.capture());
        StatisticsUpdateMessage statsMessage = statsCaptor.getValue();
        assertEquals(MessageType.POST_LIKE, statsMessage.getType());
        assertEquals(userId, statsMessage.getUserId());
        assertEquals(authorId, statsMessage.getTargetUserId());
        assertEquals(postId, statsMessage.getPostId());
        
        ArgumentCaptor<NotificationMessage> notifyCaptor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(messageQueueProducer, times(1)).sendNotificationMessage(notifyCaptor.capture());
        NotificationMessage notifyMessage = notifyCaptor.getValue();
        assertEquals(MessageType.POST_LIKED, notifyMessage.getType());
        assertEquals(authorId, notifyMessage.getReceiverId());
        assertEquals(userId, notifyMessage.getSenderId());
        assertEquals(postId, notifyMessage.getPostId());
    }

    @Test
    void testLikePost_DuplicateLike() {
        Long postId = 1L;
        Long userId = 100L;
        
        ExperiencePost post = ExperiencePost.builder()
                .id(postId)
                .userId(200L)
                .content("测试心得")
                .productType(1)
                .productId(1L)
                .build();
        
        ExperienceLike existingLike = ExperienceLike.builder()
                .id(1L)
                .postId(postId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
        
        when(experiencePostMapper.getById(postId)).thenReturn(post);
        
        String lockToken = UUID.randomUUID().toString();
        when(cacheClient.tryLock(anyString(), any(Duration.class))).thenReturn(lockToken);
        when(cacheClient.get(anyString())).thenReturn(null);
        when(experienceLikeMapper.getPostLike(postId, userId)).thenReturn(existingLike);
        when(cacheClient.unlock(anyString(), anyString())).thenReturn(true);
        
        assertThrows(DuplicateLikeException.class, () -> {
            experiencePostService.likePost(postId, userId);
        });
        
        verify(experienceLikeMapper, never()).insertPostLike(any());
        verify(experiencePostMapper, never()).updateLikeCount(anyLong(), anyInt());
        verify(messageQueueProducer, never()).sendStatisticsUpdateMessage(any());
        verify(messageQueueProducer, never()).sendNotificationMessage(any());
    }

    @Test
    void testLikePost_PostNotFound() {
        Long postId = 999L;
        Long userId = 100L;
        
        when(experiencePostMapper.getById(postId)).thenReturn(null);
        
        assertThrows(ExperiencePostNotFoundException.class, () -> {
            experiencePostService.likePost(postId, userId);
        });
        
        verify(experienceLikeMapper, never()).insertPostLike(any());
    }

    @Test
    void testLikePost_SelfLike_NoNotification() {
        Long postId = 1L;
        Long userId = 100L;
        
        ExperiencePost post = ExperiencePost.builder()
                .id(postId)
                .userId(userId)
                .content("测试心得")
                .productType(1)
                .productId(1L)
                .build();
        
        when(experiencePostMapper.getById(postId)).thenReturn(post);
        when(experienceLikeMapper.getPostLike(postId, userId)).thenReturn(null);
        when(messageQueueProducer.sendStatisticsUpdateMessage(any())).thenReturn(true);
        
        String lockToken = UUID.randomUUID().toString();
        when(cacheClient.tryLock(anyString(), any(Duration.class))).thenReturn(lockToken);
        when(cacheClient.get(anyString())).thenReturn(null);
        doNothing().when(cacheClient).set(anyString(), anyString(), any(Duration.class));
        doNothing().when(cacheClient).evict(anyString());
        when(cacheClient.unlock(anyString(), anyString())).thenReturn(true);
        
        experiencePostService.likePost(postId, userId);
        
        verify(experienceLikeMapper, times(1)).insertPostLike(any());
        verify(messageQueueProducer, times(1)).sendStatisticsUpdateMessage(any());
        verify(messageQueueProducer, never()).sendNotificationMessage(any());
    }

    @Test
    void testUnlikePost_Success() {
        Long postId = 1L;
        Long userId = 100L;
        Long authorId = 200L;
        
        ExperiencePost post = ExperiencePost.builder()
                .id(postId)
                .userId(authorId)
                .content("测试心得")
                .productType(1)
                .productId(1L)
                .likeCount(1)
                .build();
        
        ExperienceLike existingLike = ExperienceLike.builder()
                .id(1L)
                .postId(postId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
        
        when(experiencePostMapper.getById(postId)).thenReturn(post);
        when(experienceLikeMapper.getPostLike(postId, userId)).thenReturn(existingLike);
        when(messageQueueProducer.sendStatisticsUpdateMessage(any())).thenReturn(true);
        doNothing().when(cacheClient).evict(anyString());
        
        experiencePostService.unlikePost(postId, userId);
        
        verify(experienceLikeMapper, times(1)).deletePostLike(postId, userId);
        verify(experiencePostMapper, times(1)).updateLikeCount(postId, -1);
        
        ArgumentCaptor<StatisticsUpdateMessage> statsCaptor = ArgumentCaptor.forClass(StatisticsUpdateMessage.class);
        verify(messageQueueProducer, times(1)).sendStatisticsUpdateMessage(statsCaptor.capture());
        StatisticsUpdateMessage statsMessage = statsCaptor.getValue();
        assertEquals(MessageType.POST_UNLIKE, statsMessage.getType());
        assertEquals(userId, statsMessage.getUserId());
        assertEquals(authorId, statsMessage.getTargetUserId());
        assertEquals(postId, statsMessage.getPostId());
    }

    @Test
    void testUnlikePost_NotLiked_Idempotent() {
        Long postId = 1L;
        Long userId = 100L;
        
        ExperiencePost post = ExperiencePost.builder()
                .id(postId)
                .userId(200L)
                .content("测试心得")
                .productType(1)
                .productId(1L)
                .build();
        
        when(experiencePostMapper.getById(postId)).thenReturn(post);
        when(experienceLikeMapper.getPostLike(postId, userId)).thenReturn(null);
        
        assertDoesNotThrow(() -> {
            experiencePostService.unlikePost(postId, userId);
        });
        
        verify(experienceLikeMapper, never()).deletePostLike(anyLong(), anyLong());
        verify(experiencePostMapper, never()).updateLikeCount(anyLong(), anyInt());
        verify(messageQueueProducer, never()).sendStatisticsUpdateMessage(any());
    }

    @Test
    void testUnlikePost_PostNotFound() {
        Long postId = 999L;
        Long userId = 100L;
        
        when(experiencePostMapper.getById(postId)).thenReturn(null);
        
        assertThrows(ExperiencePostNotFoundException.class, () -> {
            experiencePostService.unlikePost(postId, userId);
        });
        
        verify(experienceLikeMapper, never()).deletePostLike(anyLong(), anyLong());
    }

    @Test
    void testLikePost_NullParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            experiencePostService.likePost(null, 100L);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            experiencePostService.likePost(1L, null);
        });
    }

    @Test
    void testUnlikePost_NullParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            experiencePostService.unlikePost(null, 100L);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            experiencePostService.unlikePost(1L, null);
        });
    }
}
