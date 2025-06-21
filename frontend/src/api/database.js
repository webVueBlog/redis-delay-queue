import request from '@/utils/request'

/**
 * 获取连接池状态信息
 */
export const getConnectionPoolStatus = () => {
  return request.get('/api/connection-pool/status')
}

/**
 * 执行健康检查
 */
export const performHealthCheck = () => {
  return request.post('/api/connection-pool/health-check')
}

/**
 * 运行性能测试
 * @param {Object} params - 测试参数
 * @param {number} params.threadCount - 线程数
 * @param {number} params.operationsPerThread - 每线程操作数
 * @param {number} params.testDurationSeconds - 测试持续时间
 */
export const runPerformanceTest = (params = {}) => {
  return request.post('/api/connection-pool/performance-test', params)
}

/**
 * 预热连接池
 */
export const warmUpConnectionPools = () => {
  return request.post('/api/connection-pool/warmup')
}

/**
 * 获取连接池配置信息
 */
export const getConnectionPoolConfig = () => {
  return request.get('/api/connection-pool/config')
}

/**
 * 获取数据库信息
 */
export const getDatabaseInfo = () => {
  return request.get('/api/connection-pool/database-info')
}

/**
 * 获取连接池详细信息
 */
export const getConnectionPoolDetails = () => {
  return request.get('/api/connection-pool/details')
}

/**
 * 导出连接池报告
 * @param {string} format - 导出格式 (pdf, excel)
 */
export const exportConnectionPoolReport = (format = 'pdf') => {
  return request.get(`/api/connection-pool/export/${format}`, {
    responseType: 'blob'
  })
}

/**
 * 获取连接池历史数据
 * @param {Object} params - 查询参数
 * @param {string} params.period - 时间周期 (1h, 6h, 24h, 7d)
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export const getConnectionPoolHistory = (params) => {
  return request.get('/api/connection-pool/history', { params })
}

/**
 * 重置连接池
 * @param {string} poolName - 连接池名称 (primary, user)
 */
export const resetConnectionPool = (poolName) => {
  return request.post(`/api/connection-pool/reset/${poolName}`)
}

/**
 * 更新连接池配置
 * @param {string} poolName - 连接池名称
 * @param {Object} config - 配置参数
 */
export const updateConnectionPoolConfig = (poolName, config) => {
  return request.put(`/api/connection-pool/config/${poolName}`, config)
}