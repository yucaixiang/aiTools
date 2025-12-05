package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI聊天响应VO
 *
 * @author Tool Recommend Team
 */
@Data
public class ChatResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI回复消息
     */
    private String message;

    /**
     * 推荐的工具列表
     */
    private List<ToolVO> recommendedTools;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * Token消耗
     */
    private Integer tokensUsed;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
}
