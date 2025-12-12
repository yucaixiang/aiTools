package com.toolrecommend.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.entity.Favorite;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.ToolDetailVO;
import com.toolrecommend.common.vo.ToolVO;
import com.toolrecommend.tool.service.ToolService;
import com.toolrecommend.user.mapper.FavoriteMapper;
import com.toolrecommend.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏服务实现类
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ToolService toolService;

    @Override
    public boolean addFavorite(Long userId, Long toolId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getToolId, toolId);

        Long count = favoriteMapper.selectCount(wrapper);
        if (count > 0) {
            return true; // 已经收藏过了
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setToolId(toolId);

        return favoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long toolId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getToolId, toolId);

        return favoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isFavorited(Long userId, Long toolId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getToolId, toolId);

        Long count = favoriteMapper.selectCount(wrapper);
        return count > 0;
    }

    @Override
    public PageResult<ToolVO> getUserFavorites(Long userId, Long current, Long size) {
        // 分页查询用户的收藏
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, wrapper);

        // 获取工具ID列表
        List<Long> toolIds = favoritePage.getRecords().stream()
                .map(Favorite::getToolId)
                .toList();

        // 直接调用ToolService获取工具详情
        List<ToolVO> tools = new ArrayList<>();
        if (!toolIds.isEmpty()) {
            for (Long toolId : toolIds) {
                try {
                    // 直接调用本地ToolService（单体应用内部调用）
                    ToolDetailVO detailVO = toolService.getToolDetail(toolId, userId);

                    // 将ToolDetailVO转换为ToolVO
                    ToolVO toolVO = new ToolVO();
                    BeanUtils.copyProperties(detailVO, toolVO);
                    tools.add(toolVO);
                } catch (Exception e) {
                    // 忽略单个工具查询失败,不影响其他工具
                    log.warn("获取工具详情失败, toolId: {}, 原因: {}", toolId, e.getMessage());
                }
            }
        }

        PageResult<ToolVO> result = new PageResult<>();
        result.setRecords(tools);
        result.setTotal(favoritePage.getTotal());
        result.setCurrent(favoritePage.getCurrent());
        result.setSize(favoritePage.getSize());
        result.setPages(favoritePage.getPages());

        return result;
    }
}
