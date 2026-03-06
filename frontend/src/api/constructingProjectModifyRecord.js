import request from './request'

export function listConstructingProjectModifyRecordsByProject(projectId) {
  return request({
    url: `/api/constructing-project-modify-records/by-project/${projectId}`,
    method: 'get'
  })
}
