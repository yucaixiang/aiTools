package com.toolrecommend.rating.controller;

import com.toolrecommend.common.dto.RatingDTO;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.RatingStatsVO;
import com.toolrecommend.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评分控制器
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 提交或更新评分
     */
    @PostMapping
    public Result<Void> submitRating(
            @Valid @RequestBody RatingDTO ratingDTO,
            @RequestHeader("X-User-Id") Long userId) {
        ratingService.submitRating(userId, ratingDTO);
        return Result.success();
    }

    /**
     * 获取工具的评分统计
     */
    @GetMapping("/tool/{toolId}")
    public Result<RatingStatsVO> getToolRatingStats(
            @PathVariable Long toolId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        RatingStatsVO stats = ratingService.getToolRatingStats(toolId, userId);
        return Result.success(stats);
    }

    /**
     * 删除评分
     */
    @DeleteMapping("/tool/{toolId}")
    public Result<Void> deleteRating(
            @PathVariable Long toolId,
            @RequestHeader("X-User-Id") Long userId) {
        ratingService.deleteRating(userId, toolId);
        return Result.success();
    }

    /**
     * 检查是否已评分
     */
    @GetMapping("/tool/{toolId}/check")
    public Result<Boolean> checkRating(
            @PathVariable Long toolId,
            @RequestHeader("X-User-Id") Long userId) {
        boolean hasRated = ratingService.hasRated(userId, toolId);
        return Result.success(hasRated);
    }
}
