package com.toolrecommend.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 工具提交审核DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolSubmissionReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核状态: 1-通过 2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 审核意见（拒绝时必填）
     */
    private String reviewComment;
}
