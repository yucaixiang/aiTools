import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/tool/:id',
        name: 'ToolDetail',
        component: () => import('@/views/ToolDetail.vue'),
        meta: { title: '工具详情' }
      },
      {
        path: '/chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录' }
      },
      {
        path: '/submit',
        name: 'SubmitTool',
        component: () => import('@/views/SubmitTool.vue'),
        meta: { title: '提交工具' }
      },
      {
        path: '/admin/submissions',
        name: 'AdminSubmissions',
        component: () => import('@/views/AdminSubmissions.vue'),
        meta: { title: '工具审核' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AI工具推荐` : 'AI工具推荐'
  next()
})

export default router
