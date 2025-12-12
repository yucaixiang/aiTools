# Monolith服务重启指南

## 问题原因

当前运行的Monolith服务（PID 75864）是旧版本，还没有包含HttpServletRequestWrapper的修复代码。需要重新编译并重启服务才能应用修复。

## 修复内容

已修改的文件：
- `/Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith/src/main/java/com/toolrecommend/config/JwtAuthenticationFilter.java`
- 第97-113行：添加了HttpServletRequestWrapper来注入X-User-Id等请求头

## 重启步骤

### 方法1: 使用IntelliJ IDEA重启（推荐）

1. **停止当前运行的服务**
   - 在IntelliJ IDEA底部找到"Run"标签
   - 点击红色的停止按钮 ⬛
   - 或按快捷键: `Cmd + F2` (Mac) / `Ctrl + F2` (Windows)

2. **重新编译项目**
   - 菜单: `Build` -> `Rebuild Project`
   - 等待编译完成（确保没有错误）
   - 这会重新编译JwtAuthenticationFilter.java并应用我们的修复

3. **重新启动服务**
   - 在项目中找到: `src/main/java/com/toolrecommend/ToolRecommendApplication.java`
   - 右键点击 -> `Run 'ToolRecommendApplication'`
   - 或使用上次运行配置: 点击绿色的运行按钮 ▶️
   - 或按快捷键: `Ctrl + R` (Mac) / `Shift + F10` (Windows)

4. **等待服务启动**
   - 在控制台中看到: `Started ToolRecommendApplication in X.XXX seconds`
   - 确认端口8090已启动

5. **验证修复**
   ```bash
   # 在终端运行测试脚本
   cd /Users/bjsttlp324/Desktop/tools
   ./test-auth.sh
   ```

### 方法2: 使用命令行重启

如果IntelliJ IDEA有问题，可以使用命令行：

1. **停止当前服务**
   ```bash
   # 杀掉旧进程
   kill -9 75864

   # 确认已停止
   lsof -i :8090
   ```

2. **清理并重新编译**
   ```bash
   cd /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith
   mvn clean package -DskipTests
   ```

3. **启动服务**
   ```bash
   java -jar target/tool-recommend-monolith-1.0.0.jar
   ```

4. **或使用Maven运行**
   ```bash
   mvn spring-boot:run
   ```

## 验证修复是否生效

### 方法1: 使用测试脚本（推荐）

```bash
cd /Users/bjsttlp324/Desktop/tools
./test-auth.sh
```

测试脚本会：
1. 注册一个测试用户
2. 登录获取Token
3. 使用Token访问需要认证的接口
4. 检查是否还有"未授权"或"缺少请求头"错误

### 方法2: 手动测试

1. **查看后端日志**

   在IntelliJ IDEA的控制台或命令行中，应该能看到类似的日志：
   ```
   ========== JWT认证过滤器 ==========
   请求: GET /api/favorites/1/check
   Authorization Header: Bearer eyJhbGciOiJIUzI1NiJ9...
   提取的Token: eyJhbGciOiJIUzI1...
   开始验证Token...
   用户认证成功: userId=1, username=test123, role=USER
   ```

2. **测试API**

   ```bash
   # 1. 登录获取token
   TOKEN=$(curl -s -X POST http://localhost:8090/api/users/login \
     -H "Content-Type: application/json" \
     -d '{"account":"你的用户名","password":"你的密码"}' \
     | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['token'])")

   # 2. 测试收藏检查接口
   curl http://localhost:8090/api/favorites/1/check \
     -H "Authorization: Bearer $TOKEN"
   ```

3. **期望结果**

   - ✅ 正常响应: `{"code":200,"message":"success","data":false}`
   - ❌ 错误响应: `{"code":401,"message":"未授权，请先登录"}` 或 `{"message":"缺少请求头: X-User-Id"}`

## 如果重启后仍然报错

### 检查1: 确认修复代码已应用

```bash
# 检查编译后的class文件是否包含HttpServletRequestWrapper
ls -l /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith/target/classes/com/toolrecommend/config/JwtAuthenticationFilter.class

# 查看文件修改时间，应该是最近的时间
```

### 检查2: 确认是正确的进程

```bash
# 查看8090端口的进程
lsof -i :8090

# 查看进程详情
ps aux | grep ToolRecommendApplication
```

### 检查3: 查看完整的后端日志

在IntelliJ IDEA控制台或日志文件中查找：
- JWT认证过滤器的日志
- 是否有编译错误
- 是否有其他异常

### 检查4: 确认代码没有被还原

```bash
# 查看JwtAuthenticationFilter.java的内容
grep -A 15 "HttpServletRequestWrapper" /Users/bjsttlp324/Desktop/tools/backend/tool-recommend-monolith/src/main/java/com/toolrecommend/config/JwtAuthenticationFilter.java
```

应该能看到:
```java
HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
    @Override
    public String getHeader(String name) {
        if ("X-User-Id".equalsIgnoreCase(name)) {
            return String.valueOf(userId);
        } else if ("Username".equalsIgnoreCase(name)) {
            return username;
        } else if ("User-Role".equalsIgnoreCase(name)) {
            return role;
        }
        return super.getHeader(name);
    }
};
```

## 常见问题

### Q1: IntelliJ IDEA编译失败，提示Lombok错误

**原因**: Lombok注解处理器没有正确配置

**解决方案**:
1. 确保安装了Lombok插件: `Preferences` -> `Plugins` -> 搜索"Lombok"
2. 启用注解处理: `Preferences` -> `Build, Execution, Deployment` -> `Compiler` -> `Annotation Processors` -> 勾选"Enable annotation processing"
3. 重新同步Maven: 右键`pom.xml` -> `Maven` -> `Reload project`

### Q2: 端口8090已被占用

**原因**: 旧进程没有完全停止

**解决方案**:
```bash
# 查找并杀掉占用8090端口的进程
lsof -i :8090 | grep LISTEN | awk '{print $2}' | xargs kill -9

# 等待几秒后重启
```

### Q3: 修复后前端仍然提示未授权

**原因**: 前端可能缓存了旧的token或需要刷新

**解决方案**:
1. 清除浏览器localStorage: 在浏览器Console中运行 `localStorage.clear()`
2. 重新登录获取新token
3. 刷新页面 (Cmd+Shift+R / Ctrl+Shift+R 强制刷新)

## 成功标志

重启成功后，你应该能够：

✅ 登录成功并获取Token
✅ 使用Token访问收藏接口不再报"未授权"错误
✅ 使用Token访问评论接口不再报"未授权"错误
✅ 后端日志显示"用户认证成功"
✅ 不再有"缺少请求头: X-User-Id"的错误

## 下一步

修复验证成功后，所有需要认证的功能都应该能正常工作：

- ✅ 收藏/取消收藏工具
- ✅ 查看我的收藏列表
- ✅ 发表/编辑/删除评论
- ✅ 标记评论有帮助
- ✅ 查看我的评论列表

如果还有问题，请提供：
1. 后端完整的启动日志
2. 访问接口时的后端日志
3. 浏览器Console中的错误信息
