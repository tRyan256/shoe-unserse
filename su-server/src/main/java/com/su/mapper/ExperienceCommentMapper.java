package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.admin.ExperienceCommentAdminQueryDTO;
import com.su.entity.ExperienceComment;
import com.su.vo.ExperienceCommentAdminVO;
import com.su.vo.ExperienceCommentVO;
import com.su.vo.ExperienceReplyVO;
import com.su.vo.admin.ExperienceReplyAdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 心得评论Mapper接口（合并评论和回复）
 */
@Mapper
public interface ExperienceCommentMapper {

    /**
     * 插入一级评论
     * @param comment 评论实体
     */
    void insert(ExperienceComment comment);

    /**
     * 插入回复
     * @param reply 回复实体
     */
    void insertReply(ExperienceComment reply);

    /**
     * 根据ID查询评论/回复
     * @param id 评论/回复ID
     * @return 评论/回复实体
     */
    @Select("select * from experience_comment where id = #{id}")
    ExperienceComment getById(Long id);

    /**
     * 根据心得ID查询一级评论列表（分页）
     * @param postId 心得ID
     * @param currentUserId 当前用户ID（用于显示自己隐藏的评论）
     * @return 评论VO列表
     */
    Page<ExperienceCommentVO> listByPostId(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

    /**
     * 根据父评论ID查询回复列表
     * @param parentId 父评论ID
     * @param currentUserId 当前用户ID（用于显示自己隐藏的回复）
     * @return 回复VO列表
     */
    List<ExperienceReplyVO> listRepliesByParentId(@Param("parentId") Long parentId, @Param("currentUserId") Long currentUserId);

    /**
     * 根据父评论ID列表批量查询回复
     * @param parentIds 父评论ID列表
     * @param currentUserId 当前用户ID（用于显示自己隐藏的回复）
     * @return 回复VO列表
     */
    List<ExperienceReplyVO> listRepliesByParentIds(@Param("parentIds") List<Long> parentIds, @Param("currentUserId") Long currentUserId);

    /**
     * 更新点赞数
     * @param id 评论/回复ID
     * @param increment 增量（正数增加，负数减少）
     */
    @Update("update experience_comment set like_count = like_count + #{increment} where id = #{id}")
    void updateLikeCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新回复数（仅一级评论）
     * @param id 评论ID
     * @param increment 增量（正数增加，负数减少）
     */
    @Update("update experience_comment set reply_count = reply_count + #{increment} where id = #{id}")
    void updateReplyCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新隐藏状态
     * @param id 评论/回复ID
     * @param hidden 隐藏状态 0:否 1:是
     */
    @Update("update experience_comment set hidden = #{hidden} where id = #{id}")
    void updateHidden(@Param("id") Long id, @Param("hidden") Integer hidden);

    /**
     * 根据ID列表查询评论
     * @param ids ID列表
     * @return 评论列表
     */
    List<ExperienceComment> listByIds(@Param("ids") List<Long> ids);

    /**
     * 统计心得的评论数
     * @param postId 心得ID
     * @return 评论数
     */
    @Select("select count(*) from experience_comment where post_id = #{postId} and parent_id = 0 and hidden = 0")
    Integer countByPostId(Long postId);

    /**
     * 根据用户ID查询评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("select * from experience_comment where user_id = #{userId} and parent_id = 0 and hidden = 0 order by create_time desc")
    List<ExperienceComment> listByUserId(Long userId);

    /**
     * 根据目标用户ID查询回复列表（被回复的）
     * @param targetUserId 目标用户ID
     * @return 回复列表
     */
    @Select("select * from experience_comment where target_user_id = #{targetUserId} and parent_id > 0 and hidden = 0 order by create_time desc")
    List<ExperienceComment> listByTargetUserId(Long targetUserId);

    /**
     * 管理员分页查询评论列表（不过滤隐藏状态）
     * @param postId 心得ID
     * @param hidden 隐藏状态
     * @return 评论AdminVO列表
     */
    Page<ExperienceCommentAdminVO> pageQueryAdmin(@Param("postId") Long postId,
                                                   @Param("hidden") Integer hidden);

    /**
     * 管理员分页查询评论列表（支持多条件查询）
     * @param queryDTO 查询条件DTO
     * @return 评论AdminVO列表
     */
    Page<ExperienceCommentAdminVO> pageQueryForAdmin(ExperienceCommentAdminQueryDTO queryDTO);

    /**
     * 批量更新评论隐藏状态
     * @param ids 评论ID列表
     * @param hidden 隐藏状态 0:否 1:是
     * @return 影响的行数
     */
    int batchUpdateHiddenStatus(@Param("ids") List<Long> ids, @Param("hidden") Integer hidden);

    /**
     * 管理员根据评论ID查询回复列表（包含隐藏的）
     * @param parentId 父评论ID
     * @return 回复VO列表
     */
    List<ExperienceReplyAdminVO> listRepliesByParentIdForAdmin(@Param("parentId") Long parentId);

    /**
     * 根据ID删除评论/回复
     * @param id 评论/回复ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 批量删除评论/回复
     * @param ids ID列表
     * @return 影响的行数
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 删除评论下的所有回复
     * @param parentId 父评论ID
     * @return 影响的行数
     */
    int deleteRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 更新评论/回复内容
     * @param id 评论/回复ID
     * @param content 新内容
     * @return 影响的行数
     */
    int updateContent(@Param("id") Long id, @Param("content") String content);
}
