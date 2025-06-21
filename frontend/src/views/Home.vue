<template>
  <div class="home">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h1>欢迎使用Redis延迟队列管理系统</h1>
        <p>这是一个基于Redis的延迟队列系统，支持用户管理和权限控制</p>
      </div>
    </el-card>
    
    <!-- 数据库表结构展示 -->
    <el-card class="table-structure-card">
      <template #header>
        <div class="card-header">
          <span>数据库表结构</span>
          <el-button type="primary" @click="fetchData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="error" class="error-container">
        <el-alert
          title="加载失败"
          :description="error"
          type="error"
          show-icon
          :closable="false"
        />
      </div>
      
      <div v-else>
        <el-collapse v-model="activeNames" class="table-collapse">
          <el-collapse-item
            v-for="table in groupedTables"
            :key="table.tableName"
            :title="`${table.tableName} (${table.columns.length} 个字段)`"
            :name="table.tableName"
          >
            <template #title>
              <div class="table-header">
                <el-icon class="table-icon"><Grid /></el-icon>
                <span class="table-name">{{ table.tableName }}</span>
                <el-tag size="small" class="column-count">
                  {{ table.columns.length }} 个字段
                </el-tag>
                <span v-if="table.tableComment" class="table-comment">
                  - {{ table.tableComment }}
                </span>
              </div>
            </template>
            
            <el-table
              :data="table.columns"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column
                prop="columnName"
                label="字段名"
                width="200"
                sortable
              >
                <template #default="{ row }">
                  <el-tag v-if="row.isPrimaryKey === 'YES'" type="danger" size="small" class="key-tag">
                    PK
                  </el-tag>
                  <el-tag v-else-if="row.isUnique === 'YES'" type="warning" size="small" class="key-tag">
                    UK
                  </el-tag>
                  <span class="column-name">{{ row.columnName }}</span>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnType"
                label="数据类型"
                width="150"
                sortable
              >
                <template #default="{ row }">
                  <el-tag type="info" size="small">
                    {{ row.columnType }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="isNullable"
                label="允许空值"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-tag :type="row.isNullable === 'YES' ? 'success' : 'danger'" size="small">
                    {{ row.isNullable === 'YES' ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnDefault"
                label="默认值"
                width="120"
              >
                <template #default="{ row }">
                  <span v-if="row.columnDefault !== null" class="default-value">
                    {{ row.columnDefault }}
                  </span>
                  <el-tag v-else type="info" size="small">NULL</el-tag>
                </template>
              </el-table-column>
              
              <el-table-column
                prop="columnComment"
                label="字段注释"
                min-width="200"
                show-overflow-tooltip
              >
                <template #default="{ row }">
                  <span v-if="row.columnComment" class="comment">
                    {{ row.columnComment }}
                  </span>
                  <span v-else class="no-comment">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
        
        <div v-if="groupedTables.length === 0" class="empty-state">
          <el-empty description="暂无数据" />
        </div>
      </div>
    </el-card>
    
    <!-- 核心功能区域 - 所有用户可见 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="isAdmin ? 6 : 12">
        <el-card class="stat-card" @click="$router.push('/delay-queue')">
          <div class="stat-content">
            <el-icon class="stat-icon"><Clock /></el-icon>
            <div class="stat-info">
              <h3>延迟队列</h3>
              <p>Redis延迟队列服务</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="isAdmin ? 6 : 12" class="stats-row">
        <el-card class="stat-card" @click="$router.push('/my-tasks')">
          <div class="stat-content">
            <el-icon class="stat-icon"><DataBoard /></el-icon>
            <div class="stat-info">
              <h3>我的任务</h3>
              <p>查看我的队列任务</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="isAdmin ? 6 : 12" v-if="!isAdmin" class="stats-row">
        <el-card class="stat-card" @click="$router.push('/user-permission-organizations')">
          <div class="stat-content">
            <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
            <div class="stat-info">
              <h3>权限组织</h3>
              <p>查看我的权限组织和管理范围</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 管理功能区域 - 仅管理员可见 -->
      <el-col :span="isAdmin ? 6 : 12" class="stats-row">
        <el-card class="stat-card" @click="$router.push('/users')">
          <div class="stat-content">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-info">
              <h3>用户管理</h3>
              <p>管理系统用户和权限</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="isAdmin ? 6 : 12" class="stats-row">
        <el-card class="stat-card" @click="$router.push('/monitor')">
          <div class="stat-content">
            <el-icon class="stat-icon"><Monitor /></el-icon>
            <div class="stat-info">
              <h3>系统监控</h3>
              <p>实时监控系统状态</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
      
    <!-- 管理功能第二行 - 仅管理员可见 -->
    <el-row :gutter="20" class="stats-row" >
      <el-col :span="6">
         <el-card class="stat-card" @click="$router.push('/menus')">
           <div class="stat-content">
             <el-icon class="stat-icon"><Menu /></el-icon>
             <div class="stat-info">
               <h3>菜单管理</h3>
               <p>管理系统菜单结构</p>
             </div>
           </div>
         </el-card>
       </el-col>
       
       <el-col :span="6" class="stats-row">
         <el-card class="stat-card" @click="$router.push('/organizations')">
           <div class="stat-content">
             <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
             <div class="stat-info">
               <h3>组织管理</h3>
               <p>管理组织架构</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6" class="stats-row">
         <el-card class="stat-card" @click="$router.push('/data-statistics')">
           <div class="stat-content">
             <el-icon class="stat-icon"><DataBoard /></el-icon>
             <div class="stat-info">
               <h3>数据统计</h3>
               <p>查看系统数据统计</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6" class="stats-row">
         <el-card class="stat-card" @click="$router.push('/system-settings')">
           <div class="stat-content">
             <el-icon class="stat-icon"><Setting /></el-icon>
             <div class="stat-info">
               <h3>系统设置</h3>
               <p>配置系统参数</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6" class="stats-row">
         <el-card class="stat-card" @click="$router.push('/operation-logs')">
           <div class="stat-content">
             <el-icon class="stat-icon"><Document /></el-icon>
             <div class="stat-info">
               <h3>操作日志</h3>
               <p>查看系统操作记录</p>
             </div>
           </div>
         </el-card>
       </el-col>
    </el-row>
    
    <el-card class="feature-card">
      <h2>系统功能</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="feature-item">
            <h3>用户管理</h3>
            <ul>
              <li>用户注册和登录</li>
              <li>角色权限管理</li>
              <li>用户信息维护</li>
            </ul>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="feature-item">
            <h3>延迟队列</h3>
            <ul>
              <li>基于Redis实现</li>
              <li>支持延迟任务调度</li>
              <li>高性能消息处理</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Clock, Monitor, Menu, OfficeBuilding, DataBoard, Setting, Document, Refresh, Grid } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const userRole = ref(localStorage.getItem('userRole') || '')

// 表格数据相关状态
const loading = ref(false)
const error = ref('')
const tableData = ref([])
const activeNames = ref([])

// 计算属性
const isAdmin = computed(() => userRole.value === 'ADMIN')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 处理已分组的表数据
const groupedTables = computed(() => {
  // 如果数据已经是按表分组的格式，直接使用
  if (Array.isArray(tableData.value) && tableData.value.length > 0 && tableData.value[0].columns) {
    return tableData.value.sort((a, b) => a.tableName.localeCompare(b.tableName))
  }
  
  // 兼容旧格式：如果是扁平的列数据，则重新分组
  const groups = {}
  
  tableData.value.forEach(column => {
    const tableName = column.tableName
    if (!groups[tableName]) {
      groups[tableName] = {
        tableName,
        tableComment: column.tableComment,
        columns: []
      }
    }
    groups[tableName].columns.push(column)
  })
  
  // 转换为数组并排序
  return Object.values(groups).sort((a, b) => a.tableName.localeCompare(b.tableName))
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await request.get('/api/tables/columns')
    tableData.value = response.data
    
    // 默认展开第一个表
    if (groupedTables.value.length > 0) {
      activeNames.value = [groupedTables.value[0].tableName]
    }
    ElMessage.success('数据加载成功')
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '获取数据失败'
    console.error('获取数据失败:', err)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  // 监听存储变化，实时更新用户角色
  window.addEventListener('storage', (e) => {
    if (e.key === 'userRole') {
      userRole.value = e.newValue || ''
    }
  })
  
  // 如果用户已登录，自动加载数据
  if (isLoggedIn.value) {
    fetchData()
  }
})
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
  text-align: center;
}

.welcome-content h1 {
  color: #409eff;
  margin-bottom: 10px;
}

.welcome-content p {
  color: #666;
  font-size: 16px;
}

.table-structure-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.error-container {
  padding: 20px;
}

.table-collapse {
  margin-top: 10px;
}

.table-header {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.table-icon {
  color: #409eff;
}

.table-name {
  font-weight: bold;
  color: #333;
}

.column-count {
  background-color: #f0f9ff;
  color: #409eff;
}

.table-comment {
  color: #666;
  font-style: italic;
}

.key-tag {
  margin-right: 8px;
}

.column-name {
  font-weight: 500;
}

.default-value {
  font-family: 'Courier New', monospace;
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
}

.comment {
  color: #666;
}

.no-comment {
  color: #ccc;
  font-style: italic;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card:active {
  transform: translateY(0);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 40px;
  color: #409eff;
  margin-right: 15px;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.stat-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.feature-card h2 {
  color: #333;
  margin-bottom: 20px;
}

.feature-item h3 {
  color: #409eff;
  margin-bottom: 10px;
}

.feature-item ul {
  list-style-type: none;
  padding: 0;
}

.feature-item li {
  padding: 5px 0;
  color: #666;
}

.feature-item li:before {
  content: "✓";
  color: #67c23a;
  margin-right: 8px;
}
</style>