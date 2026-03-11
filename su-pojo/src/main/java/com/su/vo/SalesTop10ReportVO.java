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
public class SalesTop10ReportVO implements Serializable {

    //鍟嗗搧鍚嶇О鍒楄〃锛屼互閫楀彿鍒嗛殧锛屼緥濡傦細楸奸鑲変笣,瀹繚楦′竵,姘寸叜楸?
    private String nameList;

    //閿€閲忓垪琛紝浠ラ€楀彿鍒嗛殧锛屼緥濡傦細260,215,200
    private String numberList;

}
