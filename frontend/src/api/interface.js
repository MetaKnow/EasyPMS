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