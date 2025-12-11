import request from './request'

const API_BASE_URL = __BACKEND_API_URL__ + '/api'

// 更新项目-标准步骤关系指定字段
export async function updateProjectRelation(relationId, changes) {
  const url = `${API_BASE_URL}/project-relations/${relationId}`
  return request({
    url,
    method: 'PUT',
    data: changes,
  })
}
