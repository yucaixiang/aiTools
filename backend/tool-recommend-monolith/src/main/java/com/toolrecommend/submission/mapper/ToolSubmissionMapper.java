package com.toolrecommend.submission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.entity.ToolSubmission;
import com.toolrecommend.common.vo.ToolSubmissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工具提交Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface ToolSubmissionMapper extends BaseMapper<ToolSubmission> {

    /**
     * 分页查询工具提交列表（包含用户、分类信息）
     *
     * @param page 分页对象
     * @param status 审核状态
     * @param userId 用户ID（可选，查询指定用户的提交）
     * @return 提交列表
     */
    IPage<ToolSubmissionVO> selectSubmissionsWithDetails(
            Page<ToolSubmissionVO> page,
            @Param("status") Integer status,
            @Param("userId") Long userId
    );

    /**
     * 根据ID查询提交详情（包含用户、分类、审核人信息）
     *
     * @param id 提交ID
     * @return 提交详情
     */
    ToolSubmissionVO selectSubmissionDetailById(@Param("id") Long id);

    /**
     * 统计待审核提交数量
     *
     * @return 待审核数量
     */
    Long countPendingSubmissions();
}
