<template>
  <div class="menu-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增菜单
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜单名称" clearable />
        </el-form-item>
        <el-form-item label="菜单编码">
          <el-input v-model="searchForm.code" placeholder="请输入菜单编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMenus">
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

    <!-- 菜单树形表格 -->
    <el-card class="table-card">
      <el-table 
        :data="menuTree" 
        v-loading="loading" 
        stripe 
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all>
        <el-table-column prop="name" label="菜单名称" width="200">
          <template #default="{ row }">
            <el-icon v-if="row.icon" class="menu-icon">
              <component :is="row.icon" />
            </el-icon>
            {{ row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="code" label="菜单编码" width="150" />
        <el-table-column prop="path" label="路径" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="requiredRole" label="所需角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.requiredRole" :type="row.requiredRole === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.requiredRole === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
            <span v-else>无限制</span>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editMenu(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="success" @click="addChildMenu(row)">
              <el-icon><Plus /></el-icon>
              添加子菜单
            </el-button>
            <el-button size="small" type="danger" @click="deleteMenu(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑菜单对话框 -->
    <el-dialog :title="dialogTitle" v-model="showAddDialog" width="600px">
      <el-form :model="menuForm" :rules="menuRules" ref="menuFormRef" label-width="100px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="menuForm.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="code">
          <el-input v-model="menuForm.code" placeholder="请输入菜单编码" />
        </el-form-item>
        <el-form-item label="菜单路径" prop="path">
          <el-input v-model="menuForm.path" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="菜单图标">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="parentMenuOptions"
            :render-after-expand="false"
            placeholder="请选择父级菜单"
            clearable
            check-strictly
            :props="{
              value: 'id',
              label: 'name',
              children: 'children'
            }"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="menuForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="所需角色">
          <el-select v-model="menuForm.requiredRole" placeholder="请选择所需角色" clearable>
            <el-option label="无限制" value="" />
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMenu" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const menuTree = ref([])
const allMenus = ref([])
const showAddDialog = ref(false)
const isEdit = ref(false)
const menuFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  status: null
})

// 菜单表单
const menuForm = reactive({
  id: null,
  name: '',
  code: '',
  path: '',
  icon: '',
  parentId: null,
  sortOrder: 0,
  status: 1,
  requiredRole: ''
})

// 表单验证规则
const menuRules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 1, max: 50, message: '菜单名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入菜单编码', trigger: 'blur' },
    { min: 1, max: 50, message: '菜单编码长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

const parentMenuOptions = computed(() => {
  const options = []
  const buildOptions = (menus, level = 0) => {
    menus.forEach(menu => {
      if (!isEdit.value || menu.id !== menuForm.id) {
        options.push({
          id: menu.id,
          name: '　'.repeat(level) + menu.name,
          children: menu.children ? [] : undefined
        })
        if (menu.children && menu.children.length > 0) {
          buildOptions(menu.children, level + 1)
        }
      }
    })
  }
  buildOptions(menuTree.value)
  return options
})

// 方法
const loadMenus = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/menus/tree')
    if (response.data.success) {
      menuTree.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载菜单列表失败')
    }
  } catch (error) {
    console.error('加载菜单列表失败:', error)
    ElMessage.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllMenus = async () => {
  try {
    const response = await axios.get('/api/menus/all')
    if (response.data.success) {
      allMenus.value = response.data.data
    }
  } catch (error) {
    console.error('加载所有菜单失败:', error)
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    code: '',
    status: null
  })
  loadMenus()
}

const editMenu = (menu) => {
  isEdit.value = true
  Object.assign(menuForm, {
    id: menu.id,
    name: menu.name,
    code: menu.code,
    path: menu.path,
    icon: menu.icon,
    parentId: menu.parentId,
    sortOrder: menu.sortOrder,
    status: menu.status,
    requiredRole: menu.requiredRole || ''
  })
  showAddDialog.value = true
}

const addChildMenu = (parentMenu) => {
  isEdit.value = false
  Object.assign(menuForm, {
    id: null,
    name: '',
    code: '',
    path: '',
    icon: '',
    parentId: parentMenu.id,
    sortOrder: 0,
    status: 1,
    requiredRole: ''
  })
  showAddDialog.value = true
}

const saveMenu = async () => {
  if (!menuFormRef.value) return
  
  try {
    await menuFormRef.value.validate()
    saving.value = true
    
    let response
    if (isEdit.value) {
      // 更新菜单
      response = await axios.put(`/api/menus/${menuForm.id}`, menuForm)
    } else {
      // 新增菜单
      response = await axios.post('/api/menus', menuForm)
    }
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '菜单更新成功' : '菜单创建成功')
      showAddDialog.value = false
      loadMenus()
      loadAllMenus()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  } finally {
    saving.value = false
  }
}

const deleteMenu = async (menu) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单 "${menu.name}" 吗？如果该菜单有子菜单，将一并删除。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.delete(`/api/menus/${menu.id}`)
    if (response.data.success) {
      ElMessage.success('菜单删除成功')
      loadMenus()
      loadAllMenus()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  }
}

const resetForm = () => {
  isEdit.value = false
  Object.assign(menuForm, {
    id: null,
    name: '',
    code: '',
    path: '',
    icon: '',
    parentId: null,
    sortOrder: 0,
    status: 1,
    requiredRole: ''
  })
  if (menuFormRef.value) {
    menuFormRef.value.resetFields()
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadMenus()
  loadAllMenus()
})
</script>

<style scoped>
.menu-management {
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

.menu-icon {
  margin-right: 5px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>