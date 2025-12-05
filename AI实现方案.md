# AI工具推荐系统 - AI实现方案

## 文档信息
- **项目名称**：智能工具推荐平台
- **版本**：v1.0
- **创建日期**：2025-12-04

---

## 一、AI方案总体设计

### 1.1 混合AI架构

```
用户请求 → 复杂度评估 → 模型选择
                    ↓
    ┌───────────────┴───────────────┐
    │                               │
简单查询                        复杂查询
    │                               │
    ↓                               ↓
Ollama本地                     云端API
Qwen2.5:7b                    通义千问
响应快、成本低                响应准、功能强
    │                               │
    └───────────────┬───────────────┘
                    ↓
              向量检索 + 排序算法
                    ↓
              生成推荐结果
```

### 1.2 核心组件

| 组件 | 技术方案 | 作用 |
|------|---------|------|
| **本地LLM** | Ollama + Qwen2.5:7b | 简单对话、快速响应 |
| **云端LLM** | 阿里通义千问 | 复杂推理、深度分析 |
| **向量化** | BGE-large-zh | 中文文本向量化 |
| **向量库** | Qdrant | 语义搜索 |
| **传统搜索** | Elasticsearch | 关键词精确匹配 |
| **排序算法** | 自研多维度评分 | 综合排序 |

---

## 二、本地LLM部署（Ollama）

### 2.1 Ollama安装和配置

**Docker部署方式**：
```yaml
# docker-compose.yml
version: '3.8'
services:
  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    environment:
      - OLLAMA_HOST=0.0.0.0
      - OLLAMA_NUM_PARALLEL=2        # 并发请求数
      - OLLAMA_MAX_LOADED_MODELS=2   # 最大加载模型数
    deploy:
      resources:
        limits:
          cpus: '4'
          memory: 8G
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]

volumes:
  ollama_data:
```

**启动命令**：
```bash
# 拉取Qwen2.5模型
docker exec -it ollama ollama pull qwen2.5:7b

# 拉取BGE向量模型
docker exec -it ollama ollama pull bge-large-zh

# 测试模型
curl http://localhost:11434/api/generate -d '{
  "model": "qwen2.5:7b",
  "prompt": "你好，介绍一下你自己"
}'
```

### 2.2 模型文件说明

**Qwen2.5:7b**：
- 参数量：70亿
- 显存占用：~5GB
- 推理速度：~30 tokens/s (GPU)
- 适用场景：通用对话、意图识别、实体提取

**BGE-large-zh**：
- 向量维度：1024
- 适用场景：中文文本向量化
- 性能：相似度计算准确

### 2.3 Spring Boot集成Ollama

```java
@Service
public class OllamaService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate;

    public OllamaService() {
        this.restTemplate = new RestTemplate();
        // 设置超时时间
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(60000);  // 60秒
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * 同步对话
     */
    public String chat(String prompt) {
        OllamaRequest request = OllamaRequest.builder()
            .model("qwen2.5:7b")
            .prompt(prompt)
            .stream(false)
            .options(Map.of(
                "temperature", 0.7,
                "top_p", 0.9,
                "max_tokens", 2000
            ))
            .build();

        try {
            ResponseEntity<OllamaResponse> response = restTemplate.postForEntity(
                ollamaBaseUrl + "/api/generate",
                request,
                OllamaResponse.class
            );

            return response.getBody().getResponse();

        } catch (Exception e) {
            log.error("Ollama调用失败", e);
            throw new AIServiceException("AI服务暂不可用");
        }
    }

    /**
     * 流式对话（SSE）
     */
    public Flux<String> chatStream(String prompt) {
        OllamaRequest request = OllamaRequest.builder()
            .model("qwen2.5:7b")
            .prompt(prompt)
            .stream(true)
            .build();

        WebClient webClient = WebClient.builder()
            .baseUrl(ollamaBaseUrl)
            .build();

        return webClient.post()
            .uri("/api/generate")
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class)
            .map(this::parseStreamResponse);
    }

    /**
     * 文本向量化
     */
    public float[] embed(String text) {
        OllamaEmbedRequest request = OllamaEmbedRequest.builder()
            .model("bge-large-zh")
            .prompt(text)
            .build();

        ResponseEntity<OllamaEmbedResponse> response = restTemplate.postForEntity(
            ollamaBaseUrl + "/api/embeddings",
            request,
            OllamaEmbedResponse.class
        );

        return response.getBody().getEmbedding();
    }
}

@Data
@Builder
class OllamaRequest {
    private String model;
    private String prompt;
    private Boolean stream;
    private Map<String, Object> options;
}

@Data
class OllamaResponse {
    private String model;
    private String response;
    private Boolean done;
}
```

