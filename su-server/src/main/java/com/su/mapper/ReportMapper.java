package com.su.mapper;


import com.su.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {


    List<BigDecimal> getTurnoverByDates(List<LocalDate> dateList);

    List<Integer> getNewUserByDates(List<LocalDate> dateList);

    List<Integer> getTotalUserByDates(List<LocalDate> dateList);

    List<Integer> getOrderCountByDates(List<LocalDate> dateList);

    List<Integer> getValidOrderCountByDates(List<LocalDate> dateList);

    List<GoodsSalesDTO> getSalesTop10(LocalDate begin, LocalDate end);

    Double getTotalTurnover();

    Integer getTotalValidOrderCount();
}
