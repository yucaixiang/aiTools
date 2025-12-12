package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
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
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 工具数量
     */
    private Integer toolCount;
}
