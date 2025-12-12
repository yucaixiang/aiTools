package com.toolrecommend.submission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.dto.ToolSubmissionCreateDTO;
import com.toolrecommend.common.dto.ToolSubmissionReviewDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ToolSubmissionVO;
import com.toolrecommend.submission.service.ToolSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 工具提交控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class ToolSubmissionController {

    private final ToolSubmissionService toolSubmissionService;

    /**
     * 用户提交工具
     */
    @PostMapping
    public Result<Long> submitTool(
            @Validated @RequestBody ToolSubmissionCreateDTO dto,
            @RequestHeader("X-User-Id") Long userId) {
        Long submissionId = toolSubmissionService.submitTool(dto, userId);
        return Result.success("提交成功，请等待审核", submissionId);
    }

    /**
     * 分页查询工具提交列表（管理员使用，可按状态筛选）
     */
    @GetMapping
    public Result<PageResult<ToolSubmissionVO>> getSubmissions(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {
        Page<ToolSubmissionVO> page = new Page<>(current, size);
        IPage<ToolSubmissionVO> result = toolSubmissionService.getSubmissions(page, status, userId);

        PageResult<ToolSubmissionVO> pageResult = PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        return Result.success(pageResult);
    }

    /**
     * 查询当前用户的提交列表
     */
    @GetMapping("/my")
    public Result<PageResult<ToolSubmissionVO>> getMySubmissions(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestHeader("X-User-Id") Long userId) {
        Page<ToolSubmissionVO> page = new Page<>(current, size);
        IPage<ToolSubmissionVO> result = toolSubmissionService.getMySubmissions(page, userId);

        PageResult<ToolSubmissionVO> pageResult = PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        return Result.success(pageResult);
    }

    /**
     * 根据ID查询提交详情
     */
    @GetMapping("/{id}")
    public Result<ToolSubmissionVO> getSubmissionById(@PathVariable Long id) {
        ToolSubmissionVO submission = toolSubmissionService.getSubmissionById(id);
        return Result.success(submission);
    }

    /**
     * 管理员审核工具提交
     */
    @PutMapping("/{id}/review")
    public Result<String> reviewSubmission(
            @PathVariable Long id,
            @Validated @RequestBody ToolSubmissionReviewDTO dto,
            @RequestHeader("X-User-Id") Long reviewerId) {
        // TODO: 在实际应用中应该检查 reviewerId 是否有管理员权限
        // 可以通过 @RequestHeader("X-User-Role") String role 获取角色信息
        boolean success = toolSubmissionService.reviewSubmission(id, dto, reviewerId);

        if (success) {
            String message = dto.getStatus() == 1 ? "审核通过，工具已发布" : "审核拒绝";
            return Result.success(message);
        } else {
            return Result.error("审核失败");
        }
    }

    /**
     * 统计待审核提交数量（管理员使用）
     */
    @GetMapping("/stats/pending")
    public Result<Long> countPendingSubmissions() {
        Long count = toolSubmissionService.countPendingSubmissions();
        return Result.success(count);
    }
}
