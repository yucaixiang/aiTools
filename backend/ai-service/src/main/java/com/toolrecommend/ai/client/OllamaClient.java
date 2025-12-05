package com.toolrecommend.ai.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ollama API客户端
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Component
public class OllamaClient {

    @Value("${ai.ollama.base-url}")
    private String baseUrl;

    @Value("${ai.ollama.model}")
    private String model;

    @Value("${ai.ollama.timeout:60000}")
    private long timeout;

    @Value("${ai.ollama.max-tokens:2000}")
    private int maxTokens;

    @Value("${ai.ollama.temperature:0.7}")
    private double temperature;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        log.info("Ollama客户端初始化完成: baseUrl={}, model={}", baseUrl, model);
    }

    /**
     * 发送聊天请求（单轮对话）
     */
    public String chat(String message) throws IOException {
        return chat(message, null);
    }

    /**
     * 发送聊天请求（支持上下文）
     */
    public String chat(String message, List<Map<String, String>> history) throws IOException {
        String url = baseUrl + "/api/chat";

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统提示
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());
        messages.add(systemMsg);

        // 添加历史对话
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        // 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        messages.add(userMsg);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", temperature);
        options.put("num_predict", maxTokens);
        requestBody.put("options", options);

        String jsonBody = JSON.toJSONString(requestBody);
        log.debug("Ollama请求: {}", jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama请求失败: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Ollama响应: {}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONObject messageObj = jsonResponse.getJSONObject("message");

            return messageObj != null ? messageObj.getString("content") : "";
        }
    }

    /**
     * 检查Ollama服务是否可用
     */
    public boolean isAvailable() {
        try {
            String url = baseUrl + "/api/tags";
            Request request = new Request.Builder().url(url).get().build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("Ollama服务检查失败", e);
            return false;
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一个AI工具推荐助手。用户会向你咨询各种工具相关的问题，你需要：\n" +
                "1. 理解用户的需求和意图\n" +
                "2. 提供专业、准确的回答\n" +
                "3. 如果用户需要工具推荐，请根据他们的需求推荐合适的工具\n" +
                "4. 保持友好、专业的交流风格\n" +
                "5. 如果不确定，可以询问更多细节\n\n" +
                "请用简洁、清晰的中文回答。";
    }
}
