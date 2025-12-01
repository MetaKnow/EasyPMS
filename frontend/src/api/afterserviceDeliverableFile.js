/**
 * 运维事件附件相关API
 */
import request from './request'

const API_BASE_URL = '/api'

/**
 * 上传运维事件附件
 * 保存至 afterServiceDeliverableFiles/<项目编号-项目名称>/<事件时间-事件名称>/，文件名“事件名称-上传时间”。
 * @param {number} projectId
 * @param {number} eventId
 * @param {File[]} files
 * @param {{ uploaderId?: number, onProgress?: Function }} options
 */
export async function uploadAfterserviceDeliverableFiles(projectId, eventId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) formData.append('files', f)
  let uploaderId = options.uploaderId
  if (uploaderId == null) {
    try {
      const raw = localStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      if (userInfo && userInfo.userId != null) uploaderId = userInfo.userId
    } catch (_) {}
  }
  if (uploaderId != null) formData.append('uploaderId', String(uploaderId))
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  return request.post(`${API_BASE_URL}/afterservice-deliverable-files/${projectId}/${eventId}/upload`, formData, {
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
 * 列出运维事件附件
 */
export async function listAfterserviceDeliverableFiles(projectId, eventId) {
  const resp = await request.get(`${API_BASE_URL}/afterservice-deliverable-files/${projectId}/${eventId}`)
  return resp?.data?.files || []
}

/**
 * 获取预览/下载URL
 */
export function getAfterserviceDeliverablePreviewUrl(fileId) {
  // 使用全局后端地址，避免在开发环境下跳转到前端服务器首页
  // eslint-disable-next-line no-undef
  const backendBase = typeof __BACKEND_API_URL__ !== 'undefined' ? __BACKEND_API_URL__ : 'http://localhost:8081'
  return `${backendBase}${API_BASE_URL}/afterservice-deliverable-files/preview/${fileId}`
}

export function getAfterserviceDeliverablePreviewPdfUrl(fileId) {
  // eslint-disable-next-line no-undef
  const backendBase = typeof __BACKEND_API_URL__ !== 'undefined' ? __BACKEND_API_URL__ : 'http://localhost:8081'
  return `${backendBase}${API_BASE_URL}/afterservice-deliverable-files/preview/pdf/${fileId}`
}

export function getAfterserviceDeliverablePreviewVideoUrl(fileId) {
  // eslint-disable-next-line no-undef
  const backendBase = typeof __BACKEND_API_URL__ !== 'undefined' ? __BACKEND_API_URL__ : 'http://localhost:8081'
  return `${backendBase}${API_BASE_URL}/afterservice-deliverable-files/preview/video/${fileId}`
}

export function getAfterserviceDeliverableDownloadUrl(fileId) {
  // eslint-disable-next-line no-undef
  const backendBase = typeof __BACKEND_API_URL__ !== 'undefined' ? __BACKEND_API_URL__ : 'http://localhost:8081'
  return `${backendBase}${API_BASE_URL}/afterservice-deliverable-files/download/${fileId}`
}

/** 删除附件 */
export async function deleteAfterserviceDeliverableFile(fileId) {
  return request.delete(`${API_BASE_URL}/afterservice-deliverable-files/${fileId}`)
}
