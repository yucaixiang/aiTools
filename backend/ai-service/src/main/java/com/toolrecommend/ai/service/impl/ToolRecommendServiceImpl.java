package com.toolrecommend.ai.service.impl;


import com.toolrecommend.ai.service.ToolRecommendService;
import com.toolrecommend.common.entity.Tool;
import com.toolrecommend.common.vo.ToolVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具推荐服务实现类
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Service
public class ToolRecommendServiceImpl implements ToolRecommendService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 关键词字典（用于简单的关键词匹配）
     */
    private static final Map<String, List<String>> KEYWORD_DICT = new HashMap<>();

    static {
        KEYWORD_DICT.put("笔记", Arrays.asList("笔记", "记录", "文档", "markdown", "notion", "obsidian"));
        KEYWORD_DICT.put("设计", Arrays.asList("设计", "UI", "原型", "绘图", "figma", "sketch"));
        KEYWORD_DICT.put("开发", Arrays.asList("开发", "编程", "代码", "IDE", "编辑器", "vscode", "git"));
        KEYWORD_DICT.put("AI", Arrays.asList("AI", "人工智能", "机器学习", "ChatGPT", "GPT"));
        KEYWORD_DICT.put("协作", Arrays.asList("协作", "团队", "项目管理", "任务", "trello", "slack"));
    }

    @Override
    public List<ToolVO> recommend(String query, int limit) {
        log.info("工具推荐: query={}, limit={}", query, limit);

        // 提取关键词
        Set<String> keywords = extractKeywords(query);
        log.debug("提取的关键词: {}", keywords);

        if (keywords.isEmpty()) {
            // 如果没有关键词，返回热门工具
            return getHotTools(limit);
        }

        // 基于关键词搜索工具
        List<Tool> tools = searchToolsByKeywords(keywords, limit * 2);

        // 转换为ToolVO并排序
        List<ToolVO> toolVOs = tools.stream()
                .map(this::convertToToolVO)
                .limit(limit)
                .collect(Collectors.toList());

        log.info("推荐工具数量: {}", toolVOs.size());
        return toolVOs;
    }

    @Override
    public List<ToolVO> recommendSimilar(Long toolId, int limit) {
        String sql = "SELECT t.* FROM tool t " +
                "INNER JOIN tool_tag tt1 ON t.id = tt1.tool_id " +
                "WHERE t.status = 1 AND t.id != ? " +
                "AND tt1.tag_id IN ( " +
                "  SELECT tt2.tag_id FROM tool_tag tt2 WHERE tt2.tool_id = ? " +
                ") " +
                "GROUP BY t.id " +
                "ORDER BY COUNT(tt1.tag_id) DESC, t.upvote_count DESC " +
                "LIMIT ?";

        List<Tool> tools = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Tool.class),
                toolId, toolId, limit);

        return tools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolVO> recommendByUserHistory(Long userId, int limit) {
        // 查询用户最近浏览/收藏的工具分类
        String sql = "SELECT DISTINCT t.category_id " +
                "FROM user_action ua " +
                "INNER JOIN tool t ON ua.tool_id = t.id " +
                "WHERE ua.user_id = ? " +
                "AND ua.action_type IN ('VIEW', 'FAVORITE', 'UPVOTE') " +
                "ORDER BY ua.created_at DESC " +
                "LIMIT 3";

        List<Integer> categoryIds = jdbcTemplate.queryForList(sql, Integer.class, userId);

        if (categoryIds.isEmpty()) {
            return getHotTools(limit);
        }

        // 推荐同类工具
        String recommendSql = "SELECT * FROM tool " +
                "WHERE status = 1 " +
                "AND category_id IN (" + String.join(",", categoryIds.stream().map(String::valueOf).collect(Collectors.toList())) + ") " +
                "AND id NOT IN ( " +
                "  SELECT tool_id FROM user_action WHERE user_id = ? " +
                ") " +
                "ORDER BY upvote_count DESC, average_rating DESC " +
                "LIMIT ?";

        List<Tool> tools = jdbcTemplate.query(recommendSql, new BeanPropertyRowMapper<>(Tool.class),
                userId, limit);

        return tools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());
    }

    /**
     * 提取查询中的关键词
     */
    private Set<String> extractKeywords(String query) {
        Set<String> keywords = new HashSet<>();
        query = query.toLowerCase();

        // 简单的关键词匹配
        for (Map.Entry<String, List<String>> entry : KEYWORD_DICT.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (query.contains(keyword.toLowerCase())) {
                    keywords.add(keyword);
                }
            }
        }

        return keywords;
    }

    /**
     * 基于关键词搜索工具
     */
    private List<Tool> searchToolsByKeywords(Set<String> keywords, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tool WHERE status = 1 AND (");

        List<String> conditions = new ArrayList<>();
        for (String keyword : keywords) {
            conditions.add("(name LIKE '%" + keyword + "%' OR tagline LIKE '%" + keyword + "%' OR description LIKE '%" + keyword + "%')");
        }

        sql.append(String.join(" OR ", conditions));
        sql.append(") ORDER BY upvote_count DESC, average_rating DESC LIMIT ?");

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Tool.class), limit);
    }

    /**
     * 获取热门工具
     */
    private List<ToolVO> getHotTools(int limit) {
        String sql = "SELECT * FROM tool WHERE status = 1 " +
                "ORDER BY upvote_count DESC, average_rating DESC LIMIT ?";

        List<Tool> tools = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Tool.class), limit);

        return tools.stream()
                .map(this::convertToToolVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为ToolVO
     */
    private ToolVO convertToToolVO(Tool tool) {
        ToolVO vo = new ToolVO();
        BeanUtils.copyProperties(tool, vo);
        return vo;
    }
}
