package com.toolrecommend.admin.controller;

import com.toolrecommend.common.dto.ToolQueryDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ToolVO;
import com.toolrecommend.admin.service.AdminToolService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 管理后台 - 工具管理控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/admin/tools")
public class AdminToolController {

    @Resource
    private AdminToolService adminToolService;

    /**
     * 分页查询所有工具（包括待审核）
     */
    @PostMapping("/query")
    public Result<PageResult<ToolVO>> queryTools(@RequestBody ToolQueryDTO queryDTO) {
        PageResult<ToolVO> result = adminToolService.queryAll(queryDTO);
        return Result.success(result);
    }

    /**
     * 审核通过工具
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approveTool(
            @PathVariable Long id,
            @RequestHeader("Admin-Id") Long adminId) {
        boolean success = adminToolService.approveTool(id, adminId);
        return success ? Result.success("审核通过") : Result.error("审核失败");
    }

    /**
     * 拒绝工具
     */
    @PutMapping("/{id}/reject")
    public Result<Void> rejectTool(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader("Admin-Id") Long adminId) {
        boolean success = adminToolService.rejectTool(id, reason, adminId);
        return success ? Result.success("已拒绝") : Result.error("操作失败");
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTool(
            @PathVariable Long id,
            @RequestHeader("Admin-Id") Long adminId) {
        boolean success = adminToolService.deleteTool(id, adminId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 查询统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminToolService.getStatistics();
        return Result.success(stats);
    }
}
