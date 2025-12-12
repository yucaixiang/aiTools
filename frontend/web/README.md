# AI工具推荐系统 - Web前端

现代化的PC端Web应用，采用Vue 3 + Vite构建。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5.0
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP客户端**: Axios 1.6
- **样式**: Sass

## 功能特性

- ✅ 现代化的PC Web设计风格
- ✅ 响应式布局，移动端兼容
- ✅ 工具浏览和搜索
- ✅ AI智能推荐对话
- ✅ 用户个人中心
- ✅ 工具详情和评论

## 页面结构

- **首页** (`/`) - 工具浏览、分类筛选、搜索功能（合并了原首页和发现页）
- **工具详情** (`/tool/:id`) - 工具详细信息、评分、评论
- **AI助手** (`/chat`) - AI对话和智能推荐
- **个人中心** (`/profile`) - 我的收藏、我的评论、提交的工具
- **登录** (`/login`) - 用户登录和注册

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 设计特点

### 1. 整洁美观的界面
- 渐变色Hero区域
- 卡片式工具展示
- 现代化的交互动画
- 清晰的视觉层次

### 2. PC优先设计
- 大屏幕优化布局
- 丰富的鼠标悬停效果
- 适合桌面浏览器使用

### 3. 移动端兼容
- 响应式栅格布局
- 触摸优化的交互
- 移动端菜单适配

### 4. 用户体验优化
- 流畅的页面切换
- 智能加载状态
- 友好的错误提示

## 项目结构

```
web/
├── public/                # 静态资源
├── src/
│   ├── api/              # API接口
│   │   ├── request.js    # Axios封装
│   │   ├── tool.js       # 工具API
│   │   ├── user.js       # 用户API
│   │   ├── review.js     # 评论API
│   │   └── ai.js         # AI API
│   ├── assets/           # 资源文件
│   │   └── styles/       # 样式文件
│   ├── components/       # 组件（预留）
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia状态管理
│   │   └── user.js       # 用户状态
│   ├── views/            # 页面组件
│   │   ├── Layout.vue    # 主布局
│   │   ├── Home.vue      # 首页
│   │   ├── ToolDetail.vue # 工具详情
│   │   ├── Chat.vue      # AI聊天
│   │   ├── Profile.vue   # 个人中心
│   │   └── Login.vue     # 登录
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 项目配置
├── vite.config.js        # Vite配置
└── README.md             # 说明文档
```

## API配置

开发环境API代理配置在 `vite.config.js`：

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 样式说明

- 使用Sass预处理器
- 采用BEM命名规范
- 响应式断点: 768px

## 浏览器兼容性

- Chrome/Edge (最新版本)
- Firefox (最新版本)
- Safari (最新版本)

## 后续优化

- [ ] 添加骨架屏加载
- [ ] 实现图片懒加载
- [ ] 添加PWA支持
- [ ] 优化SEO
- [ ] 添加国际化
