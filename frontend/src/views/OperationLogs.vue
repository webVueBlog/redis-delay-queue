<template>
  <div class="operation-logs">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button @click="refreshLogs" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportLogs">
          <el-icon><Download /></el-icon>
          导出日志
        </el-button>
        <el-button type="danger" @click="clearLogs">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedLogs.length === 0"
          @click="batchDeleteLogs"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedLogs.length }})
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="请选择操作类型" clearable>
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查看" value="VIEW" />
            <el-option label="导出" value="EXPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="请选择模块" clearable>
            <el-option label="用户管理" value="USER" />
            <el-option label="延迟队列" value="QUEUE" />
            <el-option label="菜单管理" value="MENU" />
            <el-option label="组织管理" value="ORGANIZATION" />
            <el-option label="系统设置" value="SETTINGS" />
            <el-option label="系统监控" value="MONITOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="日志级别">
          <el-select v-model="searchForm.level" placeholder="请选择日志级别" clearable>
            <el-option label="信息" value="INFO" />
            <el-option label="警告" value="WARN" />
            <el-option label="错误" value="ERROR" />
            <el-option label="调试" value="DEBUG" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogs">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon total"><Document /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.total }}</h3>
              <p>总日志数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon today"><Calendar /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.today }}</h3>
              <p>今日日志</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon error"><Warning /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.errors }}</h3>
              <p>错误日志</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon users"><User /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.activeUsers }}</h3>
              <p>活跃用户</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card class="table-card">
      <el-table 
        :data="logs" 
        v-loading="loading" 
        stripe 
        border
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="action" label="操作" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ getModuleText(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="120" />
        <el-table-column prop="userAgent" label="用户代理" width="150" show-overflow-tooltip />
        <el-table-column prop="duration" label="耗时(ms)" width="100">
          <template #default="{ row }">
            <span :class="getDurationClass(row.duration)">{{ row.duration }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog title="日志详情" v-model="showDetailDialog" width="800px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getActionType(currentLog.action)">
            {{ getActionText(currentLog.action) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">
          <el-tag type="info">
            {{ getModuleText(currentLog.module) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="日志级别">
          <el-tag :type="getLevelType(currentLog.level)">
            {{ currentLog.level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>操作描述</el-divider>
      <el-input 
        v-model="currentLog.description" 
        type="textarea" 
        :rows="3"
        readonly
      />
      
      <el-divider>用户代理</el-divider>
      <el-input 
        v-model="currentLog.userAgent" 
        type="textarea" 
        :rows="2"
        readonly
      />
      
      <el-divider v-if="currentLog.requestData">请求数据</el-divider>
      <el-input 
        v-if="currentLog.requestData"
        v-model="currentLog.requestData" 
        type="textarea" 
        :rows="4"
        readonly
      />
      
      <el-divider v-if="currentLog.responseData">响应数据</el-divider>
      <el-input 
        v-if="currentLog.responseData"
        v-model="currentLog.responseData" 
        type="textarea" 
        :rows="4"
        readonly
      />
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Download, Delete, Search, Document, Calendar, Warning, User, View 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const logs = ref([])
const selectedLogs = ref([])
const showDetailDialog = ref(false)
const currentLog = ref(null)

// 统计数据
const stats = reactive({
  total: 0,
  today: 0,
  errors: 0,
  activeUsers: 0
})

// 搜索表单
const searchForm = reactive({
  username: '',
  action: '',
  module: '',
  level: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取操作类型样式
const getActionType = (action) => {
  const types = {
    'LOGIN': 'success',
    'LOGOUT': 'info',
    'CREATE': 'primary',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'VIEW': 'info',
    'EXPORT': 'success'
  }
  return types[action] || 'info'
}

// 获取操作类型文本
const getActionText = (action) => {
  const texts = {
    'LOGIN': '登录',
    'LOGOUT': '登出',
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'VIEW': '查看',
    'EXPORT': '导出'
  }
  return texts[action] || action
}

// 获取模块文本
const getModuleText = (module) => {
  const texts = {
    'USER': '用户管理',
    'QUEUE': '延迟队列',
    'MENU': '菜单管理',
    'ORGANIZATION': '组织管理',
    'SETTINGS': '系统设置',
    'MONITOR': '系统监控'
  }
  return texts[module] || module
}

// 获取日志级别样式
const getLevelType = (level) => {
  const types = {
    'INFO': 'primary',
    'WARN': 'warning',
    'ERROR': 'danger',
    'DEBUG': 'info'
  }
  return types[level] || 'info'
}

// 获取耗时样式
const getDurationClass = (duration) => {
  if (duration > 1000) return 'duration-slow'
  if (duration > 500) return 'duration-medium'
  return 'duration-fast'
}

// 获取行样式
const getRowClassName = ({ row }) => {
  if (row.level === 'ERROR') return 'error-row'
  if (row.level === 'WARN') return 'warn-row'
  return ''
}

// 加载日志
const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      ...searchForm
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
      delete params.dateRange
    }
    
    const response = await request.get('/api/operation-logs', { params })
    logs.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 加载统计数据
    await loadStats()
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    const response = await request.get('/api/operation-logs/stats', { params })
    Object.assign(stats, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 如果统计接口失败，使用本地计算
    updateStatsLocally()
  }
}

// 本地计算统计数据
const updateStatsLocally = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const todayLogs = logs.value.filter(log => 
    new Date(log.createdAt) >= today
  )
  
  const errorLogs = logs.value.filter(log => 
    log.level === 'ERROR'
  )
  
  const uniqueUsers = new Set(logs.value.map(log => log.username))
  
  Object.assign(stats, {
    total: logs.value.length,
    today: todayLogs.length,
    errors: errorLogs.length,
    activeUsers: uniqueUsers.size
  })
}

// 搜索日志
const searchLogs = () => {
  pagination.page = 1
  loadLogs()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    action: '',
    module: '',
    level: '',
    dateRange: []
  })
  pagination.page = 1
  loadLogs()
}

// 刷新日志
const refreshLogs = () => {
  loadLogs()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadLogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadLogs()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

// 查看详情
const viewDetail = async (log) => {
  try {
    const response = await request.get(`/api/operation-logs/${log.id}`)
    currentLog.value = response.data
    showDetailDialog.value = true
  } catch (error) {
    console.error('获取日志详情失败:', error)
    ElMessage.error('获取日志详情失败')
  }
}

// 导出日志
const exportLogs = async () => {
  try {
    ElMessage.loading('正在导出日志...', 0)
    
    const params = { ...searchForm }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
      delete params.dateRange
    }
    
    const response = await request.get('/api/operation-logs/export', { 
      params,
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const now = new Date()
    const timestamp = now.toISOString().slice(0, 19).replace(/[:-]/g, '')
    link.download = `操作日志_${timestamp}.xlsx`
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.destroy()
    ElMessage.success('日志导出成功')
  } catch (error) {
    ElMessage.destroy()
    console.error('导出日志失败:', error)
    ElMessage.error('导出日志失败')
  }
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const params = {}
    // 如果有时间范围，只清空该范围内的日志
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    await request.delete('/api/operation-logs', { params })
    ElMessage.success('日志清空成功')
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空日志失败:', error)
      ElMessage.error('清空日志失败')
    }
  }
}

// 批量删除日志
const batchDeleteLogs = async () => {
  if (selectedLogs.value.length === 0) {
    ElMessage.warning('请先选择要删除的日志')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedLogs.value.length} 条日志吗？此操作不可恢复！`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedLogs.value.map(log => log.id)
    await request.delete('/api/operation-logs/batch', { data: { ids } })
    ElMessage.success('批量删除成功')
    selectedLogs.value = []
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.operation-logs {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 32px;
  margin-right: 15px;
}

.stat-icon.total {
  color: #409EFF;
}

.stat-icon.today {
  color: #67C23A;
}

.stat-icon.error {
  color: #F56C6C;
}

.stat-icon.users {
  color: #E6A23C;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.table-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.duration-fast {
  color: #67C23A;
}

.duration-medium {
  color: #E6A23C;
}

.duration-slow {
  color: #F56C6C;
}

:deep(.error-row) {
  background-color: #fef0f0;
}

:deep(.warn-row) {
  background-color: #fdf6ec;
}
</style>