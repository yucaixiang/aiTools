package com.toolrecommend.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论树形结构VO
 * 用于展示评论及其回复的层级结构
 *
 * @author Tool Recommend Team
 */
@Data
public class ReviewTreeVO {

    /**
     * 评论ID
     */
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
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 父评论ID
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
     * 子回复列表
     */
    private List<ReviewTreeVO> replies;

    /**
     * 当前用户是否点了有帮助
     */
    private Boolean isHelpful;

    /**
     * 是否是当前用户的评论
     */
    private Boolean isMine;
}
