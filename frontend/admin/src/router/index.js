import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'DataLine' }
      },
      {
        path: 'tools',
        name: 'Tools',
        component: () => import('@/views/tools/List.vue'),
        meta: { title: '工具管理', icon: 'Tools' }
      },
      {
        path: 'tools/pending',
        name: 'ToolsPending',
        component: () => import('@/views/tools/Pending.vue'),
        meta: { title: '待审核工具', icon: 'Clock' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/List.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'reviews',
        name: 'Reviews',
        component: () => import('@/views/reviews/List.vue'),
        meta: { title: '评论管理', icon: 'ChatDotSquare' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/categories/List.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('admin_token')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
