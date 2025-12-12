# API接口测试文档

## 📋 测试环境

- **后端地址**: http://localhost:8090
- **前端地址**: http://localhost:3001
- **数据库**: tool_recommend (已执行迁移)
- **服务状态**: ✅ 运行中

---

## 🔐 前置准备：获取登录Token

所有需要认证的接口都需要在Header中携带Token。

### 1. 注册账号（如果没有）

```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "123456"
  }'
```

### 2. 登录获取Token

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com"
    }
  },
  "success": true
}
```

**保存Token**:
```bash
# 将token保存到环境变量
export TOKEN="your_token_here"
```

---

## ⭐ 评分功能API测试

### 1. 获取工具评分统计（无需登录）

```bash
curl -X GET "http://localhost:8090/api/ratings/tool/1"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "averageRating": 4.5,
    "ratingCount": 10,
    "userScore": null
  },
  "success": true
}
```

### 2. 提交评分（需要登录）

```bash
curl -X POST "http://localhost:8090/api/ratings" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "score": 5
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "success": true
}
```

**验证**: 再次调用API #1，应该看到：
- `averageRating` 更新
- `ratingCount` 增加
- `userScore` 显示为5

### 3. 修改评分

```bash
curl -X POST "http://localhost:8090/api/ratings" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "score": 4
  }'
```

**验证**: `userScore` 应该更新为4

### 4. 检查是否已评分

```bash
curl -X GET "http://localhost:8090/api/ratings/tool/1/check" \
  -H "Authorization: Bearer $TOKEN"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "success": true
}
```

### 5. 删除评分

```bash
curl -X DELETE "http://localhost:8090/api/ratings/tool/1" \
  -H "Authorization: Bearer $TOKEN"
```

**验证**: 再次调用API #4，应该返回 `false`

---

## 💬 评论功能API测试

### 1. 获取工具的评论列表

```bash
curl -X GET "http://localhost:8090/api/reviews/tool/1?current=1&size=10"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "toolId": 1,
        "userId": 1,
        "username": "testuser",
        "content": "这个工具很好用！",
        "helpfulCount": 5,
        "createdAt": "2025-12-11T10:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  },
  "success": true
}
```

### 2. 发表评论（需要登录）

```bash
curl -X POST "http://localhost:8090/api/reviews" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "content": "这个工具真的很棒，强烈推荐！",
    "title": "非常实用的工具"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "评论成功",
  "data": 2,
  "success": true
}
```

### 3. 发表多条评论

评分和评论现在是分离的，所以：
- ✅ 一个用户对一个工具只能打一次分
- ✅ 一个用户可以对一个工具发表多条评论

```bash
# 第二条评论
curl -X POST "http://localhost:8090/api/reviews" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "content": "再补充一点使用心得...",
    "title": "使用心得补充"
  }'
```

**预期**: 应该成功，不会报"已评论过"的错误

### 4. 回复评论（使用parentId）

```bash
curl -X POST "http://localhost:8090/api/reviews" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "parentId": 1,
    "content": "我也觉得很好用！"
  }'
```

**说明**: 设置`parentId`表示这是对评论ID=1的回复

### 5. 删除评论

```bash
curl -X DELETE "http://localhost:8090/api/reviews/2" \
  -H "Authorization: Bearer $TOKEN"
```

### 6. 标记评论有帮助

```bash
curl -X POST "http://localhost:8090/api/reviews/1/helpful" \
  -H "Authorization: Bearer $TOKEN"
```

### 7. 取消有帮助标记

```bash
curl -X DELETE "http://localhost:8090/api/reviews/1/helpful" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔧 工具相关API测试

### 1. 获取工具详情

```bash
curl -X GET "http://localhost:8090/api/tools/1" \
  -H "Authorization: Bearer $TOKEN"
```

**验证点**:
- `averageRating` - 应该反映真实的评分
- `favoriteCount` - 收藏人数
- `reviewCount` - 评论数（只统计顶级评论）

### 2. 收藏工具

```bash
curl -X POST "http://localhost:8090/api/favorites/1" \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 取消收藏

```bash
curl -X DELETE "http://localhost:8090/api/favorites/1" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 检查收藏状态

```bash
curl -X GET "http://localhost:8090/api/favorites/1/check" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 测试场景

### 场景1: 完整的评分流程

```bash
# 1. 查看当前评分统计
curl http://localhost:8090/api/ratings/tool/1

# 2. 提交评分
curl -X POST http://localhost:8090/api/ratings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"toolId": 1, "score": 5}'

