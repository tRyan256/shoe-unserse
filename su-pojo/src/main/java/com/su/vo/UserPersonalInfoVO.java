package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VO for user personal information response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonalInfoVO implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
    private String avatar;
    private LocalDateTime createTime;
}
