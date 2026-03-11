package com.su.mapper;

import com.su.entity.LogisticsTrace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LogisticsTraceMapper {
    void insert(LogisticsTrace trace);

    List<LogisticsTrace> listByLogisticsId(Long logisticsId);
}

