package com.su.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    //鍛樺伐id
    private Long empId;

    //鏃у瘑鐮?
    private String oldPassword;

    //鏂板瘑鐮?
    private String newPassword;

}
