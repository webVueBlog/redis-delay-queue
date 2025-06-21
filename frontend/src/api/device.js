import request from '@/utils/request'

/**
 * 设备注册中心API
 */
export const deviceApi = {
  /**
   * 注册设备
   * @param {Object} data 设备注册信息
   * @returns {Promise}
   */
  registerDevice(data) {
    return request({
      url: '/api/devices/register',
      method: 'post',
      data
    })
  },

  /**
   * 设备心跳
   * @param {Object} data 心跳信息
   * @returns {Promise}
   */
  deviceHeartbeat(data) {
    return request({
      url: '/api/devices/heartbeat',
      method: 'post',
      data
    })
  },

  /**
   * 获取设备信息
   * @param {String} deviceId 设备ID
   * @returns {Promise}
   */
  getDevice(deviceId) {
    return request({
      url: `/api/devices/${deviceId}`,
      method: 'get'
    })
  },

  /**
   * 分页查询设备
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  queryDevices(params) {
    return request({
      url: '/api/devices',
      method: 'get',
      params
    })
  },

  /**
   * 更新设备信息
   * @param {String} deviceId 设备ID
   * @param {Object} data 设备信息
   * @returns {Promise}
   */
  updateDevice(deviceId, data) {
    return request({
      url: `/api/devices/${deviceId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除设备
   * @param {String} deviceId 设备ID
   * @returns {Promise}
   */
  deleteDevice(deviceId) {
    return request({
      url: `/api/devices/${deviceId}`,
      method: 'delete'
    })
  },

  /**
   * 获取设备统计信息
   * @returns {Promise}
   */
  getStatistics() {
    return request({
      url: '/api/devices/statistics',
      method: 'get'
    })
  },

  /**
   * 批量查询设备状态
   * @param {Array} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchQueryDeviceStatus(deviceIds) {
    return request({
      url: '/api/devices/status/batch',
      method: 'post',
      data: { deviceIds }
    })
  },

  /**
   * 更新设备状态
   * @param {String} deviceId 设备ID
   * @param {String} status 设备状态
   * @returns {Promise}
   */
  updateDeviceStatus(deviceId, status) {
    return request({
      url: `/api/devices/${deviceId}/status`,
      method: 'put',
      data: { status }
    })
  }
}