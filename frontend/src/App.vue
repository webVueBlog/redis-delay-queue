<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>数据库表字段查看器</h1>
        <el-button type="primary" @click="fetchData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </el-header>
      
      <el-main>
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
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

export default {
  name: 'App',
  setup() {
    const loading = ref(false)
    const error = ref('')
    const tableData = ref([])
    const activeNames = ref([])
    
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
        const response = await axios.get('/api/tables/columns')
        tableData.value = response.data
        
        // 默认展开第一个表
        if (groupedTables.value.length > 0) {
          activeNames.value = [groupedTables.value[0].tableName]
        }
      } catch (err) {
        error.value = err.response?.data?.message || err.message || '获取数据失败'
        console.error('获取数据失败:', err)
      } finally {
        loading.value = false
      }
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchData()
    })
    
    return {
      loading,
      error,
      tableData,
      activeNames,
      groupedTables,
      fetchData
    }
  }
}
</script>

<style scoped>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.el-header {
  background-color: #545c64;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.loading-container,
.error-container {
  padding: 20px;
}

.table-collapse {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.table-icon {
  margin-right: 8px;
  color: #409eff;
}

.table-name {
  font-weight: 600;
  font-size: 16px;
  margin-right: 12px;
}

.column-count {
  margin-right: 12px;
}

.table-comment {
  color: #909399;
  font-size: 14px;
}

.key-tag {
  margin-right: 8px;
}

.column-name {
  font-weight: 500;
}

.default-value {
  font-family: 'Courier New', monospace;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.comment {
  color: #606266;
}

.no-comment {
  color: #c0c4cc;
  font-style: italic;
}

.empty-state {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-table {
  margin-top: 16px;
}

.el-collapse-item__content {
  padding: 0 20px 20px 20px;
}
</style>