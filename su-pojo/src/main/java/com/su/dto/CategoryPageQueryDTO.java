package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryPageQueryDTO implements Serializable {

    //椤电爜
    private int page;

    //姣忛〉璁板綍鏁?
    private int pageSize;

    //鍒嗙被鍚嶇О
    private String name;

    //鍒嗙被绫诲瀷 1鑿滃搧鍒嗙被  2濂楅鍒嗙被
    private Integer type;

}
