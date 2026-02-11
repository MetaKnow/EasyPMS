import request from './request'

const API_BASE_URL = '/api'

export async function uploadAfterserviceLeadFiles(projectId, leadsId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) formData.append('files', f)
  let uploaderId = options.uploaderId
  if (uploaderId == null) {
    try {
      const raw = sessionStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      uploaderId = userInfo?.userId ?? null
    } catch (_) {}
  }
  if (uploaderId != null) formData.append('uploaderId', String(uploaderId))
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  return request.post(`${API_BASE_URL}/afterservice-lead-files/${projectId}/${leadsId}/upload`, formData, {
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

export async function listAfterserviceLeadFiles(leadsId) {
  const resp = await request.get(`${API_BASE_URL}/afterservice-lead-files/by-lead/${leadsId}`)
  return resp?.data?.files || []
}

export function getAfterserviceLeadPreviewUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/afterservice-lead-files/preview/${fileId}`
}

export function getAfterserviceLeadPreviewPdfUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/afterservice-lead-files/preview/pdf/${fileId}`
}

export function getAfterserviceLeadPreviewVideoUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/afterservice-lead-files/preview/video/${fileId}`
}

export function getAfterserviceLeadDownloadUrl(fileId) {
  const backendBase = __BACKEND_API_URL__
  return `${backendBase}${API_BASE_URL}/afterservice-lead-files/download/${fileId}`
}

export async function deleteAfterserviceLeadFile(fileId) {
  return request.delete(`${API_BASE_URL}/afterservice-lead-files/${fileId}`)
}
