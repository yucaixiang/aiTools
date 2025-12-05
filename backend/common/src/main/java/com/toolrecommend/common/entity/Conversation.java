package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("conversation")
public class Conversation {

    /**
     * 对话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 角色：USER-用户 ASSISTANT-助手
     */
    private String role;

    /**
     * 推荐工具ID列表（JSON数组）
     */
    private String recommendedTools;

    /**
     * 意图
     */
    private String intent;

    /**
     * 实体（JSON对象）
     */
    private String entities;

    /**
     * 使用的模型
     */
    private String modelUsed;

    /**
     * Token消耗
     */
    private Integer tokensUsed;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
