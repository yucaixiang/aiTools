package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Integer id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * URL标识
     */
    private String slug;

    /**
     * 使用次数
     */
    private Integer usageCount;
}
