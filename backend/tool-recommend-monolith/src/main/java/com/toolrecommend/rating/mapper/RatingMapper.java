package com.toolrecommend.rating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.rating.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 评分Mapper
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    /**
     * 获取工具的评分统计
     *
     * @param toolId 工具ID
     * @return 统计信息 {averageRating: 平均分, ratingCount: 评分人数}
     */
    @Select("SELECT " +
            "COALESCE(AVG(score), 0) as averageRating, " +
            "COUNT(*) as ratingCount " +
            "FROM rating " +
            "WHERE tool_id = #{toolId}")
    Map<String, Object> getToolRatingStats(@Param("toolId") Long toolId);

    /**
     * 获取用户对工具的评分
     *
     * @param toolId 工具ID
     * @param userId 用户ID
     * @return 评分记录
     */
    @Select("SELECT * FROM rating WHERE tool_id = #{toolId} AND user_id = #{userId}")
    Rating getUserRating(@Param("toolId") Long toolId, @Param("userId") Long userId);

    /**
     * 获取工具的平均评分
     *
     * @param toolId 工具ID
     * @return 平均评分
     */
    @Select("SELECT COALESCE(AVG(score), 0) FROM rating WHERE tool_id = #{toolId}")
    BigDecimal getAverageRating(@Param("toolId") Long toolId);

    /**
     * 获取工具的评分人数
     *
     * @param toolId 工具ID
     * @return 评分人数
     */
    @Select("SELECT COUNT(*) FROM rating WHERE tool_id = #{toolId}")
    Integer getRatingCount(@Param("toolId") Long toolId);
}
