package com.su.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeePageQueryDTO implements Serializable {

    //鍛樺伐濮撳悕
    private String name;

    //椤电爜
    private int page;

    //姣忛〉鏄剧ず璁板綍鏁?
    private int pageSize;

}
