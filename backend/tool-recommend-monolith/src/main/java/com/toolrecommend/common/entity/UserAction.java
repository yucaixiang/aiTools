package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("user_action")
public class UserAction {

    /**
     * 行为ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 工具ID
     */
    private Long toolId;

    /**
     * 行为类型：VIEW-查看 UPVOTE-点赞 FAVORITE-收藏 CLICK_WEBSITE-点击官网 CLICK_DOWNLOAD-点击下载
     */
    private String actionType;

    /**
     * 行为值
     */
    private String actionValue;

    /**
     * 来源
     */
    private String source;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
