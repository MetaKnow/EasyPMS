import request from './request'

const API_BASE_URL = '/api'

export function getCustomerFollowUps(params = {}) {
  return request({
    url: '/api/customer-follow-ups',
    method: 'get',
    params
  })
}

export function getAllCustomerFollowUps(afterServiceProjectId) {
  return request({
    url: '/api/customer-follow-ups/all',
    method: 'get',
    params: { afterServiceProjectId }
  })
}

export function deleteCustomerFollowUp(id) {
  return request({
    url: `/api/customer-follow-ups/${id}`,
    method: 'delete'
  })
}

export function createCustomerFollowUp(data) {
  return request({
    url: '/api/customer-follow-ups',
    method: 'post',
    data
  })
}

export function updateCustomerFollowUp(id, data) {
  return request({
    url: `/api/customer-follow-ups/${id}`,
    method: 'put',
    data
  })
}

export async function uploadCustomerFollowUpFiles(projectId, recordId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) formData.append('files', f)
  let uploaderName = options.uploaderName
  if (!uploaderName) {
    try {
      const raw = sessionStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      uploaderName = userInfo?.name || userInfo?.userName || userInfo?.userId
    } catch (_) {}
  }
  if (uploaderName != null) formData.append('uploaderName', String(uploaderName))
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  return request.post(`${API_BASE_URL}/customer-follow-up-files/${projectId}/${recordId}/upload`, formData, {
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

export async function listCustomerFollowUpFiles(recordId) {
  const resp = await request.get(`${API_BASE_URL}/customer-follow-up-files/by-record/${recordId}`)
  return resp?.data?.files || []
}

export function getCustomerFollowUpPreviewUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/customer-follow-up-files/preview/${fileId}`
}

export function getCustomerFollowUpPreviewPdfUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/customer-follow-up-files/preview/pdf/${fileId}`
}

export function getCustomerFollowUpPreviewVideoUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/customer-follow-up-files/preview/video/${fileId}`
}

export function getCustomerFollowUpDownloadUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/customer-follow-up-files/download/${fileId}`
}

export async function deleteCustomerFollowUpFile(fileId) {
  return request.delete(`${API_BASE_URL}/customer-follow-up-files/${fileId}`)
}
