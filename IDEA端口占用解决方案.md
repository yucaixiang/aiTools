# IDEA 端口占用解决方案

## 问题描述

在IDEA中重启Spring Boot服务时，经常遇到"端口已被占用"的错误：
```
Web server failed to start. Port 8082 was already in use.
```

这是因为上次运行的进程没有完全停止，导致端口仍被占用。

---

## 解决方案汇总

### 方案1: 使用自动清理脚本（推荐 ⭐⭐⭐⭐⭐）

#### 1.1 脚本位置
```
/Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
```

#### 1.2 使用方法

**清理所有服务端口**:
```bash
cd /Users/bjsttlp324/Desktop/tools/backend
./kill-service-ports.sh all
```

**清理单个服务端口**:
```bash
./kill-service-ports.sh user-service     # 清理8082端口
./kill-service-ports.sh gateway-service  # 清理8090端口
./kill-service-ports.sh review-service   # 清理8084端口
./kill-service-ports.sh tool-service     # 清理8081端口
```

#### 1.3 脚本功能
- ✅ 自动检测端口占用
- ✅ 显示占用进程的详细信息
- ✅ 优雅关闭进程（先SIGTERM，再SIGKILL）
- ✅ 彩色输出，易于阅读
- ✅ 支持清理单个或所有服务

---

### 方案2: 配置IDEA运行前脚本

#### 2.1 为每个服务配置启动前脚本

1. **打开IDEA运行配置**
   - 点击右上角的运行配置下拉菜单
   - 选择 "Edit Configurations..."

2. **选择要配置的服务**（例如 UserServiceApplication）

3. **添加启动前脚本**
   - 点击 "Before launch" 部分
   - 点击 "+" 号
   - 选择 "Run External tool"
   - 点击 "+" 创建新工具

4. **配置外部工具**
   ```
   Name: Kill User Service Port
   Program: /Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
   Arguments: user-service
   Working directory: /Users/bjsttlp324/Desktop/tools/backend
   ```

5. **点击 OK 保存**

6. **调整执行顺序**
   - 确保 "Kill User Service Port" 在 "Build" 之前执行

#### 2.2 为每个服务创建外部工具

| 服务 | 工具名称 | Arguments |
|------|---------|-----------|
| UserService | Kill User Service Port | user-service |
| GatewayService | Kill Gateway Service Port | gateway-service |
| ReviewService | Kill Review Service Port | review-service |
| ToolService | Kill Tool Service Port | tool-service |

---

### 方案3: 使用IDEA的"单实例"模式

#### 3.1 配置步骤

1. 打开运行配置 (Edit Configurations...)
2. 选择你的服务（如 UserServiceApplication）
3. 找到 "Allow multiple instances" 复选框
4. **取消勾选** "Allow multiple instances"
5. 点击 Apply 和 OK

#### 3.2 效果
- IDEA会在启动新实例前自动停止旧实例
- 但有时停止不彻底，仍可能导致端口占用

---

### 方案4: 手动清理端口

#### 4.1 使用终端命令

**查找占用端口的进程**:
```bash
lsof -i :8082
```

**杀掉进程**:
```bash
# 方法1: 使用PID
kill -9 <PID>

# 方法2: 直接杀掉占用端口的进程
lsof -ti :8082 | xargs kill -9
```

#### 4.2 一键清理所有服务端口
```bash
lsof -ti :8081,:8082,:8083,:8084,:8090 | xargs kill -9
```

---

### 方案5: 配置Spring Boot优雅停机

#### 5.1 在 application.yml 中添加配置

