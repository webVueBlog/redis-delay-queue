<template>
  <div class="system-monitor">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统监控</h2>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          @change="toggleAutoRefresh"
        />
      </div>
    </div>

    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <el-icon class="overview-icon cpu"><Cpu /></el-icon>
            <div class="overview-info">
              <h3>{{ systemInfo.cpuUsage }}%</h3>
              <p>CPU使用率</p>
            </div>
          </div>
          <el-progress 
            :percentage="systemInfo.cpuUsage" 
            :color="getProgressColor(systemInfo.cpuUsage)"
            :show-text="false"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <el-icon class="overview-icon memory"><Monitor /></el-icon>
            <div class="overview-info">
              <h3>{{ systemInfo.memoryUsage }}%</h3>
              <p>内存使用率</p>
            </div>
          </div>
          <el-progress 
            :percentage="systemInfo.memoryUsage" 
            :color="getProgressColor(systemInfo.memoryUsage)"
            :show-text="false"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <el-icon class="overview-icon redis"><DataBoard /></el-icon>
            <div class="overview-info">
              <h3>{{ redisInfo.connectedClients }}</h3>
              <p>Redis连接数</p>
            </div>
          </div>
          <div class="sub-info">
            <span>内存: {{ formatBytes(redisInfo.usedMemory) }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <el-icon class="overview-icon queue"><List /></el-icon>
            <div class="overview-info">
              <h3>{{ queueStats.totalTasks }}</h3>
              <p>队列任务总数</p>
            </div>
          </div>
          <div class="sub-info">
            <span>待处理: {{ queueStats.pendingTasks }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="CPU和内存使用趋势">
          <div ref="cpuMemoryChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="队列任务处理趋势">
          <div ref="queueChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card title="API请求统计">
          <div ref="apiChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细信息 -->
    <el-row :gutter="20" class="details-row">
      <el-col :span="8">
        <el-card title="系统信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="操作系统">{{ systemInfo.osName }}</el-descriptions-item>
            <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ formatDateTime(systemInfo.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ formatDuration(systemInfo.uptime) }}</el-descriptions-item>
            <el-descriptions-item label="可用处理器">{{ systemInfo.availableProcessors }}</el-descriptions-item>
            <el-descriptions-item label="最大内存">{{ formatBytes(systemInfo.maxMemory) }}</el-descriptions-item>
            <el-descriptions-item label="已用内存">{{ formatBytes(systemInfo.usedMemory) }}</el-descriptions-item>
            <el-descriptions-item label="空闲内存">{{ formatBytes(systemInfo.freeMemory) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="Redis信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Redis版本">{{ redisInfo.redisVersion }}</el-descriptions-item>
            <el-descriptions-item label="运行模式">{{ redisInfo.redisMode }}</el-descriptions-item>
            <el-descriptions-item label="连接的客户端">{{ redisInfo.connectedClients }}</el-descriptions-item>
            <el-descriptions-item label="已用内存">{{ formatBytes(redisInfo.usedMemory) }}</el-descriptions-item>
            <el-descriptions-item label="最大内存">{{ formatBytes(redisInfo.maxMemory) }}</el-descriptions-item>
            <el-descriptions-item label="内存使用率">{{ redisInfo.memoryUsagePercentage }}%</el-descriptions-item>
            <el-descriptions-item label="键总数">{{ redisInfo.totalKeys }}</el-descriptions-item>
            <el-descriptions-item label="过期键数">{{ redisInfo.expiredKeys }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="应用信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="应用名称">{{ appInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="应用版本">{{ appInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="Spring Boot版本">{{ appInfo.springBootVersion }}</el-descriptions-item>
            <el-descriptions-item label="活跃配置">{{ appInfo.activeProfiles.join(', ') }}</el-descriptions-item>
            <el-descriptions-item label="数据库连接池">
              <el-tag :type="appInfo.dbStatus === 'UP' ? 'success' : 'danger'">
                {{ appInfo.dbStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Redis连接">
              <el-tag :type="appInfo.redisStatus === 'UP' ? 'success' : 'danger'">
                {{ appInfo.redisStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="磁盘空间">{{ formatBytes(appInfo.diskFree) }} / {{ formatBytes(appInfo.diskTotal) }}</el-descriptions-item>
            <el-descriptions-item label="磁盘使用率">{{ appInfo.diskUsagePercentage }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志监控 -->
    <el-row :gutter="20" class="logs-row">
      <el-col :span="24">
        <el-card title="最近日志">
          <div class="log-controls">
            <el-select v-model="logLevel" @change="loadLogs" placeholder="选择日志级别">
              <el-option label="全部" value="" />
              <el-option label="ERROR" value="ERROR" />
              <el-option label="WARN" value="WARN" />
              <el-option label="INFO" value="INFO" />
              <el-option label="DEBUG" value="DEBUG" />
            </el-select>
            <el-button @click="loadLogs">
              <el-icon><Refresh /></el-icon>
              刷新日志
            </el-button>
            <el-button @click="clearLogs" type="danger">
              <el-icon><Delete /></el-icon>
              清空显示
            </el-button>
          </div>
          <div class="log-container">
            <div 
              v-for="(log, index) in logs" 
              :key="index" 
              :class="['log-item', `log-${log.level.toLowerCase()}`]"
            >
              <span class="log-time">{{ formatDateTime(log.timestamp) }}</span>
              <span class="log-level">{{ log.level }}</span>
              <span class="log-logger">{{ log.logger }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Monitor, DataBoard, List, Delete, Cpu 
} from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const autoRefresh = ref(true)
const logLevel = ref('')
const logs = ref([])
let refreshTimer = null
let charts = {}

// 系统信息
const systemInfo = reactive({
  cpuUsage: 0,
  memoryUsage: 0,
  osName: '',
  javaVersion: '',
  startTime: '',
  uptime: 0,
  availableProcessors: 0,
  maxMemory: 0,
  usedMemory: 0,
  freeMemory: 0
})

// Redis信息
const redisInfo = reactive({
  redisVersion: '',
  redisMode: '',
  connectedClients: 0,
  usedMemory: 0,
  maxMemory: 0,
  memoryUsagePercentage: 0,
  totalKeys: 0,
  expiredKeys: 0
})

// 队列统计
const queueStats = reactive({
  totalTasks: 0,
  pendingTasks: 0,
  processingTasks: 0,
  completedTasks: 0,
  failedTasks: 0
})

// 应用信息
const appInfo = reactive({
  name: '',
  version: '',
  springBootVersion: '',
  activeProfiles: [],
  dbStatus: '',
  redisStatus: '',
  diskFree: 0,
  diskTotal: 0,
  diskUsagePercentage: 0
})

// 图表引用
const cpuMemoryChart = ref()
const queueChart = ref()
const apiChart = ref()

// 历史数据
const historyData = reactive({
  cpu: [],
  memory: [],
  queueTasks: [],
  apiRequests: [],
  timestamps: []
})

// 方法
const loadSystemInfo = async () => {
  try {
    const response = await axios.get('/api/monitor/system')
    if (response.data.success) {
      Object.assign(systemInfo, response.data.data)
    }
  } catch (error) {
    console.error('加载系统信息失败:', error)
  }
}

const loadRedisInfo = async () => {
  try {
    const response = await axios.get('/api/monitor/redis')
    if (response.data.success) {
      Object.assign(redisInfo, response.data.data)
    }
  } catch (error) {
    console.error('加载Redis信息失败:', error)
  }
}

const loadQueueStats = async () => {
  try {
    const response = await axios.get('/api/monitor/queue')
    if (response.data.success) {
      Object.assign(queueStats, response.data.data)
    }
  } catch (error) {
    console.error('加载队列统计失败:', error)
  }
}

const loadAppInfo = async () => {
  try {
    const response = await axios.get('/api/monitor/app')
    if (response.data.success) {
      Object.assign(appInfo, response.data.data)
    }
  } catch (error) {
    console.error('加载应用信息失败:', error)
  }
}

const loadLogs = async () => {
  try {
    const params = {}
    if (logLevel.value) {
      params.level = logLevel.value
    }
    
    const response = await axios.get('/api/monitor/logs', { params })
    if (response.data.success) {
      logs.value = response.data.data
    }
  } catch (error) {
    console.error('加载日志失败:', error)
  }
}

const loadHistoryData = async () => {
  try {
    const response = await axios.get('/api/monitor/history')
    if (response.data.success) {
      const data = response.data.data
      historyData.cpu = data.cpu || []
      historyData.memory = data.memory || []
      historyData.queueTasks = data.queueTasks || []
      historyData.apiRequests = data.apiRequests || []
      historyData.timestamps = data.timestamps || []
      
      updateCharts()
    }
  } catch (error) {
    console.error('加载历史数据失败:', error)
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadSystemInfo(),
      loadRedisInfo(),
      loadQueueStats(),
      loadAppInfo(),
      loadHistoryData()
    ])
  } finally {
    loading.value = false
  }
}

const clearLogs = () => {
  logs.value = []
}

const toggleAutoRefresh = (enabled) => {
  if (enabled) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const startAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  refreshTimer = setInterval(() => {
    refreshData()
  }, 10000) // 每10秒刷新一次
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const initCharts = () => {
  nextTick(() => {
    // CPU和内存图表
    if (cpuMemoryChart.value) {
      charts.cpuMemory = echarts.init(cpuMemoryChart.value)
      charts.cpuMemory.setOption({
        title: { text: 'CPU和内存使用率' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['CPU使用率', '内存使用率'] },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
        series: [
          { name: 'CPU使用率', type: 'line', data: [], smooth: true },
          { name: '内存使用率', type: 'line', data: [], smooth: true }
        ]
      })
    }
    
    // 队列任务图表
    if (queueChart.value) {
      charts.queue = echarts.init(queueChart.value)
      charts.queue.setOption({
        title: { text: '队列任务数量' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['总任务数', '待处理', '处理中', '已完成', '失败'] },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [
          { name: '总任务数', type: 'line', data: [], smooth: true },
          { name: '待处理', type: 'line', data: [], smooth: true },
          { name: '处理中', type: 'line', data: [], smooth: true },
          { name: '已完成', type: 'line', data: [], smooth: true },
          { name: '失败', type: 'line', data: [], smooth: true }
        ]
      })
    }
    
    // API请求图表
    if (apiChart.value) {
      charts.api = echarts.init(apiChart.value)
      charts.api.setOption({
        title: { text: 'API请求统计' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['请求总数', '成功请求', '失败请求'] },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [
          { name: '请求总数', type: 'bar', data: [] },
          { name: '成功请求', type: 'bar', data: [] },
          { name: '失败请求', type: 'bar', data: [] }
        ]
      })
    }
  })
}

const updateCharts = () => {
  if (charts.cpuMemory) {
    charts.cpuMemory.setOption({
      xAxis: { data: historyData.timestamps },
      series: [
        { data: historyData.cpu },
        { data: historyData.memory }
      ]
    })
  }
  
  if (charts.queue) {
    charts.queue.setOption({
      xAxis: { data: historyData.timestamps },
      series: [
        { data: historyData.queueTasks }
      ]
    })
  }
  
  if (charts.api) {
    charts.api.setOption({
      xAxis: { data: historyData.timestamps },
      series: [
        { data: historyData.apiRequests }
      ]
    })
  }
}

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDateTime = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
}

const formatDuration = (milliseconds) => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`
  } else if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟 ${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

// 生命周期
onMounted(() => {
  refreshData()
  loadLogs()
  initCharts()
  
  if (autoRefresh.value) {
    startAutoRefresh()
  }
  
  // 监听窗口大小变化，重新调整图表
  window.addEventListener('resize', () => {
    Object.values(charts).forEach(chart => {
      if (chart) {
        chart.resize()
      }
    })
  })
})

onUnmounted(() => {
  stopAutoRefresh()
  
  // 销毁图表
  Object.values(charts).forEach(chart => {
    if (chart) {
      chart.dispose()
    }
  })
})
</script>

<style scoped>
.system-monitor {
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
  align-items: center;
  gap: 15px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
}

.overview-content {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.overview-icon {
  font-size: 32px;
  margin-right: 15px;
}

.overview-icon.cpu {
  color: #409eff;
}

.overview-icon.memory {
  color: #67c23a;
}

.overview-icon.redis {
  color: #e6a23c;
}

.overview-icon.queue {
  color: #f56c6c;
}

.overview-info h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 24px;
}

.overview-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.sub-info {
  margin-top: 5px;
  font-size: 12px;
  color: #999;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.details-row {
  margin-bottom: 20px;
}

.logs-row {
  margin-bottom: 20px;
}

.log-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.log-container {
  height: 400px;
  overflow-y: auto;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  display: flex;
  margin-bottom: 5px;
  padding: 2px 0;
}

.log-time {
  width: 180px;
  color: #666;
  flex-shrink: 0;
}

.log-level {
  width: 60px;
  font-weight: bold;
  flex-shrink: 0;
}

.log-logger {
  width: 200px;
  color: #333;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-message {
  flex: 1;
  color: #333;
}

.log-error .log-level {
  color: #f56c6c;
}

.log-warn .log-level {
  color: #e6a23c;
}

.log-info .log-level {
  color: #409eff;
}

.log-debug .log-level {
  color: #909399;
}

.el-card {
  margin-bottom: 20px;
}

.el-descriptions {
  margin-top: 10px;
}
</style>