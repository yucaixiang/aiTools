package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("category")
public class Category {

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * URL标识
     */
    private String slug;

    /**
     * 父分类ID
     */
    private Integer parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 工具数量
     */
    private Integer toolCount;

    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
