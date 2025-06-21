<template>
  <div id="app">
    <!-- 登录页面 -->
    <div v-if="!isLoggedIn" class="login-container">
      <router-view />
    </div>
    
    <!-- 后台管理布局 -->
    <el-container v-else class="admin-layout">
      <!-- 顶部导航栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-button 
            class="sidebar-toggle" 
            @click="toggleSidebar" 
            :icon="isCollapse ? Expand : Fold"
            text
          />
          <h1 class="system-title">Redis延迟队列管理系统</h1>
        </div>
        
        <div class="header-right">
          <el-button type="primary" @click="openHealthDialog" size="small">
            <el-icon><Monitor /></el-icon>
            系统健康检查
          </el-button>
          
          <div class="user-info">
            <el-dropdown @command="handleUserCommand">
              <span class="user-name">
                <el-avatar :size="32" :src="userAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ username }}</span>
                <el-tag v-if="isAdmin" type="warning" size="small">管理员</el-tag>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="changePassword">
                    <el-icon><Lock /></el-icon>
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <el-button @click="fetchData" :loading="loading" size="small">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </el-header>
      
      <!-- 主体容器 -->
      <el-container class="main-container">
        <!-- 左侧菜单栏 -->
        <el-aside class="admin-sidebar" :width="sidebarWidth">
          <el-scrollbar>
            <el-menu
              :default-active="activeIndex"
              :collapse="isCollapse"
              :unique-opened="true"
              @select="handleMenuSelect"
              class="sidebar-menu"
            >
              <!-- 首页 -->
              <el-menu-item index="/">
                <el-icon><HomeFilled /></el-icon>
                <template #title>首页</template>
              </el-menu-item>
              
              <!-- 业务功能 -->
              <el-sub-menu index="business">
                <template #title>
                  <el-icon><Operation /></el-icon>
                  <span>业务功能</span>
                </template>
                <el-menu-item index="/delay-queue">
                  <el-icon><Clock /></el-icon>
                  <template #title>延迟队列</template>
                </el-menu-item>
                <el-menu-item index="/my-tasks">
                  <el-icon><DataBoard /></el-icon>
                  <template #title>我的任务</template>
                </el-menu-item>
                <el-menu-item index="/user-permission-organizations">
                  <el-icon><OfficeBuilding /></el-icon>
                  <template #title>权限组织</template>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 系统管理 -->
              <el-sub-menu index="system">
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>系统管理</span>
                </template>
                <el-menu-item index="/users">
                  <el-icon><User /></el-icon>
                  <template #title>用户管理</template>
                </el-menu-item>
                <el-menu-item index="/menus">
                  <el-icon><Menu /></el-icon>
                  <template #title>菜单管理</template>
                </el-menu-item>
                <el-menu-item index="/organizations">
                  <el-icon><OfficeBuilding /></el-icon>
                  <template #title>组织管理</template>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 监控统计 -->
              <el-sub-menu index="monitor">
                <template #title>
                  <el-icon><Monitor /></el-icon>
                  <span>监控统计</span>
                </template>
                <el-menu-item index="/monitor">
                  <el-icon><Monitor /></el-icon>
                  <template #title>系统监控</template>
                </el-menu-item>
                <el-menu-item index="/database-pool">
                  <el-icon><Connection /></el-icon>
                  <template #title>连接池监控</template>
                </el-menu-item>
                <el-menu-item index="/data-statistics">
                  <el-icon><DataBoard /></el-icon>
                  <template #title>数据统计</template>
                </el-menu-item>
                <el-menu-item index="/operation-logs">
                  <el-icon><Document /></el-icon>
                  <template #title>操作日志</template>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 系统设置 -->
              <el-menu-item index="/system-settings">
                <el-icon><Tools /></el-icon>
                <template #title>系统设置</template>
              </el-menu-item>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        
        <!-- 主内容区域 -->
        <el-main class="admin-main">
          <!-- 面包屑导航 -->
          <div class="breadcrumb-container">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path" :to="item.path">
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <!-- 页面内容 -->
          <div class="page-content">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 健康检查对话框 -->
    <el-dialog
      v-model="showHealthDialog"
      title="系统健康检查"
      width="800px"
      :before-close="handleHealthDialogClose"
    >
      <div v-if="healthLoading" class="health-loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else class="health-content">
        <!-- 总体状态 -->
        <el-card class="health-overview" shadow="never">
          <template #header>
            <div class="card-header">
              <span>总体状态</span>
              <el-tag 
                :type="overallHealth.status === 'UP' ? 'success' : 'danger'" 
                size="large"
              >
                {{ overallHealth.status === 'UP' ? '健康' : '异常' }}
              </el-tag>
            </div>
          </template>
          <div class="health-summary">
            <p><strong>应用名称:</strong> {{ overallHealth.application || 'Redis延迟队列系统' }}</p>
            <p><strong>检查时间:</strong> {{ overallHealth.timestamp || new Date().toLocaleString() }}</p>
          </div>
        </el-card>

        <!-- 详细检查项 -->
        <el-row :gutter="20" class="health-details">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><DataBoard /></el-icon>
                  <span>数据库连接</span>
                  <el-tag 
                    :type="healthDetails.database?.status === 'UP' ? 'success' : 'danger'" 
                    size="small"
                  >
                    {{ healthDetails.database?.status === 'UP' ? '正常' : '异常' }}
                  </el-tag>
                </div>
              </template>
              <div class="health-item-content">
                <p v-if="healthDetails.database?.message">
                  {{ healthDetails.database.message }}
                </p>
                <p v-if="healthDetails.database?.details">
                  <small>{{ healthDetails.database.details }}</small>
                </p>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Connection /></el-icon>
                  <span>Redis连接</span>
                  <el-tag 
                    :type="healthDetails.redis?.status === 'UP' ? 'success' : 'danger'" 
                    size="small"
                  >
                    {{ healthDetails.redis?.status === 'UP' ? '正常' : '异常' }}
                  </el-tag>
                </div>
              </template>
              <div class="health-item-content">
                <p v-if="healthDetails.redis?.message">
                  {{ healthDetails.redis.message }}
                </p>
                <p v-if="healthDetails.redis?.details">
                  <small>{{ healthDetails.redis.details }}</small>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 应用信息 -->
        <el-card class="app-info" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>应用信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应用版本">
              {{ appInfo.version || '1.0.0' }}
            </el-descriptions-item>
            <el-descriptions-item label="应用描述">
              {{ appInfo.description || 'Redis延迟队列应用' }}
            </el-descriptions-item>
            <el-descriptions-item label="可用处理器">
              {{ appInfo.jvm?.availableProcessors || 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="最大内存">
              {{ formatMemory(appInfo.jvm?.maxMemory) || 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="已用内存">
              {{ formatMemory(appInfo.jvm?.usedMemory) || 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="空闲内存">
              {{ formatMemory(appInfo.jvm?.freeMemory) || 'N/A' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fetchHealthData" :loading="healthLoading" type="primary">
            <el-icon><Refresh /></el-icon>
            刷新检查
          </el-button>
          <el-button @click="showHealthDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" v-model="showChangePasswordDialog" width="400px">
      <el-form :model="changePasswordForm" :rules="changePasswordRules" ref="changePasswordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="changePasswordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="changePasswordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="changePasswordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Refresh, DataBoard, User, ArrowDown, Clock, Grid, Connection, 
  InfoFilled, Setting, Document, OfficeBuilding, HomeFilled, Operation,
  Menu, Tools, Lock, SwitchButton, Fold, Expand
} from '@element-plus/icons-vue'
import request from './utils/request'

// 路由
const router = useRouter()
const route = useRoute()

// 用户状态
const username = ref(localStorage.getItem('username') || '')
const userRole = ref(localStorage.getItem('userRole') || '')
const userId = ref(localStorage.getItem('userId') || '')
const userAvatar = ref('')

// 侧边栏状态
const isCollapse = ref(false)
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '200px')

// 面包屑导航
const breadcrumbItems = ref([])
    
// 健康检查相关状态
const showHealthDialog = ref(false)
const healthLoading = ref(false)
const overallHealth = ref({})
const healthDetails = ref({})
const appInfo = ref({})

// 数据刷新相关状态
const loading = ref(false)
    
// 修改密码相关
const showChangePasswordDialog = ref(false)
const changePasswordFormRef = ref()
const changePasswordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
    
// 计算属性
const isLoggedIn = computed(() => !!username.value)
const isAdmin = computed(() => userRole.value === 'ADMIN')
const activeIndex = computed(() => route.path)
    
// 修改密码验证规则
const changePasswordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== changePasswordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
    

    
// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 触发当前路由组件的数据刷新
    // 通过事件总线通知子组件刷新数据
    window.dispatchEvent(new CustomEvent('refreshData'))
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('数据刷新失败:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 侧边栏切换
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 菜单选择处理
const handleMenuSelect = (index) => {
  router.push(index)
}

// 更新面包屑导航
const updateBreadcrumb = () => {
  const path = route.path
  const breadcrumbMap = {
    '/': [],
    '/delay-queue': [{ title: '业务功能', path: '' }, { title: '延迟队列', path: '/delay-queue' }],
    '/my-tasks': [{ title: '业务功能', path: '' }, { title: '我的任务', path: '/my-tasks' }],
    '/user-permission-organizations': [{ title: '业务功能', path: '' }, { title: '权限组织', path: '/user-permission-organizations' }],
    '/users': [{ title: '系统管理', path: '' }, { title: '用户管理', path: '/users' }],
    '/menus': [{ title: '系统管理', path: '' }, { title: '菜单管理', path: '/menus' }],
    '/organizations': [{ title: '系统管理', path: '' }, { title: '组织管理', path: '/organizations' }],
    '/monitor': [{ title: '监控统计', path: '' }, { title: '系统监控', path: '/monitor' }],
    '/database-pool': [{ title: '监控统计', path: '' }, { title: '连接池监控', path: '/database-pool' }],
    '/data-statistics': [{ title: '监控统计', path: '' }, { title: '数据统计', path: '/data-statistics' }],
    '/operation-logs': [{ title: '监控统计', path: '' }, { title: '操作日志', path: '/operation-logs' }],
    '/system-settings': [{ title: '系统设置', path: '/system-settings' }]
  }
  
  breadcrumbItems.value = breadcrumbMap[path] || []
}

// 监听路由变化更新面包屑
watch(() => route.path, () => {
  updateBreadcrumb()
}, { immediate: true })
    
// 用户命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人信息页面
      router.push('/profile')
      break
    case 'changePassword':
      showChangePasswordDialog.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}
    
// 退出登录
const handleLogout = async () => {
  try {
    await request.post('/api/auth/logout')
  } catch (error) {
    console.error('退出登录失败:', error)
  } finally {
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userId')
    
    // 重置状态
    username.value = ''
    userRole.value = ''
    userId.value = ''
    
    // 跳转到登录页
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}
    
// 修改密码
const handleChangePassword = async () => {
  try {
    await changePasswordFormRef.value.validate()
    
    const response = await request.post('/api/auth/change-password', {
      oldPassword: changePasswordForm.value.oldPassword,
      newPassword: changePasswordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    showChangePasswordDialog.value = false
    
    // 重置表单
    changePasswordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '密码修改失败')
    } else {
      console.error('密码修改失败:', error)
    }
  }
}
    
// 获取健康检查数据
const fetchHealthData = async () => {
  healthLoading.value = true
  
  try {
    // 获取总体健康状态
    const healthResponse = await request.get('/api/health/')
    overallHealth.value = healthResponse.data
    
    // 获取详细健康检查
    const detailedResponse = await request.get('/api/health/detailed')
    healthDetails.value = detailedResponse.data
    
    // 获取应用信息
    const infoResponse = await request.get('/api/health/info')
    appInfo.value = infoResponse.data
    
  } catch (err) {
    console.error('获取健康检查数据失败:', err)
    // 设置默认的错误状态
    overallHealth.value = { 
      status: 'DOWN', 
      message: '健康检查服务不可用',
      timestamp: new Date().toLocaleString()
    }
    healthDetails.value = {
      database: { 
        status: 'DOWN', 
        message: '无法连接到健康检查服务',
        details: err.response?.status ? `HTTP ${err.response.status}` : '网络连接失败'
      },
      redis: { 
        status: 'DOWN', 
        message: '无法连接到健康检查服务',
        details: err.response?.status ? `HTTP ${err.response.status}` : '网络连接失败'
      }
    }
    appInfo.value = {
      version: '1.0.0',
      description: 'Redis延迟队列应用（离线模式）'
    }
  } finally {
    healthLoading.value = false
  }
}
    
// 打开健康检查对话框
const openHealthDialog = () => {
  showHealthDialog.value = true
  fetchHealthData()
}
    
// 格式化内存大小
const formatMemory = (bytes) => {
  if (!bytes) return 'N/A'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}
    
// 关闭健康检查对话框
const handleHealthDialogClose = () => {
  showHealthDialog.value = false
}
    
// 组件挂载时获取数据

</script>

<style scoped>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  height: 100vh;
  overflow: hidden;
}

/* 登录页面样式 */
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 后台管理布局 */
.admin-layout {
  height: 100vh;
}

/* 顶部导航栏 */
.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.sidebar-toggle {
  color: #fff !important;
  font-size: 18px;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.system-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 主体容器 */
.main-container {
  height: calc(100vh - 60px);
}

/* 左侧菜单栏 */
.admin-sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  padding-left: 20px !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #409eff;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
}

/* 主内容区域 */
.admin-main {
  background-color: #f5f7fa;
  padding: 0;
  overflow-y: auto;
}

/* 面包屑导航 */
.breadcrumb-container {
  background: #fff;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 0;
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #409eff;
  font-weight: 500;
}

/* 页面内容 */
.page-content {
  padding: 20px;
  min-height: calc(100vh - 60px - 45px);
}

/* 顶部按钮样式优化 */
.header-right .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: none;
  padding: 8px 12px;
  font-size: 13px;
}

.header-right .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.header-right .el-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 主要按钮样式 */
.header-right .el-button--primary {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.header-right .el-button--primary:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.header-right .el-button--primary:focus {
  background: rgba(255, 255, 255, 0.2);
}

/* 默认按钮样式 */
.header-right .el-button--default {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-right .el-button--default:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.header-right .el-button--default:focus {
  background: rgba(255, 255, 255, 0.1);
}

/* 按钮图标样式 */
.header-right .el-button .el-icon {
  margin-right: 4px;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.header-right .el-button:hover .el-icon {
  transform: scale(1.05);
}

/* 加载状态按钮 */
.header-right .el-button.is-loading {
  pointer-events: none;
  opacity: 0.8;
}

/* 响应式按钮样式 */
@media (max-width: 768px) {
  .header-actions .el-button {
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .header-actions .el-button .el-icon {
    font-size: 14px;
    margin-right: 4px;
  }
}

@media (max-width: 480px) {
  .header-actions .el-button span {
    display: none;
  }
  
  .header-actions .el-button .el-icon {
    margin-right: 0;
  }
  
  .header-actions .el-button {
    padding: 8px;
    min-width: 36px;
  }
}

/* 用户信息样式 */
.user-info {
  color: #fff;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-weight: 500;
  color: #fff;
}

.user-name:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.user-name:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.username {
  font-size: 14px;
  margin: 0 4px;
}

.dropdown-icon {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.user-name:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 头像样式 */
.el-avatar {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

/* 管理员标签 */
.el-tag {
  margin-left: 4px;
}

/* 用户下拉菜单样式优化 */
.el-dropdown-menu {
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 0;
}

.el-dropdown-menu .el-dropdown-item {
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: 6px;
  margin: 2px 8px;
  color: #606266;
}

.el-dropdown-menu .el-dropdown-item:hover {
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
  color: #fff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.el-dropdown-menu .el-dropdown-item:focus {
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
  color: #fff;
}

.el-dropdown-menu .el-dropdown-item.is-divided {
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin-top: 8px;
  padding-top: 12px;
}

.el-dropdown-menu .el-dropdown-item.is-divided::before {
  content: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-title {
    font-size: 16px;
  }
  
  .header-right {
    gap: 8px;
  }
  
  .header-right .el-button {
    padding: 6px 8px;
    font-size: 12px;
  }
  
  .username {
    display: none;
  }
  
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 60px;
    height: calc(100vh - 60px);
    z-index: 999;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .admin-sidebar.mobile-show {
    transform: translateX(0);
  }
  
  .page-content {
    padding: 15px;
  }
  
  .breadcrumb-container {
    padding: 8px 15px;
  }
}

@media (max-width: 480px) {
  .system-title {
    font-size: 14px;
  }
  
  .page-content {
    padding: 10px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 菜单折叠动画 */
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu) {
  transition: all 0.3s ease;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-icon),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu .el-icon) {
  margin-right: 0;
}









/* 健康检查对话框样式 */
.health-loading {
  padding: 20px;
}

.health-content {
  padding: 0;
}

.health-overview {
  margin-bottom: 20px;
}

/* 对话框按钮样式优化 - 与系统健康检查按钮保持一致 */
.dialog-footer .el-button,
.el-dialog__footer .el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: none;
  padding: 10px 16px;
  font-size: 14px;
  min-width: 80px;
}

.dialog-footer .el-button:hover,
.el-dialog__footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-footer .el-button:active,
.el-dialog__footer .el-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 对话框主要按钮样式 */
.dialog-footer .el-button--primary,
.el-dialog__footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
  color: #fff;
}

.dialog-footer .el-button--primary:hover,
.el-dialog__footer .el-button--primary:hover {
  background: linear-gradient(135deg, #337ecc 0%, #5aa3e6 100%);
}

.dialog-footer .el-button--primary:focus,
.el-dialog__footer .el-button--primary:focus {
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
}

/* 对话框默认按钮样式 */
.dialog-footer .el-button--default,
.el-dialog__footer .el-button--default {
  background: rgba(255, 255, 255, 0.9);
  color: #606266;
  backdrop-filter: blur(10px);
}

.dialog-footer .el-button--default:hover,
.el-dialog__footer .el-button--default:hover {
  background: rgba(255, 255, 255, 1);
  color: #409eff;
}

.dialog-footer .el-button--default:focus,
.el-dialog__footer .el-button--default:focus {
  background: rgba(255, 255, 255, 0.9);
  color: #606266;
}

/* 对话框按钮图标样式 */
.dialog-footer .el-button .el-icon,
.el-dialog__footer .el-button .el-icon {
  margin-right: 6px;
  font-size: 16px;
  transition: transform 0.3s ease;
}

.dialog-footer .el-button:hover .el-icon,
.el-dialog__footer .el-button:hover .el-icon {
  transform: scale(1.1);
}

/* 对话框底部按钮布局 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 16px 0 0 0;
}

.health-overview .el-card__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.health-details {
  margin-bottom: 20px;
}

.app-info {
  margin-bottom: 0;
}

.app-info .el-card__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.card-header span {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.health-summary p {
  margin: 8px 0;
  color: #606266;
}

.health-item-content {
  min-height: 60px;
}

.health-item-content p {
  margin: 8px 0;
  color: #606266;
}

.health-item-content small {
  color: #909399;
  font-size: 12px;
}



.el-descriptions {
  margin-top: 0;
}

.el-card {
  border-radius: 8px;
}

.el-card__header {
  padding: 16px 20px;
}

.el-card__body {
  padding: 20px;
}
</style>