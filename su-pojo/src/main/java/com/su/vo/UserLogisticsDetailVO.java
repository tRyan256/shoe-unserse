package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogisticsDetailVO implements Serializable {
    private UserLogisticsVO logistics;
    private List<UserLogisticsTraceVO> traces;
}
