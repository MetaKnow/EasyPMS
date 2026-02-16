import request from './request'

/**
 * 函数级注释：创建项目周报
 */
export function createConstructingProjectWeeklyReport(data) {
  return request({
    url: '/api/constructing-project-weekly-reports',
    method: 'post',
    data
  })
}

/**
 * 函数级注释：按项目ID获取周报列表
 */
export function listConstructingProjectWeeklyReportsByProject(projectId) {
  return request({
    url: `/api/constructing-project-weekly-reports/by-project/${projectId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：更新项目周报
 */
export function updateConstructingProjectWeeklyReport(id, data) {
  return request({
    url: `/api/constructing-project-weekly-reports/${id}`,
    method: 'put',
    data
  })
}

/**
 * 函数级注释：删除项目周报
 */
export function deleteConstructingProjectWeeklyReport(id) {
  return request({
    url: `/api/constructing-project-weekly-reports/${id}`,
    method: 'delete'
  })
}

/**
 * 函数级注释：上传项目周报附件
 */
export async function uploadConstructingProjectWeeklyReportFiles(projectId, weeklyReportId, files, options = {}) {
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
  const url = `/api/constructing-project-weekly-report-files/${projectId}/${weeklyReportId}/upload`
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
 * 函数级注释：按周报ID获取附件列表
 */
export function listConstructingProjectWeeklyReportFiles(weeklyReportId) {
  return request({
    url: `/api/constructing-project-weekly-report-files/by-weekly-report/${weeklyReportId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：删除周报附件
 */
export function deleteConstructingProjectWeeklyReportFile(fileId) {
  return request({
    url: `/api/constructing-project-weekly-report-files/${fileId}`,
    method: 'delete'
  })
}
