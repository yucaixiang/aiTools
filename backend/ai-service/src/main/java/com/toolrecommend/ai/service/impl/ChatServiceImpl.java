package com.toolrecommend.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.toolrecommend.ai.client.OllamaClient;
import com.toolrecommend.ai.mapper.ConversationMapper;
import com.toolrecommend.ai.service.ChatService;
import com.toolrecommend.ai.service.ToolRecommendService;
import com.toolrecommend.common.dto.ChatRequestDTO;
import com.toolrecommend.common.entity.Conversation;
import com.toolrecommend.common.vo.ChatResponseVO;
import com.toolrecommend.common.vo.ToolVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private OllamaClient ollamaClient;

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private ToolRecommendService toolRecommendService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseVO chat(ChatRequestDTO requestDTO, Long userId) {
        long startTime = System.currentTimeMillis();

        // 生成或使用会话ID
        String sessionId = requestDTO.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = createSession(userId);
        }

        // 保存用户消息
        saveConversation(sessionId, userId, requestDTO.getMessage(), "USER", null, null);

        try {
            // 获取对话历史
            List<Map<String, String>> history = getConversationHistory(sessionId);

            // 调用Ollama生成回复
            String aiResponse = ollamaClient.chat(requestDTO.getMessage(), history);

            // 工具推荐
            List<ToolVO> recommendedTools = null;
            if (requestDTO.getNeedRecommendation()) {
                recommendedTools = toolRecommendService.recommend(
                        requestDTO.getMessage(),
                        requestDTO.getRecommendCount()
                );
            }

            // 保存AI回复
            String toolIdsJson = null;
            if (recommendedTools != null && !recommendedTools.isEmpty()) {
                List<Long> toolIds = recommendedTools.stream()
                        .map(ToolVO::getId)
                        .collect(Collectors.toList());
                toolIdsJson = JSON.toJSONString(toolIds);
            }

            saveConversation(sessionId, userId, aiResponse, "ASSISTANT", toolIdsJson, "qwen2.5:7b");

            // 构建响应
            ChatResponseVO response = new ChatResponseVO();
            response.setSessionId(sessionId);
            response.setMessage(aiResponse);
            response.setRecommendedTools(recommendedTools);
            response.setIntent(extractIntent(requestDTO.getMessage()));
            response.setResponseTime(System.currentTimeMillis() - startTime);

            log.info("AI对话完成: sessionId={}, userId={}, responseTime={}ms",
                    sessionId, userId, response.getResponseTime());

            return response;

        } catch (Exception e) {
            log.error("AI对话失败: sessionId={}, userId={}", sessionId, userId, e);
            throw new RuntimeException("AI对话失败: " + e.getMessage());
        }
    }

    @Override
    public List<ChatResponseVO> getHistory(String sessionId) {
        List<Conversation> conversations = conversationMapper.selectBySessionId(sessionId);

        return conversations.stream()
                .filter(c -> "ASSISTANT".equals(c.getRole()))
                .map(this::convertToChatResponseVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearHistory(String sessionId) {
        int result = conversationMapper.deleteBySessionId(sessionId);
        log.info("清除会话历史: sessionId={}, count={}", sessionId, result);
        return result > 0;
    }

    @Override
    public String createSession(Long userId) {
        String sessionId = "sess-" + UUID.randomUUID().toString();
        log.info("创建新会话: sessionId={}, userId={}", sessionId, userId);
        return sessionId;
    }

    /**
     * 保存对话记录
     */
    private void saveConversation(String sessionId, Long userId, String message,
                                   String role, String recommendedTools, String model) {
        Conversation conversation = new Conversation();
        conversation.setSessionId(sessionId);
        conversation.setUserId(userId);
        conversation.setMessage(message);
        conversation.setRole(role);
        conversation.setRecommendedTools(recommendedTools);
        conversation.setModelUsed(model);

        conversationMapper.insert(conversation);
    }

    /**
     * 获取对话历史（转换为Ollama格式）
     */
    private List<Map<String, String>> getConversationHistory(String sessionId) {
        List<Conversation> conversations = conversationMapper.selectBySessionId(sessionId);

        // 只保留最近10轮对话
        int limit = Math.min(conversations.size(), 20);
        conversations = conversations.subList(Math.max(0, conversations.size() - limit), conversations.size());

        return conversations.stream()
                .map(c -> {
                    Map<String, String> msg = new HashMap<>();
                    msg.put("role", c.getRole().toLowerCase());
                    msg.put("content", c.getMessage());
                    return msg;
                })
                .collect(Collectors.toList());
    }

    /**
     * 提取用户意图（简单关键词匹配）
     */
    private String extractIntent(String message) {
        message = message.toLowerCase();

        if (message.contains("推荐") || message.contains("工具") || message.contains("有什么")) {
            return "TOOL_RECOMMENDATION";
        } else if (message.contains("怎么") || message.contains("如何") || message.contains("教程")) {
            return "HOW_TO";
        } else if (message.contains("对比") || message.contains("区别") || message.contains("比较")) {
            return "COMPARISON";
        } else if (message.contains("价格") || message.contains("多少钱") || message.contains("收费")) {
            return "PRICING";
        } else {
            return "GENERAL_QUERY";
        }
    }

    /**
     * 转换为ChatResponseVO
     */
    private ChatResponseVO convertToChatResponseVO(Conversation conversation) {
        ChatResponseVO vo = new ChatResponseVO();
        vo.setSessionId(conversation.getSessionId());
        vo.setMessage(conversation.getMessage());
        // TODO: 解析推荐工具ID并查询工具详情
        return vo;
    }
}
