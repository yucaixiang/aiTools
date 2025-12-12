package com.toolrecommend.tool.service;

import com.toolrecommend.common.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Tool Recommend Team
 */
public interface CategoryService {

    /**
     * 查询所有分类
     */
    List<CategoryVO> getAllCategories();

    /**
     * 查询父分类列表
     */
    List<CategoryVO> getParentCategories();

    /**
     * 查询子分类列表
     */
    List<CategoryVO> getChildCategories(Integer parentId);
}
