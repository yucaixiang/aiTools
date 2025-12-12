package com.toolrecommend.user.controller;

import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ToolVO;
import com.toolrecommend.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/{toolId}")
    public Result<Void> addFavorite(
            @PathVariable Long toolId,
            @RequestHeader("X-User-Id") Long userId) {
        favoriteService.addFavorite(userId, toolId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{toolId}")
    public Result<Void> removeFavorite(
            @PathVariable Long toolId,
            @RequestHeader("X-User-Id") Long userId) {
        favoriteService.removeFavorite(userId, toolId);
        return Result.success();
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/{toolId}/check")
    public Result<Boolean> checkFavorite(
            @PathVariable Long toolId,
            @RequestHeader("X-User-Id") Long userId) {
        boolean isFavorited = favoriteService.isFavorited(userId, toolId);
        return Result.success(isFavorited);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my")
    public Result<PageResult<ToolVO>> getMyFavorites(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestHeader("X-User-Id") Long userId) {
        PageResult<ToolVO> result = favoriteService.getUserFavorites(userId, current, size);
        return Result.success(result);
    }
}
