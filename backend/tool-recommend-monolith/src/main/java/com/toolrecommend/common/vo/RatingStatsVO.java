package com.toolrecommend.common.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 评分统计VO
 */
@Data
public class RatingStatsVO {

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 当前用户的评分(如果已登录且已评分)
     */
    private Integer userScore;
}
