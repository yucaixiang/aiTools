package com.toolrecommend.review.service;

import com.toolrecommend.common.dto.ReviewCreateDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.ReviewVO;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 *
 * @author Tool Recommend Team
 */
public interface ReviewService {

    /**
     * 创建评论
     */
    Long createReview(ReviewCreateDTO createDTO, Long userId);

    /**
     * 更新评论
     */
    boolean updateReview(Long id, ReviewCreateDTO updateDTO, Long userId);

    /**
     * 删除评论
     */
    boolean deleteReview(Long id, Long userId);

    /**
     * 分页查询工具评论
     */
    PageResult<ReviewVO> getToolReviews(Long toolId, Long current, Long size);

    /**
     * 分页查询用户评论
     */
    PageResult<ReviewVO> getUserReviews(Long userId, Long current, Long size);

    /**
     * 查询热门评论
     */
    List<ReviewVO> getHotReviews(Long toolId, Integer limit);

    /**
     * 标记评论有帮助
     */
    boolean markHelpful(Long id, Long userId);

    /**
     * 取消有帮助标记
     */
    boolean unmarkHelpful(Long id, Long userId);

    /**
     * 检查用户是否已评论过该工具
     */
    boolean hasReviewed(Long toolId, Long userId);

    /**
     * 查询评分分布
     */
    Map<String, Object> getRatingDistribution(Long toolId);
}
