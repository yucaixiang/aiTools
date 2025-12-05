package com.toolrecommend.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 *
 * @author Tool Recommend Team
 */
@Data
@TableName("tag")
public class Tag {

    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
