package com.toolrecommend.tool.controller;

import com.toolrecommend.common.dto.ToolCreateDTO;
import com.toolrecommend.common.dto.ToolQueryDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ToolDetailVO;
import com.toolrecommend.common.vo.ToolVO;
import com.toolrecommend.tool.service.ToolService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 工具控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Resource
    private ToolService toolService;

    /**
     * 分页查询工具列表
     */
    @PostMapping("/query")
    public Result<PageResult<ToolVO>> queryTools(@RequestBody ToolQueryDTO queryDTO) {
        PageResult<ToolVO> result = toolService.queryTools(queryDTO);
        return Result.success(result);
    }

    /**
     * 根据ID查询工具详情
     */
    @GetMapping("/{id}")
    public Result<ToolDetailVO> getToolDetail(
            @PathVariable Long id,
            @RequestHeader(value = "User-Id", required = false) Long userId) {

        // 增加浏览量
        toolService.incrementViewCount(id);

        ToolDetailVO detail = toolService.getToolDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 创建工具
     */
    @PostMapping
    public Result<Long> createTool(
            @Validated @RequestBody ToolCreateDTO createDTO,
            @RequestHeader("User-Id") Long userId) {
        Long toolId = toolService.createTool(createDTO, userId);
        return Result.success("工具创建成功", toolId);
    }

    /**
     * 更新工具
     */
    @PutMapping("/{id}")
    public Result<String> updateTool(
            @PathVariable Long id,
            @Validated @RequestBody ToolCreateDTO updateDTO,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.updateTool(id, updateDTO, userId);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTool(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.deleteTool(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 点赞工具
     */
    @PostMapping("/{id}/upvote")
    public Result<String> upvoteTool(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.upvoteTool(id, userId);
        return success ? Result.success("点赞成功") : Result.error("已经点赞过了");
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/upvote")
    public Result<String> cancelUpvote(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.cancelUpvote(id, userId);
        return success ? Result.success("取消点赞成功") : Result.error("未点赞");
    }

    /**
     * 收藏工具
     */
    @PostMapping("/{id}/favorite")
    public Result<String> favoriteTool(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.favoriteTool(id, userId);
        return success ? Result.success("收藏成功") : Result.error("已经收藏过了");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}/favorite")
    public Result<String> cancelFavorite(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        boolean success = toolService.cancelFavorite(id, userId);
        return success ? Result.success("取消收藏成功") : Result.error("未收藏");
    }

    /**
     * 查询热门工具
     */
    @GetMapping("/hot")
    public Result<List<ToolVO>> getHotTools(@RequestParam(defaultValue = "10") Integer limit) {
        List<ToolVO> tools = toolService.getHotTools(limit);
        return Result.success(tools);
    }

    /**
     * 查询最新工具
     */
    @GetMapping("/latest")
    public Result<List<ToolVO>> getLatestTools(@RequestParam(defaultValue = "10") Integer limit) {
        List<ToolVO> tools = toolService.getLatestTools(limit);
        return Result.success(tools);
    }

    /**
     * 查询相似工具
     */
    @GetMapping("/{id}/similar")
    public Result<List<ToolVO>> getSimilarTools(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") Integer limit) {
        List<ToolVO> tools = toolService.getSimilarTools(id, limit);
        return Result.success(tools);
    }

    /**
     * 搜索工具
     */
    @GetMapping("/search")
    public Result<PageResult<ToolVO>> searchTools(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        PageResult<ToolVO> result = toolService.searchTools(keyword, current, size);
        return Result.success(result);
    }
}
