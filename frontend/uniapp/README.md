# AI工具推荐系统 - UniApp前端

## 📱 项目简介

基于Vue 3 + TypeScript + UniApp的多端应用，支持H5、微信小程序、APP等多平台。

## 🚀 快速开始

### 1. 安装依赖

```bash
cd frontend/uniapp
npm install
```

### 2. 运行项目

```bash
# H5开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin

# 构建H5
npm run build:h5
```

### 3. 访问地址

- H5: http://localhost:3000
- 微信小程序: 使用微信开发者工具打开 dist/dev/mp-weixin 目录

## 📁 项目结构

```
frontend/uniapp/
├── api/                    # API接口封装
│   ├── tool.ts            # 工具相关API
│   ├── user.ts            # 用户相关API
│   └── ai.ts              # AI相关API
├── components/            # 公共组件
│   ├── ToolCard.vue       # 工具卡片
│   ├── RatingStars.vue    # 评分星星
│   └── ...
├── pages/                 # 页面
│   ├── index/            # 首页
│   ├── explore/          # 发现页
│   ├── chat/             # AI聊天页
│   ├── profile/          # 个人中心
│   ├── tool/             # 工具详情
│   └── auth/             # 登录注册
├── utils/                # 工具类
│   └── request.ts        # 网络请求封装
├── manifest.json         # 应用配置
├── pages.json            # 页面配置
├── vite.config.ts        # Vite配置
└── package.json          # 依赖配置
```

## 🎨 页面说明

### 已创建页面

1. **首页 (pages/index/index.vue)** ✅
   - 搜索栏
   - Banner
   - 热门分类
   - 热门工具列表
   - 最新工具列表

### 待开发页面

2. **发现页 (pages/explore/index.vue)** ⏳
   - 工具列表
   - 筛选功能
   - 排序功能
   - 分页加载

3. **AI聊天页 (pages/chat/index.vue)** ⏳
   - 聊天界面
   - 消息展示
   - 工具推荐卡片
   - 对话历史

4. **个人中心 (pages/profile/index.vue)** ⏳
   - 用户信息
   - 我的收藏
   - 我的评论
   - 设置

5. **工具详情 (pages/tool/detail.vue)** ⏳
   - 工具信息展示
   - 评分评论
   - 相似工具推荐
   - 点赞收藏功能

6. **登录注册 (pages/auth/*.vue)** ⏳
   - 登录表单
   - 注册表单
   - Token管理

## 🔧 API配置

API请求通过网关统一访问：`http://localhost:8090`

主要接口：
- `/api/tools/**` - 工具相关
- `/api/users/**` - 用户相关
- `/api/ai/**` - AI对话和推荐
- `/api/reviews/**` - 评论相关

## 📦 核心功能

### 已实现
- ✅ API接口封装
- ✅ 网络请求工具
- ✅ Token管理
- ✅ 首页布局
- ✅ 工具列表展示

### 待实现
- ⏳ 用户认证流程
- ⏳ AI聊天功能
- ⏳ 工具搜索和筛选
- ⏳ 评论功能
- ⏳ 收藏和点赞
- ⏳ 个人中心

## 🎯 下一步开发计划

### 第一阶段（1-2天）
1. 完成工具列表页
2. 完成工具详情页
3. 实现搜索和筛选

### 第二阶段（2-3天）
1. 完成登录注册页
2. 实现用户认证
3. 完成个人中心

### 第三阶段（2-3天）
1. 完成AI聊天页
2. 实现消息展示
3. 集成工具推荐

## 📝 开发注意事项

1. **TypeScript**：使用TypeScript开发，注意类型定义
2. **响应式单位**：使用rpx作为单位
3. **跨端兼容**：注意不同平台的API差异
4. **状态管理**：复杂状态使用Pinia管理
5. **性能优化**：图片懒加载、列表虚拟滚动

## 🔗 相关文档

- [UniApp官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3官方文档](https://cn.vuejs.org/)
- [TypeScript文档](https://www.typescriptlang.org/docs/)
- [Pinia文档](https://pinia.vuejs.org/zh/)

---

**当前进度：20%（基础架构完成）**
