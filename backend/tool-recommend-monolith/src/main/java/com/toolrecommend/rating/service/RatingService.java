package com.toolrecommend.rating.service;

import com.toolrecommend.common.dto.RatingDTO;
import com.toolrecommend.common.vo.RatingStatsVO;

/**
 * 评分服务接口
 */
public interface RatingService {

    /**
     * 提交或更新评分
     */
    void submitRating(Long userId, RatingDTO ratingDTO);

    /**
     * 获取工具的评分统计
     */
    RatingStatsVO getToolRatingStats(Long toolId, Long userId);

    /**
     * 删除评分
     */
    void deleteRating(Long userId, Long toolId);

    /**
     * 检查用户是否已评分
     */
    boolean hasRated(Long userId, Long toolId);
}
