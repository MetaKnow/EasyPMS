import request from './request'

/**
 * 函数级注释：创建项目评论回复
 */
export function createConstructingProjectCommentReply(data) {
  return request({
    url: '/api/constructing-project-comment-replies',
    method: 'post',
    data
  })
}

/**
 * 函数级注释：按评论获取回复列表
 */
export function listConstructingProjectCommentReplies(commentId) {
  return request({
    url: `/api/constructing-project-comment-replies/by-comment/${commentId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：删除评论回复
 */
export function deleteConstructingProjectCommentReply(replyId, userId) {
  return request({
    url: `/api/constructing-project-comment-replies/${replyId}`,
    method: 'delete',
    params: { userId }
  })
}
