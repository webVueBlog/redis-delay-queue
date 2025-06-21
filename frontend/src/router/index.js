import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import UserManagement from '../views/UserManagement.vue'
import MenuManagement from '../views/MenuManagement.vue'
import OrganizationManagement from '../views/OrganizationManagement.vue'
import DelayQueueManagement from '../views/DelayQueueManagement.vue'
import SystemMonitor from '../views/SystemMonitor.vue'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: UserManagement,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/menus',
    name: 'MenuManagement',
    component: MenuManagement,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/organizations',
    name: 'OrganizationManagement',
    component: OrganizationManagement,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/delay-queue',
    name: 'DelayQueueManagement',
    component: DelayQueueManagement,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/monitor',
    name: 'SystemMonitor',
    component: SystemMonitor,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
    // 需要管理员权限但不是管理员
    next('/')
  } else {
    next()
  }
})

export default router