import request from './request'

export function createInterface(data) {
  return request({
    url: '/api/interfaces',
    method: 'post',
    data
  })
}

export function listInterfacesByProject(projectId) {
  return request({
    url: `/api/interfaces/by-project/${projectId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：按ID删除接口
 * @param {number} interfaceId 接口ID
 * @returns {Promise} 响应数据
 */
export function deleteInterface(interfaceId) {
  return request({
    url: `/api/interfaces/${interfaceId}`,
    method: 'delete'
  })
}