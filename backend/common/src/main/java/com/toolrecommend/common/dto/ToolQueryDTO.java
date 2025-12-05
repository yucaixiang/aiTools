package com.toolrecommend.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工具查询DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ToolQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;

    /**
     * 定价模式
     */
    private String pricingModel;

    /**
     * 最低评分
     */
    private Double minRating;

    /**
     * 排序字段：view-浏览量 upvote-点赞数 rating-评分 launch-发布时间
     */
    private String sortBy;

    /**
     * 排序方向：asc-升序 desc-降序
     */
    private String sortOrder;

    /**
     * 页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 默认构造函数，设置默认值
     */
    public ToolQueryDTO() {
        this.current = 1L;
        this.size = 20L;
        this.sortBy = "upvote";
        this.sortOrder = "desc";
    }
}
