package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.entity.ExperienceNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface ExperienceNotificationMapper {

    /**
     * 插入通知
     * @param notification 通知实体
     */
    void insert(ExperienceNotification notification);

    /**
     * 根据ID查询通知
     * @param id 通知ID
     * @return 通知实体
     */
    @Select("select * from experience_notification where id = #{id}")
    ExperienceNotification getById(Long id);

    /**
     * 根据用户ID查询通知列表（分页）
     * @param userId 用户ID
     * @return 通知列表
     */
    Page<ExperienceNotification> listByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询通知VO列表（分页，带类型过滤）
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 通知VO列表
     */
    Page<com.su.vo.ExperienceNotificationVO> listNotificationVOsByUserId(@Param("userId") Long userId, @Param("type") Integer type);

    /**
     * 根据用户ID查询未读通知列表
     * @param userId 用户ID
     * @return 未读通知列表
     */
    @Select("select * from experience_notification where user_id = #{userId} and is_read = 0 order by create_time desc")
    List<ExperienceNotification> listUnreadByUserId(Long userId);

    /**
     * 标记通知为已读
     * @param id 通知ID
     */
    @Update("update experience_notification set is_read = 1 where id = #{id}")
    void markAsRead(Long id);

    /**
     * 批量标记通知为已读
     * @param ids 通知ID列表
     */
    void markAsReadBatch(@Param("ids") List<Long> ids);

    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     */
    @Update("update experience_notification set is_read = 1 where user_id = #{userId} and is_read = 0")
    void markAllAsReadByUserId(Long userId);

    /**
     * 统计用户未读通知数
     * @param userId 用户ID
     * @return 未读通知数
     */
    @Select("select count(*) from experience_notification where user_id = #{userId} and is_read = 0")
    Integer countUnreadByUserId(Long userId);

    /**
     * 删除通知
     * @param id 通知ID
     */
    @Update("delete from experience_notification where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据类型和关联ID查询通知（用于去重）
     * @param userId 用户ID
     * @param type 通知类型
     * @param sourceUserId 触发者ID
     * @param postId 心得ID
     * @param commentId 评论ID
     * @return 通知实体
     */
    @Select("select * from experience_notification where user_id = #{userId} and type = #{type} " +
            "and source_user_id = #{sourceUserId} and post_id = #{postId} and comment_id = #{commentId}")
    ExperienceNotification getByCondition(@Param("userId") Long userId, 
                                          @Param("type") Integer type,
                                          @Param("sourceUserId") Long sourceUserId,
                                          @Param("postId") Long postId,
                                          @Param("commentId") Long commentId);
}
