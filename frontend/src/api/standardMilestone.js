/**
 * 标准里程碑相关API接口
 */

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 获取标准里程碑列表（分页）
 * @param {Object} params 查询参数对象
 * @param {number} params.page 页码（从0开始）
 * @param {number} params.size 每页大小
 * @param {string} params.milestoneName 里程碑名称（可选）
 * @param {string} params.sortBy 排序字段
 * @param {string} params.sortDir 排序方向
 * @returns {Promise<Object>} 里程碑分页数据
 */
export async function getStandardMilestoneList(params = {}) {
  try {
    const {
      page = 0,
      size = 10,
      milestoneName = '',
      sortBy = 'createTime',
      sortDir = 'asc'
    } = params

    const urlParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    })

    if (milestoneName && milestoneName.trim()) {
      urlParams.append('milestoneName', milestoneName.trim())
    }

    const response = await fetch(`${API_BASE_URL}/standard-milestones?${urlParams}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('获取标准里程碑列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取标准里程碑详情
 * @param {number} milestoneId 里程碑ID
 * @returns {Promise<Object>} 里程碑详情
 */
export async function getStandardMilestoneById(milestoneId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones/${milestoneId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('获取标准里程碑详情失败:', error)
    throw error
  }
}

/**
 * 创建标准里程碑
 * @param {Object} milestoneData 里程碑数据
 * @returns {Promise<Object>} 创建结果
 */
export async function createStandardMilestone(milestoneData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(milestoneData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('创建标准里程碑失败:', error)
    throw error
  }
}

/**
 * 更新标准里程碑
 * @param {number} milestoneId 里程碑ID
 * @param {Object} milestoneData 更新的里程碑数据
 * @returns {Promise<Object>} 更新结果
 */
export async function updateStandardMilestone(milestoneId, milestoneData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones/${milestoneId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(milestoneData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('更新标准里程碑失败:', error)
    throw error
  }
}

/**
 * 删除标准里程碑
 * @param {number} milestoneId 里程碑ID
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteStandardMilestone(milestoneId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones/${milestoneId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('删除标准里程碑失败:', error)
    throw error
  }
}

/**
 * 批量删除标准里程碑
 * @param {Array<number>} milestoneIds 里程碑ID列表
 * @returns {Promise<Object>} 删除结果
 */
export async function batchDeleteStandardMilestones(milestoneIds) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones/batch`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(milestoneIds)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('批量删除标准里程碑失败:', error)
    throw error
  }
}

/**
 * 检查里程碑名称是否存在（支持编辑场景排除当前ID）
 * @param {string} milestoneName 里程碑名称
 * @param {number} [milestoneId] 编辑时的里程碑ID，用于排除自身
 * @returns {Promise<{exists:boolean}>} 是否存在
 */
export async function checkMilestoneNameExists(milestoneName, milestoneId) {
  try {
    const nameEncoded = encodeURIComponent(milestoneName || '')
    const url = `${API_BASE_URL}/standard-milestones/check-name?milestoneName=${nameEncoded}${milestoneId ? `&milestoneId=${milestoneId}` : ''}`

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('检查里程碑名称是否存在失败:', error)
    // 网络错误时返回 {exists:false}，避免阻塞输入；提交前仍进行最终校验
    return { exists: false }
  }
}

/**
 * 获取所有标准里程碑列表（不分页）
 * @returns {Promise<Object>} 所有里程碑列表
 */
export async function getAllStandardMilestones() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-milestones/all`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('获取所有标准里程碑列表失败:', error)
    throw error
  }
}