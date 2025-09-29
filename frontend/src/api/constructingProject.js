import request from './request'

/**
 * 在建项目API接口
 */

/**
 * 获取在建项目列表
 * @param {Object} params 查询参数
 * @returns {Promise} 响应数据
 */
export function getConstructingProjects(params = {}) {
  return request({
    url: '/api/constructing-projects',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取在建项目详情
 * @param {number} id 项目ID
 * @returns {Promise} 响应数据
 */
export function getConstructingProjectById(id) {
  return request({
    url: `/api/constructing-projects/${id}`,
    method: 'get'
  })
}

/**
 * 创建在建项目
 * @param {Object} data 项目数据
 * @returns {Promise} 响应数据
 */
export function createConstructingProject(data) {
  return request({
    url: '/api/constructing-projects',
    method: 'post',
    data
  })
}

/**
 * 更新在建项目
 * @param {number} id 项目ID
 * @param {Object} data 项目数据
 * @returns {Promise} 响应数据
 */
export function updateConstructingProject(id, data) {
  return request({
    url: `/api/constructing-projects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除在建项目
 * @param {number} id 项目ID
 * @returns {Promise} 响应数据
 */
export function deleteConstructingProject(id) {
  return request({
    url: `/api/constructing-projects/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除在建项目
 * @param {Array} ids 项目ID数组
 * @returns {Promise} 响应数据
 */
export function batchDeleteConstructingProjects(ids) {
  return request({
    url: '/api/constructing-projects/batch',
    method: 'delete',
    data: { ids }
  })
}