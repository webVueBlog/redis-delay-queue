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

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Refresh, DataBoard, User, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'
import request from './utils/request'

export default {
  name: 'App',
  setup() {
    // 路由
    const router = useRouter()
    const route = useRoute()
    
    const loading = ref(false)
    const error = ref('')
    const tableData = ref([])
    const activeNames = ref([])
    
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
        const response = await request.get('/api/tables/mock-columns')
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
    
    // 菜单选择处理
    const handleMenuSelect = (index) => {
      router.push(index)
    }
    
    // 用户命令处理
    const handleUserCommand = (command) => {
      switch (command) {
        case 'profile':
          ElMessage.info('个人信息功能开发中')
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
        await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        
        // 清除axios默认header
        delete axios.defaults.headers.common['Authorization']
        
        // 重置用户状态
        username.value = ''
        userRole.value = ''
        userId.value = ''
        
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch (error) {
        // 用户取消退出
      }
    }
    
    // 修改密码
    const handleChangePassword = async () => {
      if (!changePasswordFormRef.value) return
      
      try {
        await changePasswordFormRef.value.validate()
        
        const response = await request.post('/api/users/change-password', {
          oldPassword: changePasswordForm.value.oldPassword,
          newPassword: changePasswordForm.value.newPassword
        })
        
        if (response.data.success) {
          ElMessage.success('密码修改成功')
          showChangePasswordDialog.value = false
          
          // 清空表单
          Object.assign(changePasswordForm.value, {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          })
        } else {
          ElMessage.error(response.data.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error('修改密码失败')
      }
    }
    
    // 获取健康检查数据
    const fetchHealthData = async () => {
      healthLoading.value = true
      
      try {
        // 获取总体健康状态
        const healthResponse = await axios.get('/api/health/')
        overallHealth.value = healthResponse.data
        
        // 获取详细健康检查
        const detailedResponse = await axios.get('/api/health/detailed')
        healthDetails.value = detailedResponse.data
        
        // 获取应用信息
        const infoResponse = await axios.get('/api/health/info')
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
      if (!bytes || bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 处理健康检查对话框关闭
    const handleHealthDialogClose = () => {
      showHealthDialog.value = false
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchData()
    })
    
    return {
      loading,
      error,
      tableData,
      activeNames,
      groupedTables,
      fetchData,
      showHealthDialog,
      healthLoading,
      overallHealth,
      healthDetails,
      appInfo,
      fetchHealthData,
      openHealthDialog,
      handleHealthDialogClose,
      formatMemory,
      username,
      userRole,
      userId,
      isLoggedIn,
      isAdmin,
      activeIndex,
      handleMenuSelect,
      handleUserCommand,
      handleLogout,
      showChangePasswordDialog,
      changePasswordForm,
      changePasswordFormRef,
      changePasswordRules,
      handleChangePassword
    }
  }
}
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

.loading-container,
.error-container {
  padding: 20px;
}

.table-collapse {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.table-icon {
  margin-right: 8px;
  color: #409eff;
}

.table-name {
  font-weight: 600;
  font-size: 16px;
  margin-right: 12px;
}

.column-count {
  margin-right: 12px;
}

.table-comment {
  color: #909399;
  font-size: 14px;
}

.key-tag {
  margin-right: 8px;
}

.column-name {
  font-weight: 500;
}

.default-value {
  font-family: 'Courier New', monospace;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.comment {
  color: #606266;
}

.no-comment {
  color: #c0c4cc;
  font-style: italic;
}

.empty-state {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-table {
  margin-top: 16px;
}

.el-collapse-item__content {
  padding: 0 20px 20px 20px;
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