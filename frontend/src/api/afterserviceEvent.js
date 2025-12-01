import request from './request'

/**
 * 运维事件API接口
 */

/**
 * 获取某项目的运维事件（分页）
 * @param {Object} params { afterServiceProjectId, page, size }
 */
export function getAfterserviceEvents(params = {}) {
  return request({
    url: '/api/afterservice-events',
    method: 'get',
    params
  })
}

/**
 * 获取某项目的全部运维事件（不分页）
 * @param {number} afterServiceProjectId 项目ID
 */
export function getAllAfterserviceEvents(afterServiceProjectId) {
  return request({
    url: '/api/afterservice-events/all',
    method: 'get',
    params: { afterServiceProjectId }
  })
}

/**
 * 删除运维事件
 * @param {number} id 事件ID
 */
export function deleteAfterserviceEvent(id) {
  return request({
    url: `/api/afterservice-events/${id}`,
    method: 'delete'
  })
}

/**
 * 创建运维事件
 * @param {Object} data 事件数据
 */
export function createAfterserviceEvent(data) {
  return request({
    url: '/api/afterservice-events',
    method: 'post',
    data
  })
}

/**
 * 更新运维事件
 * @param {number} id 事件ID
 * @param {Object} data 事件数据
 */
export function updateAfterserviceEvent(id, data) {
  return request({
    url: `/api/afterservice-events/${id}`,
    method: 'put',
    data
  })
}
