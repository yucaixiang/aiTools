package com.toolrecommend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 工具创建DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String name;

    /**
     * URL友好标识
     */
    @NotBlank(message = "URL标识不能为空")
    private String slug;

    /**
     * 一句话介绍
     */
    private String tagline;

    /**
     * 详细描述
     */
    private String description;

    /**
     * Logo地址
     */
    private String logoUrl;

    /**
     * 官网地址
     */
    @NotBlank(message = "官网地址不能为空")
    private String websiteUrl;

    /**
     * 下载地址
     */
    private String downloadUrl;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 主分类ID
     */
    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    /**
     * 定价模式
     */
    @NotBlank(message = "定价模式不能为空")
    private String pricingModel;

    /**
     * 起始价格
     */
    private BigDecimal startingPrice;

    /**
     * 发布日期
     */
    private LocalDate launchDate;

    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;
}