---

## 三、云端AI接入（通义千问）

### 3.1 通义千问API集成

```java
@Service
public class QwenService {

    @Value("${qwen.api-key}")
    private String apiKey;

    @Value("${qwen.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    /**
     * 调用通义千问API
     */
    public String chat(String prompt, List<Message> history) {
        QwenRequest request = QwenRequest.builder()
            .model("qwen-plus")  // 或 qwen-max
            .input(QwenInput.builder()
                .messages(buildMessages(prompt, history))
                .build())
            .parameters(QwenParameters.builder()
                .temperature(0.7)
                .topP(0.9)
                .maxTokens(2000)
                .build())
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<QwenRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<QwenResponse> response = restTemplate.postForEntity(
                baseUrl + "/services/aigc/text-generation/generation",
                entity,
                QwenResponse.class
            );

            return response.getBody()
                .getOutput()
                .getText();

        } catch (Exception e) {
            log.error("通义千问调用失败", e);
            throw new AIServiceException("AI服务异常");
        }
    }

    /**
     * 流式调用
     */
    public Flux<String> chatStream(String prompt, List<Message> history) {
        // 使用WebFlux实现SSE
        QwenRequest request = QwenRequest.builder()
            .model("qwen-plus")
            .input(QwenInput.builder()
                .messages(buildMessages(prompt, history))
                .build())
            .parameters(QwenParameters.builder()
                .incrementalOutput(true)  // 增量输出
                .build())
            .build();

        WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();

        return webClient.post()
            .uri("/services/aigc/text-generation/generation")
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(QwenStreamResponse.class)
            .map(response -> response.getOutput().getText());
    }

    private List<QwenMessage> buildMessages(String prompt, List<Message> history) {
        List<QwenMessage> messages = new ArrayList<>();

        // 添加系统提示
        messages.add(QwenMessage.builder()
            .role("system")
            .content("你是一个专业的工具推荐助手...")
            .build());

        // 添加历史对话
        if (history != null) {
            for (Message msg : history) {
                messages.add(QwenMessage.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build());
            }
        }

        // 添加当前问题
        messages.add(QwenMessage.builder()
            .role("user")
            .content(prompt)
            .build());

        return messages;
    }
}

@Data
@Builder
class QwenRequest {
    private String model;
    private QwenInput input;
    private QwenParameters parameters;
}

@Data
@Builder
class QwenInput {
    private List<QwenMessage> messages;
}

@Data
@Builder
class QwenMessage {
    private String role;      // system/user/assistant
    private String content;
}

@Data
@Builder
class QwenParameters {
    private Double temperature;
    private Double topP;
    private Integer maxTokens;
    private Boolean incrementalOutput;
}
```

### 3.2 成本控制

```java
@Service
public class AIUsageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 每日调用限额
    private static final int DAILY_LIMIT_QWEN = 10000;
    private static final int DAILY_LIMIT_USER = 100;

    /**
     * 检查是否超过限额
     */
    public boolean checkQuota(Long userId, String aiType) {
        String key = String.format("ai:quota:%s:%s:%s",
            aiType,
            LocalDate.now(),
            userId != null ? userId : "anonymous"
        );

        Long count = redisTemplate.opsForValue().increment(key);

        // 设置过期时间（当天结束）
        if (count == 1) {
            long seconds = Duration.between(
                LocalDateTime.now(),
                LocalDate.now().plusDays(1).atStartOfDay()
            ).getSeconds();
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        }

        int limit = userId != null ? DAILY_LIMIT_USER : 20;
        return count <= limit;
    }

    /**
     * 记录Token消耗
     */
    public void recordUsage(Long userId, String model, int tokensUsed, double cost) {
        AIUsageRecord record = AIUsageRecord.builder()
            .userId(userId)
            .model(model)
            .tokensUsed(tokensUsed)
            .cost(cost)
            .createdAt(LocalDateTime.now())
            .build();

        aiUsageRepository.save(record);

        // 更新统计
        updateDailyStats(model, tokensUsed, cost);
    }
}
```

