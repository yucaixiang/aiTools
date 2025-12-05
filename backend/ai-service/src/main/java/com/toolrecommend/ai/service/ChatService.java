package com.toolrecommend.ai.service;

import com.toolrecommend.common.dto.ChatRequestDTO;
import com.toolrecommend.common.vo.ChatResponseVO;

import java.util.List;

/**
 * 聊天服务接口
 *
 * @author Tool Recommend Team
 */
public interface ChatService {

    /**
     * 发送聊天消息
     */
    ChatResponseVO chat(ChatRequestDTO requestDTO, Long userId);

    /**
     * 获取会话历史
     */
    List<ChatResponseVO> getHistory(String sessionId);

    /**
     * 清除会话历史
     */
    boolean clearHistory(String sessionId);

    /**
     * 创建新会话
     */
    String createSession(Long userId);
}
