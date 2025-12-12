package com.toolrecommend.user.service;

import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.ToolVO;

/**
 * 收藏服务接口
 *
 * @author Tool Recommend Team
 */
public interface FavoriteService {

    /**
     * 添加收藏
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long toolId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long toolId);

    /**
     * 检查是否已收藏
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long toolId);

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 收藏的工具列表
     */
    PageResult<ToolVO> getUserFavorites(Long userId, Long current, Long size);
}
