package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工具实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("tool")
public class Tool {

    /**
     * 工具ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * URL友好标识
     */
    private String slug;

    /**
     * 一句话介绍
     */
    private String tagline;

    /**
     * 详细描述（支持Markdown）
     */
    private String description;

    /**
     * Logo地址
     */
    private String logoUrl;

    /**
     * 官网地址
     */
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
    private Integer categoryId;

    /**
     * 定价模式：FREE-免费 FREEMIUM-免费增值 PAID-付费 OPEN_SOURCE-开源
     */
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
     * 创建者ID
     */
    private Long makerId;

    /**
     * 状态：0-草稿 1-已发布 2-已下架
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 点赞数
     */
    private Integer upvoteCount;

    /**
     * 评论数
     */
    private Integer reviewCount;

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 信息完整度
     */
    private BigDecimal profileCompleteness;

    /**
     * 月增长率
     */
    private BigDecimal monthlyGrowthRate;

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
