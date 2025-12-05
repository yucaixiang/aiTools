# AI工具推荐系统 - API接口文档

## 文档信息
- **项目名称**：智能工具推荐平台
- **版本**：v1.0
- **创建日期**：2025-12-04
- **Base URL**：`https://api.toolrecommend.com`

---

## 一、接口规范

### 1.1 通用规范

**请求头**：
```
Content-Type: application/json
Authorization: Bearer {access_token}  // 需要登录的接口
X-Request-ID: {uuid}  // 请求追踪ID
```

**响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1701676800000
}
```

**状态码**：
- `200`：成功
- `400`：请求参数错误
- `401`：未授权
- `403`：无权限
- `404`：资源不存在
- `429`：请求过于频繁
- `500`：服务器错误

---

## 二、认证接口

### 2.1 用户注册

**接口**：`POST /api/users/register`

**请求参数**：
```json
{
  "username": "string (必填，3-50字符)",
  "email": "string (邮箱或手机号二选一)",
  "phone": "string",
  "password": "string (必填，8-20字符)",
  "code": "string (必填，验证码)"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 123,
    "username": "john_doe"
  }
}
```

### 2.2 用户登录

**接口**：`POST /api/users/login`

**请求参数**：
```json
{
  "account": "string (用户名/邮箱/手机号)",
  "password": "string",
  "remember": "boolean (是否记住登录，默认false)"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
      "id": 123,
      "username": "john_doe",
      "email": "john@example.com",
      "avatar": "https://cdn.example.com/avatar.jpg",
      "role": "USER"
    }
  }
}
```

### 2.3 刷新Token

**接口**：`POST /api/users/refresh`

**请求头**：
```
Authorization: Bearer {refresh_token}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "new_access_token...",
    "expiresIn": 3600
  }
}
```

---

## 三、工具接口

### 3.1 获取工具列表

**接口**：`GET /api/tools`

**查询参数**：
```
page: int (页码，默认1)
size: int (每页数量，默认20，最大100)
categoryId: int (分类ID)
tags: string (标签，逗号分隔，如"ai,free")
pricingModel: string (定价模式：FREE/FREEMIUM/PAID/OPEN_SOURCE)
platforms: string (平台，逗号分隔)
minRating: float (最低评分，1-5)
sort: string (排序：hot/new/rating，默认hot)
```

**请求示例**：
```
GET /api/tools?categoryId=1&tags=ai,free&sort=hot&page=1&size=20
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 156,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "name": "Notion",
        "slug": "notion",
        "tagline": "一站式协作工作空间",
        "logo": "https://cdn.example.com/notion-logo.png",
        "category": {
          "id": 3,
          "name": "生产力工具"
        },
        "tags": ["笔记", "协作", "免费"],
        "pricingModel": "FREEMIUM",
        "startingPrice": 0,
        "averageRating": 4.8,
        "reviewCount": 1523,
        "viewCount": 125600,
        "favoriteCount": 8920,
        "upvoteCount": 6543,
        "launchDate": "2016-03-15"
      }
    ]
  }
}
```

### 3.2 获取工具详情

**接口**：`GET /api/tools/{id}`

**路径参数**：
- `id`: 工具ID

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Notion",
    "slug": "notion",
    "tagline": "一站式协作工作空间",
    "description": "# Notion介绍\n\nNotion是一款...",
    "logo": "https://cdn.example.com/notion-logo.png",
    "website": "https://notion.so",
    "downloadUrl": null,
    "githubUrl": null,
    "category": {
      "id": 3,
      "name": "生产力工具",
      "slug": "productivity"
    },
    "tags": ["笔记", "协作", "免费", "跨平台"],
    "pricingModel": "FREEMIUM",
    "startingPrice": 0,
    "pricing": {
      "free": {
        "name": "个人版",
        "price": 0,
        "features": ["无限页面", "5MB文件上传"]
      },
      "paid": {
        "name": "专业版",
        "price": 8,
        "features": ["无限文件上传", "版本历史"]
      }
    },
    "platforms": ["Web", "iOS", "Android", "Windows", "Mac"],
    "features": [
      {
        "type": "platform",
        "name": "Web",
        "value": null
      }
    ],
    "screenshots": [
      "https://cdn.example.com/notion-1.png",
      "https://cdn.example.com/notion-2.png"
    ],
    "averageRating": 4.8,
    "reviewCount": 1523,
    "viewCount": 125600,
    "favoriteCount": 8920,
    "upvoteCount": 6543,
    "launchDate": "2016-03-15",
    "alternatives": [
      {
        "id": 5,
        "name": "Obsidian",
        "logo": "...",
        "similarityScore": 0.85
      }
    ],
    "similarTools": [
      {
        "id": 12,
        "name": "Roam Research",
        "logo": "..."
      }
    ]
  }
}
```

