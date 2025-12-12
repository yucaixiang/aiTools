package com.toolrecommend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工具提交创建DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolSubmissionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String name;

    /**
     * 工具标语（一句话介绍）
     */
    @NotBlank(message = "工具标语不能为空")
    private String tagline;

    /**
     * 工具描述
     */
    @NotBlank(message = "工具描述不能为空")
    private String description;

    /**
     * 网站URL
     */
    @NotBlank(message = "网站URL不能为空")
    private String websiteUrl;

    /**
     * Logo URL
     */
    private String logoUrl;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /**
     * 定价模式: FREE-免费 FREEMIUM-免费增值 PAID-付费 SUBSCRIPTION-订阅
     */
    @NotBlank(message = "定价模式不能为空")
    private String pricingModel;

    /**
     * 发布日期
     */
    private LocalDateTime launchDate;
}
