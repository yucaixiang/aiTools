package com.toolrecommend.tool.service.impl;

import com.toolrecommend.common.entity.Category;
import com.toolrecommend.common.vo.CategoryVO;
import com.toolrecommend.tool.mapper.CategoryMapper;
import com.toolrecommend.tool.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 *
 * @author Tool Recommend Team
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "category:all";
    private static final long CACHE_EXPIRE_TIME = 120;

    @Override
    public List<CategoryVO> getAllCategories() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<CategoryVO> cachedCategories = (List<CategoryVO>) redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);
        if (cachedCategories != null) {
            return cachedCategories;
        }

        // 从数据库查询
        List<Category> categories = categoryMapper.selectAllActiveCategories();
        List<CategoryVO> categoryVOList = categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());

        // 缓存结果
        redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categoryVOList, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return categoryVOList;
    }

    @Override
    public List<CategoryVO> getParentCategories() {
        List<Category> categories = categoryMapper.selectParentCategories();
        return categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getChildCategories(Integer parentId) {
        List<Category> categories = categoryMapper.selectChildCategories(parentId);
        return categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
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