### 3.3 搜索工具

**接口**：`GET /api/tools/search`

**查询参数**：
```
q: string (搜索关键词，必填)
page: int (页码，默认1)
size: int (每页数量，默认20)
categoryId: int (分类ID，可选)
```

**请求示例**：
```
GET /api/tools/search?q=AI%E7%BC%96%E7%A8%8B&page=1
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 45,
    "page": 1,
    "size": 20,
    "keyword": "AI编程",
    "items": [
      {
        "id": 25,
        "name": "GitHub Copilot",
        "tagline": "AI驱动的代码助手",
        "matchScore": 0.95
      }
    ]
  }
}
```

### 3.4 获取热门工具

**接口**：`GET /api/tools/hot`

**查询参数**：
```
period: string (时间范围：today/week/month，默认week)
limit: int (数量，默认20)
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "week",
    "items": [
      {
        "rank": 1,
        "tool": {},
        "trend": "up"
      }
    ]
  }
}
```

---

## 四、AI推荐接口

### 4.1 发送对话消息

**接口**：`POST /api/ai/chat`

**请求参数**：
```json
{
  "message": "string (用户消息，必填)",
  "sessionId": "string (会话ID，首次为空)",
  "context": {
    "previousTools": [1, 5, 12]
  }
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "uuid-session-id",
    "response": "根据您的需求，我为您推荐以下工具：",
    "tools": [
      {
        "id": 1,
        "name": "Notion",
        "tagline": "一站式协作工作空间",
        "logo": "...",
        "reason": "免费版功能强大，支持多端同步，非常适合个人使用",
        "matchScore": 0.92
      }
    ],
    "followUpQuestions": [
      "您需要团队协作功能吗？",
      "您的预算是多少？"
    ]
  }
}
```

### 4.2 流式对话（SSE）

**接口**：`GET /api/ai/chat/stream`

**查询参数**：
```
message: string (用户消息)
sessionId: string (会话ID)
```

**响应格式**：Server-Sent Events
```
data: {"type":"text","content":"根据您的"}

data: {"type":"text","content":"需求，我"}

data: {"type":"tool","tool":{"id":1,"name":"Notion"}}

data: {"type":"done"}
```

### 4.3 获取对话历史

**接口**：`GET /api/ai/conversations`

**查询参数**：
```
page: int (页码)
size: int (每页数量)
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "items": [
      {
        "sessionId": "uuid-1",
        "title": "寻找笔记工具",
        "messageCount": 8,
        "lastMessage": "感谢推荐，我会试试Notion",
        "createdAt": "2025-12-01T10:30:00Z",
        "updatedAt": "2025-12-01T10:35:00Z"
      }
    ]
  }
}
```

### 4.4 对话反馈

**接口**：`POST /api/ai/feedback`

**请求参数**：
```json
{
  "conversationId": 123,
  "feedbackType": "THUMBS_UP|THUMBS_DOWN|CLICK_TOOL",
  "toolId": 1,
  "comment": "推荐的工具很合适"
}
```

---

## 五、用户中心接口

### 5.1 获取当前用户信息

