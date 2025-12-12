# Tool Recommend 单体应用

## 项目说明

本项目是将Tool Recommend微服务架构合并为单体应用的版本,旨在降低部署复杂度和运维成本。

## 已完成的工作

### ✅ 1. 代码整合
- 成功将user-service、tool-service、review-service的代码合并到单体项目
- 删除了AI和Admin模块(这些模块有缺失的依赖)

### ✅ 2. 配置文件
- **pom.xml**: 包含所有必要的依赖
  - Spring Boot 3.2.0
  - MyBatis Plus 3.5.7
  - MySQL、Redis、JWT等
- **application.yml**: 统一配置
  - 端口: 8080
  - 数据库: tool_recommend
  - Redis配置
  - MyBatis Plus配置
  - JWT配置

### ✅ 3. 主启动类
`src/main/java/com/toolrecommend/ToolRecommendApplication.java`
- 包含@SpringBootApplication和@MapperScan注解
- 启动后输出欢迎信息

### ✅ 4. 全局异常处理
`src/main/java/com/toolrecommend/config/GlobalExceptionHandler.java`
- 合并了所有微服务的异常处理逻辑
- 统一处理BusinessException、ToolNotFoundException等

### ✅ 5. 服务调用重构
修改了`FavoriteServiceImpl.java`,将微服务间的RestTemplate HTTP调用改为直接注入本地Service:

**之前 (微服务):**
```java
private final RestTemplate restTemplate;
@Value("${service.tool-service.url}")
private String toolServiceUrl;
ResponseEntity<Result<ToolVO>> response = restTemplate.exchange(url...);
```

**之后 (单体):**
```java
private final ToolService toolService;
ToolDetailVO detailVO = toolService.getToolDetail(toolId, userId);
```

### ✅ 6. Gateway配置更新
简化了`gateway-service/src/main/resources/application.yml`,将所有微服务路由合并为单一路由:

**之前:**
- tool-service → http://localhost:8081
- user-service → http://localhost:8082
- review-service → http://localhost:8084

**之后:**
- monolith-service → http://localhost:8080 (所有/api/**请求)

## ⚠️ 当前问题

### Lombok注解处理器未生效

**问题描述:**
Maven编译时Lombok注解(@Data, @Slf4j等)没有生成对应的getter/setter/log等方法,导致编译失败。

**错误示例:**
```
[ERROR] 找不到符号
  符号:   方法 setCode(int)
  位置: 类型为com.toolrecommend.common.result.Result<T>的变量 result
```

**影响范围:**
- 所有Entity类(Tool, User, Category等)
- 所有DTO类(ToolQueryDTO, UserRegisterDTO等)
- 所有VO类(ToolVO, ToolDetailVO等)
- 使用@Slf4j的Service和Controller类

## 🔧 解决方案

### 方案1: 使用IntelliJ IDEA编译(推荐)

1. 在IntelliJ IDEA中打开项目
2. 确保已安装Lombok插件:
   - File → Settings → Plugins
   - 搜索"Lombok",安装并重启IDE
3. 启用注解处理:
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - 勾选"Enable annotation processing"
4. 重新构建项目:
   - Build → Rebuild Project

### 方案2: 在命令行使用IntelliJ的Maven

```bash
cd /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith
# 使用IDEA的Maven Wrapper
./mvnw clean package -DskipTests
```

### 方案3: 检查Maven和Java环境

```bash
# 检查Java版本(需要Java 17)
java -version

# 检查Maven版本
mvn -version

# 清理本地Maven仓库缓存
rm -rf ~/.m2/repository/org/projectlombok/lombok

# 重新下载依赖
mvn clean install -U -DskipTests
```

## 📁 项目结构

```
tool-recommend-monolith/
├── pom.xml
├── src/main/
│   ├── java/com/toolrecommend/
│   │   ├── ToolRecommendApplication.java  # 主启动类
│   │   ├── common/                         # 公共模块
│   │   │   ├── entity/                     # 实体类
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   ├── vo/                         # 视图对象
│   │   │   ├── result/                     # 统一响应
│   │   │   ├── exception/                  # 自定义异常
│   │   │   └── util/                       # 工具类
│   │   ├── config/                         # 配置类
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── user/                           # 用户模块
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   └── mapper/
│   │   ├── tool/                           # 工具模块
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── mapper/
│   │   │   └── config/
│   │   └── review/                         # 评论模块
│   │       ├── controller/
│   │       ├── service/
│   │       └── mapper/
│   └── resources/
│       ├── application.yml
│       └── mapper/                         # MyBatis XML映射文件
│           ├── user/
│           ├── tool/
│           └── review/
└── target/                                 # 编译输出(生成后)
```

## 🚀 启动说明

### 前置条件
1. MySQL数据库运行在localhost:3306
   - 数据库名: tool_recommend
   - 用户名: root
   - 密码: root123456

2. Redis运行在localhost:6379
   - 密码: redis123456

### 启动步骤

**1. 编译项目**(解决Lombok问题后):
```bash
mvn clean package -DskipTests
```

**2. 启动应用**:
```bash
java -jar target/tool-recommend-monolith-1.0.0.jar
```

或使用Maven:
```bash
mvn spring-boot:run
```

**3. 验证启动**:
访问 http://localhost:8080/actuator/health

**4. 启动Gateway**:
```bash
cd /Users/bjsttlp324/Desktop/tools/backend/gateway-service
mvn spring-boot:run
```

**5. 访问应用**:
通过Gateway访问: http://localhost:8090/api/**

## 🔍 测试接口

### 用户相关
- POST http://localhost:8090/api/users/register - 用户注册
- POST http://localhost:8090/api/users/login - 用户登录
- GET http://localhost:8090/api/users/profile - 获取用户信息

### 工具相关
- GET http://localhost:8090/api/tools - 查询工具列表
- GET http://localhost:8090/api/tools/{id} - 获取工具详情
- POST http://localhost:8090/api/tools - 创建工具(需登录)

### 评论相关
- GET http://localhost:8090/api/reviews/tool/{toolId} - 获取工具评论
- POST http://localhost:8090/api/reviews - 创建评论(需登录)

## 架构对比

### 微服务架构(之前)
```
Gateway:8090
    ↓
├─→ User Service:8082
├─→ Tool Service:8081
├─→ Review Service:8084
└─→ AI Service:8083
```

### 单体架构(之后)
```
Gateway:8090
    ↓
Monolith:8080
    ├─ User Module
    ├─ Tool Module
    ├─ Review Module
    └─ Common Module
```

## 📝 注意事项

1. **数据库共享**: 所有模块共享同一个数据库`tool_recommend`
2. **事务管理**: 单体应用中跨模块调用可以使用本地事务
3. **性能**: 去除了微服务间的HTTP调用开销
4. **部署**: 只需部署一个jar包,大大简化了部署流程
5. **扩展性**: 如果将来需要拆分,可以基于当前的模块结构快速拆分

## 🐛 已知问题

- AI模块和Admin模块未包含(缺少依赖)
- Lombok编译问题需要在IDE中解决
- 需要手动启动Gateway服务

## 📞 支持

如有问题,请检查:
1. Java版本是否为17
2. Maven版本是否正常
3. Lombok插件是否安装
4. 注解处理是否启用
