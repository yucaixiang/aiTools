package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class ReviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 工具ID
     */
    private Long toolId;

    /**
     * 用户信息
     */
    private UserSimpleVO user;

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
     * 优点列表
     */
    private List<String> pros;

    /**
     * 缺点列表
     */
    private List<String> cons;

    /**
     * 使用时长
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 当前用户是否觉得有帮助
     */
    private Boolean isHelpful;

    /**
     * 是否是当前用户的评论
     */
    private Boolean isMine;
}
