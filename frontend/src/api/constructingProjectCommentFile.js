import request from './request'

const API_BASE_URL = '/api'

export async function uploadConstructingProjectCommentFiles(projectId, commentId, files, options = {}) {
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
  return request.post(`${API_BASE_URL}/constructing-project-comment-files/${projectId}/${commentId}/upload`, formData, {
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

export async function listConstructingProjectCommentFilesByProject(projectId) {
  const resp = await request.get(`${API_BASE_URL}/constructing-project-comment-files/by-project/${projectId}`)
  return resp?.data?.files || []
}

export function getConstructingProjectCommentFilePreviewUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/constructing-project-comment-files/preview/${fileId}`
}

export function getConstructingProjectCommentFileDownloadUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/constructing-project-comment-files/download/${fileId}`
}
