# AI工具推荐系统 - 后台管理前端

基于 Vue 3 + Element Plus + Vite 的后台管理系统。

## 技术栈

- **框架**: Vue 3.3.4 (Composition API)
- **UI组件库**: Element Plus 2.4.0
- **状态管理**: Pinia 2.1.6
- **路由**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.5.0
- **图表**: ECharts 5.4.3
- **构建工具**: Vite 4.4.9

## 功能模块

### 1. 登录认证
- 用户名/密码登录
- JWT Token认证
- 自动登录状态检查

### 2. 数据统计
- 工具、用户、评论总数统计
- 待审核工具数量
- 最近提交的工具列表
- 最新评论列表
- 分类统计图表

### 3. 工具管理
- 工具列表查询(支持按名称、分类、状态筛选)
- 工具审核(通过/拒绝)
- 工具删除
- 待审核工具专门管理页面

### 4. 用户管理
- 用户列表查询
- 用户状态管理(启用/禁用)
- 用户详情查看

### 5. 评论管理
- 评论列表查询(支持按工具名称、用户名、评分筛选)
- 评论详情查看
- 评论删除

### 6. 分类管理
- 分类列表
- 新增/编辑/删除分类
- 分类排序

### 7. 统计报表
- 工具提交趋势图
- 用户注册趋势图
- 分类分布饼图
- 工具状态分布图
- 热门工具排行
- 活跃用户排行

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

服务将在 `http://localhost:3001` 启动

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 项目结构

```
admin/
├── public/              # 静态资源
├── src/
│   ├── api/            # API接口定义
│   │   └── admin.js    # 管理后台API
│   ├── assets/         # 资源文件
│   ├── router/         # 路由配置
│   │   └── index.js    # 路由定义和守卫
│   ├── stores/         # Pinia状态管理
│   │   └── user.js     # 用户状态
│   ├── styles/         # 全局样式
│   │   └── index.scss  # 全局样式定义
│   ├── utils/          # 工具函数
│   │   └── request.js  # Axios封装
│   ├── views/          # 页面组件
│   │   ├── Login.vue          # 登录页
│   │   ├── MainLayout.vue     # 主布局
│   │   ├── Dashboard.vue      # 仪表盘
│   │   ├── ToolsList.vue      # 工具列表
│   │   ├── ToolsPending.vue   # 待审核工具
│   │   ├── UsersList.vue      # 用户列表
│   │   ├── ReviewsList.vue    # 评论列表
│   │   ├── CategoriesList.vue # 分类列表
│   │   └── Statistics.vue     # 统计报表
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html          # HTML入口
├── vite.config.js      # Vite配置
└── package.json        # 项目依赖
```

## 配置说明

### API代理配置

开发环境下，所有 `/api` 开头的请求会被代理到 `http://localhost:8090`

配置位置: `vite.config.js`

```javascript
server: {
  port: 3001,
  proxy: {
    '/api': {
      target: 'http://localhost:8090',
      changeOrigin: true
    }
  }
}
```

### 路由守卫

系统实现了基于Token的路由守卫:
- 未登录用户访问非登录页会被重定向到登录页
- 已登录用户访问登录页会被重定向到首页

配置位置: `src/router/index.js`

### 请求拦截器

- **请求拦截**: 自动添加 `Authorization` 和 `Admin-Id` 请求头
- **响应拦截**: 统一处理错误响应和401未授权

配置位置: `src/utils/request.js`

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## API接口列表

### 统计数据
- `GET /admin/tools/statistics` - 获取统计数据

### 工具管理
- `POST /admin/tools/query` - 查询工具列表
- `PUT /admin/tools/:id/approve` - 通过工具审核
- `PUT /admin/tools/:id/reject` - 拒绝工具审核
- `DELETE /admin/tools/:id` - 删除工具

### 用户管理
- `GET /users` - 获取用户列表

### 评论管理
- `GET /reviews` - 获取评论列表
- `DELETE /reviews/:id` - 删除评论

### 分类管理
- `GET /categories` - 获取分类列表
- `POST /categories` - 创建分类
- `PUT /categories/:id` - 更新分类
- `DELETE /categories/:id` - 删除分类

## 注意事项

1. 确保后端服务已启动在 `http://localhost:8090`
2. 确保数据库已初始化并包含必要的数据
3. Token存储在 `localStorage` 中，键名为 `admin_token`
4. 用户信息存储在 `localStorage` 中，键名为 `admin_user`
5. 管理员ID存储在 `localStorage` 中，键名为 `admin_id`

## 开发建议

1. 使用Vue DevTools进行调试
2. 查看浏览器控制台的网络请求
3. 检查localStorage中的Token是否正确设置
4. 如遇到CORS问题，检查Vite代理配置
