package com.toolrecommend.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toolrecommend.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 分页查询工具评论
     */
    IPage<Review> selectByToolId(Page<Review> page, @Param("toolId") Long toolId, @Param("status") Integer status);

    /**
     * 查询用户评论列表
     */
    IPage<Review> selectByUserId(Page<Review> page, @Param("userId") Long userId);

    /**
     * 查询工具的热门评论
     */
    List<Review> selectHotReviews(@Param("toolId") Long toolId, @Param("limit") Integer limit);

    /**
     * 检查用户是否已评论过该工具
     */
    Review selectByToolIdAndUserId(@Param("toolId") Long toolId, @Param("userId") Long userId);

    /**
     * 增加有帮助数
     */
    int incrementHelpfulCount(@Param("id") Long id);

    /**
     * 减少有帮助数
     */
    int decrementHelpfulCount(@Param("id") Long id);

    /**
     * 统计工具的评分分布
     */
    List<java.util.Map<String, Object>> selectRatingDistribution(@Param("toolId") Long toolId);

    /**
     * 查询工具的顶级评论（parent_id IS NULL）
     */
    List<Review> selectTopLevelReviews(@Param("toolId") Long toolId, @Param("status") Integer status);

    /**
     * 查询某条评论的回复列表
     */
    IPage<Review> selectRepliesByParentId(Page<Review> page, @Param("parentId") Long parentId, @Param("status") Integer status);

    /**
     * 查询某条评论的所有回复（不分页）
     */
    List<Review> selectAllRepliesByParentId(@Param("parentId") Long parentId, @Param("status") Integer status);

    /**
     * 增加回复数
     */
    int incrementReplyCount(@Param("id") Long id);

    /**
     * 减少回复数
     */
    int decrementReplyCount(@Param("id") Long id);
}
