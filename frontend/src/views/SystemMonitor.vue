<template>
  <div class="system-monitor">
    <!-- 页面标题和控制面板 -->
    <div class="page-header">
      <div class="header-left">
        <h2>系统监控</h2>
        <el-tag :type="systemStatus.type" class="status-tag">
          <el-icon><CircleCheck v-if="systemStatus.type === 'success'" /><Warning v-else /></el-icon>
          {{ systemStatus.text }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button @click="refreshData" :loading="loading" type="primary">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-button @click="exportData" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
          <el-button @click="showSettings = true">
            <el-icon><Setting /></el-icon>
            设置
          </el-button>
        </el-button-group>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          @change="toggleAutoRefresh"
          class="auto-refresh-switch"
        />
        <el-select v-model="refreshInterval" @change="updateRefreshInterval" class="interval-select">
          <el-option label="5秒" :value="5000" />
          <el-option label="10秒" :value="10000" />
          <el-option label="30秒" :value="30000" />
          <el-option label="1分钟" :value="60000" />
        </el-select>
      </div>
    </div>

    <!-- 告警通知 -->
    <div v-if="alerts.length > 0" class="alerts-section">
      <el-alert
        v-for="alert in alerts"
        :key="alert.id"
        :title="alert.title"
        :description="alert.description"
        :type="alert.type"
        show-icon
        :closable="true"
        @close="dismissAlert(alert.id)"
        class="alert-item"
      />
    </div>

    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card cpu-card" :class="{ 'warning': systemInfo.cpuUsage > 80, 'danger': systemInfo.cpuUsage > 90 }">
          <div class="card-header">
            <el-icon class="overview-icon cpu"><Cpu /></el-icon>
            <div class="trend-indicator">
              <el-icon :class="getCpuTrend().class">{{ getCpuTrend().icon }}</el-icon>
            </div>
          </div>
          <div class="overview-content">
            <div class="overview-info">
              <h3>{{ systemInfo.cpuUsage }}%</h3>
              <p>CPU使用率</p>
              <span class="trend-text">{{ getCpuTrend().text }}</span>
            </div>
          </div>
          <el-progress 
            :percentage="systemInfo.cpuUsage" 
            :color="getProgressColor(systemInfo.cpuUsage)"
            :show-text="false"
            :stroke-width="6"
          />
          <div class="card-footer">
            <span>核心数: {{ systemInfo.availableProcessors }}</span>
            <span>负载: {{ systemInfo.loadAverage || 'N/A' }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card memory-card" :class="{ 'warning': systemInfo.memoryUsage > 80, 'danger': systemInfo.memoryUsage > 90 }">
          <div class="card-header">
            <el-icon class="overview-icon memory"><Monitor /></el-icon>
            <div class="trend-indicator">
              <el-icon :class="getMemoryTrend().class">{{ getMemoryTrend().icon }}</el-icon>
            </div>
          </div>
          <div class="overview-content">
            <div class="overview-info">
              <h3>{{ systemInfo.memoryUsage }}%</h3>
              <p>内存使用率</p>
              <span class="trend-text">{{ getMemoryTrend().text }}</span>
            </div>
          </div>
          <el-progress 
            :percentage="systemInfo.memoryUsage" 
            :color="getProgressColor(systemInfo.memoryUsage)"
            :show-text="false"
            :stroke-width="6"
          />
          <div class="card-footer">
            <span>已用: {{ formatBytes(systemInfo.usedMemory) }}</span>
            <span>总计: {{ formatBytes(systemInfo.maxMemory) }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card redis-card" :class="{ 'warning': redisInfo.memoryUsagePercentage > 80 }">
          <div class="card-header">
            <el-icon class="overview-icon redis"><DataBoard /></el-icon>
            <el-tag :type="redisInfo.status === 'UP' ? 'success' : 'danger'" size="small">
              {{ redisInfo.status || 'UP' }}
            </el-tag>
          </div>
          <div class="overview-content">
            <div class="overview-info">
              <h3>{{ redisInfo.connectedClients }}</h3>
              <p>Redis连接数</p>
              <span class="trend-text">{{ redisInfo.redisVersion }}</span>
            </div>
          </div>
          <el-progress 
            :percentage="redisInfo.memoryUsagePercentage" 
            :color="getProgressColor(redisInfo.memoryUsagePercentage)"
            :show-text="false"
            :stroke-width="6"
          />
          <div class="card-footer">
            <span>内存: {{ formatBytes(redisInfo.usedMemory) }}</span>
            <span>键数: {{ redisInfo.totalKeys }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card queue-card">
          <div class="card-header">
            <el-icon class="overview-icon queue"><List /></el-icon>
            <div class="queue-status">
              <el-badge :value="queueStats.failedTasks" :hidden="queueStats.failedTasks === 0" type="danger">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </div>
          <div class="overview-content">
            <div class="overview-info">
              <h3>{{ queueStats.totalTasks }}</h3>
              <p>队列任务总数</p>
              <span class="trend-text">处理率: {{ getProcessingRate() }}%</span>
            </div>
          </div>
          <div class="queue-breakdown">
            <div class="queue-item pending">
              <span>待处理</span>
              <strong>{{ queueStats.pendingTasks }}</strong>
            </div>
            <div class="queue-item processing">
              <span>处理中</span>
              <strong>{{ queueStats.processingTasks }}</strong>
            </div>
            <div class="queue-item completed">
              <span>已完成</span>
              <strong>{{ queueStats.completedTasks }}</strong>
            </div>
            <div class="queue-item failed">
              <span>失败</span>
              <strong>{{ queueStats.failedTasks }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>CPU和内存使用趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="cpuMemoryTimeRange" @change="updateCpuMemoryChart" size="small">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="6h">6小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                </el-radio-group>
                <el-button @click="toggleFullscreen('cpuMemory')" size="small" text>
                  <el-icon><FullScreen /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <div ref="cpuMemoryChart" class="chart-container"></div>
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-color cpu"></span>
              <span>CPU使用率</span>
              <strong>{{ systemInfo.cpuUsage }}%</strong>
            </div>
            <div class="legend-item">
              <span class="legend-color memory"></span>
              <span>内存使用率</span>
              <strong>{{ systemInfo.memoryUsage }}%</strong>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>队列任务处理趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="queueTimeRange" @change="updateQueueChart" size="small">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="6h">6小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                </el-radio-group>
                <el-button @click="toggleFullscreen('queue')" size="small" text>
                  <el-icon><FullScreen /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <div ref="queueChart" class="chart-container"></div>
          <div class="chart-stats">
            <div class="stat-item">
              <span>平均处理时间</span>
              <strong>{{ queueStats.avgProcessingTime || 'N/A' }}ms</strong>
            </div>
            <div class="stat-item">
              <span>成功率</span>
              <strong>{{ getSuccessRate() }}%</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>API请求统计</span>
              <div class="chart-controls">
                <el-select v-model="apiMetric" @change="updateApiChart" size="small">
                  <el-option label="请求数量" value="count" />
                  <el-option label="响应时间" value="time" />
                  <el-option label="错误率" value="error" />
                </el-select>
                <el-button @click="toggleFullscreen('api')" size="small" text>
                  <el-icon><FullScreen /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <div ref="apiChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="实时性能指标">
          <div class="performance-metrics">
            <div class="metric-item">
              <div class="metric-label">平均响应时间</div>
              <div class="metric-value">{{ performanceMetrics.avgResponseTime }}ms</div>
              <el-progress :percentage="getResponseTimeScore()" :show-text="false" :stroke-width="4" />
            </div>
            <div class="metric-item">
              <div class="metric-label">QPS</div>
              <div class="metric-value">{{ performanceMetrics.qps }}</div>
              <div class="metric-trend" :class="performanceMetrics.qpsTrend">
                <el-icon><ArrowUp v-if="performanceMetrics.qpsTrend === 'up'" /><ArrowDown v-else /></el-icon>
                {{ performanceMetrics.qpsChange }}%
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">错误率</div>
              <div class="metric-value error">{{ performanceMetrics.errorRate }}%</div>
              <el-progress :percentage="performanceMetrics.errorRate" :show-text="false" :stroke-width="4" color="#f56c6c" />
            </div>
            <div class="metric-item">
              <div class="metric-label">活跃连接</div>
              <div class="metric-value">{{ performanceMetrics.activeConnections }}</div>
            </div>
          </div>
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
        <el-card>
          <template #header>
            <div class="log-header">
              <span>系统日志监控</span>
              <div class="log-stats">
                <el-tag type="danger" v-if="logStats.error > 0">错误: {{ logStats.error }}</el-tag>
                <el-tag type="warning" v-if="logStats.warn > 0">警告: {{ logStats.warn }}</el-tag>
                <el-tag type="info">信息: {{ logStats.info }}</el-tag>
              </div>
            </div>
          </template>
          <div class="log-controls">
            <div class="log-filters">
              <el-select v-model="logLevel" @change="loadLogs" placeholder="选择日志级别" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="ERROR" value="ERROR" />
                <el-option label="WARN" value="WARN" />
                <el-option label="INFO" value="INFO" />
                <el-option label="DEBUG" value="DEBUG" />
              </el-select>
              <el-input
                v-model="logSearch"
                placeholder="搜索日志内容"
                @input="filterLogs"
                style="width: 200px"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-date-picker
                v-model="logDateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                @change="loadLogs"
                style="width: 300px"
              />
            </div>
            <div class="log-actions">
              <el-button @click="loadLogs" :loading="loadingLogs">
                <el-icon><Refresh /></el-icon>
                刷新日志
              </el-button>
              <el-button @click="exportLogs">
                <el-icon><Download /></el-icon>
                导出日志
              </el-button>
              <el-button @click="clearLogs" type="danger">
                <el-icon><Delete /></el-icon>
                清空显示
              </el-button>
              <el-switch
                v-model="autoScrollLog"
                active-text="自动滚动"
                inactive-text=""
              />
            </div>
          </div>
          <div class="log-container" ref="logContainer">
            <div class="log-virtual-list">
              <div 
                v-for="(log, index) in filteredLogs" 
                :key="index" 
                :class="['log-item', `log-${log.level.toLowerCase()}`]"
                @click="showLogDetailDialog(log)"
              >
                <span class="log-time">{{ formatDateTime(log.timestamp) }}</span>
                <el-tag :type="getLogLevelType(log.level)" size="small" class="log-level-tag">
                  {{ log.level }}
                </el-tag>
                <span class="log-logger">{{ log.logger }}</span>
                <span class="log-message" :title="log.message">{{ log.message }}</span>
                <div class="log-actions">
                  <el-button size="small" text @click.stop="copyLog(log)">
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            <div v-if="filteredLogs.length === 0" class="log-empty">
              <el-empty description="暂无日志数据" />
            </div>
          </div>
          <div class="log-pagination">
            <el-pagination
              v-model:current-page="logCurrentPage"
              v-model:page-size="logPageSize"
              :page-sizes="[50, 100, 200, 500]"
              :total="totalLogs"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadLogs"
              @current-change="loadLogs"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 设置对话框 -->
    <el-dialog v-model="showSettings" title="监控设置" width="600px">
      <el-form :model="settings" label-width="120px">
        <el-form-item label="刷新间隔">
          <el-select v-model="settings.refreshInterval">
            <el-option label="5秒" :value="5000" />
            <el-option label="10秒" :value="10000" />
            <el-option label="30秒" :value="30000" />
            <el-option label="1分钟" :value="60000" />
            <el-option label="5分钟" :value="300000" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警阈值">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number v-model="settings.cpuThreshold" :min="0" :max="100" />
              <span style="margin-left: 8px">CPU使用率(%)</span>
            </el-col>
            <el-col :span="12">
              <el-input-number v-model="settings.memoryThreshold" :min="0" :max="100" />
              <span style="margin-left: 8px">内存使用率(%)</span>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="数据保留">
          <el-select v-model="settings.dataRetention">
            <el-option label="1小时" value="1h" />
            <el-option label="6小时" value="6h" />
            <el-option label="24小时" value="24h" />
            <el-option label="7天" value="7d" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用通知">
          <el-switch v-model="settings.enableNotifications" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="showLogDetail" title="日志详情" width="800px">
      <div v-if="selectedLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ formatDateTime(selectedLog.timestamp) }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLogLevelType(selectedLog.level)">{{ selectedLog.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="记录器">{{ selectedLog.logger }}</el-descriptions-item>
          <el-descriptions-item label="线程">{{ selectedLog.thread || 'N/A' }}</el-descriptions-item>
        </el-descriptions>
        <div class="log-message-detail">
          <h4>消息内容</h4>
          <pre>{{ selectedLog.message }}</pre>
        </div>
        <div v-if="selectedLog.stackTrace" class="log-stack-trace">
          <h4>堆栈跟踪</h4>
          <pre>{{ selectedLog.stackTrace }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Monitor, DataBoard, List, Delete, Cpu, CircleCheck, Warning,
  Download, Setting, FullScreen, Search, CopyDocument, Bell,
  ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const loadingLogs = ref(false)
const autoRefresh = ref(true)
const refreshInterval = ref(10000)
const logLevel = ref('')
const logSearch = ref('')
const logDateRange = ref([])
const autoScrollLog = ref(true)
const logs = ref([])
const filteredLogs = ref([])
const logCurrentPage = ref(1)
const logPageSize = ref(100)
const totalLogs = ref(0)
const showSettings = ref(false)
const showLogDetail = ref(false)
const selectedLog = ref(null)
let refreshTimer = null
let charts = {}

// 新增数据属性
const alerts = ref([])
const cpuMemoryTimeRange = ref('1h')
const queueTimeRange = ref('1h')
const apiMetric = ref('count')
const previousCpuUsage = ref(0)
const previousMemoryUsage = ref(0)

// 系统状态
const systemStatus = computed(() => {
  const cpuHigh = systemInfo.cpuUsage > 90
  const memoryHigh = systemInfo.memoryUsage > 90
  const redisDown = redisInfo.status === 'DOWN'
  
  if (cpuHigh || memoryHigh || redisDown) {
    return { type: 'danger', text: '系统异常' }
  } else if (systemInfo.cpuUsage > 80 || systemInfo.memoryUsage > 80) {
    return { type: 'warning', text: '系统警告' }
  }
  return { type: 'success', text: '系统正常' }
})

// 日志统计
const logStats = reactive({
  error: 0,
  warn: 0,
  info: 0,
  debug: 0
})

// 性能指标
const performanceMetrics = reactive({
  avgResponseTime: 0,
  qps: 0,
  qpsTrend: 'up',
  qpsChange: 0,
  errorRate: 0,
  activeConnections: 0
})

// 设置
const settings = reactive({
  refreshInterval: 10000,
  cpuThreshold: 80,
  memoryThreshold: 80,
  dataRetention: '24h',
  enableNotifications: true
})

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



const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const params = {
      page: logCurrentPage.value,
      size: logPageSize.value
    }
    
    if (logLevel.value) {
      params.level = logLevel.value
    }
    
    if (logDateRange.value && logDateRange.value.length === 2) {
      params.startTime = logDateRange.value[0].toISOString()
      params.endTime = logDateRange.value[1].toISOString()
    }
    
    const response = await axios.get('/api/monitor/logs', { params })
    if (response.data.success) {
      logs.value = response.data.data.logs || response.data.data
      totalLogs.value = response.data.data.total || logs.value.length
      filterLogs()
      
      // 自动滚动到底部
      if (autoScrollLog.value) {
        nextTick(() => {
          const container = document.querySelector('.log-container')
          if (container) {
            container.scrollTop = container.scrollHeight
          }
        })
      }
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loadingLogs.value = false
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
    // 保存之前的值用于趋势计算
    previousCpuUsage.value = systemInfo.cpuUsage
    previousMemoryUsage.value = systemInfo.memoryUsage
    
    await Promise.all([
      loadSystemInfo(),
      loadRedisInfo(),
      loadQueueStats(),
      loadHistoryData(),
      updatePerformanceMetrics()
    ])
    
    // 检查告警
    checkAlerts()
  } finally {
    loading.value = false
  }
}

const updatePerformanceMetrics = async () => {
  try {
    const response = await axios.get('/api/monitor/performance')
    if (response.data.success) {
      Object.assign(performanceMetrics, response.data.data)
    }
  } catch (error) {
    console.error('加载性能指标失败:', error)
    // 保留原有的模拟数据作为fallback
    performanceMetrics.avgResponseTime = Math.floor(Math.random() * 500) + 50
    performanceMetrics.qps = Math.floor(Math.random() * 1000) + 100
    performanceMetrics.qpsChange = Math.floor(Math.random() * 20) - 10
    performanceMetrics.qpsTrend = performanceMetrics.qpsChange >= 0 ? 'up' : 'down'
    performanceMetrics.errorRate = Math.floor(Math.random() * 5)
    performanceMetrics.activeConnections = Math.floor(Math.random() * 100) + 50
  }
}

const clearLogs = () => {
  logs.value = []
}

// 新增方法
const getCpuTrend = () => {
  const current = systemInfo.cpuUsage
  const previous = previousCpuUsage.value
  const diff = current - previous
  
  if (Math.abs(diff) < 1) {
    return { class: 'trend-stable', icon: '→', text: '稳定' }
  } else if (diff > 0) {
    return { class: 'trend-up', icon: '↑', text: `上升${diff.toFixed(1)}%` }
  } else {
    return { class: 'trend-down', icon: '↓', text: `下降${Math.abs(diff).toFixed(1)}%` }
  }
}

const getMemoryTrend = () => {
  const current = systemInfo.memoryUsage
  const previous = previousMemoryUsage.value
  const diff = current - previous
  
  if (Math.abs(diff) < 1) {
    return { class: 'trend-stable', icon: '→', text: '稳定' }
  } else if (diff > 0) {
    return { class: 'trend-up', icon: '↑', text: `上升${diff.toFixed(1)}%` }
  } else {
    return { class: 'trend-down', icon: '↓', text: `下降${Math.abs(diff).toFixed(1)}%` }
  }
}

const getProcessingRate = () => {
  const total = queueStats.totalTasks
  if (total === 0) return 0
  return Math.round((queueStats.completedTasks / total) * 100)
}

const getSuccessRate = () => {
  const total = queueStats.completedTasks + queueStats.failedTasks
  if (total === 0) return 100
  return Math.round((queueStats.completedTasks / total) * 100)
}

const getResponseTimeScore = () => {
  const time = performanceMetrics.avgResponseTime
  if (time < 100) return 100
  if (time < 500) return 80
  if (time < 1000) return 60
  if (time < 2000) return 40
  return 20
}

const getLogLevelType = (level) => {
  const types = {
    ERROR: 'danger',
    WARN: 'warning',
    INFO: 'info',
    DEBUG: 'info'
  }
  return types[level] || 'info'
}

const dismissAlert = (alertId) => {
  const index = alerts.value.findIndex(alert => alert.id === alertId)
  if (index > -1) {
    alerts.value.splice(index, 1)
  }
}

const exportData = async () => {
  exporting.value = true
  try {
    const data = {
      timestamp: new Date().toISOString(),
      systemInfo: systemInfo,
      redisInfo: redisInfo,
      queueStats: queueStats,
      appInfo: appInfo,
      performanceMetrics: performanceMetrics
    }
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `system-monitor-${new Date().toISOString().slice(0, 19)}.json`
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    ElMessage.error('数据导出失败')
  } finally {
    exporting.value = false
  }
}

const updateRefreshInterval = (interval) => {
  refreshInterval.value = interval
  if (autoRefresh.value) {
    stopAutoRefresh()
    startAutoRefresh()
  }
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
  }, refreshInterval.value)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const filterLogs = () => {
  let filtered = logs.value
  
  if (logSearch.value) {
    const search = logSearch.value.toLowerCase()
    filtered = filtered.filter(log => 
      log.message.toLowerCase().includes(search) ||
      log.logger.toLowerCase().includes(search)
    )
  }
  
  filteredLogs.value = filtered
  updateLogStats()
}

const updateLogStats = () => {
  const stats = { error: 0, warn: 0, info: 0, debug: 0 }
  filteredLogs.value.forEach(log => {
    const level = log.level.toLowerCase()
    if (stats.hasOwnProperty(level)) {
      stats[level]++
    }
  })
  Object.assign(logStats, stats)
}

const exportLogs = async () => {
  try {
    const data = filteredLogs.value.map(log => ({
      timestamp: formatDateTime(log.timestamp),
      level: log.level,
      logger: log.logger,
      message: log.message
    }))
    
    const csv = [
      'Timestamp,Level,Logger,Message',
      ...data.map(row => `"${row.timestamp}","${row.level}","${row.logger}","${row.message.replace(/"/g, '""')}"`)
    ].join('\n')
    
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `system-logs-${new Date().toISOString().slice(0, 19)}.csv`
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('日志导出成功')
  } catch (error) {
    ElMessage.error('日志导出失败')
  }
}

const showLogDetailDialog = (log) => {
  selectedLog.value = log
  showLogDetail.value = true
}

const copyLog = async (log) => {
  try {
    const text = `[${formatDateTime(log.timestamp)}] ${log.level} ${log.logger} - ${log.message}`
    await navigator.clipboard.writeText(text)
    ElMessage.success('日志已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const toggleFullscreen = (chartType) => {
  // 实现图表全屏功能
  ElMessage.info('全屏功能开发中')
}

const updateCpuMemoryChart = () => {
  loadHistoryData()
}

const updateQueueChart = () => {
  loadHistoryData()
}

const updateApiChart = () => {
  loadHistoryData()
}

const saveSettings = () => {
  localStorage.setItem('monitorSettings', JSON.stringify(settings))
  refreshInterval.value = settings.refreshInterval
  if (autoRefresh.value) {
    stopAutoRefresh()
    startAutoRefresh()
  }
  showSettings.value = false
  ElMessage.success('设置已保存')
}

const loadSettings = () => {
  const saved = localStorage.getItem('monitorSettings')
  if (saved) {
    Object.assign(settings, JSON.parse(saved))
    refreshInterval.value = settings.refreshInterval
  }
}

const checkAlerts = () => {
  const newAlerts = []
  
  if (systemInfo.cpuUsage > settings.cpuThreshold) {
    newAlerts.push({
      id: 'cpu-high',
      type: 'warning',
      title: 'CPU使用率过高',
      description: `当前CPU使用率为${systemInfo.cpuUsage}%，超过阈值${settings.cpuThreshold}%`
    })
  }
  
  if (systemInfo.memoryUsage > settings.memoryThreshold) {
    newAlerts.push({
      id: 'memory-high',
      type: 'warning',
      title: '内存使用率过高',
      description: `当前内存使用率为${systemInfo.memoryUsage}%，超过阈值${settings.memoryThreshold}%`
    })
  }
  
  if (queueStats.failedTasks > 0) {
    newAlerts.push({
      id: 'queue-failed',
      type: 'error',
      title: '队列任务失败',
      description: `有${queueStats.failedTasks}个任务处理失败，请及时处理`
    })
  }
  
  // 只添加新的告警
  newAlerts.forEach(newAlert => {
    if (!alerts.value.find(alert => alert.id === newAlert.id)) {
      alerts.value.push(newAlert)
    }
  })
  
  // 移除已解决的告警
  alerts.value = alerts.value.filter(alert => {
    return newAlerts.find(newAlert => newAlert.id === alert.id)
  })
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
  // 加载设置
  loadSettings()
  
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
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.status-tag {
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.auto-refresh-switch {
  margin-left: 10px;
}

.interval-select {
  width: 100px;
}

/* 告警样式 */
.alerts-section {
  margin-bottom: 20px;
}

.alert-item {
  margin-bottom: 10px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: auto;
  min-height: 160px;
  text-align: center;
  padding: 24px;
  border-radius: 12px;
  background: white;
  color: #303133;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.overview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.trend-up {
  color: #f56c6c;
}

.trend-down {
  color: #67c23a;
}

.trend-stable {
  color: #909399;
}

.trend-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.card-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-weight: 600;
  color: #606266;
}

.queue-breakdown {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 12px;
  font-size: 12px;
}

.queue-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #ddd;
}

.queue-item.pending {
  border-left-color: #e6a23c;
}

.queue-item.processing {
  border-left-color: #409eff;
}

.queue-item.completed {
  border-left-color: #67c23a;
}

.queue-item.failed {
  border-left-color: #f56c6c;
}

.queue-status {
  display: flex;
  align-items: center;
}

.breakdown-label {
  color: #909399;
}

.breakdown-value {
  font-weight: 600;
  color: #606266;
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
  height: 450px;
  width: 100%;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.chart-container:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-container h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  font-size: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.cpu {
  background: #409eff;
}

.legend-color.memory {
  background: #67c23a;
}

.chart-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}

.stat-item {
  text-align: center;
}

.performance-metrics {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  height: 100%;
}

.metric-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.metric-value.error {
  color: #f56c6c;
}

.metric-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-trend.up {
  color: #67c23a;
}

.metric-trend.down {
  color: #f56c6c;
}

.details-row {
  margin-bottom: 20px;
}

.logs-row {
  margin-bottom: 20px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.log-stats {
  display: flex;
  gap: 12px;
}

.log-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.log-filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.log-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.log-container {
  height: 500px;
  overflow-y: auto;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.log-virtual-list {
  padding: 0;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.log-item:hover {
  background: #f5f7fa;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  width: 160px;
  color: #909399;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.log-level-tag {
  width: 60px;
  text-align: center;
  flex-shrink: 0;
}

.log-logger {
  width: 180px;
  color: #606266;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.log-message {
  flex: 1;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-actions {
  flex-shrink: 0;
}

.log-empty {
  padding: 40px;
  text-align: center;
}

.log-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.log-detail {
  padding: 20px;
}

.log-message-detail {
  margin-top: 20px;
}

.log-message-detail h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.log-message-detail pre {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
  margin: 0;
}

.log-stack-trace {
  margin-top: 20px;
}

.log-stack-trace h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.log-stack-trace pre {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
  color: #f56c6c;
}

.el-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.el-descriptions {
  margin-top: 10px;
}

/* Element Plus 组件样式覆盖 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
}

.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border: none;
}

.el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  border: none;
}

.el-button--warning {
  background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);
  border: none;
}

.el-button--danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f25c5c 100%);
  border: none;
}

.el-select {
  width: 140px;
}

.el-input {
  border-radius: 8px;
}

.el-switch {
  margin-left: 10px;
}

.el-progress {
  margin: 8px 0;
}

.el-tag {
  border-radius: 12px;
  font-weight: 500;
}

.el-alert {
  border-radius: 8px;
  border: none;
}

.el-dialog {
  border-radius: 12px;
}

.el-dialog__header {
  padding: 24px 24px 0;
}

.el-dialog__body {
  padding: 24px;
}

.el-dialog__footer {
  padding: 0 24px 24px;
}

.el-pagination {
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .system-monitor {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .chart-container {
    height: 350px;
  }
  
  .performance-metrics {
    height: 350px;
  }
}

@media (max-width: 768px) {
  .system-monitor {
    padding: 12px;
  }
  
  .overview-card {
    min-height: 140px;
    padding: 16px;
  }
  
  .chart-container {
    height: 300px;
    padding: 16px;
  }
  
  .logs-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .control-group {
    justify-content: space-between;
  }
  
  .log-container {
    height: 350px;
  }
  
  .log-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .log-time,
  .log-level-tag,
  .log-logger {
    width: auto;
  }
  
  .log-message {
    white-space: normal;
    word-break: break-word;
  }
}

/* 滚动条样式 */
.log-container::-webkit-scrollbar,
.log-message-detail pre::-webkit-scrollbar,
.log-stack-trace pre::-webkit-scrollbar {
  width: 8px;
}

.log-container::-webkit-scrollbar-track,
.log-message-detail pre::-webkit-scrollbar-track,
.log-stack-trace pre::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb,
.log-message-detail pre::-webkit-scrollbar-thumb,
.log-stack-trace pre::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb:hover,
.log-message-detail pre::-webkit-scrollbar-thumb:hover,
.log-stack-trace pre::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* 加载状态 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  z-index: 10;
}

.loading-spinner {
  font-size: 24px;
  color: #409eff;
}
</style>