---

## 四、智能推荐算法

### 4.1 意图识别

```java
@Service
public class IntentRecognitionService {

    @Autowired
    private OllamaService ollamaService;

    /**
     * 识别用户意图
     */
    public IntentEntity recognizeIntent(String userMessage) {
        String prompt = buildIntentPrompt(userMessage);
        String jsonResponse = ollamaService.chat(prompt);

        return parseIntent(jsonResponse);
    }

    private String buildIntentPrompt(String userMessage) {
        return """
            你是一个意图识别专家。分析用户消息，提取关键信息，返回JSON格式：

            {
              "intent": "FIND_TOOL | COMPARE_TOOLS | GET_ALTERNATIVE | ASK_INFO",
              "toolType": "工具类型（如：笔记工具、设计工具）",
              "features": ["功能需求1", "功能需求2"],
              "platform": ["Web", "iOS", "Android"],
              "budget": "免费 | 付费 | Freemium | 具体金额",
              "scenario": "使用场景描述",
              "complexity": 1-10分（查询复杂度）
            }

            用户消息："%s"

            注意：
            1. 如果用户没提到的字段，设为null
            2. complexity评分标准：
               - 1-3分：简单查询（明确工具类型）
               - 4-6分：中等查询（有多个筛选条件）
               - 7-10分：复杂查询（需要对比、深度分析）
            3. 只返回JSON，不要其他文字
            """.formatted(userMessage);
    }

    private IntentEntity parseIntent(String jsonResponse) {
        try {
            // 提取JSON（去除Markdown代码块标记）
            String json = jsonResponse
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();

            return objectMapper.readValue(json, IntentEntity.class);

        } catch (JsonProcessingException e) {
            log.error("意图解析失败: {}", jsonResponse, e);
            // 返回默认意图
            return IntentEntity.builder()
                .intent(IntentType.FIND_TOOL)
                .complexity(5)
                .build();
        }
    }
}

@Data
@Builder
class IntentEntity {
    private IntentType intent;
    private String toolType;
    private List<String> features;
    private List<String> platform;
    private String budget;
    private String scenario;
    private Integer complexity;
}

enum IntentType {
    FIND_TOOL,         // 寻找工具
    COMPARE_TOOLS,     // 对比工具
    GET_ALTERNATIVE,   // 寻找替代品
    ASK_INFO           // 询问信息
}
```

### 4.2 向量检索

```java
@Service
public class VectorSearchService {

    @Autowired
    private QdrantClient qdrantClient;

    @Autowired
    private OllamaService ollamaService;

    /**
     * 向量检索
     */
    public List<ScoredTool> vectorSearch(String query, int limit) {
        // 1. 向量化查询
        float[] queryVector = ollamaService.embed(query);

        // 2. 向量检索
        SearchPoints searchRequest = SearchPoints.newBuilder()
            .setCollectionName("tools")
            .addAllVector(Arrays.stream(queryVector).boxed().toList())
            .setLimit(limit)
            .setScoreThreshold(0.7f)  // 相似度阈值
            .setWithPayload(true)
            .build();

        SearchResponse response = qdrantClient.search(searchRequest);

        // 3. 构造结果
        return response.getResultList().stream()
            .map(scored -> ScoredTool.builder()
                .toolId(scored.getId().getNum())
                .score(scored.getScore())
                .payload(scored.getPayloadMap())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 混合检索（向量+关键词）
     */
    public List<Long> hybridSearch(IntentEntity intent, int limit) {
        // 向量检索
        List<ScoredTool> vectorResults = vectorSearch(
            intent.toString(),
            limit * 2
        );

        // 关键词检索
        List<Long> keywordResults = elasticsearchService.search(
            intent.getToolType(),
            intent.getFeatures(),
            limit * 2
        );

        // 合并去重
        Set<Long> resultSet = new LinkedHashSet<>();
        vectorResults.forEach(r -> resultSet.add(r.getToolId()));
        resultSet.addAll(keywordResults);

        return new ArrayList<>(resultSet).subList(0, Math.min(limit, resultSet.size()));
    }
}
```

### 4.3 推荐理由生成

