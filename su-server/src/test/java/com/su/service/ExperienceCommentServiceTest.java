package com.su.service;

import com.su.dto.ExperienceCommentDTO;
import com.su.dto.ExperienceReplyDTO;
import com.su.entity.ExperienceComment;
import com.su.entity.ExperiencePost;
import com.su.exception.CommentNotFoundException;
import com.su.exception.ExperiencePostNotFoundException;
import com.su.mapper.ExperienceCommentMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.service.impl.ExperienceCommentServiceImpl;
import com.su.utils.cache.CacheClient;
import com.su.mq.producer.MessageQueueProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperienceCommentServiceTest {

    @Mock
    private ExperienceCommentMapper experienceCommentMapper;

    @Mock
    private ExperiencePostMapper experiencePostMapper;

    @Mock
    private CacheClient cacheClient;

    @Mock
    private MessageQueueProducer messageQueueProducer;

    @InjectMocks
    private ExperienceCommentServiceImpl experienceCommentService;

    private Long testPostId;
    private Long testUserId;
    private Long testCommentId;
    private ExperiencePost testPost;
    private ExperienceComment testComment;

    @BeforeEach
    void setUp() {
        testPostId = 1L;
        testUserId = 100L;
        testCommentId = 1L;
        
        testPost = ExperiencePost.builder()
                .id(testPostId)
                .userId(200L)
                .content("测试心得内容")
                .productType(1)
                .productId(1L)
                .likeCount(0)
                .commentCount(0)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .build();
        
        testComment = ExperienceComment.builder()
                .id(testCommentId)
                .postId(testPostId)
                .parentId(0L)
                .userId(testUserId)
                .content("测试评论内容")
                .targetUserId(0L)
                .targetReplyId(null)
                .replyCount(0)
                .likeCount(0)
                .isAuthor(0)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateComment_Success() {
        ExperienceCommentDTO dto = new ExperienceCommentDTO();
        dto.setPostId(testPostId);
        dto.setContent("这是一条测试评论");

        when(experiencePostMapper.getById(testPostId)).thenReturn(testPost);
        doNothing().when(experienceCommentMapper).insert(any(ExperienceComment.class));
        doNothing().when(experiencePostMapper).updateCommentCount(anyLong(), anyInt());
        doNothing().when(cacheClient).evict(anyString());
        when(messageQueueProducer.sendNotificationMessage(any())).thenReturn(true);

        var result = experienceCommentService.createComment(dto, testUserId);

        assertNotNull(result);
        assertEquals(dto.getContent(), result.getContent());
        
        verify(experienceCommentMapper, times(1)).insert(any(ExperienceComment.class));
        verify(experiencePostMapper, times(1)).updateCommentCount(testPostId, 1);
    }

    @Test
    void testCreateComment_PostNotFound() {
        ExperienceCommentDTO dto = new ExperienceCommentDTO();
        dto.setPostId(testPostId);
        dto.setContent("这是一条测试评论");

        when(experiencePostMapper.getById(testPostId)).thenReturn(null);

        assertThrows(ExperiencePostNotFoundException.class, () -> {
            experienceCommentService.createComment(dto, testUserId);
        });

        verify(experienceCommentMapper, never()).insert(any());
    }

    @Test
    void testCreateComment_EmptyContent() {
        ExperienceCommentDTO dto = new ExperienceCommentDTO();
        dto.setPostId(testPostId);
        dto.setContent("");

        assertThrows(IllegalArgumentException.class, () -> {
            experienceCommentService.createComment(dto, testUserId);
        });

        verify(experienceCommentMapper, never()).insert(any());
    }

    @Test
    void testCreateReply_Success() {
        ExperienceReplyDTO dto = new ExperienceReplyDTO();
        dto.setCommentId(testCommentId);
        dto.setContent("这是一条测试回复");
        dto.setTargetUserId(300L);
        dto.setTargetReplyId(null);

        when(experienceCommentMapper.getById(testCommentId)).thenReturn(testComment);
        when(experiencePostMapper.getById(testPostId)).thenReturn(testPost);
        doNothing().when(experienceCommentMapper).insertReply(any(ExperienceComment.class));
        doNothing().when(experienceCommentMapper).updateReplyCount(anyLong(), anyInt());
        doNothing().when(cacheClient).evict(anyString());
        when(messageQueueProducer.sendNotificationMessage(any())).thenReturn(true);

        var result = experienceCommentService.createReply(dto, testUserId);

        assertNotNull(result);
        assertEquals(dto.getContent(), result.getContent());
        assertEquals(testCommentId, result.getCommentId());
        
        verify(experienceCommentMapper, times(1)).insertReply(any(ExperienceComment.class));
        verify(experienceCommentMapper, times(1)).updateReplyCount(testCommentId, 1);
    }

    @Test
    void testCreateReply_CommentNotFound() {
        ExperienceReplyDTO dto = new ExperienceReplyDTO();
        dto.setCommentId(testCommentId);
        dto.setContent("这是一条测试回复");
        dto.setTargetUserId(300L);

        when(experienceCommentMapper.getById(testCommentId)).thenReturn(null);

        assertThrows(CommentNotFoundException.class, () -> {
            experienceCommentService.createReply(dto, testUserId);
        });

        verify(experienceCommentMapper, never()).insertReply(any());
    }

    @Test
    void testCreateReply_CannotReplyToReply() {
        ExperienceComment replyComment = ExperienceComment.builder()
                .id(2L)
                .postId(testPostId)
                .parentId(testCommentId)
                .userId(300L)
                .content("这是一条回复")
                .build();

        ExperienceReplyDTO dto = new ExperienceReplyDTO();
        dto.setCommentId(2L);
        dto.setContent("回复的回复");
        dto.setTargetUserId(300L);

        when(experienceCommentMapper.getById(2L)).thenReturn(replyComment);

        assertThrows(IllegalArgumentException.class, () -> {
            experienceCommentService.createReply(dto, testUserId);
        });

        verify(experienceCommentMapper, never()).insertReply(any());
    }

    @Test
    void testHideComment_Success() {
        when(experienceCommentMapper.getById(testCommentId)).thenReturn(testComment);
        doNothing().when(experienceCommentMapper).updateHidden(anyLong(), anyInt());
        doNothing().when(cacheClient).evict(anyString());

        assertDoesNotThrow(() -> {
            experienceCommentService.hideComment(testCommentId);
        });

        verify(experienceCommentMapper, times(1)).updateHidden(testCommentId, 1);
    }

    @Test
    void testHideComment_CommentNotFound() {
        when(experienceCommentMapper.getById(testCommentId)).thenReturn(null);

        assertThrows(CommentNotFoundException.class, () -> {
            experienceCommentService.hideComment(testCommentId);
        });

        verify(experienceCommentMapper, never()).updateHidden(anyLong(), anyInt());
    }

    @Test
    void testUnhideComment_Success() {
        when(experienceCommentMapper.getById(testCommentId)).thenReturn(testComment);
        doNothing().when(experienceCommentMapper).updateHidden(anyLong(), anyInt());
        doNothing().when(cacheClient).evict(anyString());

        assertDoesNotThrow(() -> {
            experienceCommentService.unhideComment(testCommentId);
        });

        verify(experienceCommentMapper, times(1)).updateHidden(testCommentId, 0);
    }

    @Test
    void testGetById_Success() {
        when(experienceCommentMapper.getById(testCommentId)).thenReturn(testComment);

        ExperienceComment result = experienceCommentMapper.getById(testCommentId);

        assertNotNull(result);
        assertEquals(testCommentId, result.getId());
        assertEquals(0L, result.getParentId());
    }

    @Test
    void testListByUserId_Success() {
        List<ExperienceComment> comments = Arrays.asList(testComment);
        when(experienceCommentMapper.listByUserId(testUserId)).thenReturn(comments);

        List<ExperienceComment> result = experienceCommentMapper.listByUserId(testUserId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
