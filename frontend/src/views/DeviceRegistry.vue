<template>
  <div class="device-registry">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>设备注册中心</h1>
      <p>管理和监控所有注册设备的状态和信息</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card total">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalDevices }}</div>
                <div class="stat-label">总设备数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card online">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.onlineDevices }}</div>
                <div class="stat-label">在线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card offline">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.offlineDevices }}</div>
                <div class="stat-label">离线设备</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card rate">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.onlineRate }}%</div>
                <div class="stat-label">在线率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <div class="left-actions">
        <el-button type="primary" @click="showRegisterDialog = true">
          <el-icon><Plus /></el-icon>
          注册设备
        </el-button>
        <el-button @click="refreshDevices">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportDevices">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="right-filters">
        <el-input
          v-model="queryParams.deviceId"
          placeholder="设备ID"
          clearable
          style="width: 150px; margin-right: 10px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="queryParams.deviceName"
          placeholder="设备名称"
          clearable
          style="width: 150px; margin-right: 10px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 120px; margin-right: 10px"
          @change="handleSearch"
        >
          <el-option label="在线" value="ONLINE" />
          <el-option label="离线" value="OFFLINE" />
          <el-option label="维护" value="MAINTENANCE" />
          <el-option label="错误" value="ERROR" />
        </el-select>
        <el-select
          v-model="queryParams.deviceType"
          placeholder="设备类型"
          clearable
          style="width: 120px; margin-right: 10px"
          @change="handleSearch"
        >
          <el-option label="传感器" value="SENSOR" />
          <el-option label="控制器" value="CONTROLLER" />
          <el-option label="网关" value="GATEWAY" />
          <el-option label="摄像头" value="CAMERA" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 设备列表 -->
    <el-card class="device-table-card">
      <el-table
        :data="deviceList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="deviceId" label="设备ID" width="150" fixed="left" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeTagType(row.deviceType)">{{ row.deviceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              <el-icon v-if="row.status === 'ONLINE'"><CircleCheck /></el-icon>
              <el-icon v-else-if="row.status === 'OFFLINE'"><CircleClose /></el-icon>
              <el-icon v-else-if="row.status === 'MAINTENANCE'"><Tools /></el-icon>
              <el-icon v-else><Warning /></el-icon>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="manufacturer" label="厂商" width="120" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="160">
          <template #default="{ row }">
            <span v-if="row.lastHeartbeat">{{ formatDateTime(row.lastHeartbeat) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDevice(row)">详情</el-button>
            <el-button size="small" type="primary" @click="editDevice(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDevice(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 设备注册对话框 -->
    <el-dialog
      v-model="showRegisterDialog"
      title="注册新设备"
      width="600px"
      :before-close="handleRegisterDialogClose"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备ID" prop="deviceId">
              <el-input v-model="registerForm.deviceId" placeholder="请输入设备ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="registerForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="registerForm.deviceType" placeholder="请选择设备类型">
                <el-option label="传感器" value="SENSOR" />
                <el-option label="控制器" value="CONTROLLER" />
                <el-option label="网关" value="GATEWAY" />
                <el-option label="摄像头" value="CAMERA" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IP地址" prop="ipAddress">
              <el-input v-model="registerForm.ipAddress" placeholder="请输入IP地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="registerForm.port" :min="1" :max="65535" placeholder="端口" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="MAC地址" prop="macAddress">
              <el-input v-model="registerForm.macAddress" placeholder="请输入MAC地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input v-model="registerForm.serialNumber" placeholder="请输入序列号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="厂商" prop="manufacturer">
              <el-input v-model="registerForm.manufacturer" placeholder="请输入厂商" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="registerForm.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="固件版本" prop="firmwareVersion">
              <el-input v-model="registerForm.firmwareVersion" placeholder="请输入固件版本" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="位置" prop="location">
          <el-input v-model="registerForm.location" placeholder="请输入设备位置" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="registerForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入设备描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRegisterDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="registerLoading">
            注册设备
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="设备详情"
      width="800px"
    >
      <div v-if="currentDevice" class="device-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备ID">{{ currentDevice.deviceId }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">
            <el-tag :type="getDeviceTypeTagType(currentDevice.deviceType)">{{ currentDevice.deviceType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentDevice.status)">{{ getStatusText(currentDevice.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentDevice.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="端口">{{ currentDevice.port }}</el-descriptions-item>
          <el-descriptions-item label="MAC地址">{{ currentDevice.macAddress }}</el-descriptions-item>
          <el-descriptions-item label="序列号">{{ currentDevice.serialNumber }}</el-descriptions-item>
          <el-descriptions-item label="厂商">{{ currentDevice.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentDevice.model }}</el-descriptions-item>
          <el-descriptions-item label="固件版本">{{ currentDevice.firmwareVersion }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentDevice.location }}</el-descriptions-item>
          <el-descriptions-item label="最后心跳">
            {{ currentDevice.lastHeartbeat ? formatDateTime(currentDevice.lastHeartbeat) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最后上线">
            {{ currentDevice.lastOnlineTime ? formatDateTime(currentDevice.lastOnlineTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最后离线">
            {{ currentDevice.lastOfflineTime ? formatDateTime(currentDevice.lastOfflineTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDateTime(currentDevice.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentDevice.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  ElNotification
} from 'element-plus'
import {
  Monitor,
  CircleCheck,
  CircleClose,
  TrendCharts,
  Plus,
  Refresh,
  Download,
  Search,
  Tools,
  Warning
} from '@element-plus/icons-vue'
import { deviceApi } from '@/api/device'

// 响应式数据
const loading = ref(false)
const registerLoading = ref(false)
const showRegisterDialog = ref(false)
const showDetailDialog = ref(false)
const deviceList = ref([])
const total = ref(0)
const currentDevice = ref(null)
const selectedDevices = ref([])
const registerFormRef = ref(null)

// 统计数据
const statistics = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  maintenanceDevices: 0,
  errorDevices: 0,
  onlineRate: 0
})

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 20,
  deviceId: '',
  deviceName: '',
  deviceType: '',
  status: '',
  sortBy: 'createdAt',
  sortDirection: 'desc'
})

// 注册表单
const registerForm = reactive({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  ipAddress: '',
  port: null,
  macAddress: '',
  serialNumber: '',
  manufacturer: '',
  model: '',
  firmwareVersion: '',
  location: '',
  description: ''
})

// 表单验证规则
const registerRules = {
  deviceId: [
    { required: true, message: '请输入设备ID', trigger: 'blur' },
    { min: 3, max: 50, message: '设备ID长度在3到50个字符', trigger: 'blur' }
  ],
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 100, message: '设备名称长度在2到100个字符', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ]
}

// 定时器
let refreshTimer = null

// 生命周期
onMounted(() => {
  loadDevices()
  loadStatistics()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

// 方法
const loadDevices = async () => {
  try {
    loading.value = true
    const response = await deviceApi.queryDevices(queryParams)
    deviceList.value = response.content
    total.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载设备列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const stats = await deviceApi.getStatistics()
    Object.assign(statistics, stats)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadDevices()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  loadDevices()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  loadDevices()
}

const handleSelectionChange = (selection) => {
  selectedDevices.value = selection
}

const refreshDevices = () => {
  loadDevices()
  loadStatistics()
  ElMessage.success('刷新成功')
}

const exportDevices = () => {
  ElMessage.info('导出功能开发中...')
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    await deviceApi.registerDevice(registerForm)
    
    ElMessage.success('设备注册成功')
    showRegisterDialog.value = false
    resetRegisterForm()
    loadDevices()
    loadStatistics()
  } catch (error) {
    if (error.message) {
      ElMessage.error('注册失败: ' + error.message)
    }
  } finally {
    registerLoading.value = false
  }
}

const handleRegisterDialogClose = () => {
  resetRegisterForm()
  showRegisterDialog.value = false
}

const resetRegisterForm = () => {
  Object.assign(registerForm, {
    deviceId: '',
    deviceName: '',
    deviceType: '',
    ipAddress: '',
    port: null,
    macAddress: '',
    serialNumber: '',
    manufacturer: '',
    model: '',
    firmwareVersion: '',
    location: '',
    description: ''
  })
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

const viewDevice = (device) => {
  currentDevice.value = device
  showDetailDialog.value = true
}

const editDevice = (device) => {
  ElMessage.info('编辑功能开发中...')
}

const deleteDevice = async (device) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备 "${device.deviceName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deviceApi.deleteDevice(device.deviceId)
    ElMessage.success('设备删除成功')
    loadDevices()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const getStatusTagType = (status) => {
  const typeMap = {
    'ONLINE': 'success',
    'OFFLINE': 'info',
    'MAINTENANCE': 'warning',
    'ERROR': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'MAINTENANCE': '维护',
    'ERROR': '错误'
  }
  return textMap[status] || status
}

const getDeviceTypeTagType = (type) => {
  const typeMap = {
    'SENSOR': 'primary',
    'CONTROLLER': 'success',
    'GATEWAY': 'warning',
    'CAMERA': 'info'
  }
  return typeMap[type] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadStatistics()
  }, 30000) // 30秒刷新一次统计数据
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}
</script>

<style scoped>
.device-registry {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-card.total {
  border-left: 4px solid #409eff;
}

.stat-card.online {
  border-left: 4px solid #67c23a;
}

.stat-card.offline {
  border-left: 4px solid #909399;
}

.stat-card.rate {
  border-left: 4px solid #e6a23c;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 15px;
  opacity: 0.8;
}

.stat-card.total .stat-icon {
  color: #409eff;
}

.stat-card.online .stat-icon {
  color: #67c23a;
}

.stat-card.offline .stat-icon {
  color: #909399;
}

.stat-card.rate .stat-icon {
  color: #e6a23c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.left-actions {
  display: flex;
  gap: 10px;
}

.right-filters {
  display: flex;
  align-items: center;
}

.device-table-card {
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.device-detail {
  padding: 10px 0;
}

.text-muted {
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>