<template>
  <div class="my-tasks">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的任务</h2>
      <div class="header-actions">
        <el-button @click="refreshTasks" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          新增任务
        </el-button>
      </div>
    </div>

    <!-- 任务统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon total"><Document /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.total }}</h3>
              <p>总任务数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon pending"><Clock /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.pending }}</h3>
              <p>待执行</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon running"><Loading /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.running }}</h3>
              <p>执行中</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon completed"><CircleCheck /></el-icon>
            <div class="stat-info">
              <h3>{{ stats.completed }}</h3>
              <p>已完成</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="任务ID">
          <el-input v-model="searchForm.taskId" placeholder="请输入任务ID" clearable />
        </el-form-item>
        <el-form-item label="队列名称">
          <el-input v-model="searchForm.queueName" placeholder="请输入队列名称" clearable />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待执行" value="PENDING" />
            <el-option label="执行中" value="RUNNING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
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
          <el-button type="primary" @click="searchTasks">
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

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table 
        :data="tasks" 
        v-loading="loading" 
        stripe 
        border
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="任务ID" width="120" />
        <el-table-column prop="queueName" label="队列名称" width="150" />
        <el-table-column prop="taskType" label="任务类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="delayTime" label="延迟时间" width="120">
          <template #default="{ row }">
            {{ formatDelayTime(row.delayTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.executeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100">
          <template #default="{ row }">
            <span :class="getRetryClass(row.retryCount, row.maxRetries)">
              {{ row.retryCount }}/{{ row.maxRetries }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'FAILED'" 
              size="small" 
              type="warning" 
              @click="retryTask(row)">
              <el-icon><RefreshRight /></el-icon>
              重试
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              size="small" 
              type="danger" 
              @click="cancelTask(row)">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteTask(row)">
              <el-icon><Delete /></el-icon>
              删除
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

    <!-- 新增任务对话框 -->
    <el-dialog title="新增任务" v-model="showAddDialog" width="600px">
      <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="100px">
        <el-form-item label="队列名称" prop="queueName">
          <el-input v-model="taskForm.queueName" placeholder="请输入队列名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="邮件发送" value="EMAIL" />
            <el-option label="短信发送" value="SMS" />
            <el-option label="数据处理" value="DATA_PROCESS" />
            <el-option label="文件处理" value="FILE_PROCESS" />
            <el-option label="定时任务" value="SCHEDULED" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务数据" prop="taskData">
          <el-input 
            v-model="taskForm.taskData" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入任务数据（JSON格式）" 
          />
        </el-form-item>
        <el-form-item label="延迟时间" prop="delayTime">
          <el-input-number 
            v-model="taskForm.delayTime" 
            :min="0" 
            :max="86400000" 
            placeholder="延迟时间（毫秒）" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大重试次数" prop="maxRetries">
          <el-input-number 
            v-model="taskForm.maxRetries" 
            :min="0" 
            :max="10" 
            placeholder="最大重试次数" 
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addTask" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog title="任务详情" v-model="showDetailDialog" width="800px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="队列名称">{{ currentTask.queueName }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ currentTask.taskType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ getStatusText(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTask.priority)">
            {{ currentTask.priority }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="延迟时间">{{ formatDelayTime(currentTask.delayTime) }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentTask.retryCount }}/{{ currentTask.maxRetries }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ formatDateTime(currentTask.executeTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentTask.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentTask.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>任务数据</el-divider>
      <el-input 
        v-model="currentTask.taskData" 
        type="textarea" 
        :rows="6"
        readonly
      />
      
      <el-divider v-if="currentTask.errorMessage">错误信息</el-divider>
      <el-input 
        v-if="currentTask.errorMessage"
        v-model="currentTask.errorMessage" 
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
  Refresh, Plus, Search, Document, Clock, Loading, CircleCheck, View, 
  RefreshRight, Close, Delete 
} from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const tasks = ref([])
const selectedTasks = ref([])
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const taskFormRef = ref(null)
const currentTask = ref(null)

// 统计数据
const stats = reactive({
  total: 0,
  pending: 0,
  running: 0,
  completed: 0,
  failed: 0
})

// 搜索表单
const searchForm = reactive({
  taskId: '',
  queueName: '',
  status: '',
  dateRange: []
})

// 任务表单
const taskForm = reactive({
  queueName: '',
  taskType: '',
  taskData: '',
  delayTime: 0,
  priority: 'NORMAL',
  maxRetries: 3
})

// 表单验证规则
const taskRules = {
  queueName: [{ required: true, message: '请输入队列名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  taskData: [{ required: true, message: '请输入任务数据', trigger: 'blur' }],
  delayTime: [{ required: true, message: '请输入延迟时间', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  maxRetries: [{ required: true, message: '请输入最大重试次数', trigger: 'blur' }]
}

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

// 格式化延迟时间
const formatDelayTime = (delayTime) => {
  if (!delayTime) return '-'
  const seconds = Math.floor(delayTime / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days}天${hours % 24}小时`
  if (hours > 0) return `${hours}小时${minutes % 60}分钟`
  if (minutes > 0) return `${minutes}分钟${seconds % 60}秒`
  return `${seconds}秒`
}

// 获取状态类型样式
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'RUNNING': 'primary',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待执行',
    'RUNNING': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

// 获取优先级类型样式
const getPriorityType = (priority) => {
  const types = {
    'LOW': 'info',
    'NORMAL': 'primary',
    'HIGH': 'warning',
    'URGENT': 'danger'
  }
  return types[priority] || 'info'
}

// 获取重试次数样式
const getRetryClass = (retryCount, maxRetries) => {
  if (retryCount >= maxRetries) return 'retry-max'
  if (retryCount > maxRetries * 0.7) return 'retry-high'
  return 'retry-normal'
}

// 加载任务
const loadTasks = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    
    const response = await axios.get('/api/tasks/my', { params })
    if (response.data.success) {
      tasks.value = response.data.data.tasks
      pagination.total = response.data.data.total
      Object.assign(stats, response.data.data.stats)
    }
  } catch (error) {
    console.error('加载任务失败:', error)
    ElMessage.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

// 搜索任务
const searchTasks = () => {
  pagination.page = 1
  loadTasks()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    taskId: '',
    queueName: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadTasks()
}

// 刷新任务
const refreshTasks = () => {
  loadTasks()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadTasks()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadTasks()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 查看详情
const viewDetail = (task) => {
  currentTask.value = task
  showDetailDialog.value = true
}

// 新增任务
const addTask = async () => {
  if (!taskFormRef.value) return
  
  try {
    await taskFormRef.value.validate()
    saving.value = true
    
    // 验证JSON格式
    try {
      JSON.parse(taskForm.taskData)
    } catch (e) {
      ElMessage.error('任务数据必须是有效的JSON格式')
      return
    }
    
    const response = await axios.post('/api/tasks', taskForm)
    if (response.data.success) {
      ElMessage.success('任务创建成功')
      showAddDialog.value = false
      resetTaskForm()
      loadTasks()
    } else {
      ElMessage.error(response.data.message || '任务创建失败')
    }
  } catch (error) {
    console.error('创建任务失败:', error)
    ElMessage.error('创建任务失败')
  } finally {
    saving.value = false
  }
}

// 重试任务
const retryTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要重试任务 ${task.id} 吗？`, '确认重试', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.post(`/api/tasks/${task.id}/retry`)
    if (response.data.success) {
      ElMessage.success('任务重试成功')
      loadTasks()
    } else {
      ElMessage.error(response.data.message || '任务重试失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重试任务失败:', error)
      ElMessage.error('重试任务失败')
    }
  }
}

// 取消任务
const cancelTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要取消任务 ${task.id} 吗？`, '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.post(`/api/tasks/${task.id}/cancel`)
    if (response.data.success) {
      ElMessage.success('任务取消成功')
      loadTasks()
    } else {
      ElMessage.error(response.data.message || '任务取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败:', error)
      ElMessage.error('取消任务失败')
    }
  }
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务 ${task.id} 吗？此操作不可恢复！`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/api/tasks/${task.id}`)
    if (response.data.success) {
      ElMessage.success('任务删除成功')
      loadTasks()
    } else {
      ElMessage.error(response.data.message || '任务删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 重置任务表单
const resetTaskForm = () => {
  Object.assign(taskForm, {
    queueName: '',
    taskType: '',
    taskData: '',
    delayTime: 0,
    priority: 'NORMAL',
    maxRetries: 3
  })
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
}

// 生命周期
onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.my-tasks {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
  cursor: default;
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

.stat-icon.pending {
  color: #E6A23C;
}

.stat-icon.running {
  color: #409EFF;
}

.stat-icon.completed {
  color: #67C23A;
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.retry-normal {
  color: #67C23A;
}

.retry-high {
  color: #E6A23C;
}

.retry-max {
  color: #F56C6C;
  font-weight: bold;
}
</style>