# 端口清理脚本使用说明

## 快速开始

### 清理单个服务端口

```bash
# 进入backend目录
cd /Users/bjsttlp324/Desktop/tools/backend

# 清理用户服务端口（8082）
./kill-service-ports.sh user-service

# 清理网关服务端口（8090）
./kill-service-ports.sh gateway-service

# 清理评论服务端口（8084）
./kill-service-ports.sh review-service

# 清理工具服务端口（8081）
./kill-service-ports.sh tool-service
```

### 清理所有服务端口

```bash
./kill-service-ports.sh all
```

## 在IDEA中使用

### 方法1: 运行前手动执行

1. 打开终端
2. 执行清理脚本
3. 在IDEA中启动服务

### 方法2: 配置IDEA外部工具（推荐）

#### 步骤1: 创建外部工具

1. 打开 IDEA
2. 进入 `Preferences` → `Tools` → `External Tools`
3. 点击 `+` 创建新工具

**配置示例（User Service）**:
```
Name: Kill User Service Port
Description: 清理用户服务8082端口
Program: /Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
Arguments: user-service
Working directory: /Users/bjsttlp324/Desktop/tools/backend
```

#### 步骤2: 添加到运行配置

1. 打开运行配置: `Run` → `Edit Configurations...`
2. 选择你的服务（如 UserServiceApplication）
3. 找到 `Before launch` 部分
4. 点击 `+` → `Run External tool`
5. 选择刚创建的 `Kill User Service Port`
6. 确保该工具在 `Build` 之前执行

#### 步骤3: 禁用多实例

在同一个运行配置页面：
- 取消勾选 `Allow multiple instances`
- 点击 Apply 和 OK

### 完成！

现在在IDEA中点击运行按钮时，会自动清理端口，避免端口占用问题。

## 服务端口对照表

| 服务名 | 端口 | 脚本参数 |
|--------|------|---------|
| 工具服务 | 8081 | tool-service |
| 用户服务 | 8082 | user-service |
| AI服务 | 8083 | ai-service |
| 评论服务 | 8084 | review-service |
| 网关服务 | 8090 | gateway-service |
| 所有服务 | - | all |

## 常见问题

### Q: 脚本执行失败怎么办？

A: 确保脚本有执行权限：
```bash
chmod +x /Users/bjsttlp324/Desktop/tools/backend/kill-service-ports.sh
```

### Q: 端口仍然被占用？

A: 手动检查端口占用：
```bash
lsof -i :8082
```

然后强制杀掉：
```bash
lsof -ti :8082 | xargs kill -9
```

### Q: 如何查看脚本输出？

A: 脚本会显示彩色输出：
- 🟢 绿色：正常信息
- 🟡 黄色：警告（端口被占用）
- 🔴 红色：错误（清理失败）

## 更多帮助

详细配置指南请查看: `/Users/bjsttlp324/Desktop/tools/IDEA端口占用解决方案.md`
