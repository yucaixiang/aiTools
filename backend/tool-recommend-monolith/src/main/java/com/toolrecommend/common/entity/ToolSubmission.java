package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具提交实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("tool_submission")
public class ToolSubmission {

    /**
     * 提交ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

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
     * 定价模式: FREE-免费 FREEMIUM-免费增值 PAID-付费 SUBSCRIPTION-订阅
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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
