package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoeSpu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String brand;

    private String model;

    private String description;

    private Integer isLimited;

    private LocalDate releaseDate;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
