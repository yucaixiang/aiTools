package com.toolrecommend.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.common.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询所有启用的分类（含子分类）
     */
    List<Category> selectAllActiveCategories();

    /**
     * 查询父分类列表
     */
    List<Category> selectParentCategories();

    /**
     * 根据父ID查询子分类
     */
    List<Category> selectChildCategories(@Param("parentId") Integer parentId);

    /**
     * 增加分类工具数量
     */
    int incrementToolCount(@Param("id") Integer id);

    /**
     * 减少分类工具数量
     */
    int decrementToolCount(@Param("id") Integer id);
}
