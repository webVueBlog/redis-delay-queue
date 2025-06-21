<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="welcome-text">
          <h1>欢迎回来，{{ currentUser.username || '用户' }}！</h1>
          <p class="welcome-subtitle">{{ getWelcomeMessage() }}</p>
          <div class="user-stats">
            <el-tag type="success" size="large">{{ currentUser.role === 'ADMIN' ? '系统管理员' : '普通用户' }}</el-tag>
            <span class="last-login">上次登录：{{ formatLastLogin() }}</span>
          </div>
        </div>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="$router.push('/delay-queue')">
            <el-icon><Clock /></el-icon>
            创建延迟任务
          </el-button>
          <el-button size="large" @click="$router.push('/my-tasks')">
            <el-icon><DataBoard /></el-icon>
            查看我的任务
          </el-button>
        </div>
      </div>
    </div>

    <!-- 快速统计 -->
    <el-row :gutter="20" class="quick-stats">
      <el-col :span="6">
        <el-card class="stat-overview-card">
          <div class="stat-item">
            <div class="stat-number">{{ systemStats.totalTasks || 0 }}</div>
            <div class="stat-label">总任务数</div>
            <el-icon class="stat-icon-bg"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-overview-card">
          <div class="stat-item">
            <div class="stat-number">{{ systemStats.pendingTasks || 0 }}</div>
            <div class="stat-label">待执行任务</div>
            <el-icon class="stat-icon-bg"><Timer /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-overview-card">
          <div class="stat-item">
            <div class="stat-number">{{ systemStats.completedTasks || 0 }}</div>
            <div class="stat-label">已完成任务</div>
            <el-icon class="stat-icon-bg"><CircleCheck /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-overview-card">
          <div class="stat-item">
            <div class="stat-number">{{ systemStats.onlineUsers || 0 }}</div>
            <div class="stat-label">在线用户</div>
            <el-icon class="stat-icon-bg"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-card class="quick-actions-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">快速操作</span>
          <el-button text @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="4" v-for="action in quickActions" :key="action.key">
          <div class="quick-action-item" @click="handleQuickAction(action)">
            <el-icon class="action-icon" :class="action.iconClass">
              <component :is="action.icon" />
            </el-icon>
            <div class="action-text">
              <div class="action-title">{{ action.title }}</div>
              <div class="action-desc">{{ action.description }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 数据库表结构展示 -->
    <el-card class="table-structure-card">
      <template #header>
        <div class="card-header">
          <span>数据库表结构</span>
          <el-button type="primary" @click="fetchData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="error" class="error-container">
        <el-alert
          title="加载失败"
          :description="error"
          type="error"
          show-icon
          :closable="false"
        />
      </div>
      
      <div v-else>
        <el-collapse v-model="activeNames" class="table-collapse">
          <el-collapse-item
            v-for="table in groupedTables"
            :key="table.tableName"
            :title="`${table.tableName} (${table.columns.length} 个字段)`"
            :name="table.tableName"
          >
            <template #title>
              <div class="table-header">
                <el-icon class="table-icon"><Grid /></el-icon>
                <span class="table-name">{{ table.tableName }}</span>
                <el-tag size="small" class="column-count">
                  {{ table.columns.length }} 个字段
                </el-tag>
                <span v-if="table.tableComment" class="table-comment">
                  - {{ table.tableComment }}
                </span>
              </div>
            </template>
            
            <el-table
              :data="table.columns"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column
                prop="columnName"
                label="字段名"
                width="200"
                sortable
              >
                <template #default="{ row }">
                  <el-tag v-if="row.isPrimaryKey === 'YES'" type="danger" size="small" class="key-tag">
                    PK
                  </el-tag>
                  <el-tag v-else-if="row.isUnique === 'YES'" type="warning" size="small" class="key-tag">
                    UK
                  </el-tag>
                  <span class="column-name">{{ row.columnName }}</span>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnType"
                label="数据类型"
                width="150"
                sortable
              >
                <template #default="{ row }">
                  <el-tag type="info" size="small">
                    {{ row.columnType }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="isNullable"
                label="允许空值"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-tag :type="row.isNullable === 'YES' ? 'success' : 'danger'" size="small">
                    {{ row.isNullable === 'YES' ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnDefault"
                label="默认值"
                width="120"
              >
                <template #default="{ row }">
                  <span v-if="row.columnDefault !== null" class="default-value">
                    {{ row.columnDefault }}
                  </span>
                  <el-tag v-else type="info" size="small">NULL</el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnComment"
                label="字段注释"
                min-width="200"
                show-overflow-tooltip
              >
                <template #default="{ row }">
                  <span v-if="row.columnComment" class="comment">
                    {{ row.columnComment }}
                  </span>
                  <span v-else class="no-comment">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
        
        <div v-if="groupedTables.length === 0" class="empty-state">
          <el-empty description="暂无数据" />
        </div>
      </div>
    </el-card>
    
    <!-- 功能模块 -->
    <el-row :gutter="20" class="function-modules">
      <!-- 业务功能 -->
      <el-col :span="12">
        <el-card class="module-card business-module">
          <template #header>
            <div class="module-header">
              <el-icon class="module-icon"><Operation /></el-icon>
              <span class="module-title">业务功能</span>
            </div>
          </template>
          <div class="module-content">
            <div class="function-grid">
              <div class="function-item primary" @click="$router.push('/delay-queue')">
                <el-icon><Clock /></el-icon>
                <span>延迟队列</span>
              </div>
              <div class="function-item" @click="$router.push('/my-tasks')">
                <el-icon><DataBoard /></el-icon>
                <span>我的任务</span>
              </div>
              <div class="function-item" @click="$router.push('/user-permission-organizations')">
                <el-icon><OfficeBuilding /></el-icon>
                <span>权限组织</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统管理 -->
      <el-col :span="12" v-if="isAdmin">
        <el-card class="module-card admin-module">
          <template #header>
            <div class="module-header">
              <el-icon class="module-icon"><Setting /></el-icon>
              <span class="module-title">系统管理</span>
            </div>
          </template>
          <div class="module-content">
            <div class="function-grid">
              <div class="function-item" @click="$router.push('/users')">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </div>
              <div class="function-item" @click="$router.push('/menus')">
                <el-icon><Menu /></el-icon>
                <span>菜单管理</span>
              </div>
              <div class="function-item" @click="$router.push('/organizations')">
                <el-icon><OfficeBuilding /></el-icon>
                <span>组织管理</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="function-modules">
      <!-- 监控统计 -->
      <el-col :span="12">
        <el-card class="module-card monitor-module">
          <template #header>
            <div class="module-header">
              <el-icon class="module-icon"><Monitor /></el-icon>
              <span class="module-title">监控统计</span>
            </div>
          </template>
          <div class="module-content">
            <div class="function-grid">
              <div class="function-item" @click="$router.push('/monitor')">
                <el-icon><Monitor /></el-icon>
                <span>系统监控</span>
              </div>
              <div class="function-item" @click="$router.push('/data-statistics')">
                <el-icon><DataBoard /></el-icon>
                <span>数据统计</span>
              </div>
              <div class="function-item" @click="$router.push('/operation-logs')">
                <el-icon><Document /></el-icon>
                <span>操作日志</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统设置 -->
      <el-col :span="12">
        <el-card class="module-card settings-module">
          <template #header>
            <div class="module-header">
              <el-icon class="module-icon"><Tools /></el-icon>
              <span class="module-title">系统配置</span>
            </div>
          </template>
          <div class="module-content">
            <div class="function-grid">
              <div class="function-item" @click="$router.push('/system-settings')">
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </div>
              <div class="function-item" @click="openHealthDialog">
                <el-icon><Monitor /></el-icon>
                <span>健康检查</span>
              </div>
              <div class="function-item" @click="refreshData">
                <el-icon><Refresh /></el-icon>
                <span>刷新数据</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="feature-card">
      <h2>系统功能</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="feature-item">
            <h3>用户管理</h3>
            <ul>
              <li>用户注册和登录</li>
              <li>角色权限管理</li>
              <li>用户信息维护</li>
            </ul>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="feature-item">
            <h3>延迟队列</h3>
            <ul>
              <li>基于Redis实现</li>
              <li>支持延迟任务调度</li>
              <li>高性能消息处理</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, Clock, Monitor, Menu, OfficeBuilding, DataBoard, Setting, Document, 
  Refresh, Grid, Timer, CircleCheck, Operation, Tools 
} from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const userRole = ref(localStorage.getItem('userRole') || '')

// 当前用户信息
const currentUser = ref({
  username: localStorage.getItem('username') || '用户',
  role: localStorage.getItem('userRole') || 'USER',
  lastLoginTime: localStorage.getItem('lastLoginTime') || new Date().toISOString()
})

// 系统统计数据
const systemStats = ref({
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  onlineUsers: 0
})

// 快速操作配置
const quickActions = ref([
  {
    key: 'create-task',
    title: '创建任务',
    description: '新建延迟任务',
    icon: Clock,
    iconClass: 'primary-icon',
    route: '/delay-queue'
  },
  {
    key: 'my-tasks',
    title: '我的任务',
    description: '查看任务列表',
    icon: DataBoard,
    iconClass: 'success-icon',
    route: '/my-tasks'
  },
  {
    key: 'monitor',
    title: '系统监控',
    description: '实时监控',
    icon: Monitor,
    iconClass: 'warning-icon',
    route: '/monitor'
  },
  {
    key: 'users',
    title: '用户管理',
    description: '管理用户',
    icon: User,
    iconClass: 'info-icon',
    route: '/users'
  },
  {
    key: 'settings',
    title: '系统设置',
    description: '配置参数',
    icon: Setting,
    iconClass: 'default-icon',
    route: '/system-settings'
  },
  {
    key: 'logs',
    title: '操作日志',
    description: '查看日志',
    icon: Document,
    iconClass: 'danger-icon',
    route: '/operation-logs'
  }
])

// 获取欢迎消息
const getWelcomeMessage = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息哦！'
  if (hour < 9) return '早上好！新的一天开始了！'
  if (hour < 12) return '上午好！今天也要加油哦！'
  if (hour < 14) return '中午好！记得休息一下！'
  if (hour < 18) return '下午好！工作进展如何？'
  if (hour < 22) return '晚上好！今天辛苦了！'
  return '夜深了，早点休息吧！'
}

// 格式化最后登录时间
const formatLastLogin = () => {
  const lastLogin = currentUser.value.lastLoginTime
  if (!lastLogin) return '首次登录'
  
  const date = new Date(lastLogin)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 处理快速操作点击
const handleQuickAction = (action) => {
  if (action.route) {
    router.push(action.route)
  }
}

// 打开健康检查对话框
const openHealthDialog = () => {
  // 这里可以调用App.vue中的健康检查方法
  ElMessage.info('健康检查功能开发中...')
}

// 刷新数据
const refreshData = async () => {
  try {
    await Promise.all([
      fetchSystemStats(),
      fetchData()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('数据刷新失败')
  }
}

// 获取系统统计数据
const fetchSystemStats = async () => {
  try {
    // 并行获取多个统计数据
    const [queueStatsRes, overviewRes, logStatsRes] = await Promise.allSettled([
      request.get('/api/monitor/queue-stats'),
      request.get('/api/statistics/overview'),
      request.get('/api/operation-logs/stats')
    ])
    
    // 处理队列统计数据
    let queueStats = {}
    if (queueStatsRes.status === 'fulfilled' && queueStatsRes.value?.data?.success) {
      queueStats = queueStatsRes.value.data.data || {}
    }
    
    // 处理概览统计数据
    let overviewStats = {}
    if (overviewRes.status === 'fulfilled' && overviewRes.value?.data?.success) {
      overviewStats = overviewRes.value.data.data || {}
    }
    
    // 处理日志统计数据
    let logStats = {}
    if (logStatsRes.status === 'fulfilled' && logStatsRes.value?.data?.success) {
      logStats = logStatsRes.value.data.data || {}
    }
    
    // 合并统计数据
    systemStats.value = {
      totalTasks: queueStats.totalTasks || overviewStats.totalTasks || 0,
      pendingTasks: queueStats.pendingTasks || 0,
      completedTasks: queueStats.completedTasks || 0,
      onlineUsers: logStats.activeUsers || overviewStats.totalUsers || 0
    }
    
    console.log('系统统计数据获取成功:', systemStats.value)
  } catch (error) {
    console.error('获取系统统计失败:', error)
    // 发生错误时使用默认值
    systemStats.value = {
      totalTasks: 0,
      pendingTasks: 0,
      completedTasks: 0,
      onlineUsers: 0
    }
    ElMessage.warning('获取统计数据失败，显示默认数据')
  }
}

// 表格数据相关状态
const loading = ref(false)
const error = ref('')
const tableData = ref([])
const activeNames = ref([])

// 计算属性
const isAdmin = computed(() => userRole.value === 'ADMIN')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 处理已分组的表数据
const groupedTables = computed(() => {
  // 如果数据已经是按表分组的格式，直接使用
  if (Array.isArray(tableData.value) && tableData.value.length > 0 && tableData.value[0].columns) {
    return tableData.value.sort((a, b) => a.tableName.localeCompare(b.tableName))
  }
  
  // 兼容旧格式：如果是扁平的列数据，则重新分组
  const groups = {}
  
  tableData.value.forEach(column => {
    const tableName = column.tableName
    if (!groups[tableName]) {
      groups[tableName] = {
        tableName,
        tableComment: column.tableComment,
        columns: []
      }
    }
    groups[tableName].columns.push(column)
  })
  
  // 转换为数组并排序
  return Object.values(groups).sort((a, b) => a.tableName.localeCompare(b.tableName))
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await request.get('/api/tables/columns')
    tableData.value = response.data
    
    // 默认展开第一个表
    if (groupedTables.value.length > 0) {
      activeNames.value = [groupedTables.value[0].tableName]
    }
    ElMessage.success('数据加载成功')
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '获取数据失败'
    console.error('获取数据失败:', err)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  // 监听存储变化，实时更新用户角色
  window.addEventListener('storage', (e) => {
    if (e.key === 'userRole') {
      userRole.value = e.newValue || ''
    }
  })
  
  // 监听来自App.vue的刷新事件
  const handleRefreshData = () => {
    if (isLoggedIn.value) {
      fetchData()
    }
  }
  window.addEventListener('refreshData', handleRefreshData)
  
  // 如果用户已登录，自动加载数据
  if (isLoggedIn.value) {
    fetchData()
    fetchSystemStats()
  }
  
  // 组件销毁时移除事件监听器
  onUnmounted(() => {
    window.removeEventListener('refreshData', handleRefreshData)
  })
})
</script>

<style scoped>

body {
  margin: 0;
}

.home {
}

/* 欢迎横幅样式 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.welcome-text h1 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 16px 0;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.last-login {
  font-size: 14px;
  opacity: 0.8;
}

.banner-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.banner-actions .el-button {
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.banner-actions .el-button--primary {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.banner-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 快速统计样式 */
.quick-stats {
  margin-bottom: 24px;
}

.stat-overview-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-item {
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 8px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.stat-icon-bg {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 48px;
  color: #ecf0f1;
  z-index: 1;
}

/* 快速操作样式 */
.quick-actions-card {
  margin-bottom: 24px;
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.quick-action-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.quick-action-item:hover {
  background: white;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.action-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #409eff;
}

.action-text {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 14px;
  color: #7f8c8d;
}

.primary-icon { color: #409eff; }
.success-icon { color: #67c23a; }
.warning-icon { color: #e6a23c; }
.danger-icon { color: #f56c6c; }
.info-icon { color: #909399; }
.default-icon { color: #606266; }

.table-structure-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.error-container {
  padding: 20px;
}

.table-collapse {
  margin-top: 10px;
}

.table-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.table-icon {
  color: #409eff;
}

.table-name {
  font-weight: bold;
  color: #333;
}

.column-count {
  background-color: #f0f9ff;
  color: #409eff;
}

.table-comment {
  color: #666;
  font-style: italic;
}

.key-tag {
  margin-right: 8px;
}

.column-name {
  font-weight: 500;
}

.default-value {
  font-family: 'Courier New', monospace;
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
}

.comment {
  color: #666;
}

.no-comment {
  color: #ccc;
  font-style: italic;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

/* 功能模块样式 */
.function-modules {
  margin-bottom: 24px;
}

.module-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: white;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-icon {
  font-size: 24px;
}

.module-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.module-content {
  padding: 0;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1px;
  background: #f0f2f5;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 100px;
  position: relative;
  overflow: hidden;
}

.function-item:hover {
  background: #f8f9ff;
  transform: translateY(-2px);
}

.function-item.primary {
  background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
  color: white;
}

.function-item.primary:hover {
  background: linear-gradient(135deg, #337ecc 0%, #4a90c2 100%);
}

.function-item .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
  color: #409eff;
}

.function-item.primary .el-icon {
  color: white;
}

.function-item span {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  text-align: center;
}

.function-item.primary span {
  color: white;
}

/* 不同模块的主题色 */
.business-module .module-icon {
  color: #409eff;
}

.admin-module .module-icon {
  color: #e6a23c;
}

.monitor-module .module-icon {
  color: #67c23a;
}

.settings-module .module-icon {
  color: #909399;
}

.business-module .el-card__header {
  background: linear-gradient(135deg, #409eff10 0%, #409eff20 100%);
}

.admin-module .el-card__header {
  background: linear-gradient(135deg, #e6a23c10 0%, #e6a23c20 100%);
}

.monitor-module .el-card__header {
  background: linear-gradient(135deg, #67c23a10 0%, #67c23a20 100%);
}

.settings-module .el-card__header {
  background: linear-gradient(135deg, #90939910 0%, #90939920 100%);
}

.feature-card h2 {
  color: #333;
  margin-bottom: 20px;
}

.feature-item h3 {
  color: #409eff;
  margin-bottom: 10px;
}

.feature-item ul {
  list-style-type: none;
  padding: 0;
}

.feature-item li {
  padding: 5px 0;
  color: #666;
}

.feature-item li:before {
  content: "✓";
  color: #67c23a;
  margin-right: 8px;
}
</style>