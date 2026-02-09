import request from './request'

/**
 * 类级注释：
 * 合同外需求相关API：创建与按项目查询列表。
 */

/**
 * 函数级注释：创建合同外需求条目
 * @param {Object} data 包含 `projectId`, `requirementName`, 以及其它可选字段
 * @returns {Promise} 响应数据
 */
export function createExtraRequirement(data) {
  return request({
    url: '/api/extra-requirements',
    method: 'post',
    data
  })
}

/**
 * 函数级注释：按项目查询合同外需求条目列表
 * @param {number} projectId 项目ID
 * @returns {Promise} 响应数据
 */
export function listExtraRequirementsByProject(projectId) {
  return request({
    url: `/api/extra-requirements/by-project/${projectId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：更新合同外需求条目
 * @param {number} id 需求ID
 * @param {Object} data 更新数据
 * @returns {Promise} 响应数据
 */
export function updateExtraRequirement(id, data) {
  return request({
    url: `/api/extra-requirements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 函数级注释：删除合同外需求条目
 * @param {number} id 需求ID
 * @returns {Promise}
 */
export function deleteExtraRequirement(id) {
  return request({
    url: `/api/extra-requirements/${id}`,
    method: 'delete'
  })
}

/**
 * 函数级注释：上传合同外需求附件
 * @param {number} projectId 项目ID
 * @param {number} requirementId 需求ID
 * @param {File[]} files 文件列表
 * @param {{ uploaderName?: string, onProgress?: Function }} options
 * @returns {Promise}
 */
export async function uploadExtraRequirementFiles(projectId, requirementId, files, options = {}) {
  const formData = new FormData()
  for (const f of files || []) formData.append('files', f)
  let uploaderName = options.uploaderName
  if (!uploaderName) {
    try {
      const raw = sessionStorage.getItem('userInfo')
      const userInfo = raw ? JSON.parse(raw) : null
      uploaderName = (userInfo && (userInfo.name || userInfo.userName)) || ''
    } catch (_) {}
  }
  if (uploaderName) formData.append('uploaderName', uploaderName)
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null
  const url = `/api/extra-requirement-files/${projectId}/${requirementId}/upload`
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
 * 函数级注释：获取合同外需求附件列表
 * @param {number} requirementId 需求ID
 * @returns {Promise}
 */
export function listExtraRequirementFiles(requirementId) {
  return request({
    url: `/api/extra-requirement-files/by-requirement/${requirementId}`,
    method: 'get'
  })
}

/**
 * 函数级注释：删除合同外需求附件
 * @param {number} fileId 文件ID
 * @returns {Promise}
 */
export function deleteExtraRequirementFile(fileId) {
  return request({
    url: `/api/extra-requirement-files/${fileId}`,
    method: 'delete'
  })
}
