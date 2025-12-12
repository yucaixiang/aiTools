package com.toolrecommend.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.dto.ToolCreateDTO;
import com.toolrecommend.common.dto.ToolQueryDTO;
import com.toolrecommend.common.entity.Category;
import com.toolrecommend.common.entity.Tag;
import com.toolrecommend.common.entity.Tool;
import com.toolrecommend.common.entity.UserAction;
import com.toolrecommend.common.exception.ToolNotFoundException;
import com.toolrecommend.common.result.PageResult;
import com.toolrecommend.common.vo.*;
import com.toolrecommend.tool.mapper.CategoryMapper;
import com.toolrecommend.tool.mapper.TagMapper;
import com.toolrecommend.tool.mapper.ToolMapper;
import com.toolrecommend.tool.mapper.UserActionMapper;
import com.toolrecommend.tool.service.ToolService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 工具服务实现类
 *
 * @author Tool Recommend Team
 */
@Service
public class ToolServiceImpl implements ToolService {

    @Resource
    private ToolMapper toolMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOOL_CACHE_KEY = "tool:detail:";
    private static final String HOT_TOOLS_CACHE_KEY = "tool:hot";
    private static final long CACHE_EXPIRE_TIME = 60;

    @Override
    public PageResult<ToolVO> queryTools(ToolQueryDTO queryDTO) {
        Page<Tool> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        IPage<Tool> toolPage = toolMapper.selectToolsWithTags(
                page,
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getPricingModel(),
                queryDTO.getMinRating(),
                queryDTO.getSortBy(),
                queryDTO.getSortOrder()
        );

        List<ToolVO> toolVOList = toolPage.getRecords().stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());

