package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代找/代开发请求实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("help_request")
public class HelpRequest {

    /**
     * 请求ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 请求类型: FIND-代找工具 DEVELOP-代开发
     */
    private String requestType;

    /**
     * 标题
     */
    private String title;

    /**
     * 需求描述
     */
    private String description;

    /**
     * 预算范围
     */
    private String budget;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 状态: 0-待处理 1-处理中 2-已完成 3-已关闭
     */
    private Integer status;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime repliedAt;

    /**
     * 回复人ID
     */
    private Long repliedBy;

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
