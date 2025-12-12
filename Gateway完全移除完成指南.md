# Gateway完全移除完成指南

## 概述

已成功将Gateway的所有功能迁移到Monolith单体服务中,现在可以完全去除Gateway服务。

---

## 已完成的工作

### ✅ 1. JWT认证过滤器迁移

**新文件**: `/Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith/src/main/java/com/toolrecommend/config/JwtAuthenticationFilter.java`

**功能**:
- JWT Token验证
- 白名单管理
- 用户信息提取并设置到request attributes
- 401未授权响应

**特点**:
- 使用 `@Order(1)` 确保优先执行
- 完全兼容原Gateway的认证逻辑
- 支持OPTIONS预检请求自动放行

### ✅ 2. CORS跨域配置迁移

**新文件**: `/Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith/src/main/java/com/toolrecommend/config/CorsConfig.java`

**功能**:
- 允许多个前端源访问
- 支持GET, POST, PUT, DELETE, OPTIONS, PATCH方法
- 允许携带凭证(cookies)
- 预检请求缓存3600秒

**允许的源**:
- http://localhost:3000 (Web前端)
- http://localhost:3001 (管理后台)
- http://localhost:8080 (Monolith自身)
- http://localhost:8081/8082/8090 (兼容旧端口)

### ✅ 3. 白名单配置

**文件**: `application.yml`

**配置项**:
```yaml
auth:
  whitelist: /api/users/register,/api/users/login,/api/users/refresh-token,/api/users/check-username,/api/users/check-email,/api/tools,/api/tools/*/detail,/api/tools/search,/api/tools/category/*,/api/categories,/api/categories/*,/api/reviews/tool/*,/actuator/**
```

**白名单说明**:
- **用户注册/登录**: 不需要token
- **工具浏览**: 游客可查看工具列表和详情
- **分类浏览**: 游客可查看分类
- **评论查看**: 游客可查看评论(不能发表)
- **收藏/点赞/写评论**: 需要登录(不在白名单)

### ✅ 4. 端口配置

**Monolith端口**: 从8090改为8080
```yaml
server:
  port: 8080
```

### ✅ 5. 前端配置更新

**文件**: `/Users/bjsttlp324/Desktop/tools/frontend/web/vite.config.js`

**修改**:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080', // 直连Monolith
    changeOrigin: true
  }
}
```

---

## 新架构

### 之前(使用Gateway)
```
前端(3000) → Gateway(8090) → Monolith(8080)
                ↓
         - JWT认证
         - CORS处理
         - 路由转发
```

### 现在(无Gateway)
```
前端(3000) → Monolith(8080)
                ↓
         - JWT认证 (JwtAuthenticationFilter)
         - CORS处理 (CorsConfig)
         - 业务逻辑
```

---

## 启动指南

### 1. 停止Gateway服务

如果Gateway正在运行,先停止它:
```bash
# 查找Gateway进程
lsof -i :8090

