<template>
  <div class="system-settings">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统设置</h2>
      <el-button type="primary" @click="saveAllSettings" size="large" :loading="loading">
        <el-icon><Check /></el-icon>
        保存所有设置
      </el-button>
    </div>

    <!-- 设置分类 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="category-card">
          <el-menu
            :default-active="activeCategory"
            @select="handleCategorySelect"
            class="settings-menu"
          >
            <el-menu-item index="basic">
              <el-icon><Setting /></el-icon>
              <span>基础设置</span>
            </el-menu-item>
            <el-menu-item index="redis">
              <el-icon><DataBoard /></el-icon>
              <span>Redis配置</span>
            </el-menu-item>
            <el-menu-item index="queue">
              <el-icon><List /></el-icon>
              <span>队列设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="performance">
              <el-icon><TrendCharts /></el-icon>
              <span>性能优化</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <!-- 基础设置 -->
        <el-card v-show="activeCategory === 'basic'" class="settings-card">
          <template #header>
            <span>基础设置</span>
          </template>
          
          <el-form :model="basicSettings" label-width="150px">
            <el-form-item label="系统名称">
              <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input 
                v-model="basicSettings.systemDescription" 
                type="textarea" 
                :rows="3"
                placeholder="请输入系统描述" 
              />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="basicSettings.systemVersion" placeholder="请输入系统版本" />
            </el-form-item>
            <el-form-item label="管理员邮箱">
              <el-input v-model="basicSettings.adminEmail" placeholder="请输入管理员邮箱" />
            </el-form-item>
            <el-form-item label="时区设置">
              <el-select v-model="basicSettings.timezone" placeholder="请选择时区">
                <el-option label="Asia/Shanghai" value="Asia/Shanghai" />
                <el-option label="UTC" value="UTC" />
                <el-option label="America/New_York" value="America/New_York" />
                <el-option label="Europe/London" value="Europe/London" />
              </el-select>
            </el-form-item>
            <el-form-item label="日志级别">
              <el-select v-model="basicSettings.logLevel" placeholder="请选择日志级别">
                <el-option label="DEBUG" value="DEBUG" />
                <el-option label="INFO" value="INFO" />
                <el-option label="WARN" value="WARN" />
                <el-option label="ERROR" value="ERROR" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- Redis配置 -->
        <el-card v-show="activeCategory === 'redis'" class="settings-card">
          <template #header>
            <div class="card-header">
              <span>Redis配置</span>
              <el-button type="primary" size="small" class="test-redis-btn" @click="testRedisConnection" :loading="loading">
                <el-icon><Connection /></el-icon>
                测试连接
              </el-button>
            </div>
          </template>
          
          <el-form :model="redisSettings" label-width="150px">
            <el-form-item label="Redis主机">
              <el-input v-model="redisSettings.host" placeholder="请输入Redis主机地址" />
            </el-form-item>
            <el-form-item label="Redis端口">
              <el-input-number v-model="redisSettings.port" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="数据库索引">
              <el-input-number v-model="redisSettings.database" :min="0" :max="15" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="redisSettings.password" type="password" show-password placeholder="请输入Redis密码" />
            </el-form-item>
            <el-form-item label="连接超时(ms)">
              <el-input-number v-model="redisSettings.timeout" :min="1000" :max="30000" />
            </el-form-item>
            <el-form-item label="最大连接数">
              <el-input-number v-model="redisSettings.maxConnections" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="最小空闲连接">
              <el-input-number v-model="redisSettings.minIdle" :min="0" :max="50" />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 队列设置 -->
        <el-card v-show="activeCategory === 'queue'" class="settings-card">
          <template #header>
            <span>队列设置</span>
          </template>
          
          <el-form :model="queueSettings" label-width="150px">
            <el-form-item label="默认队列名称">
              <el-input v-model="queueSettings.defaultQueueName" placeholder="请输入默认队列名称" />
            </el-form-item>
            <el-form-item label="最大重试次数">
              <el-input-number v-model="queueSettings.maxRetries" :min="0" :max="10" />
            </el-form-item>
            <el-form-item label="任务超时时间(秒)">
              <el-input-number v-model="queueSettings.taskTimeout" :min="1" :max="3600" />
            </el-form-item>
            <el-form-item label="批处理大小">
              <el-input-number v-model="queueSettings.batchSize" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item label="扫描间隔(秒)">
              <el-input-number v-model="queueSettings.scanInterval" :min="1" :max="60" />
            </el-form-item>
            <el-form-item label="启用任务持久化">
              <el-switch v-model="queueSettings.enablePersistence" />
            </el-form-item>
            <el-form-item label="启用任务监控">
              <el-switch v-model="queueSettings.enableMonitoring" />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 安全设置 -->
        <el-card v-show="activeCategory === 'security'" class="settings-card">
          <template #header>
            <span>安全设置</span>
          </template>
          
          <el-form :model="securitySettings" label-width="150px">
            <el-form-item label="JWT密钥">
              <el-input v-model="securitySettings.jwtSecret" type="password" show-password placeholder="请输入JWT密钥" />
            </el-form-item>
            <el-form-item label="Token过期时间(小时)">
              <el-input-number v-model="securitySettings.tokenExpiration" :min="1" :max="168" />
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securitySettings.minPasswordLength" :min="6" :max="20" />
            </el-form-item>
            <el-form-item label="登录失败锁定次数">
              <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
            </el-form-item>
            <el-form-item label="账户锁定时间(分钟)">
              <el-input-number v-model="securitySettings.lockoutDuration" :min="5" :max="60" />
            </el-form-item>
            <el-form-item label="启用双因子认证">
              <el-switch v-model="securitySettings.enableTwoFactor" />
            </el-form-item>
            <el-form-item label="启用IP白名单">
              <el-switch v-model="securitySettings.enableIpWhitelist" />
            </el-form-item>
            <el-form-item label="IP白名单" v-if="securitySettings.enableIpWhitelist">
              <el-input 
                v-model="securitySettings.ipWhitelist" 
                type="textarea" 
                :rows="3"
                placeholder="请输入IP地址，每行一个" 
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 通知设置 -->
        <el-card v-show="activeCategory === 'notification'" class="settings-card">
          <template #header>
            <span>通知设置</span>
          </template>
          
          <el-form :model="notificationSettings" label-width="150px">
            <el-form-item label="启用邮件通知">
              <el-switch v-model="notificationSettings.enableEmail" />
            </el-form-item>
            <el-form-item label="SMTP服务器" v-if="notificationSettings.enableEmail">
              <el-input v-model="notificationSettings.smtpHost" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            <el-form-item label="SMTP端口" v-if="notificationSettings.enableEmail">
              <el-input-number v-model="notificationSettings.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱" v-if="notificationSettings.enableEmail">
              <el-input v-model="notificationSettings.fromEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            <el-form-item label="邮箱密码" v-if="notificationSettings.enableEmail">
              <el-input v-model="notificationSettings.emailPassword" type="password" show-password placeholder="请输入邮箱密码" />
            </el-form-item>
            <el-form-item label="启用Webhook通知">
              <el-switch v-model="notificationSettings.enableWebhook" />
            </el-form-item>
            <el-form-item label="Webhook URL" v-if="notificationSettings.enableWebhook">
              <el-input v-model="notificationSettings.webhookUrl" placeholder="请输入Webhook URL" />
            </el-form-item>
            <el-form-item label="通知事件">
              <el-checkbox-group v-model="notificationSettings.events">
                <el-checkbox label="TASK_FAILED">任务失败</el-checkbox>
                <el-checkbox label="TASK_COMPLETED">任务完成</el-checkbox>
                <el-checkbox label="SYSTEM_ERROR">系统错误</el-checkbox>
                <el-checkbox label="USER_LOGIN">用户登录</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 性能优化 -->
        <el-card v-show="activeCategory === 'performance'" class="settings-card">
          <template #header>
            <span>性能优化</span>
          </template>
          
          <el-form :model="performanceSettings" label-width="150px">
            <el-form-item label="线程池大小">
              <el-input-number v-model="performanceSettings.threadPoolSize" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="队列容量">
              <el-input-number v-model="performanceSettings.queueCapacity" :min="100" :max="10000" />
            </el-form-item>
            <el-form-item label="启用缓存">
              <el-switch v-model="performanceSettings.enableCache" />
            </el-form-item>
            <el-form-item label="缓存过期时间(秒)" v-if="performanceSettings.enableCache">
              <el-input-number v-model="performanceSettings.cacheExpiration" :min="60" :max="3600" />
            </el-form-item>
            <el-form-item label="启用压缩">
              <el-switch v-model="performanceSettings.enableCompression" />
            </el-form-item>
            <el-form-item label="启用连接池">
              <el-switch v-model="performanceSettings.enableConnectionPool" />
            </el-form-item>
            <el-form-item label="最大并发数">
              <el-input-number v-model="performanceSettings.maxConcurrency" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item label="请求超时时间(秒)">
              <el-input-number v-model="performanceSettings.requestTimeout" :min="1" :max="300" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Setting, DataBoard, List, Lock, Bell, TrendCharts, Check, Connection 
} from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const activeCategory = ref('basic')
const loading = ref(false)

