package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具视图对象（列表展示）
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolVO implements Serializable {

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
     * Logo地址
     */
    private String logoUrl;

    /**
     * 官网地址
     */
    private String websiteUrl;

    /**
     * 分类名称
     */
    private String categoryName;

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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否已收藏（当前用户）
     */
    private Boolean isFavorited;

    /**
     * 是否已点赞（当前用户）
     */
    private Boolean isUpvoted;
}
