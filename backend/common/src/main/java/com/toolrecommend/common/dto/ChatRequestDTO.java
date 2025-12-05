package com.toolrecommend.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.io.Serializable;

/**
 * AI聊天请求DTO
 *
 * @author Tool Recommend Team
 */
@Data
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（可选，用于多轮对话）
     */
    private String sessionId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 是否需要工具推荐
     */
    private Boolean needRecommendation = true;

    /**
     * 推荐数量
     */
    private Integer recommendCount = 5;
}
