import request from './request'

const API_BASE_URL = '/api'

/**
 * 函数级注释：上传评论回复附件
 */
export async function uploadConstructingProjectCommentReplyFiles(projectId, commentId, replyId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) formData.append('files', f)
  let uploaderId = options.uploaderId
  if (uploaderId == null) {
    try {
      const raw = sessionStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      if (userInfo && userInfo.userId != null) uploaderId = userInfo.userId
    } catch (_) {}
  }
  if (uploaderId != null) formData.append('uploaderId', String(uploaderId))
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  return request.post(`${API_BASE_URL}/constructing-project-comment-reply-files/${projectId}/${commentId}/${replyId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (pe) => {
      try {
        const total = pe.total || 0
        const loaded = pe.loaded || 0
        const percent = total ? Math.round((loaded / total) * 100) : (loaded > 0 ? 100 : 0)
        if (onProgress) onProgress(percent, pe)
      } catch (_) {}
    }
  })
}

/**
 * 函数级注释：按评论获取回复附件
 */
export async function listConstructingProjectCommentReplyFilesByComment(commentId) {
  const resp = await request.get(`${API_BASE_URL}/constructing-project-comment-reply-files/by-comment/${commentId}`)
  return resp?.data?.files || []
}

/**
 * 函数级注释：获取回复附件预览URL
 */
export function getConstructingProjectCommentReplyFilePreviewUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/constructing-project-comment-reply-files/preview/${fileId}`
}

/**
 * 函数级注释：获取回复附件下载URL
 */
export function getConstructingProjectCommentReplyFileDownloadUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/constructing-project-comment-reply-files/download/${fileId}`
}
