package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具详情视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具ID
     */
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
     * 详细描述（Markdown）
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
     * 分类信息
     */
    private CategoryVO category;

    /**
     * 定价模式
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
     * 创建者信息
     */
    private UserSimpleVO maker;

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
     * 标签列表
     */
    private List<TagVO> tags;

    /**
     * 热门评论（前3条）
     */
    private List<ReviewVO> topReviews;

    /**
     * 相似工具推荐
     */
    private List<ToolVO> similarTools;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否已收藏（当前用户）
     */
    private Boolean isFavorited;

    /**
     * 是否已点赞（当前用户）
     */
    private Boolean isUpvoted;
}
