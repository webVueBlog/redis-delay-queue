<template>
  <div class="organization-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>组织管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增组织
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="组织名称">
          <el-input v-model="searchForm.name" placeholder="请输入组织名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrganizations">
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

    <!-- 组织树形表格 -->
    <el-card class="table-card">
      <el-table 
        :data="organizationTree" 
        v-loading="loading" 
        stripe 
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all>
        <el-table-column prop="name" label="组织名称" width="250">
          <template #default="{ row }">
            <el-icon class="org-icon"><OfficeBuilding /></el-icon>
            {{ row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="300" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editOrganization(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="success" @click="addChildOrganization(row)">
              <el-icon><Plus /></el-icon>
              添加子组织
            </el-button>
            <el-button size="small" type="info" @click="viewUsers(row)">
              <el-icon><User /></el-icon>
              查看用户
            </el-button>
            <el-button size="small" type="danger" @click="deleteOrganization(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑组织对话框 -->
    <el-dialog :title="dialogTitle" v-model="showAddDialog" width="600px">
      <el-form :model="organizationForm" :rules="organizationRules" ref="organizationFormRef" label-width="100px">
        <el-form-item label="组织名称" prop="name">
          <el-input v-model="organizationForm.name" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织描述">
          <el-input 
            v-model="organizationForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入组织描述" 
          />
        </el-form-item>
        <el-form-item label="父级组织">
          <el-tree-select
            v-model="organizationForm.parentId"
            :data="parentOrganizationOptions"
            :render-after-expand="false"
            placeholder="请选择父级组织"
            clearable
            check-strictly
            :props="{
              value: 'id',
              label: 'name',
              children: 'children'
            }"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="organizationForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveOrganization" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看用户对话框 -->
    <el-dialog title="组织用户" v-model="showUsersDialog" width="800px">
      <el-table :data="organizationUsers" v-loading="loadingUsers" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showUsersDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, User, OfficeBuilding } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const loadingUsers = ref(false)
const organizationTree = ref([])
const allOrganizations = ref([])
const organizationUsers = ref([])
const showAddDialog = ref(false)
const showUsersDialog = ref(false)
const isEdit = ref(false)
const organizationFormRef = ref()
const currentOrganization = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null
})

// 组织表单
const organizationForm = reactive({
  id: null,
  name: '',
  description: '',
  parentId: null,
  status: 1
})

// 表单验证规则
const organizationRules = {
  name: [
    { required: true, message: '请输入组织名称', trigger: 'blur' },
    { min: 1, max: 100, message: '组织名称长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑组织' : '新增组织')

const parentOrganizationOptions = computed(() => {
  const options = []
  const buildOptions = (organizations, level = 0) => {
    organizations.forEach(org => {
      if (!isEdit.value || org.id !== organizationForm.id) {
        options.push({
          id: org.id,
          name: '　'.repeat(level) + org.name,
          children: org.children ? [] : undefined
        })
        if (org.children && org.children.length > 0) {
          buildOptions(org.children, level + 1)
        }
      }
    })
  }
  buildOptions(organizationTree.value)
  return options
})

// 方法
const loadOrganizations = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/organizations/tree')
    if (response.data.success) {
      organizationTree.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载组织列表失败')
    }
  } catch (error) {
    console.error('加载组织列表失败:', error)
    ElMessage.error('加载组织列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllOrganizations = async () => {
  try {
    const response = await axios.get('/api/organizations/all')
    if (response.data.success) {
      allOrganizations.value = response.data.data
    }
  } catch (error) {
    console.error('加载所有组织失败:', error)
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    status: null
  })
  loadOrganizations()
}

const editOrganization = (organization) => {
  isEdit.value = true
  Object.assign(organizationForm, {
    id: organization.id,
    name: organization.name,
    description: organization.description,
    parentId: organization.parentId,
    status: organization.status
  })
  showAddDialog.value = true
}

const addChildOrganization = (parentOrganization) => {
  isEdit.value = false
  Object.assign(organizationForm, {
    id: null,
    name: '',
    description: '',
    parentId: parentOrganization.id,
    status: 1
  })
  showAddDialog.value = true
}

const saveOrganization = async () => {
  if (!organizationFormRef.value) return
  
  try {
    await organizationFormRef.value.validate()
    saving.value = true
    
    let response
    if (isEdit.value) {
      // 更新组织
      response = await axios.put(`/api/organizations/${organizationForm.id}`, organizationForm)
    } else {
      // 新增组织
      response = await axios.post('/api/organizations', organizationForm)
    }
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '组织更新成功' : '组织创建成功')
      showAddDialog.value = false
      loadOrganizations()
      loadAllOrganizations()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('保存组织失败:', error)
    ElMessage.error('保存组织失败')
  } finally {
    saving.value = false
  }
}

const deleteOrganization = async (organization) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除组织 "${organization.name}" 吗？如果该组织有子组织，将一并删除。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.delete(`/api/organizations/${organization.id}`)
    if (response.data.success) {
      ElMessage.success('组织删除成功')
      loadOrganizations()
      loadAllOrganizations()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除组织失败:', error)
      ElMessage.error('删除组织失败')
    }
  }
}

const viewUsers = async (organization) => {
  currentOrganization.value = organization
  loadingUsers.value = true
  showUsersDialog.value = true
  
  try {
    // 获取组织及其所有子组织的ID
    const orgIdsResponse = await axios.get(`/api/organizations/${organization.id}/all-ids`)
    if (orgIdsResponse.data.success) {
      const orgIds = orgIdsResponse.data.data
      
      // 根据组织ID获取用户列表
      const usersResponse = await axios.get('/api/users/by-organizations', {
        params: { organizationIds: orgIds.join(',') }
      })
      
      if (usersResponse.data.success) {
        organizationUsers.value = usersResponse.data.data
      } else {
        ElMessage.error('获取用户列表失败')
        organizationUsers.value = []
      }
    } else {
      ElMessage.error('获取组织ID失败')
      organizationUsers.value = []
    }
  } catch (error) {
    console.error('获取组织用户失败:', error)
    ElMessage.error('获取组织用户失败')
    organizationUsers.value = []
  } finally {
    loadingUsers.value = false
  }
}

const resetForm = () => {
  isEdit.value = false
  Object.assign(organizationForm, {
    id: null,
    name: '',
    description: '',
    parentId: null,
    status: 1
  })
  if (organizationFormRef.value) {
    organizationFormRef.value.resetFields()
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadOrganizations()
  loadAllOrganizations()
})
</script>

<style scoped>
.organization-management {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  min-height: 400px;
}

.org-icon {
  margin-right: 5px;
  color: #409eff;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>