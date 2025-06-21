<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>Redis延迟队列管理系统</h1>
        <div class="header-nav">
          <el-menu 
            mode="horizontal" 
            :default-active="activeIndex"
            @select="handleMenuSelect"
            v-if="isLoggedIn">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/users" v-if="isAdmin">用户管理</el-menu-item>
            <el-menu-item index="/menus" v-if="isAdmin">菜单管理</el-menu-item>
            <el-menu-item index="/organizations" v-if="isAdmin">组织管理</el-menu-item>
            <el-menu-item index="/delay-queue">
              <el-icon><Clock /></el-icon>
              <span>延迟队列</span>
            </el-menu-item>
            <el-menu-item index="/my-tasks">
              <el-icon><DataBoard /></el-icon>
              <span>我的任务</span>
            </el-menu-item>
            <el-menu-item index="/user-permission-organizations">
              <el-icon><OfficeBuilding /></el-icon>
              <span>权限组织</span>
            </el-menu-item>
            <el-menu-item index="/monitor" v-if="isAdmin">
              <el-icon><Monitor /></el-icon>
              <span>系统监控</span>
            </el-menu-item>
            <el-menu-item index="/data-statistics" v-if="isAdmin">
              <el-icon><DataBoard /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/system-settings" v-if="isAdmin">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
            <el-menu-item index="/operation-logs" v-if="isAdmin">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openHealthDialog">
            <el-icon><Monitor /></el-icon>
            系统健康检查
          </el-button>
          <div class="user-info" v-if="isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <span class="user-name">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-button v-else @click="$router.push('/login')">
            <el-icon><User /></el-icon>
            登录
          </el-button>
          <el-button type="primary" @click="fetchData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Refresh, DataBoard, User, ArrowDown, Clock, Grid, Connection, InfoFilled, Setting, Document, OfficeBuilding } from '@element-plus/icons-vue'
import request from './utils/request'

// 路由
const router = useRouter()
const route = useRoute()

    
// 用户状态
const username = ref(localStorage.getItem('username') || '')
const userRole = ref(localStorage.getItem('userRole') || '')
const userId = ref(localStorage.getItem('userId') || '')
    
// 健康检查相关状态
const showHealthDialog = ref(false)
const healthLoading = ref(false)
const overallHealth = ref({})
const healthDetails = ref({})
const appInfo = ref({})
    
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

    
// 菜单选择处理
const handleMenuSelect = (index) => {
  router.push(index)
}
    
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
}

.el-header {
  background-color: #545c64;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
  flex-shrink: 0;
}

.header-nav {
  flex: 1;
  margin: 0 20px;
}

.header-nav :deep(.el-menu) {
  background-color: transparent;
  border-bottom: none;
}

.header-nav :deep(.el-menu-item) {
  color: #fff;
  border-bottom: 2px solid transparent;
}

.header-nav :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.header-nav :deep(.el-menu-item.is-active) {
  background-color: transparent;
  border-bottom-color: #409eff;
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-shrink: 0;
}

.user-info {
  color: #fff;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-name:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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