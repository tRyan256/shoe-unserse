package com.su.mapper;


import com.su.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where phone = #{phone}")
    User getByPhone(String phone);

    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    Integer countByMap(Map map);

    void update(User user);

    @Update("update user set follower_count = follower_count + #{increment} where id = #{userId}")
    void updateFollowerCount(@Param("userId") Long userId, @Param("increment") Integer increment);

    @Update("update user set following_count = following_count + #{increment} where id = #{userId}")
    void updateFollowingCount(@Param("userId") Long userId, @Param("increment") Integer increment);

    @Update("update user set liked_count = liked_count + #{increment} where id = #{userId}")
    void updateLikedCount(@Param("userId") Long userId, @Param("increment") Integer increment);

    @Update("update user set post_count = post_count + #{increment} where id = #{userId}")
    void updatePostCount(@Param("userId") Long userId, @Param("increment") Integer increment);
}