# 3. 验证评分已提交
curl http://localhost:8090/api/ratings/tool/1/check \
  -H "Authorization: Bearer $TOKEN"

# 4. 再次查看统计（应该有变化）
curl http://localhost:8090/api/ratings/tool/1

# 5. 修改评分
curl -X POST http://localhost:8090/api/ratings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"toolId": 1, "score": 4}'

# 6. 删除评分
curl -X DELETE http://localhost:8090/api/ratings/tool/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 场景2: 评论与回复

```bash
# 1. 发表顶级评论
curl -X POST http://localhost:8090/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "content": "这是我的第一条评论"
  }'

# 2. 再发表一条评论（测试多条评论）
curl -X POST http://localhost:8090/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "content": "这是我的第二条评论"
  }'

# 3. 回复第一条评论（假设ID=1）
curl -X POST http://localhost:8090/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toolId": 1,
    "parentId": 1,
    "content": "这是对第一条评论的回复"
  }'

# 4. 查看所有评论
curl "http://localhost:8090/api/reviews/tool/1?current=1&size=10"
```

### 场景3: 综合测试

```bash
# 1. 评分
curl -X POST http://localhost:8090/api/ratings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"toolId": 1, "score": 5}'

# 2. 发表评论
curl -X POST http://localhost:8090/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"toolId": 1, "content": "五星好评！"}'

# 3. 收藏工具
curl -X POST http://localhost:8090/api/favorites/1 \
  -H "Authorization: Bearer $TOKEN"

# 4. 查看工具详情（验证统计数据）
curl http://localhost:8090/api/tools/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## ✅ 验证清单

### 评分功能
- [ ] 可以提交评分（1-5分）
- [ ] 评分后统计数据立即更新
- [ ] 可以修改已有评分
- [ ] 一个用户对一个工具只能有一条评分记录
- [ ] 可以删除评分
- [ ] 未登录可以查看统计，但不能评分

### 评论功能
- [ ] 可以发表评论（不含评分）
- [ ] 可以发表多条评论
- [ ] 可以回复评论（设置parentId）
- [ ] 可以删除自己的评论
- [ ] 可以标记评论有帮助
- [ ] 评论数统计正确

### 数据一致性
- [ ] 评分与评论分离（rating表 vs review表）
- [ ] 工具详情页显示正确的averageRating
- [ ] review表不再有rating字段
- [ ] review表有parentId和replyCount字段

---

## 🐛 常见错误及解决方案

### 错误1: 401 Unauthorized

**原因**: Token无效或未提供
**解决**:
```bash
# 重新登录获取新Token
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'
```

### 错误2: 403 Forbidden

**原因**: 无权限操作（如删除他人评论）
**解决**: 只能操作自己的数据

### 错误3: 404 Not Found

**原因**: 工具或评论不存在
**解决**: 使用有效的ID

### 错误4: 400 Bad Request

**原因**: 请求参数错误
**解决**: 检查JSON格式和字段名称

---

## 📝 测试报告模板

### 测试环境
- 测试时间: 2025-12-11
- 后端版本: 1.0.0
- 测试人:
- Token: (已获取)

### 测试结果

#### 评分API测试
| API | 方法 | 路径 | 状态 | 备注 |
|-----|------|------|------|------|
| 获取评分统计 | GET | /api/ratings/tool/1 | ⬜ |  |
| 提交评分 | POST | /api/ratings | ⬜ |  |
| 检查评分 | GET | /api/ratings/tool/1/check | ⬜ |  |
| 删除评分 | DELETE | /api/ratings/tool/1 | ⬜ |  |

#### 评论API测试
| API | 方法 | 路径 | 状态 | 备注 |
|-----|------|------|------|------|
| 获取评论列表 | GET | /api/reviews/tool/1 | ⬜ |  |
| 发表评论 | POST | /api/reviews | ⬜ |  |
| 发表第二条评论 | POST | /api/reviews | ⬜ |  |
| 回复评论 | POST | /api/reviews | ⬜ |  |
| 删除评论 | DELETE | /api/reviews/:id | ⬜ |  |

### 发现的问题
1.
2.
3.

### 建议
1.
2.
3.

---

## 🎯 下一步

测试完成后：
1. ✅ 评分功能正常 → 可以部署使用
2. ✅ 评论功能正常 → 可以实现前端评论回复UI
3. ❌ 发现问题 → 查看日志，修复bug

---

**祝测试顺利！** 🚀
