package com.toolrecommend.review.controller;

import com.toolrecommend.common.dto.ReviewCreateDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ReviewVO;
import com.toolrecommend.review.service.ReviewService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /**
     * 创建评论
     */
    @PostMapping
    public Result<Long> createReview(
            @Validated @RequestBody ReviewCreateDTO createDTO,
            @RequestHeader("User-Id") Long userId) {
        Long reviewId = reviewService.createReview(createDTO, userId);
        return Result.success("评论成功", reviewId);
    }

    /**
     * 更新评论
     */
    @PutMapping("/{id}")
    public Result<Void> updateReview(
            @PathVariable Long id,
            @Validated @RequestBody ReviewCreateDTO updateDTO,
            @RequestHeader("User-Id") Long userId) {
        boolean success = reviewService.updateReview(id, updateDTO, userId);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = reviewService.deleteReview(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 分页查询工具评论
     */
    @GetMapping("/tool/{toolId}")
    public Result<PageResult<ReviewVO>> getToolReviews(
            @PathVariable Long toolId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<ReviewVO> result = reviewService.getToolReviews(toolId, current, size);
        return Result.success(result);
    }

    /**
     * 分页查询用户评论
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<ReviewVO>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<ReviewVO> result = reviewService.getUserReviews(userId, current, size);
        return Result.success(result);
    }

    /**
     * 查询热门评论
     */
    @GetMapping("/tool/{toolId}/hot")
    public Result<List<ReviewVO>> getHotReviews(
            @PathVariable Long toolId,
            @RequestParam(defaultValue = "3") Integer limit) {
        List<ReviewVO> reviews = reviewService.getHotReviews(toolId, limit);
        return Result.success(reviews);
    }

    /**
     * 标记评论有帮助
     */
    @PostMapping("/{id}/helpful")
    public Result<Void> markHelpful(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = reviewService.markHelpful(id, userId);
        return success ? Result.success("标记成功") : Result.error("已标记过");
    }

    /**
     * 取消有帮助标记
     */
    @DeleteMapping("/{id}/helpful")
    public Result<Void> unmarkHelpful(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = reviewService.unmarkHelpful(id, userId);
        return success ? Result.success("取消成功") : Result.error("未标记过");
    }

    /**
     * 检查用户是否已评论过该工具
     */
    @GetMapping("/check")
    public Result<Boolean> hasReviewed(
            @RequestParam Long toolId,
            @RequestHeader("User-Id") Long userId) {
        boolean hasReviewed = reviewService.hasReviewed(toolId, userId);
        return Result.success(hasReviewed);
    }

    /**
     * 查询评分分布
     */
    @GetMapping("/tool/{toolId}/rating-distribution")
    public Result<Map<String, Object>> getRatingDistribution(@PathVariable Long toolId) {
        Map<String, Object> distribution = reviewService.getRatingDistribution(toolId);
        return Result.success(distribution);
    }
}
