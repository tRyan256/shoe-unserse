package com.su.mapper;

import com.su.entity.DrawRecordItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrawRecordItemMapper {
    void insertBatch(@Param("items") List<DrawRecordItem> items);

    void deleteByRecordId(@Param("recordId") Long recordId);

    List<DrawRecordItem> listByRecordId(@Param("recordId") Long recordId);
}

