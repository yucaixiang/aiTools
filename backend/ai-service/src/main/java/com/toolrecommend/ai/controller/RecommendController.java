package com.toolrecommend.ai.controller;

import com.toolrecommend.ai.service.ToolRecommendService;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ToolVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工具推荐控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/ai/recommend")
public class RecommendController {

    @Resource
    private ToolRecommendService toolRecommendService;

    /**
     * 基于查询推荐工具
     */
    @GetMapping
    public Result<List<ToolVO>> recommend(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        List<ToolVO> tools = toolRecommendService.recommend(query, limit);
        return Result.success(tools);
    }

    /**
     * 推荐相似工具
     */
    @GetMapping("/similar/{toolId}")
    public Result<List<ToolVO>> recommendSimilar(
            @PathVariable Long toolId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ToolVO> tools = toolRecommendService.recommendSimilar(toolId, limit);
        return Result.success(tools);
    }

    /**
     * 基于用户历史推荐
     */
    @GetMapping("/personalized")
    public Result<List<ToolVO>> recommendPersonalized(
            @RequestHeader("User-Id") Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ToolVO> tools = toolRecommendService.recommendByUserHistory(userId, limit);
        return Result.success(tools);
    }
}
