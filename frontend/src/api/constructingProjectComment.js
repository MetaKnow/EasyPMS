import request from './request'

export function listConstructingProjectComments(projectId) {
  return request({
    url: `/api/constructing-project-comments/by-project/${projectId}`,
    method: 'get'
  })
}

export function createConstructingProjectComment(data) {
  return request({
    url: '/api/constructing-project-comments',
    method: 'post',
    data
  })
}

export function deleteConstructingProjectComment(commentId, userId) {
  return request({
    url: `/api/constructing-project-comments/${commentId}`,
    method: 'delete'
    ,
    params: { userId }
  })
}
