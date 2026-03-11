package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.OutletPageQueryDTO;
import com.su.entity.Outlet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OutletMapper {
    void insert(Outlet outlet);

    void update(Outlet outlet);

    void deleteById(Long id);

    @Select("select * from outlet where id = #{id}")
    Outlet getById(Long id);

    @Select("select id from outlet")
    List<Long> listAllIds();

    @Select("select * from outlet where status = 1 and longitude is not null and latitude is not null")
    List<Outlet> listEnabledWithLocation();

    Page<Outlet> pageQuery(OutletPageQueryDTO dto);
}
