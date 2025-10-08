/**
 * 标准交付步骤相关API接口
 */

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 分页查询标准交付步骤列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码（从0开始，默认0）
 * @param {number} params.size 每页大小（默认10）
 * @param {string} params.sstepName 步骤名称（可选）
 * @param {string} params.type 步骤类型（可选）
 * @param {string} params.systemName 系统名称（可选）
 * @param {number} params.smilestoneId 里程碑ID（可选）
 * @param {string} params.sortBy 排序字段（默认sstepId）
 * @param {string} params.sortDir 排序方向（默认desc）
 * @returns {Promise<Object>} 分页数据
 */
export async function getStandardConstructSteps(params = {}) {
  try {
    const queryParams = new URLSearchParams()
    
    // 添加查询参数
    if (params.page !== undefined) queryParams.append('page', params.page)
    if (params.size !== undefined) queryParams.append('size', params.size)
    if (params.sstepName) queryParams.append('sstepName', params.sstepName)
    if (params.type) queryParams.append('type', params.type)
    if (params.systemName) queryParams.append('systemName', params.systemName)
    if (params.smilestoneId) queryParams.append('smilestoneId', params.smilestoneId)
    if (params.sortBy) queryParams.append('sortBy', params.sortBy)
    if (params.sortDir) queryParams.append('sortDir', params.sortDir)

    const response = await fetch(`${API_BASE_URL}/standard-construct-steps?${queryParams}`, {
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
    console.error('获取标准交付步骤列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取标准交付步骤详情
 * @param {number} sstepId 步骤ID
 * @returns {Promise<Object>} 步骤详情
 */
export async function getStandardConstructStepById(sstepId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/${sstepId}`, {
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
    console.error('获取标准交付步骤详情失败:', error)
    throw error
  }
}

/**
 * 创建新的标准交付步骤
 * @param {Object} stepData 步骤数据
 * @param {string} stepData.sstepName 步骤名称
 * @param {string} stepData.type 步骤类型
 * @param {string} stepData.systemName 系统名称
 * @param {number} stepData.smilestoneId 里程碑ID
 * @returns {Promise<Object>} 创建的步骤
 */
export async function createStandardConstructStep(stepData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(stepData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('创建标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 更新标准交付步骤
 * @param {number} sstepId 步骤ID
 * @param {Object} stepData 更新的步骤数据
 * @returns {Promise<Object>} 更新后的步骤
 */
export async function updateStandardConstructStep(sstepId, stepData) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/${sstepId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(stepData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('更新标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 删除标准交付步骤
 * @param {number} sstepId 步骤ID
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteStandardConstructStep(sstepId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/${sstepId}`, {
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
    console.error('删除标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 批量删除标准交付步骤
 * @param {number[]} sstepIds 步骤ID数组
 * @returns {Promise<Object>} 删除结果
 */
export async function deleteStandardConstructSteps(sstepIds) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/batch`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(sstepIds)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('批量删除标准交付步骤失败:', error)
    throw error
  }
}



/**
 * 获取所有标准交付步骤列表
 * @returns {Promise<Object[]>} 步骤列表
 */
export async function getAllStandardConstructSteps() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/all`, {
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
    console.error('获取所有标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 根据系统名称获取标准交付步骤列表
 * @param {string} systemName 系统名称
 * @returns {Promise<Object[]>} 步骤列表
 */
export async function getStandardConstructStepsBySystemName(systemName) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/by-system?systemName=${encodeURIComponent(systemName)}`, {
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
    console.error('根据系统名称获取标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 根据里程碑ID获取标准交付步骤列表
 * @param {number} smilestoneId 里程碑ID
 * @returns {Promise<Object[]>} 步骤列表
 */
export async function getStandardConstructStepsByMilestoneId(smilestoneId) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/by-milestone?smilestoneId=${smilestoneId}`, {
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
    console.error('根据里程碑ID获取标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 根据步骤类型获取标准交付步骤列表
 * @param {string} type 步骤类型
 * @returns {Promise<Object[]>} 步骤列表
 */
export async function getStandardConstructStepsByType(type) {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/by-type/${encodeURIComponent(type)}`, {
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
    console.error('根据步骤类型获取标准交付步骤失败:', error)
    throw error
  }
}

/**
 * 获取所有去重的系统名称
 * @returns {Promise<string[]>} 系统名称列表
 */
export async function getDistinctSystemNames() {
  try {
    const response = await fetch(`${API_BASE_URL}/standard-construct-steps/distinct-system-names`, {
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