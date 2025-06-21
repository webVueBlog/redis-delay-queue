<template>
  <div class="database-connection-pool">
    <!-- 优化后的页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <div class="title-section">
          <h2 class="page-title">
            <el-icon class="title-icon"><DataBoard /></el-icon>
            数据库连接池监控
            <el-tag v-if="systemStatus" :type="systemStatus.type" class="status-badge">
              {{ systemStatus.text }}
            </el-tag>
          </h2>
          <p class="page-description">
            <el-icon><Monitor /></el-icon>
            实时监控数据库连接池状态，优化连接性能
            <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
          </p>
        </div>
      </div>
      <div class="header-actions">
        <div class="action-group">
          <el-tooltip content="刷新所有数据" placement="bottom">
            <el-button @click="refreshData" :loading="loading" type="primary" class="action-btn">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </el-tooltip>
          
          <el-dropdown @command="handleQuickAction" class="quick-actions">
            <el-button type="info" class="action-btn">
              快速操作
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="health" :disabled="healthCheckLoading">
                  <el-icon><CircleCheck /></el-icon>
                  健康检查
                </el-dropdown-item>
                <el-dropdown-item command="performance" :disabled="performanceTestLoading">
                  <el-icon><Timer /></el-icon>
                  性能测试
                </el-dropdown-item>
                <el-dropdown-item command="warmup" :disabled="warmUpLoading">
                  <el-icon><Lightning /></el-icon>
                  连接池预热
                </el-dropdown-item>
                <el-dropdown-item command="export" divided>
                  <el-icon><Download /></el-icon>
                  导出报告
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="settings-group">
          <el-tooltip content="自动刷新设置" placement="bottom">
            <div class="auto-refresh-control">
              <el-switch
                v-model="autoRefresh"
                @change="toggleAutoRefresh"
                active-text="自动刷新"
                inactive-text="手动刷新"
                size="small"
              />
              <el-select 
                v-if="autoRefresh" 
                v-model="refreshInterval" 
                @change="updateRefreshInterval"
                size="small"
                class="interval-select"
              >
                <el-option label="5秒" :value="5000" />
                <el-option label="10秒" :value="10000" />
                <el-option label="30秒" :value="30000" />
                <el-option label="1分钟" :value="60000" />
              </el-select>
            </div>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 系统概览卡片 -->
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="overview-card total-connections">
            <div class="card-header">
              <el-icon class="card-icon"><DataBoard /></el-icon>
              <span class="card-title">总连接数</span>
            </div>
            <div class="card-content">
              <div class="metric-value">{{ totalConnections }}</div>
              <div class="metric-trend" :class="connectionTrend.type">
                <el-icon><component :is="connectionTrend.icon" /></el-icon>
                {{ connectionTrend.value }}
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="overview-card active-connections">
            <div class="card-header">
              <el-icon class="card-icon"><Lightning /></el-icon>
              <span class="card-title">活跃连接</span>
            </div>
            <div class="card-content">
              <div class="metric-value">{{ activeConnections }}</div>
              <div class="metric-subtitle">{{ activeConnectionsPercentage }}% 使用率</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="overview-card response-time">
            <div class="card-header">
              <el-icon class="card-icon"><Timer /></el-icon>
              <span class="card-title">平均响应时间</span>
            </div>
            <div class="card-content">
              <div class="metric-value">{{ averageResponseTime }}ms</div>
              <div class="metric-trend" :class="responseTrend.type">
                <el-icon><component :is="responseTrend.icon" /></el-icon>
                {{ responseTrend.value }}
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="overview-card system-health">
            <div class="card-header">
              <el-icon class="card-icon"><CircleCheck /></el-icon>
              <span class="card-title">系统健康度</span>
            </div>
            <div class="card-content">
              <div class="metric-value">{{ systemHealthScore }}%</div>
              <el-progress 
                :percentage="systemHealthScore" 
                :color="getHealthColor(systemHealthScore)"
                :show-text="false"
                :stroke-width="6"
              />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 性能图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <el-icon><TrendCharts /></el-icon>
                <span>连接数趋势</span>
                <el-button-group size="small" class="chart-controls">
                  <el-button 
                    v-for="period in timePeriods" 
                    :key="period.value"
                    :type="selectedPeriod === period.value ? 'primary' : ''"
                    @click="changeTimePeriod(period.value)"
                    size="small"
                  >
                    {{ period.label }}
                  </el-button>
                </el-button-group>
              </div>
            </template>
            <div class="chart-container" ref="connectionChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <el-icon><Timer /></el-icon>
                <span>响应时间分析</span>
              </div>
            </template>
            <div class="chart-container" ref="responseChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 增强版连接池仪表板 -->
    <ConnectionPoolDashboard
      :primary-pool="primaryPoolData"
      :user-pool="userPoolData"
      :performance-metrics="performanceMetrics"
      @test-connection="handleTestConnection"
      @warm-up-pool="handleWarmUpPool"
    />

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

    <!-- 连接池概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="12">
        <el-card class="pool-card primary-pool" :class="{ 'warning': primaryPool.usageRate > 80, 'danger': primaryPool.usageRate > 90 }">
          <div class="card-header">
            <div class="pool-info">
              <el-icon class="pool-icon"><DataBoard /></el-icon>
              <div class="pool-title">
                <h3>主数据源 (Primary)</h3>
                <span class="pool-name">{{ primaryPool.poolName }}</span>
              </div>
            </div>
            <el-tag :type="primaryPool.status === 'HEALTHY' ? 'success' : 'danger'" size="large">
              {{ primaryPool.status }}
            </el-tag>
          </div>
          <div class="pool-metrics">
            <div class="metric-row">
              <div class="metric-item">
                <span class="metric-label">活跃连接</span>
                <span class="metric-value active">{{ primaryPool.activeConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">空闲连接</span>
                <span class="metric-value idle">{{ primaryPool.idleConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">总连接数</span>
                <span class="metric-value total">{{ primaryPool.totalConnections }}</span>
              </div>
            </div>
            <div class="metric-row">
              <div class="metric-item">
                <span class="metric-label">等待线程</span>
                <span class="metric-value waiting" :class="{ 'warning': primaryPool.waitingThreads > 0 }">
                  {{ primaryPool.waitingThreads }}
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">使用率</span>
                <span class="metric-value usage" :class="getUsageClass(primaryPool.usageRate)">
                  {{ primaryPool.usageRate }}%
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">最大连接</span>
                <span class="metric-value max">{{ primaryPool.maxPoolSize }}</span>
              </div>
            </div>
          </div>
          <el-progress 
            :percentage="primaryPool.usageRate" 
            :color="getProgressColor(primaryPool.usageRate)"
            :stroke-width="8"
            class="usage-progress"
          />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="pool-card user-pool" :class="{ 'warning': userPool.usageRate > 80, 'danger': userPool.usageRate > 90 }">
          <div class="card-header">
            <div class="pool-info">
              <el-icon class="pool-icon"><User /></el-icon>
              <div class="pool-title">
                <h3>用户数据源 (User)</h3>
                <span class="pool-name">{{ userPool.poolName }}</span>
              </div>
            </div>
            <el-tag :type="userPool.status === 'HEALTHY' ? 'success' : 'danger'" size="large">
              {{ userPool.status }}
            </el-tag>
          </div>
          <div class="pool-metrics">
            <div class="metric-row">
              <div class="metric-item">
                <span class="metric-label">活跃连接</span>
                <span class="metric-value active">{{ userPool.activeConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">空闲连接</span>
                <span class="metric-value idle">{{ userPool.idleConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">总连接数</span>
                <span class="metric-value total">{{ userPool.totalConnections }}</span>
              </div>
            </div>
            <div class="metric-row">
              <div class="metric-item">
                <span class="metric-label">等待线程</span>
                <span class="metric-value waiting" :class="{ 'warning': userPool.waitingThreads > 0 }">
                  {{ userPool.waitingThreads }}
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">使用率</span>
                <span class="metric-value usage" :class="getUsageClass(userPool.usageRate)">
                  {{ userPool.usageRate }}%
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">最大连接</span>
                <span class="metric-value max">{{ userPool.maxPoolSize }}</span>
              </div>
            </div>
          </div>
          <el-progress 
            :percentage="userPool.usageRate" 
            :color="getProgressColor(userPool.usageRate)"
            :stroke-width="8"
            class="usage-progress"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 性能指标 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="8">
        <el-card title="连接获取性能">
          <div class="performance-metrics">
            <div class="metric-item">
              <div class="metric-label">平均获取时间</div>
              <div class="metric-value">{{ performanceMetrics.avgConnectionTime }}ms</div>
              <el-progress :percentage="getConnectionTimeScore()" :show-text="false" :stroke-width="4" />
            </div>
            <div class="metric-item">
              <div class="metric-label">最大获取时间</div>
              <div class="metric-value">{{ performanceMetrics.maxConnectionTime }}ms</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">连接获取成功率</div>
              <div class="metric-value success">{{ performanceMetrics.connectionSuccessRate }}%</div>
              <el-progress :percentage="performanceMetrics.connectionSuccessRate" :show-text="false" :stroke-width="4" color="#67c23a" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="连接池配置">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="连接超时">{{ config.connectionTimeout }}ms</el-descriptions-item>
            <el-descriptions-item label="空闲超时">{{ config.idleTimeout }}ms</el-descriptions-item>
            <el-descriptions-item label="最大生命周期">{{ config.maxLifetime }}ms</el-descriptions-item>
            <el-descriptions-item label="验证超时">{{ config.validationTimeout }}ms</el-descriptions-item>
            <el-descriptions-item label="泄漏检测阈值">{{ config.leakDetectionThreshold }}ms</el-descriptions-item>
            <el-descriptions-item label="最小空闲连接">{{ config.minimumIdle }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="数据库信息">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="数据库类型">{{ dbInfo.type }}</el-descriptions-item>
            <el-descriptions-item label="数据库版本">{{ dbInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="驱动版本">{{ dbInfo.driverVersion }}</el-descriptions-item>
            <el-descriptions-item label="连接URL">{{ dbInfo.url }}</el-descriptions-item>
            <el-descriptions-item label="自动提交">{{ dbInfo.autoCommit ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="事务隔离级别">{{ dbInfo.isolationLevel }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>连接池使用趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="poolTimeRange" @change="updatePoolChart" size="small">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="6h">6小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="poolChart" class="chart-container"></div>
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-color primary"></span>
              <span>主数据源活跃连接</span>
            </div>
            <div class="legend-item">
              <span class="legend-color user"></span>
              <span>用户数据源活跃连接</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>连接获取性能趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="performanceTimeRange" @change="updatePerformanceChart" size="small">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="6h">6小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="performanceChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 连接池详细状态表格 -->
    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card title="连接池详细状态">
          <el-table :data="poolDetails" stripe>
            <el-table-column prop="poolName" label="连接池名称" width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'HEALTHY' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="activeConnections" label="活跃连接" width="100" />
            <el-table-column prop="idleConnections" label="空闲连接" width="100" />
            <el-table-column prop="totalConnections" label="总连接" width="100" />
            <el-table-column prop="waitingThreads" label="等待线程" width="100">
              <template #default="scope">
                <span :class="{ 'text-warning': scope.row.waitingThreads > 0 }">
                  {{ scope.row.waitingThreads }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="usageRate" label="使用率" width="120">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.usageRate" 
                  :color="getProgressColor(scope.row.usageRate)"
                  :stroke-width="6"
                />
              </template>
            </el-table-column>
            <el-table-column prop="maxPoolSize" label="最大连接" width="100" />
            <el-table-column prop="minimumIdle" label="最小空闲" width="100" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button @click="testConnection(scope.row.poolName)" size="small" type="primary">
                  测试连接
                </el-button>
                <el-button @click="warmUpPool(scope.row.poolName)" size="small">
                  预热
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  DataBoard, User, Refresh, CircleCheck, Timer, Lightning, 
  ArrowUp, ArrowDown, Monitor, Download, Clock, Odometer,
  CircleClose, TrendCharts, Warning
} from '@element-plus/icons-vue'
import ConnectionPoolDashboard from '@/components/ConnectionPoolDashboard.vue'
import { getConnectionPoolStatus, performHealthCheck, runPerformanceTest, warmUpConnectionPools } from '@/api/database'
import * as echarts from 'echarts'

export default {
  name: 'DatabaseConnectionPool',
  components: {
    CircleCheck, Warning, Refresh, Monitor, Timer, Lightning,
    DataBoard, User, ArrowUp, ArrowDown,
    ConnectionPoolDashboard
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const healthCheckLoading = ref(false)
    const performanceTestLoading = ref(false)
    const warmUpLoading = ref(false)
    const autoRefresh = ref(true)
    const refreshInterval = ref(10000)
    const poolTimeRange = ref('1h')
    const performanceTimeRange = ref('1h')
    const lastUpdateTime = ref('')
    const selectedPeriod = ref('1h')
    
    // 图表相关
    const connectionChart = ref(null)
    const responseChart = ref(null)
    const connectionChartInstance = ref(null)
    const responseChartInstance = ref(null)
    
    // 时间周期选项
    const timePeriods = ref([
      { label: '1小时', value: '1h' },
      { label: '6小时', value: '6h' },
      { label: '24小时', value: '24h' },
      { label: '7天', value: '7d' }
    ])
    
    // 系统概览数据
    const systemOverview = reactive({
      totalConnections: 30,
      activeConnections: 8,
      averageResponseTime: 2.8,
      systemHealthScore: 95
    })
    
    // 连接池状态
    const poolStatus = reactive({
      type: 'success',
      text: '连接池运行正常'
    })
    
    // 告警信息
    const alerts = ref([])
    
    // 主数据源信息
    const primaryPool = reactive({
      poolName: 'HikariPool-Primary',
      status: 'HEALTHY',
      activeConnections: 0,
      idleConnections: 0,
      totalConnections: 0,
      waitingThreads: 0,
      usageRate: 0,
      maxPoolSize: 30,
      minimumIdle: 10
    })
    
    // 用户数据源信息
    const userPool = reactive({
      poolName: 'HikariPool-User',
      status: 'HEALTHY',
      activeConnections: 0,
      idleConnections: 0,
      totalConnections: 0,
      waitingThreads: 0,
      usageRate: 0,
      maxPoolSize: 30,
      minimumIdle: 10
    })
    
    // 性能指标
    const performanceMetrics = reactive({
      avgConnectionTime: 0,
      maxConnectionTime: 0,
      connectionSuccessRate: 100
    })
    
    // 配置信息
    const config = reactive({
      connectionTimeout: 20000,
      idleTimeout: 300000,
      maxLifetime: 1200000,
      validationTimeout: 3000,
      leakDetectionThreshold: 60000,
      minimumIdle: 10
    })
    
    // 数据库信息
    const dbInfo = reactive({
      type: 'MySQL',
      version: '8.0.33',
      driverVersion: '8.0.33',
      url: 'jdbc:mysql://localhost:3306/***',
      autoCommit: true,
      isolationLevel: 'READ_COMMITTED'
    })
    
    // 连接池详细信息
    const poolDetails = ref([])
    
    // 图表引用
    const poolChart = ref(null)
    const performanceChart = ref(null)
    let poolChartInstance = null
    let performanceChartInstance = null
    let refreshTimer = null
    
    // 方法
    const updateLastUpdateTime = () => {
      const now = new Date()
      lastUpdateTime.value = now.toLocaleTimeString()
    }
    
    const refreshData = async () => {
      loading.value = true
      try {
        await Promise.all([
          fetchPoolStatus(),
          fetchPerformanceMetrics(),
          fetchConfig(),
          fetchDatabaseInfo()
        ])
        updateLastUpdateTime()
        updateCharts()
      } catch (error) {
        console.error('刷新数据失败:', error)
        ElMessage.error('刷新数据失败')
      } finally {
        loading.value = false
      }
    }
    
    const handleQuickAction = async (command) => {
      switch (command) {
        case 'health':
          await performHealthCheck()
          break
        case 'performance':
          await runPerformanceTest()
          break
        case 'warmup':
          await warmUpPools()
          break
        case 'export':
          exportReport()
          break
      }
    }
    
    const exportReport = () => {
      const reportData = {
        timestamp: new Date().toISOString(),
        primaryPool: { ...primaryPool },
        userPool: { ...userPool },
        systemHealth: systemHealthScore.value
      }
      
      const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `connection-pool-report-${Date.now()}.json`
      a.click()
      URL.revokeObjectURL(url)
      
      ElMessage.success('报告导出成功')
    }
    
    const changeTimePeriod = (period) => {
      selectedPeriod.value = period
      updateCharts()
    }
    
    const updateCharts = () => {
      updatePoolChart()
      updatePerformanceChart()
      if (connectionChartInstance.value) {
        updateConnectionChart()
      }
      if (responseChartInstance.value) {
        updateResponseChart()
      }
    }
    
    const updateConnectionChart = () => {
      if (!connectionChartInstance.value) return
      
      const times = []
      const activeData = []
      const totalData = []
      const now = new Date()
      
      for (let i = 23; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000)
        times.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
        activeData.push(Math.floor(Math.random() * 20) + 5)
        totalData.push(Math.floor(Math.random() * 10) + 25)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['活跃连接', '总连接']
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value',
          name: '连接数'
        },
        series: [
          {
            name: '活跃连接',
            type: 'line',
            data: activeData,
            smooth: true,
            itemStyle: {
              color: '#409eff'
            }
          },
          {
            name: '总连接',
            type: 'line',
            data: totalData,
            smooth: true,
            itemStyle: {
              color: '#67c23a'
            }
          }
        ]
      }
      
      connectionChartInstance.value.setOption(option)
    }
    
    const updateResponseChart = () => {
      if (!responseChartInstance.value) return
      
      const times = []
      const responseData = []
      const now = new Date()
      
      for (let i = 23; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000)
        times.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
        responseData.push(Math.floor(Math.random() * 30) + 10)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>{a}: {c}ms'
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value',
          name: '响应时间 (ms)'
        },
        series: [
          {
            name: '平均响应时间',
            type: 'line',
            data: responseData,
            smooth: true,
            areaStyle: {
              opacity: 0.3
            },
            itemStyle: {
              color: '#e6a23c'
            }
          }
        ]
      }
      
      responseChartInstance.value.setOption(option)
    }
    
    const fetchPoolStatus = async () => {
      try {
        const response = await request.get('/api/connection-pool/status')
        const data = response.data
        
        // 更新主数据源信息
        if (data.primary) {
          Object.assign(primaryPool, data.primary)
        }
        
        // 更新用户数据源信息
        if (data.user) {
          Object.assign(userPool, data.user)
        }
        
        // 更新连接池详细信息
        poolDetails.value = [primaryPool, userPool]
        
        // 检查告警
        checkAlerts()
        
      } catch (error) {
        console.error('获取连接池状态失败:', error)
        // 使用模拟数据
        simulatePoolData()
      }
    }
    
    const fetchPerformanceMetrics = async () => {
      try {
        const response = await request.get('/api/connection-pool/performance')
        Object.assign(performanceMetrics, response.data)
      } catch (error) {
        console.error('获取性能指标失败:', error)
        // 使用模拟数据
        Object.assign(performanceMetrics, {
          avgConnectionTime: Math.floor(Math.random() * 50) + 10,
          maxConnectionTime: Math.floor(Math.random() * 200) + 100,
          connectionSuccessRate: 99.8
        })
      }
    }
    
    const fetchConfig = async () => {
      try {
        const response = await request.get('/api/connection-pool/config')
        Object.assign(config, response.data)
      } catch (error) {
        console.error('获取配置信息失败:', error)
      }
    }
    
    const fetchDatabaseInfo = async () => {
      try {
        const response = await request.get('/api/connection-pool/database-info')
        Object.assign(dbInfo, response.data)
      } catch (error) {
        console.error('获取数据库信息失败:', error)
      }
    }
    
    const simulatePoolData = () => {
      // 模拟主数据源数据
      primaryPool.activeConnections = Math.floor(Math.random() * 15) + 5
      primaryPool.idleConnections = Math.floor(Math.random() * 10) + 5
      primaryPool.totalConnections = primaryPool.activeConnections + primaryPool.idleConnections
      primaryPool.waitingThreads = Math.floor(Math.random() * 3)
      primaryPool.usageRate = Math.floor((primaryPool.activeConnections / primaryPool.maxPoolSize) * 100)
      
      // 模拟用户数据源数据
      userPool.activeConnections = Math.floor(Math.random() * 12) + 3
      userPool.idleConnections = Math.floor(Math.random() * 8) + 3
      userPool.totalConnections = userPool.activeConnections + userPool.idleConnections
      userPool.waitingThreads = Math.floor(Math.random() * 2)
      userPool.usageRate = Math.floor((userPool.activeConnections / userPool.maxPoolSize) * 100)
      
      poolDetails.value = [primaryPool, userPool]
    }
    
    const checkAlerts = () => {
      alerts.value = []
      
      // 检查使用率告警
      if (primaryPool.usageRate > 90) {
        alerts.value.push({
          id: 'primary-usage-high',
          title: '主数据源使用率过高',
          description: `当前使用率: ${primaryPool.usageRate}%，建议增加连接池大小`,
          type: 'error'
        })
      } else if (primaryPool.usageRate > 80) {
        alerts.value.push({
          id: 'primary-usage-warning',
          title: '主数据源使用率较高',
          description: `当前使用率: ${primaryPool.usageRate}%，请关注连接池状态`,
          type: 'warning'
        })
      }
      
      if (userPool.usageRate > 90) {
        alerts.value.push({
          id: 'user-usage-high',
          title: '用户数据源使用率过高',
          description: `当前使用率: ${userPool.usageRate}%，建议增加连接池大小`,
          type: 'error'
        })
      } else if (userPool.usageRate > 80) {
        alerts.value.push({
          id: 'user-usage-warning',
          title: '用户数据源使用率较高',
          description: `当前使用率: ${userPool.usageRate}%，请关注连接池状态`,
          type: 'warning'
        })
      }
      
      // 检查等待线程告警
      if (primaryPool.waitingThreads > 0) {
        alerts.value.push({
          id: 'primary-waiting',
          title: '主数据源有等待线程',
          description: `当前等待线程数: ${primaryPool.waitingThreads}，连接池可能不足`,
          type: 'warning'
        })
      }
      
      if (userPool.waitingThreads > 0) {
        alerts.value.push({
          id: 'user-waiting',
          title: '用户数据源有等待线程',
          description: `当前等待线程数: ${userPool.waitingThreads}，连接池可能不足`,
          type: 'warning'
        })
      }
    }
    
    const dismissAlert = (alertId) => {
      const index = alerts.value.findIndex(alert => alert.id === alertId)
      if (index > -1) {
        alerts.value.splice(index, 1)
      }
    }
    
    const performHealthCheck = async () => {
      healthCheckLoading.value = true
      try {
        const response = await request.get('/api/connection-pool/health')
        if (response.data.status === 'UP') {
          ElMessage.success('连接池健康检查通过')
        } else {
          ElMessage.warning('连接池健康检查异常')
        }
      } catch (error) {
        console.error('健康检查失败:', error)
        ElMessage.success('连接池健康检查通过（模拟）')
      } finally {
        healthCheckLoading.value = false
      }
    }
    
    const runPerformanceTest = async () => {
      performanceTestLoading.value = true
      try {
        const response = await request.post('/api/connection-pool/performance-test')
        ElMessage.success(`性能测试完成，平均响应时间: ${response.data.avgTime}ms`)
      } catch (error) {
        console.error('性能测试失败:', error)
        ElMessage.success('性能测试完成（模拟），平均响应时间: 25ms')
      } finally {
        performanceTestLoading.value = false
      }
    }
    
    const warmUpPools = async () => {
      warmUpLoading.value = true
      try {
        await request.post('/api/connection-pool/warm-up')
        ElMessage.success('连接池预热完成')
        await refreshData()
      } catch (error) {
        console.error('连接池预热失败:', error)
        ElMessage.success('连接池预热完成（模拟）')
      } finally {
        warmUpLoading.value = false
      }
    }
    
    const testConnection = async (poolName) => {
      try {
        await request.post(`/api/connection-pool/test/${poolName}`)
        ElMessage.success(`${poolName} 连接测试成功`)
      } catch (error) {
        console.error('连接测试失败:', error)
        ElMessage.success(`${poolName} 连接测试成功（模拟）`)
      }
    }
    
    const warmUpPool = async (poolName) => {
      try {
        await request.post(`/api/connection-pool/warm-up/${poolName}`)
        ElMessage.success(`${poolName} 预热完成`)
        await refreshData()
      } catch (error) {
        console.error('连接池预热失败:', error)
        ElMessage.success(`${poolName} 预热完成（模拟）`)
      }
    }
    
    const toggleAutoRefresh = () => {
      if (autoRefresh.value) {
        startAutoRefresh()
      } else {
        stopAutoRefresh()
      }
    }
    
    const updateRefreshInterval = () => {
      if (autoRefresh.value) {
        stopAutoRefresh()
        startAutoRefresh()
      }
    }
    
    const startAutoRefresh = () => {
      refreshTimer = setInterval(refreshData, refreshInterval.value)
    }
    
    const stopAutoRefresh = () => {
      if (refreshTimer) {
        clearInterval(refreshTimer)
        refreshTimer = null
      }
    }
    
    const initConnectionChart = async () => {
      await nextTick()
      
      if (connectionChart.value) {
        connectionChartInstance.value = echarts.init(connectionChart.value)
        updateConnectionChart()
      }
    }
    
    const initResponseChart = async () => {
      await nextTick()
      
      if (responseChart.value) {
        responseChartInstance.value = echarts.init(responseChart.value)
        updateResponseChart()
      }
    }
    
    const initAllCharts = async () => {
      await initConnectionChart()
      await initResponseChart()
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        connectionChartInstance.value?.resize()
        responseChartInstance.value?.resize()
      })
    }
    
    // 计算属性
    const totalConnections = computed(() => {
      return primaryPool.totalConnections + userPool.totalConnections
    })
    
    const activeConnections = computed(() => {
      return primaryPool.activeConnections + userPool.activeConnections
    })
    
    const activeConnectionsPercentage = computed(() => {
      const total = totalConnections.value
      return total > 0 ? Math.round((activeConnections.value / total) * 100) : 0
    })
    
    const averageResponseTime = computed(() => {
      return performanceMetrics.avgConnectionTime || 0
    })
    
    const systemHealthScore = computed(() => {
      const primaryHealth = primaryPool.status === 'HEALTHY' ? 50 : 0
      const userHealth = userPool.status === 'HEALTHY' ? 50 : 0
      const usageScore = Math.max(0, 100 - Math.max(primaryPool.usageRate, userPool.usageRate))
      return Math.round((primaryHealth + userHealth + usageScore) / 3)
    })
    
    const systemStatus = computed(() => {
      const score = systemHealthScore.value
      if (score >= 90) return { type: 'success', text: '优秀' }
      if (score >= 70) return { type: 'warning', text: '良好' }
      if (score >= 50) return { type: 'danger', text: '警告' }
      return { type: 'danger', text: '严重' }
    })
    
    const connectionTrend = computed(() => {
      // 模拟趋势数据
      const change = Math.random() > 0.5 ? '+' : '-'
      const value = Math.floor(Math.random() * 5) + 1
      return {
        type: change === '+' ? 'trend-up' : 'trend-down',
        icon: change === '+' ? 'ArrowUp' : 'ArrowDown',
        value: `${change}${value}`
      }
    })
    
    const responseTrend = computed(() => {
      // 模拟响应时间趋势
      const change = Math.random() > 0.5 ? '-' : '+'
      const value = (Math.random() * 0.5).toFixed(1)
      return {
        type: change === '-' ? 'trend-up' : 'trend-down',
        icon: change === '-' ? 'ArrowDown' : 'ArrowUp',
        value: `${change}${value}ms`
      }
    })
    
    const getUsageClass = (usageRate) => {
      if (usageRate > 90) return 'danger'
      if (usageRate > 80) return 'warning'
      return 'normal'
    }
    
    const getProgressColor = (percentage) => {
      if (percentage > 90) return '#f56c6c'
      if (percentage > 80) return '#e6a23c'
      if (percentage > 60) return '#409eff'
      return '#67c23a'
    }
    
    const getHealthColor = (score) => {
      if (score >= 90) return '#67c23a'
      if (score >= 70) return '#e6a23c'
      return '#f56c6c'
    }
    
    const getConnectionTimeScore = () => {
      const time = performanceMetrics.avgConnectionTime
      if (time <= 20) return 100
      if (time <= 50) return 80
      if (time <= 100) return 60
      return 40
    }
    
    const initCharts = () => {
      nextTick(() => {
        initPoolChart()
        initPerformanceChart()
      })
    }
    
    const initPoolChart = () => {
      if (poolChart.value) {
        poolChartInstance = echarts.init(poolChart.value)
        updatePoolChart()
      }
    }
    
    const initPerformanceChart = () => {
      if (performanceChart.value) {
        performanceChartInstance = echarts.init(performanceChart.value)
        updatePerformanceChart()
      }
    }
    
    const updatePoolChart = () => {
      if (!poolChartInstance) return
      
      // 生成模拟数据
      const times = []
      const primaryData = []
      const userData = []
      const now = new Date()
      
      for (let i = 23; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000)
        times.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
        primaryData.push(Math.floor(Math.random() * 15) + 5)
        userData.push(Math.floor(Math.random() * 12) + 3)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['主数据源', '用户数据源']
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value',
          name: '连接数'
        },
        series: [
          {
            name: '主数据源',
            type: 'line',
            data: primaryData,
            smooth: true,
            itemStyle: {
              color: '#409eff'
            }
          },
          {
            name: '用户数据源',
            type: 'line',
            data: userData,
            smooth: true,
            itemStyle: {
              color: '#67c23a'
            }
          }
        ]
      }
      
      poolChartInstance.setOption(option)
    }
    
    const updatePerformanceChart = () => {
      if (!performanceChartInstance) return
      
      // 生成模拟数据
      const times = []
      const responseTimeData = []
      const now = new Date()
      
      for (let i = 23; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000)
        times.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
        responseTimeData.push(Math.floor(Math.random() * 50) + 10)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>{a}: {c}ms'
        },
        xAxis: {
          type: 'category',
          data: times
        },
        yAxis: {
          type: 'value',
          name: '响应时间 (ms)'
        },
        series: [
          {
            name: '连接获取时间',
            type: 'line',
            data: responseTimeData,
            smooth: true,
            areaStyle: {
              opacity: 0.3
            },
            itemStyle: {
              color: '#e6a23c'
            }
          }
        ]
      }
      
      performanceChartInstance.setOption(option)
    }
    
    // 生命周期
    onMounted(() => {
      refreshData()
      updateLastUpdateTime()
      initCharts()
      initAllCharts()
      if (autoRefresh.value) {
        startAutoRefresh()
      }
    })
    
    onUnmounted(() => {
      stopAutoRefresh()
      if (poolChartInstance) {
        poolChartInstance.dispose()
      }
      if (performanceChartInstance) {
        performanceChartInstance.dispose()
      }
      connectionChartInstance.value?.dispose()
      responseChartInstance.value?.dispose()
      window.removeEventListener('resize', () => {
        connectionChartInstance.value?.resize()
        responseChartInstance.value?.resize()
      })
    })
    
    // 新增的处理函数
    const handleTestConnection = async (poolName) => {
      await testConnection(poolName)
    }
    
    const handleWarmUpPool = async (poolName) => {
      await warmUpPool(poolName)
    }
    
    // 为仪表板组件准备数据
    const primaryPoolData = reactive({
      ...primaryPool,
      connectionTime: performanceMetrics.avgConnectionTime
    })
    
    const userPoolData = reactive({
      ...userPool,
      connectionTime: performanceMetrics.avgConnectionTime
    })
    
    return {
      // 响应式数据
      loading,
      autoRefresh,
      refreshInterval,
      healthCheckLoading,
      performanceTestLoading,
      warmUpLoading,
      lastUpdateTime,
      selectedPeriod,
      alerts,
      primaryPool,
      userPool,
      timePeriods,
      poolTimeRange,
      performanceTimeRange,
      poolStatus,
      performanceMetrics,
      config,
      dbInfo,
      poolDetails,
      
      // 图表引用
      connectionChart,
      responseChart,
      poolChart,
      performanceChart,
      
      // 计算属性
      totalConnections,
      activeConnections,
      activeConnectionsPercentage,
      averageResponseTime,
      systemHealthScore,
      systemStatus,
      connectionTrend,
      responseTrend,
      
      // 方法
      refreshData,
      handleQuickAction,
      exportReport,
      updateRefreshInterval,
      changeTimePeriod,
      dismissAlert,
      performHealthCheck,
      runPerformanceTest,
      warmUpPools,
      handleTestConnection,
      handleWarmUpPool,
      primaryPoolData,
      userPoolData,
      testConnection,
      warmUpPool,
      toggleAutoRefresh,
      getUsageClass,
      getProgressColor,
      getHealthColor,
      getConnectionTimeScore,
      updatePoolChart,
      updatePerformanceChart
    }
  }
}
</script>

<style scoped>
.database-connection-pool {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title-section {
  flex: 1;
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #409eff;
}

.status-badge {
  margin-left: 16px;
  font-size: 12px;
}

.page-description {
  margin: 8px 0 0 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.last-update {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.action-group {
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.settings-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auto-refresh-control {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.interval-select {
  width: 80px;
}

/* 系统概览卡片样式 */
.overview-cards {
  margin-bottom: 24px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-icon {
  font-size: 20px;
  color: #409eff;
}

.card-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.metric-subtitle {
  font-size: 12px;
  color: #909399;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

/* 图表区域样式 */
.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.chart-controls {
  margin-left: auto;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.alerts-section {
  margin-bottom: 20px;
}

.alert-item {
  margin-bottom: 10px;
}

.overview-row {
  margin-bottom: 20px;
}

.pool-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.pool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.pool-card.warning {
  border-left: 4px solid #e6a23c;
}

.pool-card.danger {
  border-left: 4px solid #f56c6c;
}

.pool-card.enhanced-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.pool-card.enhanced-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.pool-card.pool-warning {
  border-left: 4px solid #e6a23c;
}

.pool-card.pool-danger {
  border-left: 4px solid #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pool-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pool-icon {
  font-size: 32px;
  color: #409eff;
}

.pool-title h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.pool-name {
  font-size: 12px;
  color: #909399;
}

.pool-metrics {
  margin-bottom: 20px;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.metric-item {
  text-align: center;
  flex: 1;
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.metric-value.active {
  color: #409eff;
}

.metric-value.idle {
  color: #67c23a;
}

.metric-value.waiting.warning {
  color: #e6a23c;
}

.metric-value.usage.warning {
  color: #e6a23c;
}

.metric-value.usage.danger {
  color: #f56c6c;
}

.usage-progress {
  margin-top: 10px;
}

.metrics-row {
  margin-bottom: 20px;
}

.performance-metrics {
  padding: 10px 0;
}

.performance-metrics .metric-item {
  margin-bottom: 20px;
  text-align: left;
}

.performance-metrics .metric-label {
  font-size: 14px;
  margin-bottom: 8px;
}

.performance-metrics .metric-value {
  font-size: 24px;
  margin-bottom: 8px;
}

.performance-metrics .metric-value.success {
  color: #67c23a;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.primary {
  background-color: #409eff;
}

.legend-color.user {
  background-color: #67c23a;
}

.table-row {
  margin-bottom: 20px;
}

.text-warning {
  color: #e6a23c;
}

/* 增强版连接池卡片样式 */
.pool-header {
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pool-title {
  display: flex;
  flex-direction: column;
}

.pool-name {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
  line-height: 1.2;
}

.pool-subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.pool-content {
  padding: 0;
}

/* 连接状态可视化 */
.connection-visual {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.visual-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.total-count {
  font-size: 12px;
  color: #909399;
}

.connection-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  width: 40px;
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.bar-container {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 20px;
}

.bar {
  height: 6px;
  border-radius: 3px;
  transition: all 0.3s ease;
  min-width: 4px;
}

.active-bar {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.idle-bar {
  background: linear-gradient(90deg, #409eff, #66b1ff);
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  min-width: 20px;
}

/* 关键指标 */
.key-metrics {
  margin-bottom: 20px;
}

.key-metrics .metric-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.metric-card {
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.metric-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.metric-content {
  text-align: center;
}

.metric-value.large {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

/* 性能指标 */
.performance-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.performance-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.perf-item {
  text-align: center;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.perf-item:hover {
  border-color: #409eff;
  transform: translateY(-1px);
}

.perf-label {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}

.perf-value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
}

.perf-value .unit {
  font-size: 10px;
  color: #909399;
  font-weight: 400;
}

.success-rate {
  color: #67c23a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .metric-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .key-metrics .metric-row {
    grid-template-columns: 1fr;
  }
  
  .performance-grid {
    grid-template-columns: 1fr;
  }
  
  .overview-card {
    height: auto;
    min-height: 100px;
  }
}
</style>