// 基础设置
const basicSettings = reactive({
  systemName: 'Redis延迟队列管理系统',
  systemDescription: '基于Redis实现的高性能延迟队列管理系统',
  systemVersion: '1.0.0',
  adminEmail: 'admin@example.com',
  timezone: 'Asia/Shanghai',
  logLevel: 'INFO'
})

// Redis配置
const redisSettings = reactive({
  host: 'localhost',
  port: 6379,
  database: 0,
  password: '',
  timeout: 5000,
  maxConnections: 20,
  minIdle: 5
})

// 队列设置
const queueSettings = reactive({
  defaultQueueName: 'default',
  maxRetries: 3,
  taskTimeout: 300,
  batchSize: 100,
  scanInterval: 5,
  enablePersistence: true,
  enableMonitoring: true
})

// 安全设置
const securitySettings = reactive({
  jwtSecret: '',
  tokenExpiration: 24,
  minPasswordLength: 8,
  maxLoginAttempts: 5,
  lockoutDuration: 15,
  enableTwoFactor: false,
  enableIpWhitelist: false,
  ipWhitelist: ''
})

// 通知设置
const notificationSettings = reactive({
  enableEmail: false,
  smtpHost: '',
  smtpPort: 587,
  fromEmail: '',
  emailPassword: '',
  enableWebhook: false,
  webhookUrl: '',
  events: ['TASK_FAILED', 'SYSTEM_ERROR']
})

