package com.su.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO for updating user personal information
 */
@Data
public class UserPersonalInfoUpdateDTO implements Serializable {
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
    private String avatar;
}
