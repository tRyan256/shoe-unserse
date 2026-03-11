package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.dto.admin.BatchOperationResult;
import com.su.dto.admin.ExperienceCommentAdminQueryDTO;
import com.su.dto.admin.ExperiencePostAdminQueryDTO;
import com.su.mapper.ExperienceCommentMapper;
import com.su.mapper.ExperiencePostMapper;
import com.su.result.PageResult;
import com.su.service.AdminExperienceService;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.admin.ExperienceReplyAdminVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员端体验心得管理服务实现类（合并评论和回复）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminExperienceServiceImpl implements AdminExperienceService {

    private final ExperiencePostMapper experiencePostMapper;
    private final ExperienceCommentMapper experienceCommentMapper;

    /**
     * 分页查询心得列表（包含商品信息）
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
    public PageResult<ExperiencePostAdminVO> pageQueryPosts(ExperiencePostAdminQueryDTO queryDTO) {
        log.info("管理员分页查询心得列表，查询条件: {}", queryDTO);

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        Page<ExperiencePostAdminVO> page = experiencePostMapper.pageQueryForAdmin(queryDTO);

        PageResult<ExperiencePostAdminVO> result = new PageResult<>(page.getTotal(), page.getResult());

        log.info("查询完成，总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 更新心得隐藏状态
     *
     * @param id     心得ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    @Override
    public void updatePostHiddenStatus(Long id, Integer hidden) {
        log.info("更新心得隐藏状态，心得ID: {}, 隐藏状态: {}", id, hidden);

        experiencePostMapper.updateHidden(id, hidden);

        log.info("心得隐藏状态更新成功");
    }

    /**
     * 批量更新心得隐藏状态
     *
     * @param ids    心得ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchUpdatePostsHiddenStatus(List<Long> ids, Integer hidden) {
        log.info("批量更新心得隐藏状态，心得ID列表: {}, 隐藏状态: {}", ids, hidden);

        if (ids == null || ids.isEmpty()) {
            log.warn("心得ID列表为空，无需更新");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experiencePostMapper.updateHidden(id, hidden);
                successCount++;
            } catch (Exception e) {
                log.error("更新心得隐藏状态失败，心得ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量更新完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }

    /**
     * 分页查询评论列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @Override
    public PageResult<ExperienceCommentAdminVO> pageQueryComments(ExperienceCommentAdminQueryDTO queryDTO) {
        log.info("管理员分页查询评论列表，查询条件: {}", queryDTO);

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        Page<ExperienceCommentAdminVO> page = experienceCommentMapper.pageQueryForAdmin(queryDTO);

        PageResult<ExperienceCommentAdminVO> result = new PageResult<>(page.getTotal(), page.getResult());

        log.info("查询完成，总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 更新评论隐藏状态
     *
     * @param id     评论ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    @Override
    public void updateCommentHiddenStatus(Long id, Integer hidden) {
        log.info("更新评论隐藏状态，评论ID: {}, 隐藏状态: {}", id, hidden);

        experienceCommentMapper.updateHidden(id, hidden);

        log.info("评论隐藏状态更新成功");
    }

    /**
     * 批量更新评论隐藏状态
     *
     * @param ids    评论ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchUpdateCommentsHiddenStatus(List<Long> ids, Integer hidden) {
        log.info("批量更新评论隐藏状态，评论ID列表: {}, 隐藏状态: {}", ids, hidden);

        if (ids == null || ids.isEmpty()) {
            log.warn("评论ID列表为空，无需更新");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experienceCommentMapper.updateHidden(id, hidden);
                successCount++;
            } catch (Exception e) {
                log.error("更新评论隐藏状态失败，评论ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量更新完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }

    /**
     * 根据评论ID查询回复列表
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    @Override
    public List<ExperienceReplyAdminVO> listRepliesByCommentId(Long commentId) {
        log.info("管理员查询评论回复列表，评论ID: {}", commentId);

        List<ExperienceReplyAdminVO> replies = experienceCommentMapper.listRepliesByParentIdForAdmin(commentId);

        log.info("查询完成，回复数量: {}", replies.size());

        return replies;
    }

    /**
     * 更新回复隐藏状态
     *
     * @param id     回复ID
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     */
    @Override
    public void updateReplyHiddenStatus(Long id, Integer hidden) {
        log.info("更新回复隐藏状态，回复ID: {}, 隐藏状态: {}", id, hidden);

        experienceCommentMapper.updateHidden(id, hidden);

        log.info("回复隐藏状态更新成功");
    }

    /**
     * 批量更新回复隐藏状态
     *
     * @param ids    回复ID列表
     * @param hidden 隐藏状态 0:未隐藏 1:已隐藏
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchUpdateRepliesHiddenStatus(List<Long> ids, Integer hidden) {
        log.info("批量更新回复隐藏状态，回复ID列表: {}, 隐藏状态: {}", ids, hidden);

        if (ids == null || ids.isEmpty()) {
            log.warn("回复ID列表为空，无需更新");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experienceCommentMapper.updateHidden(id, hidden);
                successCount++;
            } catch (Exception e) {
                log.error("更新回复隐藏状态失败，回复ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量更新完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }

    /**
     * 删除心得
     *
     * @param id 心得ID
     */
    @Override
    public void deletePost(Long id) {
        log.info("删除心得，心得ID: {}", id);

        experiencePostMapper.deleteById(id);

        log.info("心得删除成功");
    }

    /**
     * 批量删除心得
     *
     * @param ids 心得ID列表
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchDeletePosts(List<Long> ids) {
        log.info("批量删除心得，心得ID列表: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.warn("心得ID列表为空，无需删除");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experiencePostMapper.deleteById(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除心得失败，心得ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量删除完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }

    /**
     * 删除评论（同时删除关联回复）
     *
     * @param id 评论ID
     */
    @Override
    public void deleteComment(Long id) {
        log.info("删除评论，评论ID: {}", id);

        experienceCommentMapper.deleteRepliesByParentId(id);
        experienceCommentMapper.deleteById(id);

        log.info("评论及其回复删除成功");
    }

    /**
     * 批量删除评论（同时删除关联回复）
     *
     * @param ids 评论ID列表
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchDeleteComments(List<Long> ids) {
        log.info("批量删除评论，评论ID列表: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.warn("评论ID列表为空，无需删除");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experienceCommentMapper.deleteRepliesByParentId(id);
                experienceCommentMapper.deleteById(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除评论失败，评论ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量删除完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }

    /**
     * 删除回复
     *
     * @param id 回复ID
     */
    @Override
    public void deleteReply(Long id) {
        log.info("删除回复，回复ID: {}", id);

        experienceCommentMapper.deleteById(id);

        log.info("回复删除成功");
    }

    /**
     * 批量删除回复
     *
     * @param ids 回复ID列表
     * @return 批量操作结果统计
     */
    @Override
    public BatchOperationResult batchDeleteReplies(List<Long> ids) {
        log.info("批量删除回复，回复ID列表: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.warn("回复ID列表为空，无需删除");
            return BatchOperationResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failures(new ArrayList<>())
                    .build();
        }

        List<BatchOperationResult.BatchOperationFailure> failures = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                experienceCommentMapper.deleteById(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除回复失败，回复ID: {}", id, e);
                failures.add(BatchOperationResult.BatchOperationFailure.builder()
                        .id(id)
                        .reason(e.getMessage())
                        .build());
            }
        }

        int failureCount = ids.size() - successCount;

        BatchOperationResult result = BatchOperationResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        log.info("批量删除完成，成功: {}, 失败: {}", successCount, failureCount);

        return result;
    }
}
