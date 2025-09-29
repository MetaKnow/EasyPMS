import request from './request'

/**
 * 运维项目API接口
 */

/**
 * 获取运维项目列表
 * @param {Object} params 查询参数
 * @returns {Promise} 响应数据
 */
export function getAfterserviceProjects(params = {}) {
  return request({
    url: '/api/afterservice-projects',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取运维项目详情
 * @param {number} id 项目ID
 * @returns {Promise} 响应数据
 */
export function getAfterserviceProjectById(id) {
  return request({
    url: `/api/afterservice-projects/${id}`,
    method: 'get'
  })
}

/**
 * 创建运维项目
 * @param {Object} data 项目数据
 * @returns {Promise} 响应数据
 */
export function createAfterserviceProject(data) {
  return request({
    url: '/api/afterservice-projects',
    method: 'post',
    data
  })
}

/**
 * 更新运维项目
 * @param {number} id 项目ID
 * @param {Object} data 项目数据
 * @returns {Promise} 响应数据
 */
export function updateAfterserviceProject(id, data) {
  return request({
    url: `/api/afterservice-projects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除运维项目
 * @param {number} id 项目ID
 * @returns {Promise} 响应数据
 */
export function deleteAfterserviceProject(id) {
  return request({
    url: `/api/afterservice-projects/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除运维项目
 * @param {Array} ids 项目ID数组
 * @returns {Promise} 响应数据
 */
export function batchDeleteAfterserviceProjects(ids) {
  return request({
    url: '/api/afterservice-projects/batch',
    method: 'delete',
    data: { ids }
  })
}