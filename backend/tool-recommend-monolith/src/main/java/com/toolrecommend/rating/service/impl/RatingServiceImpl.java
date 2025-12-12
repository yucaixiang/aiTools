package com.toolrecommend.rating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toolrecommend.common.dto.RatingDTO;
import com.toolrecommend.common.vo.RatingStatsVO;
import com.toolrecommend.rating.entity.Rating;
import com.toolrecommend.rating.mapper.RatingMapper;
import com.toolrecommend.rating.service.RatingService;
import com.toolrecommend.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 评分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingMapper ratingMapper;
    private final ToolService toolService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long userId, RatingDTO ratingDTO) {
        // 查询是否已有评分
        Rating existing = ratingMapper.getUserRating(ratingDTO.getToolId(), userId);

        if (existing != null) {
            // 更新评分
            existing.setScore(ratingDTO.getScore());
            ratingMapper.updateById(existing);
            log.info("用户{}更新工具{}的评分为{}", userId, ratingDTO.getToolId(), ratingDTO.getScore());
        } else {
            // 创建新评分
            Rating rating = new Rating();
            rating.setToolId(ratingDTO.getToolId());
            rating.setUserId(userId);
            rating.setScore(ratingDTO.getScore());
            ratingMapper.insert(rating);
            log.info("用户{}对工具{}评分{}", userId, ratingDTO.getToolId(), ratingDTO.getScore());
        }

        // 更新工具的平均评分
        updateToolAverageRating(ratingDTO.getToolId());
    }

    @Override
    public RatingStatsVO getToolRatingStats(Long toolId, Long userId) {
        Map<String, Object> stats = ratingMapper.getToolRatingStats(toolId);

        RatingStatsVO vo = new RatingStatsVO();
        vo.setAverageRating((BigDecimal) stats.get("averageRating"));
        vo.setRatingCount(((Long) stats.get("ratingCount")).intValue());

        // 如果用户已登录，获取用户的评分
        if (userId != null) {
            Rating userRating = ratingMapper.getUserRating(toolId, userId);
            if (userRating != null) {
                vo.setUserScore(userRating.getScore());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRating(Long userId, Long toolId) {
        LambdaQueryWrapper<Rating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Rating::getToolId, toolId)
               .eq(Rating::getUserId, userId);

        int deleted = ratingMapper.delete(wrapper);
        if (deleted > 0) {
            log.info("用户{}删除对工具{}的评分", userId, toolId);
            // 更新工具的平均评分
            updateToolAverageRating(toolId);
        }
    }

    @Override
    public boolean hasRated(Long userId, Long toolId) {
        return ratingMapper.getUserRating(toolId, userId) != null;
    }

    /**
     * 更新工具的平均评分
     */
    private void updateToolAverageRating(Long toolId) {
        BigDecimal avgRating = ratingMapper.getAverageRating(toolId);
        toolService.updateAverageRating(toolId, avgRating);
    }
}