// 性能优化设置
const performanceSettings = reactive({
  threadPoolSize: 10,
  queueCapacity: 1000,
  enableCache: true,
  cacheExpiration: 300,
  enableCompression: true,
  enableConnectionPool: true,
  maxConcurrency: 100,
  requestTimeout: 30
})

// 处理分类选择
const handleCategorySelect = (category) => {
  activeCategory.value = category
}

// 加载设置
const loadSettings = async () => {
  try {
    const response = await axios.get('/api/settings')
    if (response.data.success) {
      const data = response.data.data
      
      // 更新各个设置对象
      Object.assign(basicSettings, data.basicSettings || {})
      Object.assign(redisSettings, data.redisSettings || {})
      Object.assign(queueSettings, data.queueSettings || {})
      Object.assign(securitySettings, data.securitySettings || {})
      Object.assign(notificationSettings, data.notificationSettings || {})
      Object.assign(performanceSettings, data.performanceSettings || {})
      
      ElMessage.success('设置加载成功')
    } else {
      ElMessage.error(response.data.message || '加载设置失败')
    }
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载设置失败: ' + (error.response?.data?.message || error.message))
  }
}

// 保存所有设置
const saveAllSettings = async () => {
  try {
    await ElMessageBox.confirm('确定要保存所有设置吗？', '确认保存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    saving.value = true
    
    const settings = {
      basicSettings: { ...basicSettings },
      redisSettings: { ...redisSettings },
      queueSettings: { ...queueSettings },
      securitySettings: { ...securitySettings },
      notificationSettings: { ...notificationSettings },
      performanceSettings: { ...performanceSettings }
    }
    
    const response = await axios.put('/api/settings', settings, {
      headers: {
        'X-User-Name': 'admin' // 实际项目中应该从用户状态获取
      }
    })
    
    if (response.data.success) {
      ElMessage.success('设置保存成功')
    } else {
      ElMessage.error(response.data.message || '保存设置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存设置失败:', error)
      ElMessage.error('保存设置失败: ' + (error.response?.data?.message || error.message))
    }
  } finally {
    saving.value = false
  }
}

// 测试Redis连接
const testRedisConnection = async () => {
  try {
    // 验证必填字段
    if (!redisSettings.host || !redisSettings.port) {
      ElMessage.warning('请填写Redis主机地址和端口')
      return
    }
    
    const testButton = document.querySelector('.test-redis-btn')
    if (testButton) {
      testButton.disabled = true
      testButton.textContent = '测试中...'
    }
    
    const response = await axios.post('/api/settings/test-redis', {
      host: redisSettings.host,
      port: redisSettings.port,
      database: redisSettings.database,
      password: redisSettings.password,
      timeout: redisSettings.timeout,
      maxActive: redisSettings.maxActive,
      maxIdle: redisSettings.maxIdle,
      minIdle: redisSettings.minIdle
    })
    
    if (response.data.success) {
      ElMessage.success('Redis连接测试成功')
    } else {
      ElMessage.error(response.data.message || 'Redis连接测试失败')
    }
  } catch (error) {
    console.error('Redis连接测试失败:', error)
    ElMessage.error('Redis连接测试失败: ' + (error.response?.data?.message || error.message))
  } finally {
    const testButton = document.querySelector('.test-redis-btn')
    if (testButton) {
      testButton.disabled = false
      testButton.textContent = '测试连接'
    }
  }
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.category-card {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.settings-menu {
  border: none;
}

.settings-card {
  min-height: calc(100vh - 200px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #EBEEF5;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
}
</style>