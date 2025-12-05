package com.toolrecommend.ai.controller;

import com.toolrecommend.ai.service.ChatService;
import com.toolrecommend.common.dto.ChatRequestDTO;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.ChatResponseVO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 发送聊天消息
     */
    @PostMapping
    public Result<ChatResponseVO> chat(
            @Validated @RequestBody ChatRequestDTO requestDTO,
            @RequestHeader(value = "User-Id", required = false) Long userId) {

        ChatResponseVO response = chatService.chat(requestDTO, userId);
        return Result.success(response);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<ChatResponseVO>> getHistory(@PathVariable String sessionId) {
        List<ChatResponseVO> history = chatService.getHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/history/{sessionId}")
    public Result<String> clearHistory(@PathVariable String sessionId) {
        boolean success = chatService.clearHistory(sessionId);
        return success ? Result.success("清除成功") : Result.error("清除失败");
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public Result<String> createSession(
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        String sessionId = chatService.createSession(userId);
        return Result.success("会话创建成功", sessionId);
    }
}
