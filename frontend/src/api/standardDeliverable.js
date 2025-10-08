/**
 * 标准交付物相关API接口
 */

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 分页查询标准交付物列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码（从0开始，默认0）
 * @param {number} params.size 每页大小（默认10）
 * @param {string} params.deliverableName 交付物名称（可选）
 * @param {string} params.systemName 系统名称（可选）
 * @param {string} params.deliverableType 交付物类型（可选）
 * @param {number} params.sstepId 标准步骤ID（可选）
 * @param {number} params.milestoneId 里程碑ID（可选）
 * @param {string} params.sortBy 排序字段（默认deliverableId）
 * @param {string} params.sortDir 排序方向（默认desc）
 * @returns {Promise<Object>} 分页数据
 */
export async function getStandardDeliverables(params = {}) {
  try {
    const queryParams = new URLSearchParams()
    
    // 添加查询参数
    if (params.page !== undefined) queryParams.append('page', params.page)
    if (params.size !== undefined) queryParams.append('size', params.size)
    if (params.deliverableName) queryParams.append('deliverableName', params.deliverableName)
    if (params.systemName) queryParams.append('systemName', params.systemName)
    if (params.deliverableType) queryParams.append('deliverableType', params.deliverableType)
    if (params.sstepId) queryParams.append('sstepId', params.sstepId)
    if (params.milestoneId) queryParams.append('milestoneId', params.milestoneId)
    if (params.sortBy) queryParams.append('sortBy', params.sortBy)
    if (params.sortDir) queryParams.append('sortDir', params.sortDir)

    const response = await fetch(`${API_BASE_URL}/standard-deliverables?${queryParams}`, {
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
    console.error('获取标准交付物列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取标准交付物详情
 * @param {number} deliverableId 交付物ID
 * @returns {Promise<Object>} 交付物详情
 */
export async function getStandardDeliverableById(deliverableId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/${deliverableId}`, {
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
    console.error('获取标准交付物详情失败:', error)
    throw error
  }
}

/**
 * 创建新的标准交付物
 * @param {Object} deliverableData 交付物数据
 * @param {string} deliverableData.deliverableName 交付物名称
 * @param {string} deliverableData.systemName 系统名称
 * @param {string} deliverableData.deliverableType 交付物类型
 * @param {boolean} deliverableData.isMustLoad 是否必须加载
 * @param {number} deliverableData.sstepId 标准步骤ID（可选）
 * @param {number} deliverableData.milestoneId 里程碑ID（可选）
 * @returns {Promise<Object>} 创建的交付物
 */
export async function createStandardDeliverable(deliverableData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json; charset=utf-8'
      },
      body: JSON.stringify(deliverableData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('创建标准交付物失败:', error)
    throw error
  }
}

/**
 * 更新标准交付物
 * @param {number} deliverableId 交付物ID
 * @param {Object} deliverableData 更新的交付物数据
 * @returns {Promise<Object>} 更新后的交付物
 */
export async function updateStandardDeliverable(deliverableId, deliverableData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/${deliverableId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json; charset=utf-8'
      },
      body: JSON.stringify(deliverableData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('更新标准交付物失败:', error)
    throw error
  }
}

/**
 * 删除标准交付物
 * @param {number} deliverableId 交付物ID
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteStandardDeliverable(deliverableId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/${deliverableId}`, {
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
    console.error('删除标准交付物失败:', error)
    throw error
  }
}

/**
 * 批量删除标准交付物
 * @param {number[]} deliverableIds 交付物ID数组
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteStandardDeliverables(deliverableIds) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/batch`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(deliverableIds)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('批量删除标准交付物失败:', error)
    throw error
  }
}

/**
 * 获取所有标准交付物列表
 * @returns {Promise<Object[]>} 交付物列表
 */
export async function getAllStandardDeliverables() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/all`, {
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
    console.error('获取所有标准交付物失败:', error)
    throw error
  }
}

/**
 * 根据系统名称获取标准交付物列表
 * @param {string} systemName 系统名称
 * @returns {Promise<Object[]>} 交付物列表
 */
export async function getStandardDeliverablesBySystemName(systemName) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/by-system/${encodeURIComponent(systemName)}`, {
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
    console.error('根据系统名称获取标准交付物失败:', error)
    throw error
  }
}

/**
 * 根据系统名称和交付物类型获取标准交付物列表
 * @param {string} systemName 系统名称
 * @param {string} deliverableType 交付物类型
 * @returns {Promise<Object[]>} 交付物列表
 */
export async function getStandardDeliverablesBySystemNameAndType(systemName, deliverableType) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/by-system-type/${encodeURIComponent(systemName)}/${encodeURIComponent(deliverableType)}`, {
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
    console.error('根据系统名称和交付物类型获取标准交付物失败:', error)
    throw error
  }
}

/**
 * 根据标准步骤ID获取标准交付物列表
 * @param {number} sstepId 标准步骤ID
 * @returns {Promise<Object[]>} 交付物列表
 */
export async function getStandardDeliverablesByStepId(sstepId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/by-step/${sstepId}`, {
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
    console.error('根据标准步骤ID获取标准交付物失败:', error)
    throw error
  }
}

/**
 * 根据里程碑ID获取标准交付物列表
 * @param {number} milestoneId 里程碑ID
 * @returns {Promise<Object[]>} 交付物列表
 */
export async function getStandardDeliverablesByMilestoneId(milestoneId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/by-milestone/${milestoneId}`, {
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
    console.error('根据里程碑ID获取标准交付物失败:', error)
    throw error
  }
}

/**
 * 获取所有去重的系统名称
 * @returns {Promise<string[]>} 系统名称列表
 */
export async function getDistinctSystemNames() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/distinct-system-names`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    return data.systemNames || []
  } catch (error) {
    console.error('获取系统名称列表失败:', error)
    throw error
  }
}

/**
 * 检查交付物名称是否存在
 * @param {string} deliverableName 交付物名称
 * @param {number} excludeId 排除的交付物ID（用于更新时检查）
 * @returns {Promise<boolean>} 是否存在
 */
export async function checkDeliverableNameExists(deliverableName, excludeId = null) {
  try {
    const queryParams = new URLSearchParams()
    queryParams.append('deliverableName', deliverableName)
    if (excludeId) queryParams.append('excludeId', excludeId)

    const response = await fetch(`${API_BASE_URL}/standard-deliverables/check-name-exists?${queryParams}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    return data.exists || false
  } catch (error) {
    console.error('检查交付物名称是否存在失败:', error)
    throw error
  }
}

/**
 * 获取标准交付物统计信息
 * @returns {Promise<Object>} 统计信息
 */
export async function getStandardDeliverableStats() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-deliverables/stats`, {
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
    console.error('获取标准交付物统计信息失败:', error)
    throw error
  }
}

/**
 * 获取所有去重的产品名称（从基础产品维护模块）
 * @returns {Promise<string[]>} 产品名称列表
 */
export async function getDistinctProductNames() {
  try {
    const response = await fetch(`${API_BASE_URL}/products/distinct-names`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    return data.productNames || []
  } catch (error) {
    console.error('获取产品名称列表失败:', error)
    throw error
  }
}