**接口**：`GET /api/users/me`

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "138****5678",
    "avatar": "https://cdn.example.com/avatar.jpg",
    "nickname": "John",
    "bio": "热爱尝试新工具的产品经理",
    "role": "USER",
    "stats": {
      "favoriteCount": 25,
      "reviewCount": 8,
      "upvoteCount": 42,
      "conversationCount": 15
    },
    "createdAt": "2024-01-15T08:00:00Z"
  }
}
```

### 5.2 收藏工具

**接口**：`POST /api/users/favorites/{toolId}`

**响应示例**：
```json
{
  "code": 200,
  "message": "收藏成功"
}
```

### 5.3 取消收藏

**接口**：`DELETE /api/users/favorites/{toolId}`

### 5.4 获取收藏列表

**接口**：`GET /api/users/favorites`

**查询参数**：
```
page: int
size: int
```

### 5.5 点赞工具

**接口**：`POST /api/users/upvotes/{toolId}`

### 5.6 获取浏览历史

**接口**：`GET /api/users/history`

---

## 六、评论接口

### 6.1 提交评论

**接口**：`POST /api/reviews`

**请求参数**：
```json
{
  "toolId": 1,
  "rating": 5,
  "title": "非常好用的工具",
  "content": "用了三个月，体验非常好...",
  "pros": ["界面简洁", "功能强大"],
  "cons": ["价格略贵"],
  "usageDuration": "QUARTER",
  "usageScenario": "工作"
}
```

### 6.2 获取工具评论列表

**接口**：`GET /api/reviews/tool/{toolId}`

**查询参数**：
```
sort: string (hot/new/rating，默认hot)
page: int
size: int
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 1523,
    "page": 1,
    "items": [
      {
        "id": 5001,
        "user": {
          "id": 123,
          "username": "john_doe",
          "avatar": "..."
        },
        "rating": 5,
        "title": "非常好用",
        "content": "...",
        "pros": ["简洁", "强大"],
        "cons": ["略贵"],
        "helpfulCount": 42,
        "replyCount": 5,
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ]
  }
}
```

### 6.3 评论点赞

**接口**：`POST /api/reviews/{reviewId}/like`

---

## 七、管理后台接口

### 7.1 工具审核列表

**接口**：`GET /api/admin/tools/pending`
**权限**：ADMIN

**查询参数**：
```
page: int
size: int
```

### 7.2 审核通过

**接口**：`POST /api/admin/tools/{id}/approve`
**权限**：ADMIN

### 7.3 审核驳回

**接口**：`POST /api/admin/tools/{id}/reject`
**权限**：ADMIN

**请求参数**：
```json
{
  "reason": "信息不完整，请补充详细描述"
}
```

### 7.4 数据统计

**接口**：`GET /api/admin/stats/overview`
**权限**：ADMIN

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "today": {
      "pv": 15620,
      "uv": 8340,
      "newUsers": 124,
      "aiConversations": 562
    },
    "total": {
      "tools": 2456,
      "users": 45623,
      "reviews": 23451
    }
  }
}
```

---

## 八、错误码说明

| 错误码 | 说明 | 解决方案 |
|-------|------|---------|
| 1001 | 用户名已存在 | 更换用户名 |
| 1002 | 邮箱已被注册 | 使用其他邮箱或直接登录 |
| 1003 | 验证码错误 | 重新获取验证码 |
| 1004 | 密码错误 | 检查密码 |
| 1005 | 账号被禁用 | 联系管理员 |
| 2001 | 工具不存在 | 检查工具ID |
| 2002 | 工具已下架 | 查看替代工具 |
| 3001 | 已评论过该工具 | 一个工具只能评论一次 |
| 3002 | 评论不存在 | 检查评论ID |
| 4001 | AI服务暂不可用 | 稍后重试或使用搜索功能 |
| 4002 | 对话会话不存在 | 开始新对话 |

---

## 九、请求示例（curl）

```bash
# 1. 用户注册
curl -X POST https://api.toolrecommend.com/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "code": "123456"
  }'

# 2. 用户登录
curl -X POST https://api.toolrecommend.com/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "john_doe",
    "password": "password123"
  }'

# 3. 获取工具列表（带Token）
curl -X GET "https://api.toolrecommend.com/api/tools?page=1&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."

# 4. AI对话
curl -X POST https://api.toolrecommend.com/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token" \
  -d '{
    "message": "我想找一个免费的笔记工具",
    "sessionId": null
  }'

# 5. 收藏工具
curl -X POST https://api.toolrecommend.com/api/users/favorites/1 \
  -H "Authorization: Bearer your_token"
```

---

## 十、SDK示例

### JavaScript/TypeScript
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://api.toolrecommend.com',
  timeout: 10000
});

// 添加请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 获取工具列表
export const getTools = (params) => {
  return api.get('/api/tools', { params });
};

// AI对话
export const chat = (message, sessionId) => {
  return api.post('/api/ai/chat', { message, sessionId });
};

// 收藏工具
export const favoriteTool = (toolId) => {
  return api.post(`/api/users/favorites/${toolId}`);
};
```

---

**文档版本**：v1.0
**最后更新**：2025-12-04
**维护人员**：[待填写]
