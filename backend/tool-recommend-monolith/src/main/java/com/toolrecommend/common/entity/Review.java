package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("review")
public class Review {

    /**
     * 评论ID
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
     * 父评论ID（NULL表示顶级评论）
     */
    private Long parentId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 优点（JSON数组）
     */
    private String pros;

    /**
     * 缺点（JSON数组）
     */
    private String cons;

    /**
     * 使用时长：WEEK-一周 MONTH-一月 QUARTER-三月 HALF_YEAR-半年 YEAR-一年
     */
    private String usageDuration;

    /**
     * 有帮助数
     */
    private Integer helpfulCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 状态：0-待审核 1-已发布 2-已隐藏
     */
    private Integer status;

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