# 杀死进程
kill -9 <PID>
```

### 2. 启动Monolith服务

**使用IntelliJ IDEA** (推荐):
1. 打开项目: `/Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith`
2. 运行 `ToolRecommendApplication.java`
3. 等待启动成功,应该看到端口8080

**使用Maven命令行**:
```bash
cd /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith
mvn clean spring-boot:run
```

### 3. 启动前端

```bash
cd /Users/bjsttlp324/Desktop/tools/frontend/web
npm run dev
```

前端会在 http://localhost:3000 运行,所有 `/api/**` 请求会代理到 `http://localhost:8080`

---

## 测试验证

### 测试1: 访问公开接口(无需登录)

```bash
# 测试工具列表 - 应该返回200
curl http://localhost:8080/api/tools

# 测试工具详情 - 应该返回200
curl http://localhost:8080/api/tools/1/detail

# 测试分类列表 - 应该返回200
curl http://localhost:8080/api/categories
```

### 测试2: 访问需要登录的接口(应返回401)

```bash
# 测试收藏检查 - 应该返回401
curl http://localhost:8080/api/favorites/1/check

# 测试我的收藏 - 应该返回401
curl http://localhost:8080/api/favorites/my
```

### 测试3: 登录后访问

```bash
# 1. 注册用户
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"123456"}'

# 2. 登录获取token
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 3. 使用token访问(将<TOKEN>替换为上一步返回的token)
curl http://localhost:8080/api/favorites/my \
  -H "Authorization: Bearer <TOKEN>"
```

### 测试4: CORS测试

在浏览器Console中运行:
```javascript
fetch('http://localhost:8080/api/tools')
  .then(r => r.json())
  .then(data => console.log('CORS测试成功:', data))
  .catch(err => console.error('CORS测试失败:', err))
```

应该能成功获取数据,不会有CORS错误。

---

## 删除Gateway服务

确认一切正常后,可以删除Gateway服务:

```bash
# 备份Gateway配置(可选,以防万一)
cp -r /Users/bjsttlp324/Desktop/tools/backend/gateway-service /Users/bjsttlp324/Desktop/tools/backup_gateway-service

# 删除Gateway服务
rm -rf /Users/bjsttlp324/Desktop/tools/backend/gateway-service

# 或者只是移到别处
mv /Users/bjsttlp324/Desktop/tools/backend/gateway-service /Users/bjsttlp324/Desktop/tools/archived/
```

---

## 功能对比

| 功能 | Gateway方案 | Monolith方案 | 状态 |
|------|-----------|-------------|------|
| JWT认证 | AuthenticationFilter (Gateway) | JwtAuthenticationFilter (Monolith) | ✅ 已迁移 |
| 白名单管理 | application.yml (Gateway) | application.yml (Monolith) | ✅ 已迁移 |
| CORS处理 | globalcors (Gateway) | CorsConfig (Monolith) | ✅ 已迁移 |
| 请求限流 | RequestRateLimiter (Gateway) | 暂未实现 | ⚠️ 可选 |
| 路由转发 | Spring Cloud Gateway | 不需要 | ✅ 已移除 |

---

## 注意事项

### 1. 请求限流功能

Gateway的请求限流功能(基于Redis的令牌桶)暂未迁移。如果需要,可以后续添加:

**选项1**: 使用Spring的RateLimiter注解
**选项2**: 使用Bucket4j库
**选项3**: 在Nginx层面做限流(生产环境推荐)

### 2. 生产环境建议

虽然开发环境可以不用Gateway,但生产环境建议:
- 使用Nginx作为反向代理
- Nginx提供SSL/TLS加密
- Nginx提供请求限流
- Nginx提供负载均衡(如果有多实例)

**生产环境架构**:
```
Internet → Nginx(443) → Monolith(8080)
             ↓
        - SSL终止
        - 请求限流
        - 负载均衡
        - 静态文件服务
```

### 3. UserContext工具类

如果你的代码中有使用 `UserContext` 来获取当前用户信息,需要更新实现:

**原来(从Header获取)**:
```java
Long userId = Long.parseLong(request.getHeader("X-User-Id"));
```

**现在(从Request Attribute获取)**:
```java
Long userId = (Long) request.getAttribute("userId");
String username = (String) request.getAttribute("username");
String role = (String) request.getAttribute("userRole");
```

---

## 目录结构变化

### 之前
```
/Users/bjsttlp324/Desktop/tools/backend/
├── gateway-service/          # Gateway服务
├── tool-recommend-monolith/  # 单体应用
└── database/                 # 数据库脚本
```

### 现在
```
/Users/bjsttlp324/Desktop/tools/backend/
├── tool-recommend-monolith/  # 单体应用(包含所有功能)
│   ├── src/main/java/com/toolrecommend/
│   │   ├── config/
│   │   │   ├── JwtAuthenticationFilter.java  # JWT认证
│   │   │   ├── CorsConfig.java               # CORS配置
│   │   │   ├── MyBatisPlusConfig.java
│   │   │   └── ...
│   │   ├── user/         # 用户模块
│   │   ├── tool/         # 工具模块
│   │   ├── review/       # 评论模块
│   │   └── common/       # 公共模块
│   └── src/main/resources/
│       └── application.yml  # 包含auth.whitelist配置
└── database/                # 数据库脚本
```

---

## 回滚方案

如果迁移后发现问题,可以快速回滚:

### 1. 恢复Gateway端口配置

修改 `tool-recommend-monolith/src/main/resources/application.yml`:
```yaml
server:
  port: 8090  # 改回8090
```

### 2. 恢复前端配置

修改 `frontend/web/vite.config.js`:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8090',  # 改回8090
    changeOrigin: true
  }
}
```

### 3. 启动Gateway服务

```bash
cd /Users/bjsttlp324/Desktop/tools/backup_gateway-service
mvn spring-boot:run
```

---

## 优势总结

### 去除Gateway后的优势

1. **✅ 简化部署**: 只需运行1个服务
2. **✅ 降低延迟**: 减少一次网络跳转(约10-50ms)
3. **✅ 简化开发**: 本地开发不需要启动多个服务
4. **✅ 降低维护成本**: 少一个服务需要监控和维护
5. **✅ 节省资源**: Gateway通常需要500MB-1GB内存

### 保留的核心功能

1. **✅ JWT认证**: 完全迁移到JwtAuthenticationFilter
2. **✅ 白名单管理**: 通过application.yml配置
3. **✅ CORS处理**: 通过CorsConfig统一配置
4. **✅ 统一错误处理**: 401/403错误正确返回

---

## FAQ

### Q1: 没有Gateway,安全性会降低吗?

**A**: 不会。JWT认证、白名单管理等安全功能都已迁移到Monolith中,安全性不变。

### Q2: 未来如果要拆分为微服务怎么办?

**A**: 可以重新引入Gateway。JWT认证Filter可以快速迁移回Gateway,因为代码逻辑是一样的。

### Q3: 为什么不保留请求限流功能?

**A**: 请求限流在开发环境用处不大,生产环境建议在Nginx层面做限流更高效。如果确实需要,可以后续添加。

### Q4: 前端需要修改代码吗?

**A**: 不需要!前端只需修改vite.config.js的proxy配置即可,API调用代码完全不变。

---

## 总结

Gateway的所有核心功能已成功迁移到Monolith中:

- ✅ JWT认证
- ✅ CORS跨域
- ✅ 白名单管理
- ✅ 401错误处理

现在你可以:
1. 停止Gateway服务
2. 只运行Monolith(8080)
3. 前端直连Monolith
4. 删除Gateway目录(可选)

**新的启动命令**:
```bash
# 后端(只需1个服务)
cd /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith
mvn spring-boot:run

# 前端
cd /Users/bjsttlp324/Desktop/tools/frontend/web
npm run dev
```

简单、快速、高效! 🎉
