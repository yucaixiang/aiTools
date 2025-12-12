package com.toolrecommend.tool.controller;

import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.CategoryVO;
import com.toolrecommend.tool.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 分类控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 查询所有分类（树形结构）
     */
    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 查询父分类列表
     */
    @GetMapping("/parents")
    public Result<List<CategoryVO>> getParentCategories() {
        List<CategoryVO> categories = categoryService.getParentCategories();
        return Result.success(categories);
    }

    /**
     * 查询子分类列表
     */
    @GetMapping("/{parentId}/children")
    public Result<List<CategoryVO>> getChildCategories(@PathVariable Integer parentId) {
        List<CategoryVO> categories = categoryService.getChildCategories(parentId);
        return Result.success(categories);
    }
}