```java
@Service
public class RecommendationReasonService {

    @Autowired
    private CloudAIService cloudAIService;

    /**
     * 生成推荐理由
     */
    public String generateReason(Tool tool, IntentEntity intent) {
        String prompt = """
            用户需求：%s

            工具信息：
            - 名称：%s
            - 介绍：%s
            - 价格：%s
            - 平台：%s
            - 特性：%s

            请用1-2句话（30字内）说明为什么推荐这个工具给用户。
            要求：
            1. 突出匹配用户需求的点
            2. 语言简洁、自然
            3. 不要重复工具名称
            """.formatted(
                intent.toUserDescription(),
                tool.getName(),
                tool.getTagline(),
                tool.getPricingModel(),
                String.join(", ", tool.getPlatforms()),
                String.join(", ", tool.getTags())
            );

        return cloudAIService.chat(prompt);
    }

    /**
     * 批量生成（性能优化）
     */
    public Map<Long, String> batchGenerateReasons(
        List<Tool> tools,
        IntentEntity intent
    ) {
        CompletableFuture<?>[] futures = tools.stream()
            .map(tool -> CompletableFuture.supplyAsync(() -> {
                String reason = generateReason(tool, intent);
                return Map.entry(tool.getId(), reason);
            }))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        return Arrays.stream(futures)
            .map(f -> (Map.Entry<Long, String>) f.join())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
}
```

---

## 五、提示词工程

### 5.1 系统提示词

```java
public class PromptTemplates {

    public static final String SYSTEM_PROMPT = """
        你是智能工具推荐平台的AI助手，名叫"工具小助手"。

        你的职责：
        1. 理解用户需求，推荐最适合的工具
        2. 如果需求不明确，礼貌地追问关键信息
        3. 对每个推荐的工具，简要说明推荐理由
        4. 保持友好、专业的态度

        推荐原则：
        - 优先推荐免费或开源工具
        - 考虑用户的平台和预算限制
        - 推荐3-5个工具，不要太多
        - 避免推荐已停服或评分过低的工具

        回复格式：
        1. 简短回应用户（1句话）
        2. 列出推荐的工具
        3. 询问是否需要更多信息

        注意事项：
        - 不要编造工具信息
        - 不要过度承诺工具功能
        - 如果不确定，建议用户自行体验
        """;

    public static final String CLARIFICATION_PROMPT = """
        用户需求不够明确，需要追问关键信息。

        用户消息："%s"

        已知信息：%s

        请生成3个追问选项（每个20字内），帮助明确需求：
        1. 关于使用场景
        2. 关于平台需求
        3. 关于预算限制

        返回JSON格式：
        {
          "questions": ["问题1", "问题2", "问题3"]
        }
        """;

    public static final String ALTERNATIVE_PROMPT = """
        用户想找"%s"的替代品。

        原因：%s

        候选工具列表：
        %s

        请从中选择3个最适合的替代品，并说明：
        1. 为什么适合作为替代品
        2. 相比原工具的优势
        3. 可能的不足

        返回JSON格式：
        {
          "recommendations": [
            {
              "toolId": 1,
              "reason": "推荐理由",
              "pros": ["优势1", "优势2"],
              "cons": ["不足1"]
            }
          ]
        }
        """;
}
```

---

## 六、性能优化

### 6.1 缓存策略

```java
@Service
public class AICacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存常见问题答案
     */
    @Cacheable(value = "ai:qa", key = "#question", unless = "#result == null")
    public String getCachedAnswer(String question) {
        // 查询相似问题
        String cacheKey = "ai:qa:" + md5(question);
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 缓存推荐结果
     */
    public void cacheRecommendation(String query, List<Tool> tools, int ttl) {
        String cacheKey = "ai:recommend:" + md5(query);
        redisTemplate.opsForValue().set(cacheKey, tools, ttl, TimeUnit.MINUTES);
    }

    /**
     * 向量缓存
     */
    @Cacheable(value = "ai:embedding", key = "#text")
    public float[] getCachedEmbedding(String text) {
        return ollamaService.embed(text);
    }
}
```

### 6.2 异步处理

```java
@Service
public class AsyncAIService {

    @Async("aiExecutor")
    public CompletableFuture<ChatResponse> asyncChat(String message, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            return aiRecommendationService.recommend(message, sessionId);
        });
    }

    @Bean("aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-task-");
        executor.initialize();
        return executor;
    }
}
```

---

**文档版本**：v1.0
**最后更新**：2025-12-04
