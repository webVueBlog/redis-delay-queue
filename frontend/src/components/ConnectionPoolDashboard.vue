<template>
  <div class="connection-pool-dashboard">
    <!-- 实时状态卡片 -->
    <div class="status-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="status-card" :class="getCardClass('primary')">
            <div class="card-content">
              <div class="card-icon primary">
                <el-icon><DataBoard /></el-icon>
              </div>
              <div class="card-info">
                <h3>{{ primaryPool.activeConnections }}</h3>
                <p>主数据源活跃连接</p>
                <div class="card-trend">
                  <el-icon :class="getTrendClass(primaryTrend)">{{ getTrendIcon(primaryTrend) }}</el-icon>
                  <span>{{ primaryTrend }}%</span>
                </div>
              </div>
            </div>
            <div class="card-progress">
              <el-progress 
                :percentage="primaryPool.usageRate" 
                :color="getProgressColor(primaryPool.usageRate)"
                :show-text="false"
                :stroke-width="4"
              />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="status-card" :class="getCardClass('user')">
            <div class="card-content">
              <div class="card-icon user">
                <el-icon><User /></el-icon>
              </div>
              <div class="card-info">
                <h3>{{ userPool.activeConnections }}</h3>
                <p>用户数据源活跃连接</p>
                <div class="card-trend">
                  <el-icon :class="getTrendClass(userTrend)">{{ getTrendIcon(userTrend) }}</el-icon>
                  <span>{{ userTrend }}%</span>
                </div>
              </div>
            </div>
            <div class="card-progress">
              <el-progress 
                :percentage="userPool.usageRate" 
                :color="getProgressColor(userPool.usageRate)"
                :show-text="false"
                :stroke-width="4"
              />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="status-card performance">
            <div class="card-content">
              <div class="card-icon performance">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="card-info">
                <h3>{{ performanceMetrics.avgConnectionTime }}ms</h3>
                <p>平均连接时间</p>
                <div class="card-trend">
                  <el-icon :class="getTrendClass(performanceTrend)">{{ getTrendIcon(performanceTrend) }}</el-icon>
                  <span>{{ Math.abs(performanceTrend) }}%</span>
                </div>
              </div>
            </div>
            <div class="card-progress">
              <el-progress 
                :percentage="getPerformanceScore()" 
                :color="getPerformanceColor()"
                :show-text="false"
                :stroke-width="4"
              />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="status-card health">
            <div class="card-content">
              <div class="card-icon health">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="card-info">
                <h3>{{ healthScore }}%</h3>
                <p>连接池健康度</p>
                <div class="card-status">
                  <el-tag :type="getHealthType()" size="small">
                    {{ getHealthText() }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="card-progress">
              <el-progress 
                :percentage="healthScore" 
                :color="getHealthColor()"
                :show-text="false"
                :stroke-width="4"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时图表 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="chart-header">
                <span>连接池实时监控</span>
                <div class="chart-controls">
                  <el-button-group size="small">
                    <el-button 
                      v-for="range in timeRanges" 
                      :key="range.value"
                      :type="selectedTimeRange === range.value ? 'primary' : ''"
                      @click="changeTimeRange(range.value)"
                    >
                      {{ range.label }}
                    </el-button>
                  </el-button-group>
                  <el-button @click="toggleRealTime" :type="realTimeEnabled ? 'success' : ''" size="small">
                    <el-icon><VideoPlay v-if="!realTimeEnabled" /><VideoPause v-else /></el-icon>
                    {{ realTimeEnabled ? '暂停' : '实时' }}
                  </el-button>
                </div>
              </div>
            </template>
            <div ref="realTimeChart" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>连接池分布</span>
            </template>
            <div ref="distributionChart" class="chart-container small"></div>
            <div class="distribution-legend">
              <div class="legend-item">
                <span class="legend-dot active"></span>
                <span>活跃连接</span>
                <strong>{{ totalActiveConnections }}</strong>
              </div>
              <div class="legend-item">
                <span class="legend-dot idle"></span>
                <span>空闲连接</span>
                <strong>{{ totalIdleConnections }}</strong>
              </div>
              <div class="legend-item">
                <span class="legend-dot waiting"></span>
                <span>等待线程</span>
                <strong>{{ totalWaitingThreads }}</strong>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细指标表格 -->
    <div class="metrics-table">
      <el-card>
        <template #header>
          <div class="table-header">
            <span>连接池详细指标</span>
            <div class="table-actions">
              <el-input
                v-model="searchText"
                placeholder="搜索连接池"
                size="small"
                style="width: 200px;"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button @click="exportMetrics" size="small">
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="filteredPoolData" 
          stripe 
          :default-sort="{ prop: 'usageRate', order: 'descending' }"
          @sort-change="handleSortChange"
        >
          <el-table-column prop="poolName" label="连接池名称" width="180" sortable>
            <template #default="scope">
              <div class="pool-name-cell">
                <el-icon class="pool-icon" :class="scope.row.type">
                  <DataBoard v-if="scope.row.type === 'primary'" />
                  <User v-else />
                </el-icon>
                <span>{{ scope.row.poolName }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100" sortable>
            <template #default="scope">
              <el-tag :type="scope.row.status === 'HEALTHY' ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 'HEALTHY' ? '健康' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="activeConnections" label="活跃连接" width="100" sortable>
            <template #default="scope">
              <div class="metric-cell">
                <span class="metric-value">{{ scope.row.activeConnections }}</span>
                <el-progress 
                  :percentage="(scope.row.activeConnections / scope.row.maxPoolSize) * 100" 
                  :show-text="false"
                  :stroke-width="3"
                  color="#409eff"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="idleConnections" label="空闲连接" width="100" sortable>
            <template #default="scope">
              <div class="metric-cell">
                <span class="metric-value">{{ scope.row.idleConnections }}</span>
                <el-progress 
                  :percentage="(scope.row.idleConnections / scope.row.maxPoolSize) * 100" 
                  :show-text="false"
                  :stroke-width="3"
                  color="#67c23a"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="waitingThreads" label="等待线程" width="100" sortable>
            <template #default="scope">
              <span :class="{ 'text-warning': scope.row.waitingThreads > 0, 'text-danger': scope.row.waitingThreads > 5 }">
                {{ scope.row.waitingThreads }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="usageRate" label="使用率" width="120" sortable>
            <template #default="scope">
              <div class="usage-cell">
                <span class="usage-text" :class="getUsageClass(scope.row.usageRate)">
                  {{ scope.row.usageRate }}%
                </span>
                <el-progress 
                  :percentage="scope.row.usageRate" 
                  :color="getProgressColor(scope.row.usageRate)"
                  :stroke-width="6"
                  :show-text="false"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="avgConnectionTime" label="平均连接时间" width="120" sortable>
            <template #default="scope">
              <span :class="getConnectionTimeClass(scope.row.avgConnectionTime)">
                {{ scope.row.avgConnectionTime }}ms
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button-group size="small">
                <el-button @click="testConnection(scope.row)" type="primary">
                  <el-icon><Connection /></el-icon>
                  测试
                </el-button>
                <el-button @click="warmUpPool(scope.row)">
                  <el-icon><Lightning /></el-icon>
                  预热
                </el-button>
                <el-button @click="showPoolDetails(scope.row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 连接池详情对话框 -->
    <el-dialog
      v-model="showDetailsDialog"
      :title="`${selectedPool?.poolName} 详细信息`"
      width="800px"
    >
      <div v-if="selectedPool" class="pool-details">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card title="基本信息" shadow="never">
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="连接池名称">{{ selectedPool.poolName }}</el-descriptions-item>
                <el-descriptions-item label="连接池类型">{{ selectedPool.type === 'primary' ? '主数据源' : '用户数据源' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="selectedPool.status === 'HEALTHY' ? 'success' : 'danger'">
                    {{ selectedPool.status === 'HEALTHY' ? '健康' : '异常' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ selectedPool.createTime || '2024-01-01 10:00:00' }}</el-descriptions-item>
                <el-descriptions-item label="最后检查时间">{{ selectedPool.lastCheckTime || new Date().toLocaleString() }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card title="连接配置" shadow="never">
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="最大连接数">{{ selectedPool.maxPoolSize }}</el-descriptions-item>
                <el-descriptions-item label="最小空闲连接">{{ selectedPool.minimumIdle }}</el-descriptions-item>
                <el-descriptions-item label="连接超时">{{ selectedPool.connectionTimeout }}ms</el-descriptions-item>
                <el-descriptions-item label="空闲超时">{{ selectedPool.idleTimeout }}ms</el-descriptions-item>
                <el-descriptions-item label="最大生命周期">{{ selectedPool.maxLifetime }}ms</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
        
        <el-card title="实时指标" shadow="never" style="margin-top: 20px;">
          <div class="real-time-metrics">
            <div class="metric-group">
              <div class="metric-item">
                <span class="metric-label">活跃连接</span>
                <span class="metric-value active">{{ selectedPool.activeConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">空闲连接</span>
                <span class="metric-value idle">{{ selectedPool.idleConnections }}</span>
              </div>
              <div class="metric-item">
                <span class="metric-label">总连接数</span>
                <span class="metric-value total">{{ selectedPool.totalConnections }}</span>
              </div>
            </div>
            
            <div class="metric-group">
              <div class="metric-item">
                <span class="metric-label">等待线程</span>
                <span class="metric-value waiting" :class="{ 'warning': selectedPool.waitingThreads > 0 }">
                  {{ selectedPool.waitingThreads }}
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">使用率</span>
                <span class="metric-value usage" :class="getUsageClass(selectedPool.usageRate)">
                  {{ selectedPool.usageRate }}%
                </span>
              </div>
              <div class="metric-item">
                <span class="metric-label">平均连接时间</span>
                <span class="metric-value time" :class="getConnectionTimeClass(selectedPool.avgConnectionTime)">
                  {{ selectedPool.avgConnectionTime }}ms
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="showDetailsDialog = false">关闭</el-button>
        <el-button @click="testConnection(selectedPool)" type="primary">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataBoard, User, Timer, CircleCheck, VideoPlay, VideoPause,
  Search, Download, Connection, Lightning, View
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

export default {
  name: 'ConnectionPoolDashboard',
  components: {
    DataBoard, User, Timer, CircleCheck, VideoPlay, VideoPause,
    Search, Download, Connection, Lightning, View
  },
  props: {
    primaryPool: {
      type: Object,
      default: () => ({})
    },
    userPool: {
      type: Object,
      default: () => ({})
    },
    performanceMetrics: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['test-connection', 'warm-up-pool'],
  setup(props, { emit }) {
    // 响应式数据
    const realTimeChart = ref(null)
    const distributionChart = ref(null)
    const searchText = ref('')
    const selectedTimeRange = ref('1h')
    const realTimeEnabled = ref(true)
    const showDetailsDialog = ref(false)
    const selectedPool = ref(null)
    
    // 趋势数据
    const primaryTrend = ref(2.5)
    const userTrend = ref(-1.2)
    const performanceTrend = ref(-3.1)
    
    // 时间范围选项
    const timeRanges = [
      { label: '1小时', value: '1h' },
      { label: '6小时', value: '6h' },
      { label: '24小时', value: '24h' },
      { label: '7天', value: '7d' }
    ]
    
    // 图表实例
    let realTimeChartInstance = null
    let distributionChartInstance = null
    let realTimeTimer = null
    
    // 计算属性
    const healthScore = computed(() => {
      const primaryHealth = props.primaryPool.usageRate < 90 ? 100 : 60
      const userHealth = props.userPool.usageRate < 90 ? 100 : 60
      const performanceHealth = props.performanceMetrics.avgConnectionTime < 50 ? 100 : 70
      return Math.floor((primaryHealth + userHealth + performanceHealth) / 3)
    })
    
    const totalActiveConnections = computed(() => {
      return (props.primaryPool.activeConnections || 0) + (props.userPool.activeConnections || 0)
    })
    
    const totalIdleConnections = computed(() => {
      return (props.primaryPool.idleConnections || 0) + (props.userPool.idleConnections || 0)
    })
    
    const totalWaitingThreads = computed(() => {
      return (props.primaryPool.waitingThreads || 0) + (props.userPool.waitingThreads || 0)
    })
    
    const filteredPoolData = computed(() => {
      const pools = [
        { ...props.primaryPool, type: 'primary', avgConnectionTime: Math.floor(Math.random() * 30) + 10 },
        { ...props.userPool, type: 'user', avgConnectionTime: Math.floor(Math.random() * 25) + 8 }
      ]
      
      if (!searchText.value) return pools
      
      return pools.filter(pool => 
        pool.poolName?.toLowerCase().includes(searchText.value.toLowerCase())
      )
    })
    
    // 方法
    const getCardClass = (type) => {
      const pool = type === 'primary' ? props.primaryPool : props.userPool
      if (pool.usageRate > 90) return 'danger'
      if (pool.usageRate > 80) return 'warning'
      return 'normal'
    }
    
    const getTrendClass = (trend) => {
      if (trend > 0) return 'trend-up'
      if (trend < 0) return 'trend-down'
      return 'trend-stable'
    }
    
    const getTrendIcon = (trend) => {
      if (trend > 0) return '↗'
      if (trend < 0) return '↘'
      return '→'
    }
    
    const getProgressColor = (percentage) => {
      if (percentage > 90) return '#f56c6c'
      if (percentage > 80) return '#e6a23c'
      if (percentage > 60) return '#409eff'
      return '#67c23a'
    }
    
    const getPerformanceScore = () => {
      const time = props.performanceMetrics.avgConnectionTime || 0
      if (time <= 20) return 100
      if (time <= 50) return 80
      if (time <= 100) return 60
      return 40
    }
    
    const getPerformanceColor = () => {
      const score = getPerformanceScore()
      if (score >= 80) return '#67c23a'
      if (score >= 60) return '#409eff'
      if (score >= 40) return '#e6a23c'
      return '#f56c6c'
    }
    
    const getHealthType = () => {
      if (healthScore.value >= 90) return 'success'
      if (healthScore.value >= 70) return 'warning'
      return 'danger'
    }
    
    const getHealthText = () => {
      if (healthScore.value >= 90) return '优秀'
      if (healthScore.value >= 70) return '良好'
      if (healthScore.value >= 50) return '一般'
      return '较差'
    }
    
    const getHealthColor = () => {
      if (healthScore.value >= 90) return '#67c23a'
      if (healthScore.value >= 70) return '#409eff'
      if (healthScore.value >= 50) return '#e6a23c'
      return '#f56c6c'
    }
    
    const getUsageClass = (usageRate) => {
      if (usageRate > 90) return 'danger'
      if (usageRate > 80) return 'warning'
      return 'normal'
    }
    
    const getConnectionTimeClass = (time) => {
      if (time > 100) return 'text-danger'
      if (time > 50) return 'text-warning'
      return 'text-success'
    }
    
    const changeTimeRange = (range) => {
      selectedTimeRange.value = range
      updateRealTimeChart()
    }
    
    const toggleRealTime = () => {
      realTimeEnabled.value = !realTimeEnabled.value
      if (realTimeEnabled.value) {
        startRealTimeUpdate()
      } else {
        stopRealTimeUpdate()
      }
    }
    
    const testConnection = (pool) => {
      emit('test-connection', pool)
    }
    
    const warmUpPool = (pool) => {
      emit('warm-up-pool', pool)
    }
    
    const showPoolDetails = (pool) => {
      selectedPool.value = {
        ...pool,
        connectionTimeout: 20000,
        idleTimeout: 300000,
        maxLifetime: 1200000,
        createTime: '2024-01-01 10:00:00',
        lastCheckTime: new Date().toLocaleString()
      }
      showDetailsDialog.value = true
    }
    
    const exportMetrics = () => {
      // 导出功能实现
      const data = filteredPoolData.value
      const csv = [
        ['连接池名称', '状态', '活跃连接', '空闲连接', '等待线程', '使用率', '平均连接时间'],
        ...data.map(pool => [
          pool.poolName,
          pool.status,
          pool.activeConnections,
          pool.idleConnections,
          pool.waitingThreads,
          pool.usageRate + '%',
          pool.avgConnectionTime + 'ms'
        ])
      ].map(row => row.join(',')).join('\n')
      
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `connection_pool_metrics_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      
      ElMessage.success('数据导出成功')
    }
    
    const handleSortChange = ({ prop, order }) => {
      // 排序处理逻辑
      console.log('排序:', prop, order)
    }
    
    const initCharts = () => {
      nextTick(() => {
        initRealTimeChart()
        initDistributionChart()
        if (realTimeEnabled.value) {
          startRealTimeUpdate()
        }
      })
    }
    
    const initRealTimeChart = () => {
      if (realTimeChart.value) {
        realTimeChartInstance = echarts.init(realTimeChart.value)
        updateRealTimeChart()
      }
    }
    
    const initDistributionChart = () => {
      if (distributionChart.value) {
        distributionChartInstance = echarts.init(distributionChart.value)
        updateDistributionChart()
      }
    }
    
    const updateRealTimeChart = () => {
      if (!realTimeChartInstance) return
      
      // 生成模拟实时数据
      const times = []
      const primaryData = []
      const userData = []
      const now = new Date()
      
      const points = selectedTimeRange.value === '1h' ? 60 : 
                    selectedTimeRange.value === '6h' ? 72 : 
                    selectedTimeRange.value === '24h' ? 96 : 168
      
      const interval = selectedTimeRange.value === '1h' ? 60000 : 
                      selectedTimeRange.value === '6h' ? 300000 : 
                      selectedTimeRange.value === '24h' ? 900000 : 3600000
      
      for (let i = points - 1; i >= 0; i--) {
        const time = new Date(now.getTime() - i * interval)
        times.push(time.toLocaleTimeString('zh-CN', { 
          hour: '2-digit', 
          minute: '2-digit',
          ...(selectedTimeRange.value !== '1h' && { month: '2-digit', day: '2-digit' })
        }))
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
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: times,
          axisLabel: {
            rotate: selectedTimeRange.value === '1h' ? 0 : 45
          }
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
            },
            areaStyle: {
              opacity: 0.3
            }
          },
          {
            name: '用户数据源',
            type: 'line',
            data: userData,
            smooth: true,
            itemStyle: {
              color: '#67c23a'
            },
            areaStyle: {
              opacity: 0.3
            }
          }
        ]
      }
      
      realTimeChartInstance.setOption(option)
    }
    
    const updateDistributionChart = () => {
      if (!distributionChartInstance) return
      
      const data = [
        { value: totalActiveConnections.value, name: '活跃连接', itemStyle: { color: '#409eff' } },
        { value: totalIdleConnections.value, name: '空闲连接', itemStyle: { color: '#67c23a' } },
        { value: totalWaitingThreads.value, name: '等待线程', itemStyle: { color: '#e6a23c' } }
      ]
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [
          {
            name: '连接分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '50%'],
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              show: false
            },
            labelLine: {
              show: false
            }
          }
        ]
      }
      
      distributionChartInstance.setOption(option)
    }
    
    const startRealTimeUpdate = () => {
      realTimeTimer = setInterval(() => {
        updateRealTimeChart()
        updateDistributionChart()
      }, 5000)
    }
    
    const stopRealTimeUpdate = () => {
      if (realTimeTimer) {
        clearInterval(realTimeTimer)
        realTimeTimer = null
      }
    }
    
    // 生命周期
    onMounted(() => {
      initCharts()
    })
    
    onUnmounted(() => {
      stopRealTimeUpdate()
      if (realTimeChartInstance) {
        realTimeChartInstance.dispose()
      }
      if (distributionChartInstance) {
        distributionChartInstance.dispose()
      }
    })
    
    return {
      realTimeChart,
      distributionChart,
      searchText,
      selectedTimeRange,
      realTimeEnabled,
      showDetailsDialog,
      selectedPool,
      primaryTrend,
      userTrend,
      performanceTrend,
      timeRanges,
      healthScore,
      totalActiveConnections,
      totalIdleConnections,
      totalWaitingThreads,
      filteredPoolData,
      getCardClass,
      getTrendClass,
      getTrendIcon,
      getProgressColor,
      getPerformanceScore,
      getPerformanceColor,
      getHealthType,
      getHealthText,
      getHealthColor,
      getUsageClass,
      getConnectionTimeClass,
      changeTimeRange,
      toggleRealTime,
      testConnection,
      warmUpPool,
      showPoolDetails,
      exportMetrics,
      handleSortChange
    }
  }
}
</script>

<style scoped>
.connection-pool-dashboard {
  padding: 20px;
}

.status-cards {
  margin-bottom: 20px;
}

.status-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.status-card.warning {
  border-left: 4px solid #e6a23c;
}

.status-card.danger {
  border-left: 4px solid #f56c6c;
}

.card-content {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.card-icon.primary {
  background: linear-gradient(135deg, #409eff, #66b3ff);
}

.card-icon.user {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-icon.performance {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.card-icon.health {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.card-info h3 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.card-info p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 14px;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-stable {
  color: #909399;
}

.card-status {
  margin-top: 5px;
}

.card-progress {
  margin-top: 10px;
}

.charts-section {
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
  height: 350px;
  width: 100%;
}

.chart-container.small {
  height: 250px;
}

.distribution-legend {
  margin-top: 15px;
  padding: 0 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-dot.active {
  background-color: #409eff;
}

.legend-dot.idle {
  background-color: #67c23a;
}

.legend-dot.waiting {
  background-color: #e6a23c;
}

.metrics-table {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pool-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pool-icon {
  font-size: 16px;
}

.pool-icon.primary {
  color: #409eff;
}

.pool-icon.user {
  color: #67c23a;
}

.metric-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.metric-value {
  font-weight: 600;
  margin-bottom: 3px;
}

.usage-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.usage-text.warning {
  color: #e6a23c;
}

.usage-text.danger {
  color: #f56c6c;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.pool-details {
  padding: 10px 0;
}

.real-time-metrics {
  padding: 20px 0;
}

.metric-group {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.metric-item {
  text-align: center;
}

.metric-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  display: block;
  font-size: 24px;
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

.metric-value.time.text-success {
  color: #67c23a;
}

.metric-value.time.text-warning {
  color: #e6a23c;
}

.metric-value.time.text-danger {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .chart-container {
    height: 300px;
  }
  
  .chart-container.small {
    height: 200px;
  }
}

@media (max-width: 768px) {
  .connection-pool-dashboard {
    padding: 10px;
  }
  
  .card-content {
    flex-direction: column;
    text-align: center;
  }
  
  .card-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 10px;
  }
}
</style>