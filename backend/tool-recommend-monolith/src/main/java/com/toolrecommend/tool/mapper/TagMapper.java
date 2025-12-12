package com.toolrecommend.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.common.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据工具ID查询标签列表
     */
    List<Tag> selectTagsByToolId(@Param("toolId") Long toolId);

    /**
     * 查询热门标签
     */
    List<Tag> selectHotTags(@Param("limit") Integer limit);

    /**
     * 增加标签使用次数
     */
    int incrementUsageCount(@Param("id") Integer id);

    /**
     * 减少标签使用次数
     */
    int decrementUsageCount(@Param("id") Integer id);
}
