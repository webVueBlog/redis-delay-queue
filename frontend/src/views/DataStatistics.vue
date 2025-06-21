<template>
  <div class="data-statistics">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据统计</h2>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadStatistics"
        />
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon users"><User /></el-icon>
            <div class="stat-info">
              <h3>{{ statistics.totalUsers }}</h3>
              <p>总用户数</p>
              <span class="stat-change" :class="{ positive: statistics.userGrowth > 0 }">
                {{ statistics.userGrowth > 0 ? '+' : '' }}{{ statistics.userGrowth }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon tasks"><List /></el-icon>
            <div class="stat-info">
              <h3>{{ statistics.totalTasks }}</h3>
              <p>总任务数</p>
              <span class="stat-change" :class="{ positive: statistics.taskGrowth > 0 }">
                {{ statistics.taskGrowth > 0 ? '+' : '' }}{{ statistics.taskGrowth }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon success"><Check /></el-icon>
            <div class="stat-info">
              <h3>{{ statistics.successRate }}%</h3>
              <p>任务成功率</p>
              <span class="stat-change" :class="{ positive: statistics.successRateChange > 0 }">
                {{ statistics.successRateChange > 0 ? '+' : '' }}{{ statistics.successRateChange }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon performance"><TrendCharts /></el-icon>
            <div class="stat-info">
              <h3>{{ statistics.avgProcessTime }}ms</h3>
              <p>平均处理时间</p>
              <span class="stat-change" :class="{ negative: statistics.processTimeChange > 0 }">
                {{ statistics.processTimeChange > 0 ? '+' : '' }}{{ statistics.processTimeChange }}ms
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="用户增长趋势">
          <div ref="userGrowthChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="任务处理统计">
          <div ref="taskStatsChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card title="系统性能趋势">
          <div ref="performanceChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>详细统计数据</span>
          <el-button type="primary" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </template>
      
      <el-table :data="detailStats" v-loading="loading" stripe border>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="newUsers" label="新增用户" width="100" />
        <el-table-column prop="totalTasks" label="任务总数" width="100" />
        <el-table-column prop="completedTasks" label="完成任务" width="100" />
        <el-table-column prop="failedTasks" label="失败任务" width="100" />
        <el-table-column prop="successRate" label="成功率" width="100">
          <template #default="{ row }">
            {{ row.successRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="avgProcessTime" label="平均处理时间" width="120">
          <template #default="{ row }">
            {{ row.avgProcessTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="peakConcurrency" label="峰值并发" width="100" />
        <el-table-column prop="errorRate" label="错误率" width="100">
          <template #default="{ row }">
            {{ row.errorRate }}%
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, List, Check, TrendCharts, Refresh, Download 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const dateRange = ref([])
const detailStats = ref([])

// 统计数据
const statistics = reactive({
  totalUsers: 0,
  userGrowth: 0,
  totalTasks: 0,
  taskGrowth: 0,
  successRate: 0,
  successRateChange: 0,
  avgProcessTime: 0,
  processTimeChange: 0
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 图表引用
const userGrowthChart = ref()
const taskStatsChart = ref()
const performanceChart = ref()

// 图表实例
let userGrowthChartInstance = null
let taskStatsChartInstance = null
let performanceChartInstance = null

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await axios.get('/api/statistics', { params })
    if (response.data.success) {
      const data = response.data.data
      Object.assign(statistics, data.overview)
      detailStats.value = data.details
      pagination.total = data.total
      
      // 更新图表
      updateCharts(data.charts)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  loadStatistics()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadStatistics()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadStatistics()
}

// 导出数据
const exportData = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await axios.get('/api/statistics/export', { 
      params,
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `statistics_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出数据失败')
  }
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    if (userGrowthChart.value) {
      userGrowthChartInstance = echarts.init(userGrowthChart.value)
    }
    if (taskStatsChart.value) {
      taskStatsChartInstance = echarts.init(taskStatsChart.value)
    }
    if (performanceChart.value) {
      performanceChartInstance = echarts.init(performanceChart.value)
    }
  })
}

// 更新图表
const updateCharts = (chartData) => {
  if (userGrowthChartInstance && chartData.userGrowth) {
    const option = {
      title: { text: '用户增长趋势' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: chartData.userGrowth.dates
      },
      yAxis: { type: 'value' },
      series: [{
        name: '新增用户',
        type: 'line',
        data: chartData.userGrowth.values,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      }]
    }
    userGrowthChartInstance.setOption(option)
  }
  
  if (taskStatsChartInstance && chartData.taskStats) {
    const option = {
      title: { text: '任务处理统计' },
      tooltip: { trigger: 'item' },
      series: [{
        name: '任务状态',
        type: 'pie',
        radius: '50%',
        data: chartData.taskStats,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    taskStatsChartInstance.setOption(option)
  }
  
  if (performanceChartInstance && chartData.performance) {
    const option = {
      title: { text: '系统性能趋势' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['响应时间', '吞吐量'] },
      xAxis: {
        type: 'category',
        data: chartData.performance.dates
      },
      yAxis: [{
        type: 'value',
        name: '响应时间(ms)'
      }, {
        type: 'value',
        name: '吞吐量(req/s)'
      }],
      series: [{
        name: '响应时间',
        type: 'line',
        data: chartData.performance.responseTime,
        itemStyle: { color: '#E6A23C' }
      }, {
        name: '吞吐量',
        type: 'line',
        yAxisIndex: 1,
        data: chartData.performance.throughput,
        itemStyle: { color: '#67C23A' }
      }]
    }
    performanceChartInstance.setOption(option)
  }
}

// 生命周期
onMounted(() => {
  // 设置默认日期范围（最近7天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 7)
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  initCharts()
  loadStatistics()
})

onUnmounted(() => {
  if (userGrowthChartInstance) {
    userGrowthChartInstance.dispose()
  }
  if (taskStatsChartInstance) {
    taskStatsChartInstance.dispose()
  }
  if (performanceChartInstance) {
    performanceChartInstance.dispose()
  }
})
</script>

<style scoped>
.data-statistics {
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
  align-items: center;
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
}

.stat-icon.users {
  color: #409EFF;
}

.stat-icon.tasks {
  color: #67C23A;
}

.stat-icon.success {
  color: #E6A23C;
}

.stat-icon.performance {
  color: #F56C6C;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-info p {
  margin: 0 0 5px 0;
  color: #909399;
  font-size: 14px;
}

.stat-change {
  font-size: 12px;
  font-weight: bold;
}

.stat-change.positive {
  color: #67C23A;
}

.stat-change.negative {
  color: #F56C6C;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.table-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>