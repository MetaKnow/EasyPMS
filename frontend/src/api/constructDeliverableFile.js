/**
 * 项目交付物文件相关API接口
 */
import request from './request'

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 函数级注释：上传项目交付物文件
 * 路径：`/api/construct-deliverable-files/{projectId}/{deliverableId}/upload`
 * @param {number} projectId 项目ID
 * @param {number} deliverableId 标准交付物ID
 * @param {File[]} files 文件数组
 * @param {Object} [options] 其他可选参数，如 { uploaderId, projectStepId, nstepId, interfaceId, personalDevId }
 * 行为说明：后端要求 `uploaderId` 不为空。若未在 `options` 提供，将尝试从 `localStorage.userInfo.userId` 自动填充。
 * @returns {Promise<Object>} 后端返回的上传结果（包含 files 列表）
 */
/**
 * 函数级注释：上传项目交付物文件（支持进度回调）
 * - 自动从 `localStorage.userInfo.userId` 填充 `uploaderId`（若未显式传入）。
 * - 使用 axios 实例发送 multipart/form-data 并通过 onUploadProgress 回调上报进度。
 * @param {number} projectId 项目ID
 * @param {number} deliverableId 标准交付物ID
 * @param {File[]} files 文件数组
 * @param {Object} [options] 其他可选参数，如 { uploaderId, projectStepId, nstepId, interfaceId, personalDevId, onProgress }
 * @returns {Promise<Object>} 后端返回的上传结果（包含 files 列表）
 */
export async function uploadConstructDeliverableFiles(projectId, deliverableId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) {
    formData.append('files', f)
  }
  // 若未传 uploaderId，则从 localStorage.userInfo.userId 自动填充
  let uploaderId = options.uploaderId
  if (uploaderId == null) {
    try {
      const raw = localStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      if (userInfo && userInfo.userId != null) {
        uploaderId = userInfo.userId
      }
    } catch (_) {}
  }
  if (uploaderId != null) formData.append('uploaderId', String(uploaderId))
  if (options.projectStepId != null) formData.append('projectStepId', String(options.projectStepId))
  if (options.nstepId != null) formData.append('nstepId', String(options.nstepId))
  if (options.interfaceId != null) formData.append('interfaceId', String(options.interfaceId))
  if (options.personalDevId != null) formData.append('personalDevId', String(options.personalDevId))

  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  const url = `/api/construct-deliverable-files/${projectId}/${deliverableId}/upload`
  const response = await request.post(url, formData, {
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
  return response?.data
}

/**
 * 函数级注释：列出项目交付物文件
 * 路径：`/api/construct-deliverable-files/{projectId}/{deliverableId}`
 * @param {number} projectId 项目ID
 * @param {number} deliverableId 标准交付物ID
 * @returns {Promise<Array>} 文件信息列表
 */
export async function listConstructDeliverableFiles(projectId, deliverableId) {
  try {
    const response = await fetch(`${API_BASE_URL}/construct-deliverable-files/${projectId}/${deliverableId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    return Array.isArray(data) ? data : (data.files || [])
  } catch (error) {
    console.error('获取项目交付物文件列表失败:', error)
    throw error
  }
}

/**
 * 函数级注释：删除项目交付物文件
 * 路径：`/api/construct-deliverable-files/{fileId}`
 * @param {number} fileId 文件记录ID
 * @returns {Promise<void>}
 */
export async function deleteConstructDeliverableFile(fileId) {
  try {
    const response = await fetch(`${API_BASE_URL}/construct-deliverable-files/${fileId}`, {
      method: 'DELETE'
    })
    if (!response.ok) {
      let errMsg = `HTTP error! status: ${response.status}`
      try {
        const err = await response.json()
        errMsg = err.message || errMsg
      } catch {}
      throw new Error(errMsg)
    }
  } catch (error) {
    console.error('删除项目交付物文件失败:', error)
    throw error
  }
}