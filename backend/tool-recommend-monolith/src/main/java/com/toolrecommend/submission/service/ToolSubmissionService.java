package com.toolrecommend.submission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.dto.ToolSubmissionCreateDTO;
import com.toolrecommend.common.dto.ToolSubmissionReviewDTO;
import com.toolrecommend.common.vo.ToolSubmissionVO;

/**
 * 工具提交服务接口
 *
 * @author Tool Recommend Team
 */
public interface ToolSubmissionService {

    /**
     * 用户提交工具
     *
     * @param dto 工具提交数据
     * @param userId 用户ID
     * @return 提交ID
     */
    Long submitTool(ToolSubmissionCreateDTO dto, Long userId);

    /**
     * 分页查询工具提交列表
     *
     * @param page 分页对象
     * @param status 审核状态（可选）
     * @param userId 用户ID（可选，查询指定用户的提交）
     * @return 提交列表
     */
    IPage<ToolSubmissionVO> getSubmissions(Page<ToolSubmissionVO> page, Integer status, Long userId);

    /**
     * 查询用户自己的提交列表
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @return 提交列表
     */
    IPage<ToolSubmissionVO> getMySubmissions(Page<ToolSubmissionVO> page, Long userId);

    /**
     * 根据ID查询提交详情
     *
     * @param id 提交ID
     * @return 提交详情
     */
    ToolSubmissionVO getSubmissionById(Long id);

    /**
     * 管理员审核工具提交
     *
     * @param id 提交ID
     * @param dto 审核数据
     * @param reviewerId 审核人ID
     * @return 是否成功
     */
    boolean reviewSubmission(Long id, ToolSubmissionReviewDTO dto, Long reviewerId);

    /**
     * 统计待审核提交数量
     *
     * @return 待审核数量
     */
    Long countPendingSubmissions();
}