        return PageResult.of(toolVOList, toolPage.getTotal(), toolPage.getCurrent(), toolPage.getSize());
    }

    @Override
    public ToolDetailVO getToolDetail(Long id, Long currentUserId) {
        // 先从缓存获取
        String cacheKey = TOOL_CACHE_KEY + id;
        ToolDetailVO cachedTool = (ToolDetailVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTool != null) {
            // 设置用户相关信息
            if (currentUserId != null) {
                cachedTool.setIsFavorited(checkUserFavorite(id, currentUserId));
                cachedTool.setIsUpvoted(checkUserUpvote(id, currentUserId));
            }
            return cachedTool;
        }

        // 从数据库查询
        Tool tool = toolMapper.selectToolDetailById(id);
        if (tool == null || tool.getStatus() != 1) {
            throw new ToolNotFoundException(id);
        }

        ToolDetailVO detailVO = convertToToolDetailVO(tool);

        // 查询标签
        List<Tag> tags = tagMapper.selectTagsByToolId(id);
        detailVO.setTags(tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));

        // 查询分类
        Category category = categoryMapper.selectById(tool.getCategoryId());
        if (category != null) {
            detailVO.setCategory(convertToCategoryVO(category));
        }

        // 查询相似工具
        List<Tool> similarTools = toolMapper.selectSimilarTools(id, tool.getCategoryId(), 6);
        detailVO.setSimilarTools(similarTools.stream().map(this::convertToToolVO).collect(Collectors.toList()));

        // 设置用户相关信息
        if (currentUserId != null) {
            detailVO.setIsFavorited(checkUserFavorite(id, currentUserId));
            detailVO.setIsUpvoted(checkUserUpvote(id, currentUserId));
        }

        // 缓存工具详情
        redisTemplate.opsForValue().set(cacheKey, detailVO, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTool(ToolCreateDTO createDTO, Long userId) {
        Tool tool = new Tool();
        BeanUtils.copyProperties(createDTO, tool);
        tool.setMakerId(userId);
        tool.setStatus(0); // 草稿状态
        tool.setViewCount(0);
        tool.setFavoriteCount(0);
        tool.setUpvoteCount(0);
        tool.setReviewCount(0);
        tool.setAverageRating(BigDecimal.ZERO);
        tool.setProfileCompleteness(calculateCompleteness(tool));

        toolMapper.insert(tool);

        // 更新分类工具数量
        categoryMapper.incrementToolCount(tool.getCategoryId());

        // 处理标签关联
        if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
            // TODO: 插入tool_tag关联表
        }

        return tool.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTool(Long id, ToolCreateDTO updateDTO, Long userId) {
        Tool existingTool = toolMapper.selectById(id);
        if (existingTool == null) {
            throw new ToolNotFoundException(id);
        }

        // 检查权限（只有创建者或管理员可以编辑）
        // TODO: 添加权限检查

        Tool tool = new Tool();
        BeanUtils.copyProperties(updateDTO, tool);
        tool.setId(id);
        tool.setProfileCompleteness(calculateCompleteness(tool));

        int result = toolMapper.updateById(tool);

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTool(Long id, Long userId) {
        Tool tool = toolMapper.selectById(id);
        if (tool == null) {
            throw new ToolNotFoundException(id);
        }

        // 检查权限
        // TODO: 添加权限检查

        // 软删除：更新状态为已下架
        tool.setStatus(2);
        int result = toolMapper.updateById(tool);

        // 更新分类工具数量
        categoryMapper.decrementToolCount(tool.getCategoryId());

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return result > 0;
    }

    @Override
    public boolean incrementViewCount(Long id) {
        int result = toolMapper.incrementViewCount(id);
        if (result > 0) {
            redisTemplate.delete(TOOL_CACHE_KEY + id);
        }
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean upvoteTool(Long id, Long userId) {
        // 检查是否已点赞
        if (checkUserUpvote(id, userId)) {
            return false;
        }

        // 增加点赞数
        toolMapper.incrementUpvoteCount(id);

        // 记录用户行为
        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setToolId(id);
        action.setActionType("UPVOTE");
        userActionMapper.insert(action);

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelUpvote(Long id, Long userId) {
        // 检查是否已点赞
        if (!checkUserUpvote(id, userId)) {
            return false;
        }

        // 减少点赞数
        toolMapper.decrementUpvoteCount(id);

        // 删除用户行为记录
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
                .eq(UserAction::getToolId, id)
                .eq(UserAction::getActionType, "UPVOTE");
        userActionMapper.delete(wrapper);

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favoriteTool(Long id, Long userId) {
        // 检查是否已收藏
        if (checkUserFavorite(id, userId)) {
            return false;
        }

        // 增加收藏数
        toolMapper.incrementFavoriteCount(id);

        // 记录用户行为
        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setToolId(id);
        action.setActionType("FAVORITE");
        userActionMapper.insert(action);

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelFavorite(Long id, Long userId) {
        // 检查是否已收藏
        if (!checkUserFavorite(id, userId)) {
            return false;
        }

        // 减少收藏数
        toolMapper.decrementFavoriteCount(id);

        // 删除用户行为记录
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
                .eq(UserAction::getToolId, id)
                .eq(UserAction::getActionType, "FAVORITE");
        userActionMapper.delete(wrapper);

        // 清除缓存
        redisTemplate.delete(TOOL_CACHE_KEY + id);

        return true;
    }

    @Override
    public List<ToolVO> getHotTools(Integer limit) {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<ToolVO> cachedTools = (List<ToolVO>) redisTemplate.opsForValue().get(HOT_TOOLS_CACHE_KEY);
        if (cachedTools != null && !cachedTools.isEmpty()) {
            return cachedTools.stream().limit(limit).collect(Collectors.toList());
        }

        // 从数据库查询
        List<Tool> tools = toolMapper.selectHotTools(limit);
        List<ToolVO> toolVOList = tools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());

        // 缓存结果
        redisTemplate.opsForValue().set(HOT_TOOLS_CACHE_KEY, toolVOList, 30, TimeUnit.MINUTES);

        return toolVOList;
    }

    @Override
    public List<ToolVO> getLatestTools(Integer limit) {
        List<Tool> tools = toolMapper.selectLatestTools(limit);
        return tools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolVO> getSimilarTools(Long toolId, Integer limit) {
        Tool tool = toolMapper.selectById(toolId);
        if (tool == null) {
            return new ArrayList<>();
        }

        List<Tool> similarTools = toolMapper.selectSimilarTools(toolId, tool.getCategoryId(), limit);
        return similarTools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ToolVO> searchTools(String keyword, Long current, Long size) {
        ToolQueryDTO queryDTO = new ToolQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        return queryTools(queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAverageRating(Long toolId, BigDecimal averageRating) {
        Tool tool = new Tool();
        tool.setId(toolId);
        tool.setAverageRating(averageRating);
        toolMapper.updateById(tool);

        // 清除缓存
        String cacheKey = TOOL_CACHE_KEY + toolId;
        redisTemplate.delete(cacheKey);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检查用户是否已点赞
     */
    private boolean checkUserUpvote(Long toolId, Long userId) {
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
                .eq(UserAction::getToolId, toolId)
                .eq(UserAction::getActionType, "UPVOTE");
        return userActionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否已收藏
     */
    private boolean checkUserFavorite(Long toolId, Long userId) {
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
                .eq(UserAction::getToolId, toolId)
                .eq(UserAction::getActionType, "FAVORITE");
        return userActionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 计算工具信息完整度
     */
    private BigDecimal calculateCompleteness(Tool tool) {
        int score = 0;
        int total = 10;

        if (tool.getName() != null && !tool.getName().isEmpty()) score++;
        if (tool.getTagline() != null && !tool.getTagline().isEmpty()) score++;
        if (tool.getDescription() != null && !tool.getDescription().isEmpty()) score++;
        if (tool.getLogoUrl() != null && !tool.getLogoUrl().isEmpty()) score++;
        if (tool.getWebsiteUrl() != null && !tool.getWebsiteUrl().isEmpty()) score++;
        if (tool.getDownloadUrl() != null && !tool.getDownloadUrl().isEmpty()) score++;
        if (tool.getGithubUrl() != null && !tool.getGithubUrl().isEmpty()) score++;
        if (tool.getCategoryId() != null) score++;
        if (tool.getPricingModel() != null && !tool.getPricingModel().isEmpty()) score++;
        if (tool.getLaunchDate() != null) score++;

        return BigDecimal.valueOf((double) score / total);
    }

    /**
     * 转换为ToolVO
     */
    private ToolVO convertToToolVO(Tool tool) {
        ToolVO vo = new ToolVO();
        BeanUtils.copyProperties(tool, vo);

        // 查询分类名称
        Category category = categoryMapper.selectById(tool.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 查询标签
        List<Tag> tags = tagMapper.selectTagsByToolId(tool.getId());
        vo.setTags(tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 转换为ToolDetailVO
     */
    private ToolDetailVO convertToToolDetailVO(Tool tool) {
        ToolDetailVO vo = new ToolDetailVO();
        BeanUtils.copyProperties(tool, vo);
        return vo;
    }

    /**
     * 转换为TagVO
     */
    private TagVO convertToTagVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }

    /**
     * 转换为CategoryVO
     */
    private CategoryVO convertToCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
