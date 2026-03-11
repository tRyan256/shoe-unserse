package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserOrderDetailVO extends UserOrderVO {

    private UserLogisticsVO logistics;

    private List<UserLogisticsTraceVO> logisticsTraceList;
}
