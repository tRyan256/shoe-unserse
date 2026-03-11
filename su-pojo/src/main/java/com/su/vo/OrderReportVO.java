package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportVO implements Serializable {

    //鏃ユ湡锛屼互閫楀彿鍒嗛殧锛屼緥濡傦細2022-10-01,2022-10-02,2022-10-03
    private String dateList;

    //姣忔棩璁㈠崟鏁帮紝浠ラ€楀彿鍒嗛殧锛屼緥濡傦細260,210,215
    private String orderCountList;

    //姣忔棩鏈夋晥璁㈠崟鏁帮紝浠ラ€楀彿鍒嗛殧锛屼緥濡傦細20,21,10
    private String validOrderCountList;

    //璁㈠崟鎬绘暟
    private Integer totalOrderCount;

    //鏈夋晥璁㈠崟鏁?
    private Integer validOrderCount;

    //璁㈠崟瀹屾垚鐜?
    private Double orderCompletionRate;

}
