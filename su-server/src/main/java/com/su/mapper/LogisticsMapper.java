package com.su.mapper;

import com.su.entity.Logistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LogisticsMapper {
    void insert(Logistics logistics);

    Logistics getByOrderNo(String orderNo);

    void update(Logistics logistics);

    List<Logistics> listNeedSimulate(@Param("beforeTime") LocalDateTime beforeTime, @Param("limit") Integer limit);
}
