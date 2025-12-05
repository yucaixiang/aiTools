package com.toolrecommend.ai.service;

import com.toolrecommend.common.vo.ToolVO;

import java.util.List;

/**
 * 工具推荐服务接口
 *
 * @author Tool Recommend Team
 */
public interface ToolRecommendService {

    /**
     * 基于用户查询推荐工具
     */
    List<ToolVO> recommend(String query, int limit);

    /**
     * 基于工具ID推荐相似工具
     */
    List<ToolVO> recommendSimilar(Long toolId, int limit);

    /**
     * 基于用户历史行为推荐工具
     */
    List<ToolVO> recommendByUserHistory(Long userId, int limit);
}
