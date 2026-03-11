package com.su.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 关注者ID
     */
    @NotNull(message = "关注者ID不能为空")
    private Long followerId;

    /**
     * 被关注者ID
     */
    @NotNull(message = "被关注者ID不能为空")
    private Long followeeId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