```yaml
# 优雅停机配置
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

#### 5.2 效果
- Spring Boot会在收到停止信号后优雅关闭
- 但IDEA的停止按钮可能不会发送正确的信号

---

## 推荐配置流程

### 第一步: 创建外部工具清单

在IDEA中创建以下外部工具（Settings -> Tools -> External Tools）:

#### 工具1: Kill All Service Ports
```
Name: Kill All Service Ports
Program: /Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
Arguments: all
Working directory: /Users/bjsttlp324/Desktop/tools/backend
```

#### 工具2-5: 为每个服务创建单独的清理工具
按照上面"方案2"的表格创建。

### 第二步: 配置运行配置

为每个服务配置：

1. **UserServiceApplication**
   - Before launch: 添加 "Kill User Service Port"
   - 取消勾选 "Allow multiple instances"

2. **GatewayServiceApplication**
   - Before launch: 添加 "Kill Gateway Service Port"
   - 取消勾选 "Allow multiple instances"

3. **ReviewServiceApplication**
   - Before launch: 添加 "Kill Review Service Port"
   - 取消勾选 "Allow multiple instances"

4. **ToolServiceApplication**
   - Before launch: 添加 "Kill Tool Service Port"
   - 取消勾选 "Allow multiple instances"

### 第三步: 测试配置

1. 启动一个服务（如 UserService）
2. 等待启动完成
3. 点击重启按钮
4. 观察控制台输出，应该看到脚本执行的日志
5. 服务应该能正常启动，没有端口占用错误

---

## 快速参考

### 常用命令

```bash
# 查看所有服务端口占用情况
lsof -i :8081,:8082,:8083,:8084,:8090

# 清理所有服务端口
cd /Users/bjsttlp324/Desktop/tools/backend
./kill-service-ports.sh all

# 清理单个服务
./kill-service-ports.sh user-service

# 查看脚本帮助
./kill-service-ports.sh
```

### IDEA 快捷操作

| 操作 | 快捷键 (Mac) | 快捷键 (Windows/Linux) |
|------|-------------|----------------------|
| 停止运行 | ⌘ + F2 | Ctrl + F2 |
| 重新运行 | ⌃ + R | Shift + F10 |
| 调试模式运行 | ⌃ + D | Shift + F9 |
| 打开运行配置 | ⌃ + ⌥ + R | Alt + Shift + F10 |

---

## 常见问题 FAQ

### Q1: 脚本执行失败，提示权限不足
**A**: 确保脚本有执行权限
```bash
chmod +x /Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
```

### Q2: IDEA启动前脚本不执行
**A**: 检查以下几点：
1. 外部工具配置的路径是否正确
2. Before launch 中是否正确添加了工具
3. 工具是否在 Build 之前执行

### Q3: 端口仍然被占用
**A**: 手动执行脚本查看详细信息
```bash
./kill-service-ports.sh user-service
```
如果脚本也无法清理，可能是权限问题，尝试：
```bash
sudo ./kill-service-ports.sh user-service
```

### Q4: 如何查看哪个进程占用了端口？
**A**: 使用 lsof 命令
```bash
lsof -i :8082
# 显示详细信息
lsof -i :8082 | grep LISTEN
```

### Q5: Maven进程无法停止怎么办？
**A**: 强制杀掉所有Maven进程
```bash
# 查找Maven进程
ps aux | grep maven

# 杀掉所有Maven进程
pkill -f maven
```

---

## 最佳实践

### 1. 开发阶段
- ✅ 使用方案1的自动脚本
- ✅ 配置IDEA运行前脚本（方案2）
- ✅ 禁用多实例运行（方案3）

### 2. 生产环境
- ✅ 使用systemd或docker管理服务
- ✅ 配置优雅停机（方案5）
- ✅ 监控端口占用情况

### 3. 团队协作
- ✅ 将脚本提交到代码仓库
- ✅ 在README中说明配置方法
- ✅ 统一团队的IDEA配置

---

## 附录: 各服务端口清单

| 服务 | 端口 | 描述 |
|------|------|------|
| tool-service | 8081 | 工具服务 |
| user-service | 8082 | 用户服务（包含收藏功能） |
| ai-service | 8083 | AI服务 |
| review-service | 8084 | 评论服务 |
| gateway-service | 8090 | 网关服务 |

---

## 总结

推荐使用 **方案1（自动脚本） + 方案2（IDEA配置）** 的组合：

1. ✅ 自动化程度高
2. ✅ 不需要手动干预
3. ✅ 错误提示清晰
4. ✅ 支持单个和批量操作

这样配置后，在IDEA中点击运行按钮时，会自动清理端口，避免端口占用问题。

---

**文档版本**: v1.0
**更新日期**: 2025-12-10
**作者**: AI Tool Recommend Team
