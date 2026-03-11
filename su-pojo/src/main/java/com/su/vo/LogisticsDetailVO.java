package com.su.vo;

import com.su.entity.Logistics;
import com.su.entity.LogisticsTrace;
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
public class LogisticsDetailVO implements Serializable {
    private Logistics logistics;
    private List<LogisticsTrace> traces;
}

