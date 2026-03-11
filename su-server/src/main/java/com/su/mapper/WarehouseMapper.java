package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.WarehousePageQueryDTO;
import com.su.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WarehouseMapper {
    void insert(Warehouse warehouse);

    void update(Warehouse warehouse);

    void deleteById(Long id);

    @Select("select * from warehouse where id = #{id}")
    Warehouse getById(Long id);

    Page<Warehouse> pageQuery(WarehousePageQueryDTO dto);
}

