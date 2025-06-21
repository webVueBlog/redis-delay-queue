import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import UserManagement from '../views/UserManagement.vue'
import MenuManagement from '../views/MenuManagement.vue'
import OrganizationManagement from '../views/OrganizationManagement.vue'
import DelayQueueManagement from '../views/DelayQueueManagement.vue'
import SystemMonitor from '../views/SystemMonitor.vue'
import DataStatistics from '../views/DataStatistics.vue'
import SystemSettings from '../views/SystemSettings.vue'
import OperationLogs from '../views/OperationLogs.vue'
import MyTasks from '../views/MyTasks.vue'
import UserPermissionOrganizations from '../views/UserPermissionOrganizations.vue'
import DatabaseConnectionPool from '../views/DatabaseConnectionPool.vue'
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
      requiresAuth: true
    }
  },
  {
    path: '/menus',
    name: 'MenuManagement',
    component: MenuManagement,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/organizations',
    name: 'OrganizationManagement',
    component: OrganizationManagement,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/delay-queue',
    name: 'DelayQueueManagement',
    component: DelayQueueManagement,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/monitor',
    name: 'SystemMonitor',
    component: SystemMonitor,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/database-pool',
    name: 'DatabaseConnectionPool',
    component: DatabaseConnectionPool,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/data-statistics',
    name: 'DataStatistics',
    component: DataStatistics,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/system-settings',
    name: 'SystemSettings',
    component: SystemSettings,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/operation-logs',
    name: 'OperationLogs',
    component: OperationLogs,
    meta: {
      requiresAuth: true
    }
  },
  {
      path: '/my-tasks',
      name: 'MyTasks',
      component: MyTasks,
      meta: { requiresAuth: true }
    },
    {
      path: '/user-permission-organizations',
      name: 'UserPermissionOrganizations',
      component: UserPermissionOrganizations,
      meta: { requiresAuth: true }
    }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else {
    next()
  }
})

export default router