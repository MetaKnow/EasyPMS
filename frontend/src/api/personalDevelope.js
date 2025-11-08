import request from './request'

/**
 * 类级注释：
 * 个性化开发相关API：创建个性化需求与按项目查询列表。
 */

/**
 * 函数级注释：创建个性化开发条目
 * @param {Object} data 包含 `projectId`, `milestoneId`, `personalDevName`
 */
export function createPersonalDevelope(data) {
  return request({
    url: '/api/personal-developes',
    method: 'post',
    data
  })
}

/**
 * 函数级注释：按项目查询个性化开发条目列表
 * @param {number} projectId 项目ID
 */
export function listPersonalDevelopesByProject(projectId) {
  return request({
    url: `/api/personal-developes/by-project/${projectId}`,
    method: 'get'
  })
}