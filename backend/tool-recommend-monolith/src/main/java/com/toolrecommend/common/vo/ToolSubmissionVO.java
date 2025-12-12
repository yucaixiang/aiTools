package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工具提交视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolSubmissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交ID
     */
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 提交用户名
     */
    private String username;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具标语
     */
    private String tagline;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 网站URL
     */
    private String websiteUrl;

    /**
     * Logo URL
     */
    private String logoUrl;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 定价模式
     */
    private String pricingModel;

    /**
     * 发布日期
     */
    private LocalDateTime launchDate;

    /**
     * 审核状态: 0-待审核 1-已通过 2-已拒绝
     */
    private Integer status;

    /**
     * 审核状态描述
     */
    private String statusDesc;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * 审核人ID
     */
    private Long reviewedBy;

    /**
     * 审核人姓名
     */
    private String reviewerName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
