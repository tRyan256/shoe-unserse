package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.annotation.AutoFill;
import com.su.dto.ShoeSpuPageQueryDTO;
import com.su.entity.ShoeSpu;
import com.su.enumeration.OperationType;
import com.su.vo.ShoeSpuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShoeSpuMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(ShoeSpu shoeSpu);

    Page<ShoeSpuVO> pageQuery(ShoeSpuPageQueryDTO queryDTO);

    void delete(List<Long> ids);

    @Select("select * from shoe_spu where id = #{id}")
    ShoeSpu getById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(ShoeSpu shoeSpu);

    @Update("update shoe_spu set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);

    List<ShoeSpuVO> listByCategoryIds(List<Long> categoryIds);

    List<ShoeSpuVO> listAll();

    ShoeSpuVO getVOById(Long id);

    @Select("select id from shoe_spu where status = 1")
    List<Long> listAllEnabledIds();

    @Select("select id from shoe_spu")
    List<Long> listAllIds();

    Integer countByMap(Map map);

    List<ShoeSpuVO> searchByKeyword(String keyword);
}
