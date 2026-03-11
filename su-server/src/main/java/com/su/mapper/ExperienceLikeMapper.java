package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.entity.ExperienceLike;
import com.su.vo.UserProfileVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 体验点赞Mapper接口（心得/评论点赞）
 */
@Mapper
public interface ExperienceLikeMapper {

    /**
     * 插入心得点赞记录
     * @param like 点赞实体
     */
    void insertPostLike(ExperienceLike like);

    /**
     * 插入评论点赞记录
     * @param like 点赞实体
     */
    void insertCommentLike(ExperienceLike like);

    /**
     * 删除心得点赞记录
     * @param postId 心得ID
     * @param userId 用户ID
     */
    @Delete("delete from experience_like where post_id = #{postId} and user_id = #{userId}")
    void deletePostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 删除评论点赞记录
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    @Delete("delete from experience_like where comment_id = #{commentId} and user_id = #{userId}")
    void deleteCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 查询用户是否已点赞心得
     * @param postId 心得ID
     * @param userId 用户ID
     * @return 点赞记录，不存在返回null
     */
    @Select("select * from experience_like where post_id = #{postId} and user_id = #{userId}")
    ExperienceLike getPostLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询用户是否已点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录，不存在返回null
     */
    @Select("select * from experience_like where comment_id = #{commentId} and user_id = #{userId}")
    ExperienceLike getCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 查询心得的点赞用户ID列表
     * @param postId 心得ID
     * @return 用户ID列表
     */
    @Select("select user_id from experience_like where post_id = #{postId}")
    List<Long> listUserIdsByPostId(Long postId);

    /**
     * 查询评论的点赞用户ID列表
     * @param commentId 评论ID
     * @return 用户ID列表
     */
    @Select("select user_id from experience_like where comment_id = #{commentId}")
    List<Long> listUserIdsByCommentId(Long commentId);

    /**
     * 查询用户点赞的心得ID列表
     * @param userId 用户ID
     * @return 心得ID列表
     */
    @Select("select post_id from experience_like where user_id = #{userId} and post_id is not null")
    List<Long> listPostIdsByUserId(Long userId);

    /**
     * 查询用户点赞的评论ID列表
     * @param userId 用户ID
     * @return 评论ID列表
     */
    @Select("select comment_id from experience_like where user_id = #{userId} and comment_id is not null")
    List<Long> listCommentIdsByUserId(Long userId);

    /**
     * 统计心得的点赞数
     * @param postId 心得ID
     * @return 点赞数
     */
    @Select("select count(*) from experience_like where post_id = #{postId}")
    Integer countByPostId(Long postId);

    /**
     * 统计评论的点赞数
     * @param commentId 评论ID
     * @return 点赞数
     */
    @Select("select count(*) from experience_like where comment_id = #{commentId}")
    Integer countByCommentId(Long commentId);

    /**
     * 查询点赞心得的用户列表（分页）
     * @param postId 心得ID
     * @return 用户资料VO列表
     */
    Page<UserProfileVO> listPostLikeUsers(@Param("postId") Long postId);
}
