package com.toolrecommend.tool.service;

import com.toolrecommend.common.dto.ToolCreateDTO;
import com.toolrecommend.common.dto.ToolQueryDTO;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.ToolDetailVO;
import com.toolrecommend.common.vo.ToolVO;

import java.util.List;

/**
 * 工具服务接口
 *
 * @author Tool Recommend Team
 */
public interface ToolService {

    /**
     * 分页查询工具列表
     */
    PageResult<ToolVO> queryTools(ToolQueryDTO queryDTO);

    /**
     * 根据ID查询工具详情
     */
    ToolDetailVO getToolDetail(Long id, Long currentUserId);

    /**
     * 创建工具
     */
    Long createTool(ToolCreateDTO createDTO, Long userId);

    /**
     * 更新工具
     */
    boolean updateTool(Long id, ToolCreateDTO updateDTO, Long userId);

    /**
     * 删除工具
     */
    boolean deleteTool(Long id, Long userId);

    /**
     * 增加浏览量
     */
    boolean incrementViewCount(Long id);

    /**
     * 点赞工具
     */
    boolean upvoteTool(Long id, Long userId);

    /**
     * 取消点赞
     */
    boolean cancelUpvote(Long id, Long userId);

    /**
     * 收藏工具
     */
    boolean favoriteTool(Long id, Long userId);

    /**
     * 取消收藏
     */
    boolean cancelFavorite(Long id, Long userId);

    /**
     * 查询热门工具
     */
    List<ToolVO> getHotTools(Integer limit);

    /**
     * 查询最新工具
     */
    List<ToolVO> getLatestTools(Integer limit);

    /**
     * 查询相似工具
     */
    List<ToolVO> getSimilarTools(Long toolId, Integer limit);

    /**
     * 搜索工具（全文搜索）
     */
    PageResult<ToolVO> searchTools(String keyword, Long current, Long size);

    /**
     * 更新工具的平均评分
     */
    void updateAverageRating(Long toolId, java.math.BigDecimal averageRating);
}
