/**
 * 机构管理相关API接口
 */

const API_BASE_URL = __BACKEND_API_URL__ + '/api'

/**
 * 获取机构树
 * @returns {Promise} 机构树数据
 */
export async function getOrganTree() {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/tree`, {
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
    console.error('获取机构树失败:', error)
    throw error
  }
}

/**
 * 获取所有机构列表（平铺）
 * @returns {Promise} 机构列表
 */
export async function getOrganList() {
  try {
    const response = await fetch(`${API_BASE_URL}/organs`, {
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
    console.error('获取机构列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取机构详情
 * @param {number} organId 机构ID
 * @returns {Promise} 机构详情
 */
export async function getOrganById(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}`, {
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
    console.error('获取机构详情失败:', error)
    throw error
  }
}

/**
 * 创建新机构
 * @param {Object} organData 机构数据
 * @param {string} organData.organName 机构名称
 * @param {number} organData.parentOrganId 父机构ID
 * @returns {Promise} 创建结果
 */
export async function createOrgan(organData) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(organData)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('创建机构失败:', error)
    throw error
  }
}

/**
 * 更新机构信息
 * @param {number} organId 机构ID
 * @param {Object} organData 机构数据
 * @param {string} organData.organName 机构名称
 * @returns {Promise} 更新结果
 */
export async function updateOrgan(organId, organData) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(organData)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('更新机构失败:', error)
    throw error
  }
}

/**
 * 删除机构
 * @param {number} organId 机构ID
 * @returns {Promise} 删除结果
 */
export async function deleteOrgan(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}`, {
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
    console.error('删除机构失败:', error)
    throw error
  }
}

/**
 * 检查机构下是否有用户
 * @param {number} organId 机构ID
 * @returns {Promise} 检查结果
 */
export async function checkOrganHasUsers(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}/users/count`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    return result.count > 0
  } catch (error) {
    console.error('检查机构用户失败:', error)
    throw error
  }
}

/**
 * 检查机构及其所有子机构的用户详情（函数级注释：获取机构及其子机构的详细用户信息，用于删除前的确认提示）
 * @param {number} organId 机构ID
 * @returns {Promise} 检查结果，包含详细的用户信息
 */
export async function checkOrganAndChildrenUsersDetail(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}/check-users-detail`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    return result.data
  } catch (error) {
    console.error('检查机构用户详情失败:', error)
    throw error
  }
}

/**
 * 检查机构下是否有子机构
 * @param {number} organId 机构ID
 * @returns {Promise} 检查结果
 */
export async function checkOrganHasChildren(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}/children/count`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    return result.count > 0
  } catch (error) {
    console.error('检查子机构失败:', error)
    throw error
  }
}

/**
 * 初始化默认机构（管软信息技术（北京）有限公司）
 * @returns {Promise} 初始化结果
 */
export async function initDefaultOrgan() {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/init-default`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('初始化默认机构失败:', error)
    throw error
  }
}
