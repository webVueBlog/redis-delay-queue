<template>
  <div class="user-permission-organizations">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户权限组织</h2>
      <el-button type="primary" @click="loadCurrentUserOrganizations">
        <el-icon><Refresh /></el-icon>
        刷新权限
      </el-button>
    </div>

    <!-- 当前用户权限组织 -->
    <el-card class="user-org-card">
      <template #header>
        <div class="card-header">
          <span>我的权限组织</span>
          <el-tag type="info">{{ userOrganizations.length }} 个组织</el-tag>
        </div>
      </template>
      
      <div v-loading="loading">
        <div v-if="userOrganizations.length === 0" class="empty-state">
          <el-empty description="暂无权限组织" />
        </div>
        <div v-else class="org-list">
          <el-row :gutter="16">
            <el-col :span="8" v-for="orgId in userOrganizations" :key="orgId">
              <el-card class="org-item" shadow="hover">
                <div class="org-info">
                  <el-icon class="org-icon"><OfficeBuilding /></el-icon>
                  <div class="org-details">
                    <div class="org-id">组织ID: {{ orgId }}</div>
                    <div class="org-name">{{ getOrganizationName(orgId) }}</div>
                  </div>
                </div>
                <div class="org-actions">
                  <el-button size="small" @click="viewOrganizationDetails(orgId)">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 管理员功能：查看其他用户权限组织 -->
    <el-card v-if="isAdmin" class="admin-section">
      <template #header>
        <span>用户权限组织管理</span>
      </template>
      
      <!-- 用户选择 -->
      <el-form :model="adminForm" inline>
        <el-form-item label="选择用户">
          <el-select 
            v-model="adminForm.selectedUserId" 
            placeholder="请选择用户" 
            filterable 
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            @change="loadUserOrganizations">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.username} (${user.email})`"
              :value="user.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUserOrganizations" :disabled="!adminForm.selectedUserId">
            <el-icon><Search /></el-icon>
            查询权限
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 选中用户的权限组织 -->
      <div v-if="selectedUserOrganizations.length > 0" class="selected-user-orgs">
        <h4>{{ selectedUserInfo.username }} 的权限组织</h4>
        <el-table :data="selectedUserOrganizations" stripe border>
          <el-table-column prop="id" label="组织ID" width="100" />
          <el-table-column prop="name" label="组织名称" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="level" label="层级" width="80" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="viewOrganizationDetails(row.id)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 组织详情对话框 -->
    <el-dialog title="组织详情" v-model="showOrgDialog" width="600px">
      <div v-loading="orgDetailLoading">
        <el-descriptions v-if="selectedOrganization" :column="2" border>
          <el-descriptions-item label="组织ID">{{ selectedOrganization.id }}</el-descriptions-item>
          <el-descriptions-item label="组织名称">{{ selectedOrganization.name }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ selectedOrganization.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedOrganization.status === 1 ? 'success' : 'danger'">
              {{ selectedOrganization.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedOrganization.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(selectedOrganization.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showOrgDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, Refresh, Search, View } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const userSearchLoading = ref(false)
const orgDetailLoading = ref(false)
const showOrgDialog = ref(false)

const userOrganizations = ref([])
const selectedUserOrganizations = ref([])
const userOptions = ref([])
const organizationMap = ref(new Map())
const selectedOrganization = ref(null)

const adminForm = reactive({
  selectedUserId: null
})

const selectedUserInfo = ref({})

// 计算属性
const isAdmin = computed(() => {
  const userRole = localStorage.getItem('userRole')
  return userRole === 'ADMIN'
})

// 方法
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getOrganizationName = (orgId) => {
  const org = organizationMap.value.get(orgId)
  return org ? org.name : `组织-${orgId}`
}

const loadCurrentUserOrganizations = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/users/organizations')
    if (response.data.success) {
      userOrganizations.value = response.data.data
      // 加载组织详情
      await loadOrganizationDetails(response.data.data)
      ElMessage.success('权限组织加载成功')
    } else {
      ElMessage.error(response.data.message || '加载权限组织失败')
    }
  } catch (error) {
    console.error('加载权限组织失败:', error)
    ElMessage.error('加载权限组织失败')
  } finally {
    loading.value = false
  }
}

const loadOrganizationDetails = async (orgIds) => {
  try {
    for (const orgId of orgIds) {
      if (!organizationMap.value.has(orgId)) {
        const response = await axios.get(`/api/organizations/${orgId}`)
        if (response.data.success) {
          organizationMap.value.set(orgId, response.data.data)
        }
      }
    }
  } catch (error) {
    console.error('加载组织详情失败:', error)
  }
}

const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }
  
  userSearchLoading.value = true
  try {
    const response = await axios.get('/api/users', {
      params: {
        username: query,
        size: 20
      }
    })
    if (response.data.success) {
      userOptions.value = response.data.data.content
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

const loadUserOrganizations = async () => {
  if (!adminForm.selectedUserId) return
  
  loading.value = true
  try {
    // 获取用户信息
    const userResponse = await axios.get(`/api/users/${adminForm.selectedUserId}`)
    if (userResponse.data.success) {
      selectedUserInfo.value = userResponse.data.data
    }
    
    // 获取用户权限组织ID
      const orgResponse = await axios.get(`/api/users/${adminForm.selectedUserId}/organizations`)
      if (orgResponse.data.success) {
        const orgIds = orgResponse.data.data
      
      // 获取组织详情
      const orgDetails = []
      for (const orgId of orgIds) {
        try {
          const detailResponse = await axios.get(`/api/organizations/${orgId}`)
          if (detailResponse.data.success) {
            orgDetails.push(detailResponse.data.data)
          }
        } catch (error) {
          console.error(`获取组织${orgId}详情失败:`, error)
        }
      }
      
      selectedUserOrganizations.value = orgDetails
      ElMessage.success('用户权限组织加载成功')
    } else {
      ElMessage.error(orgResponse.data.message || '加载用户权限组织失败')
    }
  } catch (error) {
    console.error('加载用户权限组织失败:', error)
    ElMessage.error('加载用户权限组织失败')
  } finally {
    loading.value = false
  }
}

const viewOrganizationDetails = async (orgId) => {
  orgDetailLoading.value = true
  showOrgDialog.value = true
  
  try {
    const response = await axios.get(`/api/organizations/${orgId}`)
    if (response.data.success) {
      selectedOrganization.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载组织详情失败')
    }
  } catch (error) {
    console.error('加载组织详情失败:', error)
    ElMessage.error('加载组织详情失败')
  } finally {
    orgDetailLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCurrentUserOrganizations()
})
</script>

<style scoped>
.user-permission-organizations {
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

.user-org-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.org-list {
  margin-top: 20px;
}

.org-item {
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.org-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.org-info {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.org-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 12px;
}

.org-details {
  flex: 1;
}

.org-id {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.org-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.org-actions {
  text-align: right;
}

.admin-section {
  margin-top: 20px;
}

.selected-user-orgs {
  margin-top: 20px;
}

.selected-user-orgs h4 {
  margin-bottom: 16px;
  color: #303133;
}
</style>