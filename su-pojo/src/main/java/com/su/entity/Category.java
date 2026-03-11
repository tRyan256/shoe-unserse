package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //绫诲瀷: 1鑿滃搧鍒嗙被 2濂楅鍒嗙被
    private Integer type;

    //鍒嗙被鍚嶇О
    private String name;

    //椤哄簭
    private Integer sort;

    //鍒嗙被鐘舵€?0鏍囪瘑绂佺敤 1琛ㄧず鍚敤
    private Integer status;

    //鍒涘缓鏃堕棿
    private LocalDateTime createTime;

    //鏇存柊鏃堕棿
    private LocalDateTime updateTime;

    //鍒涘缓浜?
    private Long createUser;

    //淇敼浜?
    private Long updateUser;
}
