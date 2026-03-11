package com.su.service;

import com.github.pagehelper.Page;
import com.su.dto.admin.BatchOperationResult;
import com.su.dto.admin.ExperienceCommentAdminQueryDTO;
import com.su.dto.admin.ExperiencePostAdminQueryDTO;
import com.su.mapper.ExperienceCommentMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.result.PageResult;
import com.su.service.impl.AdminExperienceServiceImpl;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperiencePostAdminVO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AdminExperienceService单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminExperienceServiceTest {

    @Mock
    private ExperiencePostMapper experiencePostMapper;

    @Mock
    private ExperienceCommentMapper experienceCommentMapper;

    @InjectMocks
    private AdminExperienceServiceImpl adminExperienceService;

    @Test
    void testPageQueryComments() {
        // 准备测试数据
        ExperienceCommentAdminQueryDTO queryDTO = new ExperienceCommentAdminQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(20);
        queryDTO.setPostId(1L);

        Page<ExperienceCommentAdminVO> mockPage = new Page<>();
        ExperienceCommentAdminVO comment = ExperienceCommentAdminVO.builder()
                .id(1L)
                .postId(1L)
                .postTitle("测试心得")
                .userId(100L)
                .userName("测试用户")
                .userAvatar("avatar.jpg")
                .content("测试评论内容")
                .likeCount(10)
                .replyCount(5)
                .hidden(0)
                .createTime(LocalDateTime.now())
                .build();
        mockPage.add(comment);
        mockPage.setTotal(1);

        when(experienceCommentMapper.pageQueryForAdmin(any(ExperienceCommentAdminQueryDTO.class)))
                .thenReturn(mockPage);

        // 执行测试
        PageResult<ExperienceCommentAdminVO> result = adminExperienceService.pageQueryComments(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getRecords().get(0).getId());
        assertEquals("测试评论内容", result.getRecords().get(0).getContent());

        // 验证方法调用
        verify(experienceCommentMapper, times(1)).pageQueryForAdmin(any(ExperienceCommentAdminQueryDTO.class));
    }

    @Test
    void testUpdateCommentHiddenStatus() {
        // 准备测试数据
        Long commentId = 1L;
        Integer hidden = 1;

        doNothing().when(experienceCommentMapper).updateHidden(commentId, hidden);

        // 执行测试
        adminExperienceService.updateCommentHiddenStatus(commentId, hidden);

        // 验证方法调用
        verify(experienceCommentMapper, times(1)).updateHidden(commentId, hidden);
    }

    @Test
    void testBatchUpdateCommentsHiddenStatus_Success() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        Integer hidden = 1;

        doNothing().when(experienceCommentMapper).updateHidden(anyLong(), eq(hidden));

        // 执行测试
        BatchOperationResult result = adminExperienceService.batchUpdateCommentsHiddenStatus(ids, hidden);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getFailures().isEmpty());

        // 验证方法调用
        verify(experienceCommentMapper, times(3)).updateHidden(anyLong(), eq(hidden));
    }

    @Test
    void testBatchUpdateCommentsHiddenStatus_PartialFailure() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        Integer hidden = 1;

        // 模拟第二个ID更新失败
        doNothing().when(experienceCommentMapper).updateHidden(eq(1L), eq(hidden));
        doThrow(new RuntimeException("数据库错误")).when(experienceCommentMapper).updateHidden(eq(2L), eq(hidden));
        doNothing().when(experienceCommentMapper).updateHidden(eq(3L), eq(hidden));

        // 执行测试
        BatchOperationResult result = adminExperienceService.batchUpdateCommentsHiddenStatus(ids, hidden);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(1, result.getFailures().size());
        assertEquals(2L, result.getFailures().get(0).getId());
        assertTrue(result.getFailures().get(0).getReason().contains("数据库错误"));

        // 验证方法调用
        verify(experienceCommentMapper, times(3)).updateHidden(anyLong(), eq(hidden));
    }

    @Test
    void testBatchUpdateCommentsHiddenStatus_EmptyList() {
        // 准备测试数据
        List<Long> ids = Arrays.asList();
        Integer hidden = 1;

        // 执行测试
        BatchOperationResult result = adminExperienceService.batchUpdateCommentsHiddenStatus(ids, hidden);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getFailures().isEmpty());

        // 验证方法未被调用
        verify(experienceCommentMapper, never()).updateHidden(anyLong(), anyInt());
    }

    @Test
    void testBatchUpdateCommentsHiddenStatus_NullList() {
        // 准备测试数据
        Integer hidden = 1;

        // 执行测试
        BatchOperationResult result = adminExperienceService.batchUpdateCommentsHiddenStatus(null, hidden);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getFailures().isEmpty());

        // 验证方法未被调用
        verify(experienceCommentMapper, never()).updateHidden(anyLong(), anyInt());
    }
}
