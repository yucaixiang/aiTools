package com.toolrecommend.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.entity.Tool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工具Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    /**
     * 分页查询工具列表（带标签）
     */
    IPage<Tool> selectToolsWithTags(Page<Tool> page, @Param("keyword") String keyword,
                                     @Param("categoryId") Integer categoryId,
                                     @Param("pricingModel") String pricingModel,
                                     @Param("minRating") Double minRating,
                                     @Param("sortBy") String sortBy,
                                     @Param("sortOrder") String sortOrder);

    /**
     * 根据ID查询工具详情（包含分类、标签）
     */
    Tool selectToolDetailById(@Param("id") Long id);

    /**
     * 查询相似工具
     */
    List<Tool> selectSimilarTools(@Param("toolId") Long toolId, @Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    /**
     * 增加浏览量
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加收藏数
     */
    int incrementFavoriteCount(@Param("id") Long id);

    /**
     * 减少收藏数
     */
    int decrementFavoriteCount(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    int incrementUpvoteCount(@Param("id") Long id);

    /**
     * 减少点赞数
     */
    int decrementUpvoteCount(@Param("id") Long id);

    /**
     * 查询热门工具
     */
    List<Tool> selectHotTools(@Param("limit") Integer limit);

    /**
     * 查询最新工具
     */
    List<Tool> selectLatestTools(@Param("limit") Integer limit);
}
