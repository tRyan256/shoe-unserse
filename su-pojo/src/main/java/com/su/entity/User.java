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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

    private String avatar;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likedCount;

    private Integer postCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
