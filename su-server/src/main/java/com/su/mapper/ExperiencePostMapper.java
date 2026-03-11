package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.entity.ExperiencePost;
import com.su.vo.ExperiencePostAdminVO;
import com.su.vo.ExperiencePostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 体验心得Mapper接口
 */
@Mapper
public interface ExperiencePostMapper {

    /**
     * 插入体验心得
     * @param post 体验心得实体
     */
    void insert(ExperiencePost post);

    /**
     * 根据ID查询体验心得
     * @param id 心得ID
     * @return 体验心得实体
     */
    @Select("select * from experience_post where id = #{id}")
    ExperiencePost getById(Long id);

    /**
     * 根据商品查询心得列表（分页）
     * @param productType 商品类型
     * @param productId 商品ID
     * @param sortBy 排序方式（create_time或like_count）
     * @return 心得VO列表
     */
    Page<ExperiencePostVO> listByProduct(@Param("productType") Integer productType, 
                                         @Param("productId") Long productId, 
                                         @Param("sortBy") String sortBy);

    /**
     * 根据用户ID查询心得列表（分页）
     * @param userId 用户ID
     * @return 心得VO列表
     */
    Page<ExperiencePostVO> listByUserId(@Param("userId") Long userId);

    /**
     * 更新点赞数
     * @param id 心得ID
     * @param increment 增量（正数增加，负数减少）
     */
    @Update("update experience_post set like_count = like_count + #{increment} where id = #{id}")
    void updateLikeCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新评论数
     * @param id 心得ID
     * @param increment 增量（正数增加，负数减少）
     */
    @Update("update experience_post set comment_count = comment_count + #{increment} where id = #{id}")
    void updateCommentCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新隐藏状态
     * @param id 心得ID
     * @param hidden 隐藏状态 0:否 1:是
     */
    @Update("update experience_post set hidden = #{hidden} where id = #{id}")
    void updateHidden(@Param("id") Long id, @Param("hidden") Integer hidden);

    /**
     * 根据ID列表查询心得
     * @param ids ID列表
     * @return 心得列表
     */
    List<ExperiencePost> listByIds(@Param("ids") List<Long> ids);

    /**
     * 查询用户的心得数量
     * @param userId 用户ID
     * @return 心得数量
     */
    @Select("select count(*) from experience_post where user_id = #{userId} and hidden = 0")
    Integer countByUserId(Long userId);

    /**
     * 管理员分页查询心得列表（不过滤隐藏状态）
     * @param productType 商品类型
     * @param productId 商品ID
     * @param hidden 隐藏状态
     * @return 心得AdminVO列表
     */
    Page<ExperiencePostAdminVO> pageQueryAdmin(@Param("productType") Integer productType,
                                               @Param("productId") Long productId,
                                               @Param("hidden") Integer hidden);

    /**
     * 管理员分页查询心得列表（支持多条件查询）
     * @param queryDTO 查询条件DTO
     * @return 心得AdminVO列表
     */
    Page<ExperiencePostAdminVO> pageQueryForAdmin(@Param("dto") com.su.dto.admin.ExperiencePostAdminQueryDTO queryDTO);

    /**
     * 批量更新心得隐藏状态
     * @param ids 心得ID列表
     * @param hidden 隐藏状态 0:否 1:是
     * @return 影响的行数
     */
    int batchUpdateHiddenStatus(@Param("ids") List<Long> ids, @Param("hidden") Integer hidden);

    /**
     * 查询关注用户的心得列表（分页）
     * @param userIds 关注的用户ID列表
     * @param sortBy 排序方式（create_time或like_count）
     * @return 心得VO列表
     */
    Page<ExperiencePostVO> listByFollowing(@Param("userIds") List<Long> userIds, @Param("sortBy") String sortBy);

    /**
     * 查询用户点赞过的心得列表（分页）
     * @param userId 用户ID
     * @param sortBy 排序方式（create_time或like_count）
     * @return 心得VO列表
     */
    Page<ExperiencePostVO> listLikedByUser(@Param("userId") Long userId, @Param("sortBy") String sortBy);

    /**
     * 查询所有公开心得列表（分页）
     * @param sortBy 排序方式（create_time或like_count）
     * @param keyword 搜索关键词（可选）
     * @return 心得VO列表
     */
    Page<ExperiencePostVO> listAllPublic(@Param("sortBy") String sortBy, @Param("keyword") String keyword);

    /**
     * 更新心得
     * @param post 心得实体
     */
    void update(ExperiencePost post);

    /**
     * 根据ID删除心得
     * @param id 心得ID
     */
    @Update("delete from experience_post where id = #{id}")
    void deleteById(Long id);
}
