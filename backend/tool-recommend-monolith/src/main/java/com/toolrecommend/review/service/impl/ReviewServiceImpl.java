package com.toolrecommend.review.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.dto.ReviewCreateDTO;
import com.toolrecommend.common.entity.Review;
import com.toolrecommend.common.exception.BusinessException;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.ReviewVO;
import com.toolrecommend.review.mapper.ReviewMapper;
import com.toolrecommend.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REVIEW_HELPFUL_KEY = "review:helpful:";
    private static final String TOOL_REVIEWS_CACHE_KEY = "tool:reviews:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(ReviewCreateDTO createDTO, Long userId) {
        // 评分与评论已分离，用户可以发表多条评论
        // 不再需要检查是否已评论过

        // 创建评论
        Review review = new Review();
        BeanUtils.copyProperties(createDTO, review);
        review.setUserId(userId);
        review.setStatus(1); // 默认已发布（可以改为0-待审核）
        review.setHelpfulCount(0);
        review.setReplyCount(0);

        // 处理优缺点JSON
        if (createDTO.getPros() != null && !createDTO.getPros().isEmpty()) {
            review.setPros(JSON.toJSONString(createDTO.getPros()));
        }
        if (createDTO.getCons() != null && !createDTO.getCons().isEmpty()) {
            review.setCons(JSON.toJSONString(createDTO.getCons()));
        }

        reviewMapper.insert(review);

        // 如果是回复，更新父评论的回复数
        if (createDTO.getParentId() != null) {
            reviewMapper.incrementReplyCount(createDTO.getParentId());
        }

        // 更新工具的评分统计（异步处理）
        updateToolRating(createDTO.getToolId());

        // 清除缓存
        clearReviewCache(createDTO.getToolId());

        log.info("评论创建成功: reviewId={}, toolId={}, userId={}, parentId={}",
                review.getId(), createDTO.getToolId(), userId, createDTO.getParentId());

        return review.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReview(Long id, ReviewCreateDTO updateDTO, Long userId) {
        Review existingReview = reviewMapper.selectById(id);
        if (existingReview == null) {
            throw new BusinessException(3002, "评论不存在");
        }

        // 检查权限
        if (!existingReview.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限修改他人评论");
        }

        // 更新评论
        Review review = new Review();
        BeanUtils.copyProperties(updateDTO, review);
        review.setId(id);

        if (updateDTO.getPros() != null) {
            review.setPros(JSON.toJSONString(updateDTO.getPros()));
        }
        if (updateDTO.getCons() != null) {
            review.setCons(JSON.toJSONString(updateDTO.getCons()));
        }

        int result = reviewMapper.updateById(review);

        // 更新工具评分
        updateToolRating(existingReview.getToolId());

        // 清除缓存
        clearReviewCache(existingReview.getToolId());

        log.info("评论更新成功: reviewId={}", id);

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReview(Long id, Long userId) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException(3002, "评论不存在");
        }

        // 检查权限
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限删除他人评论");
        }

        // 如果是回复，减少父评论的回复数
        if (review.getParentId() != null) {
            reviewMapper.decrementReplyCount(review.getParentId());
        }

        // 软删除：更新状态为已隐藏
        review.setStatus(2);
        int result = reviewMapper.updateById(review);

        // 更新工具评分
        updateToolRating(review.getToolId());

        // 清除缓存
        clearReviewCache(review.getToolId());

        log.info("评论删除成功: reviewId={}", id);

        return result > 0;
    }

    @Override
    public PageResult<ReviewVO> getToolReviews(Long toolId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        IPage<Review> reviewPage = reviewMapper.selectByToolId(page, toolId, 1);

        List<ReviewVO> reviewVOList = reviewPage.getRecords().stream()
                .map(this::convertToReviewVO)
                .collect(Collectors.toList());

        return PageResult.of(reviewVOList, reviewPage.getTotal(), reviewPage.getCurrent(), reviewPage.getSize());
    }

    @Override
    public PageResult<ReviewVO> getUserReviews(Long userId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        IPage<Review> reviewPage = reviewMapper.selectByUserId(page, userId);

        List<ReviewVO> reviewVOList = reviewPage.getRecords().stream()
                .map(this::convertToReviewVO)
                .collect(Collectors.toList());

        return PageResult.of(reviewVOList, reviewPage.getTotal(), reviewPage.getCurrent(), reviewPage.getSize());
    }

    @Override
    public List<ReviewVO> getHotReviews(Long toolId, Integer limit) {
        List<Review> reviews = reviewMapper.selectHotReviews(toolId, limit);

        return reviews.stream()
                .map(this::convertToReviewVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markHelpful(Long id, Long userId) {
        // 检查是否已标记
        String key = REVIEW_HELPFUL_KEY + id + ":" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return false;
        }

        // 增加有帮助数
        reviewMapper.incrementHelpfulCount(id);

        // 记录用户标记
        redisTemplate.opsForValue().set(key, "1");

        log.info("标记有帮助: reviewId={}, userId={}", id, userId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unmarkHelpful(Long id, Long userId) {
        // 检查是否已标记
        String key = REVIEW_HELPFUL_KEY + id + ":" + userId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return false;
        }

        // 减少有帮助数
        reviewMapper.decrementHelpfulCount(id);

        // 删除用户标记
        redisTemplate.delete(key);

        log.info("取消有帮助: reviewId={}, userId={}", id, userId);

        return true;
    }

    @Override
    public boolean hasReviewed(Long toolId, Long userId) {
        Review review = reviewMapper.selectByToolIdAndUserId(toolId, userId);
        return review != null;
    }

    @Override
    public Map<String, Object> getRatingDistribution(Long toolId) {
        // 评分已迁移到rating表，此方法已弃用
        Map<String, Object> result = new HashMap<>();
        result.put("distribution", Collections.emptyList());
        result.put("totalCount", 0);
        result.put("averageRating", 0.0);
        return result;
    }

    @Override
    public List<com.toolrecommend.common.vo.ReviewTreeVO> getToolReviewsTree(Long toolId, Long userId) {
        // 查询所有顶级评论
        List<Review> topLevelReviews = reviewMapper.selectTopLevelReviews(toolId, 1);

        // 转换为树形结构
        return topLevelReviews.stream()
                .map(review -> buildReviewTree(review, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ReviewVO> getReviewReplies(Long reviewId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        IPage<Review> replyPage = reviewMapper.selectRepliesByParentId(page, reviewId, 1);

        List<ReviewVO> replyVOList = replyPage.getRecords().stream()
                .map(this::convertToReviewVO)
                .collect(Collectors.toList());

        return PageResult.of(replyVOList, replyPage.getTotal(), replyPage.getCurrent(), replyPage.getSize());
    }

    /**
     * 更新工具评分（应该调用Tool Service的接口，这里简化处理）
     */
    private void updateToolRating(Long toolId) {
        // TODO: 调用Tool Service更新工具的评分和评论数
        // 实际应该通过Feign或RestTemplate调用
        log.debug("更新工具评分: toolId={}", toolId);
    }

    /**
     * 清除评论缓存
     */
    private void clearReviewCache(Long toolId) {
        String cacheKey = TOOL_REVIEWS_CACHE_KEY + toolId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 转换为ReviewVO
     */
    private ReviewVO convertToReviewVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 解析优缺点JSON
        if (review.getPros() != null && !review.getPros().isEmpty()) {
            vo.setPros(JSON.parseArray(review.getPros(), String.class));
        }
        if (review.getCons() != null && !review.getCons().isEmpty()) {
            vo.setCons(JSON.parseArray(review.getCons(), String.class));
        }

        // TODO: 查询用户信息
        // vo.setUser(userSimpleVO);

        return vo;
    }

    /**
     * 递归构建评论树
     */
    private com.toolrecommend.common.vo.ReviewTreeVO buildReviewTree(Review review, Long currentUserId) {
        com.toolrecommend.common.vo.ReviewTreeVO treeVO = new com.toolrecommend.common.vo.ReviewTreeVO();
        BeanUtils.copyProperties(review, treeVO);

        // 解析优缺点JSON
        if (review.getPros() != null && !review.getPros().isEmpty()) {
            treeVO.setPros(review.getPros());
        }
        if (review.getCons() != null && !review.getCons().isEmpty()) {
            treeVO.setCons(review.getCons());
        }

        // TODO: 查询用户信息并设置username和userAvatar
        // User user = userService.getById(review.getUserId());
        // treeVO.setUsername(user.getUsername());
        // treeVO.setUserAvatar(user.getAvatar());

        // 设置是否是当前用户的评论
        treeVO.setIsMine(currentUserId != null && currentUserId.equals(review.getUserId()));

        // 检查当前用户是否点了有帮助
        if (currentUserId != null) {
            String key = REVIEW_HELPFUL_KEY + review.getId() + ":" + currentUserId;
            treeVO.setIsHelpful(Boolean.TRUE.equals(redisTemplate.hasKey(key)));
        } else {
            treeVO.setIsHelpful(false);
        }

        // 递归查询所有回复
        List<Review> replies = reviewMapper.selectAllRepliesByParentId(review.getId(), 1);
        if (replies != null && !replies.isEmpty()) {
            List<com.toolrecommend.common.vo.ReviewTreeVO> replyTreeVOs = replies.stream()
                    .map(reply -> buildReviewTree(reply, currentUserId))
                    .collect(Collectors.toList());
            treeVO.setReplies(replyTreeVOs);
        } else {
            treeVO.setReplies(Collections.emptyList());
        }

        return treeVO;
    }
}
