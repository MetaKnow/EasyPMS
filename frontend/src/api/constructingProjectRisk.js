import request from './request'

/**
 * 函数级注释：创建项目风险
 */
export function createConstructingProjectRisk(data) {
  return request({
    url: '/api/constructing-project-risks',
    method: 'post',
    data
  })
}

/**
 * 函数级注释：按项目ID获取风险列表
 */
export function listConstructingProjectRisksByProject(projectId) {
  return request({
    url: `/api/constructing-project-risks/by-project/${projectId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：更新项目风险
 */
export function updateConstructingProjectRisk(id, data) {
  return request({
    url: `/api/constructing-project-risks/${id}`,
    method: 'put',
    data
  })
}

/**
 * 函数级注释：删除项目风险
 */
export function deleteConstructingProjectRisk(id) {
  return request({
    url: `/api/constructing-project-risks/${id}`,
    method: 'delete'
  })
}

/**
 * 函数级注释：上传项目风险附件
 */
export async function uploadConstructingProjectRiskFiles(projectId, riskId, files, options = {}) {
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
  const url = `/api/constructing-project-risk-files/${projectId}/${riskId}/upload`
  return request.post(url, formData, {
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
 * 函数级注释：按风险ID获取附件列表
 */
export function listConstructingProjectRiskFiles(riskId) {
  return request({
    url: `/api/constructing-project-risk-files/by-risk/${riskId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：删除风险附件
 */
export function deleteConstructingProjectRiskFile(fileId) {
  return request({
    url: `/api/constructing-project-risk-files/${fileId}`,
    method: 'delete'
  })
}
