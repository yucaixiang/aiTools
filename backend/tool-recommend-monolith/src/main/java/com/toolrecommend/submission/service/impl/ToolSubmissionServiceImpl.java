package com.toolrecommend.submission.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.dto.ToolSubmissionCreateDTO;
import com.toolrecommend.common.dto.ToolSubmissionReviewDTO;
import com.toolrecommend.common.entity.Tool;
import com.toolrecommend.common.entity.ToolSubmission;
import com.toolrecommend.common.exception.BusinessException;
import com.toolrecommend.common.vo.ToolSubmissionVO;
import com.toolrecommend.submission.mapper.ToolSubmissionMapper;
import com.toolrecommend.submission.service.ToolSubmissionService;
import com.toolrecommend.tool.mapper.ToolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工具提交服务实现类
 *
 * @author Tool Recommend Team
 */
@Service
@RequiredArgsConstructor
public class ToolSubmissionServiceImpl implements ToolSubmissionService {

    private final ToolSubmissionMapper toolSubmissionMapper;
    private final ToolMapper toolMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitTool(ToolSubmissionCreateDTO dto, Long userId) {
        // 创建提交记录
        ToolSubmission submission = new ToolSubmission();
        BeanUtils.copyProperties(dto, submission);
        submission.setUserId(userId);
        submission.setStatus(0); // 待审核
        submission.setCreatedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());

        toolSubmissionMapper.insert(submission);
        return submission.getId();
    }

    @Override
    public IPage<ToolSubmissionVO> getSubmissions(Page<ToolSubmissionVO> page, Integer status, Long userId) {
        return toolSubmissionMapper.selectSubmissionsWithDetails(page, status, userId);
    }

    @Override
    public IPage<ToolSubmissionVO> getMySubmissions(Page<ToolSubmissionVO> page, Long userId) {
        return toolSubmissionMapper.selectSubmissionsWithDetails(page, null, userId);
    }

    @Override
    public ToolSubmissionVO getSubmissionById(Long id) {
        ToolSubmissionVO submission = toolSubmissionMapper.selectSubmissionDetailById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        return submission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewSubmission(Long id, ToolSubmissionReviewDTO dto, Long reviewerId) {
        // 查询提交记录
        ToolSubmission submission = toolSubmissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        // 检查是否已审核
        if (submission.getStatus() != 0) {
            throw new BusinessException("该提交已经审核过了");
        }

        // 检查审核状态是否合法
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BusinessException("审核状态不合法");
        }

        // 拒绝时必须填写审核意见
        if (dto.getStatus() == 2 && (dto.getReviewComment() == null || dto.getReviewComment().trim().isEmpty())) {
            throw new BusinessException("拒绝时必须填写审核意见");
        }

        // 更新提交记录
        submission.setStatus(dto.getStatus());
        submission.setReviewComment(dto.getReviewComment());
        submission.setReviewedBy(reviewerId);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());

        int updated = toolSubmissionMapper.updateById(submission);

        // 如果审核通过，自动创建工具记录
        if (dto.getStatus() == 1) {
            createToolFromSubmission(submission);
        }

        return updated > 0;
    }

    @Override
    public Long countPendingSubmissions() {
        return toolSubmissionMapper.countPendingSubmissions();
    }

    /**
     * 根据提交记录创建工具
     *
     * @param submission 提交记录
     */
    private void createToolFromSubmission(ToolSubmission submission) {
        Tool tool = new Tool();

        // 基本信息
        tool.setName(submission.getName());
        tool.setSlug(generateSlug(submission.getName()));
        tool.setTagline(submission.getTagline());
        tool.setDescription(submission.getDescription());
        tool.setLogoUrl(submission.getLogoUrl());
        tool.setWebsiteUrl(submission.getWebsiteUrl());

        // 分类和定价
        tool.setCategoryId(submission.getCategoryId().intValue());
        tool.setPricingModel(submission.getPricingModel());

        // 发布日期
        if (submission.getLaunchDate() != null) {
            tool.setLaunchDate(submission.getLaunchDate().toLocalDate());
        }

        // 创建者
        tool.setMakerId(submission.getUserId());

        // 状态为已发布
        tool.setStatus(1);

        // 初始化统计数据
        tool.setViewCount(0);
        tool.setFavoriteCount(0);
        tool.setUpvoteCount(0);
        tool.setReviewCount(0);
        tool.setAverageRating(BigDecimal.ZERO);
        tool.setProfileCompleteness(calculateProfileCompleteness(tool));
        tool.setMonthlyGrowthRate(BigDecimal.ZERO);

        // 时间字段由MyBatis Plus自动填充
        toolMapper.insert(tool);
    }

    /**
     * 生成URL友好的slug
     *
     * @param name 工具名称
     * @return slug
     */
    private String generateSlug(String name) {
        // 转小写，替换空格和特殊字符为连字符
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // 如果slug为空或太短，使用时间戳
        if (slug.isEmpty() || slug.length() < 2) {
            slug = "tool-" + System.currentTimeMillis();
        }

        // 检查slug是否已存在，如果存在则添加数字后缀
        String finalSlug = slug;
        int counter = 1;
        while (toolMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Tool>()
                .eq("slug", finalSlug)) > 0) {
            finalSlug = slug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }

    /**
     * 计算信息完整度
     *
     * @param tool 工具对象
     * @return 完整度（0-100）
     */
    private BigDecimal calculateProfileCompleteness(Tool tool) {
        int totalFields = 8; // 总字段数
        int completedFields = 0;

        // 必填字段
        if (tool.getName() != null && !tool.getName().isEmpty()) completedFields++;
        if (tool.getTagline() != null && !tool.getTagline().isEmpty()) completedFields++;
        if (tool.getDescription() != null && !tool.getDescription().isEmpty()) completedFields++;
        if (tool.getWebsiteUrl() != null && !tool.getWebsiteUrl().isEmpty()) completedFields++;

        // 可选但重要的字段
        if (tool.getLogoUrl() != null && !tool.getLogoUrl().isEmpty()) completedFields++;
        if (tool.getCategoryId() != null) completedFields++;
        if (tool.getPricingModel() != null && !tool.getPricingModel().isEmpty()) completedFields++;
        if (tool.getLaunchDate() != null) completedFields++;

        return BigDecimal.valueOf((completedFields * 100.0) / totalFields)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
