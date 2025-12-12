package com.toolrecommend.rating.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具评分实体
 * 一个用户对一个工具只能打一次分
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("rating")
public class Rating {

    /**
     * 评分ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工具ID
     */
    private Long toolId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评分（1-5分）
     */
    private Integer score;

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
