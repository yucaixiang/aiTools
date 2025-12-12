package com.toolrecommend.common.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


import java.io.Serializable;
import java.util.List;

/**
 * 评论创建DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ReviewCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具ID
     */
    @NotNull(message = "工具ID不能为空")
    private Long toolId;

    /**
     * 父评论ID（回复时使用，NULL表示顶级评论）
     */
    private Long parentId;

    /**
     * 标题（可选）
     */
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不能超过2000字")
    private String content;

    /**
     * 优点列表
     */
    private List<String> pros;

    /**
     * 缺点列表
     */
    private List<String> cons;

    /**
     * 使用时长：WEEK-一周 MONTH-一月 QUARTER-三月 HALF_YEAR-半年 YEAR-一年
     */
    private String usageDuration;
}
