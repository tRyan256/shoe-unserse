package com.su.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量操作结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failureCount;

    /**
     * 失败详情
     */
    private List<BatchOperationFailure> failures;

    /**
     * 批量操作失败详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOperationFailure implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 失败的ID
         */
        private Long id;

        /**
         * 失败原因
         */
        private String reason;
    }
}
