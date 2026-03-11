package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.entity.UserFollow;
import com.su.vo.UserProfileVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注Mapper接口
 */
@Mapper
public interface UserFollowMapper {

    /**
     * 插入关注记录
     * @param follow 关注实体
     */
    void insert(UserFollow follow);

    /**
     * 删除关注记录
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    @Delete("delete from user_follow where follower_id = #{followerId} and followee_id = #{followeeId}")
    void delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    /**
     * 查询是否已关注
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 关注记录，不存在返回null
     */
    @Select("select * from user_follow where follower_id = #{followerId} and followee_id = #{followeeId}")
    UserFollow getByFollowerIdAndFolloweeId(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    /**
     * 查询用户的关注列表（分页）
     * @param followerId 关注者ID
     * @return 用户资料VO列表
     */
    Page<UserProfileVO> listFollowing(@Param("followerId") Long followerId);

    /**
     * 查询用户的粉丝列表（分页）
     * @param followeeId 被关注者ID
     * @return 用户资料VO列表
     */
    Page<UserProfileVO> listFollowers(@Param("followeeId") Long followeeId);

    /**
     * 统计用户的关注数
     * @param followerId 关注者ID
     * @return 关注数
     */
    @Select("select count(*) from user_follow where follower_id = #{followerId}")
    Integer countFollowing(Long followerId);

    /**
     * 统计用户的粉丝数
     * @param followeeId 被关注者ID
     * @return 粉丝数
     */
    @Select("select count(*) from user_follow where followee_id = #{followeeId}")
    Integer countFollowers(Long followeeId);

    /**
     * 查询用户关注的用户ID列表
     * @param followerId 关注者ID
     * @return 被关注者ID列表
     */
    @Select("select followee_id from user_follow where follower_id = #{followerId}")
    List<Long> listFolloweeIds(Long followerId);

    /**
     * 查询用户的粉丝ID列表
     * @param followeeId 被关注者ID
     * @return 关注者ID列表
     */
    @Select("select follower_id from user_follow where followee_id = #{followeeId}")
    List<Long> listFollowerIds(Long followeeId);
}
