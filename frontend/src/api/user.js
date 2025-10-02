/**
 * 用户管理相关API接口
 */

import request from './request'

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 获取所有用户列表
 * @param {Object} params 查询参数
 * @param {number} params.organId 机构ID（可选）
 * @param {string} params.keyword 搜索关键词（可选）
 * @param {number} params.page 页码（可选）
 * @param {number} params.size 每页大小（可选）
 * @returns {Promise} 用户列表
 */
export async function getUserList(params = {}) {
  try {
    const queryParams = new URLSearchParams()
    
    if (params.organId) {
      queryParams.append('organId', params.organId)
    }
    if (params.keyword) {
      queryParams.append('keyword', params.keyword)
    }
    if (params.page) {
      queryParams.append('page', params.page)
    }
    if (params.size) {
      queryParams.append('size', params.size)
    }
    
    const url = `${API_BASE_URL}/users${queryParams.toString() ? '?' + queryParams.toString() : ''}`
    
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
    console.error('获取用户列表失败:', error)
    throw error
  }
}

/**
 * 根据机构ID获取用户列表
 * @param {number} organId 机构ID
 * @returns {Promise} 用户列表
 */
export async function getUsersByOrganId(organId) {
  try {
    const response = await fetch(`${API_BASE_URL}/organs/${organId}/users`, {
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
    console.error('获取机构用户列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取用户详情
 * @param {number} userId 用户ID
 * @returns {Promise} 用户详情
 */
export async function getUserById(userId) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
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
    console.error('获取用户详情失败:', error)
    throw error
  }
}

/**
 * 创建新用户
 * @param {Object} userData 用户数据
 * @param {string} userData.userName 用户名
 * @param {string} userData.password 密码
 * @param {string} userData.name 姓名（可选）
 * @param {number} userData.organId 机构ID
 * @param {number} userData.roleId 角色ID（可选）
 * @param {boolean} userData.locked 是否锁定（可选）
 * @returns {Promise} 创建结果
 */
export async function createUser(userData) {
  try {
    const response = await fetch(`${API_BASE_URL}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    })
    
    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorMessage
      } catch (jsonError) {
        // 如果响应不是有效的JSON，使用默认错误消息
        console.warn('无法解析错误响应的JSON:', jsonError)
      }
      throw new Error(errorMessage)
    }
    
    return await response.json()
  } catch (error) {
    console.error('创建用户失败:', error)
    throw error
  }
}

/**
 * 更新用户信息
 * @param {number} userId 用户ID
 * @param {Object} userData 用户数据
 * @param {string} userData.name 姓名（可选）
 * @param {number} userData.roleId 角色ID（可选）
 * @param {boolean} userData.locked 是否锁定（可选）
 * @returns {Promise} 更新结果
 */
export async function updateUser(userId, userData) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    })
    
    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorMessage
      } catch (jsonError) {
        // 如果响应不是有效的JSON，使用默认错误消息
        console.warn('无法解析错误响应的JSON:', jsonError)
      }
      throw new Error(errorMessage)
    }
    
    return await response.json()
  } catch (error) {
    console.error('更新用户失败:', error)
    throw error
  }
}

/**
 * 删除用户
 * @param {number} userId 用户ID
 * @returns {Promise} 删除结果
 */
export async function deleteUser(userId) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorMessage
      } catch (jsonError) {
        // 如果响应不是有效的JSON，使用默认错误消息
        console.warn('无法解析错误响应的JSON:', jsonError)
      }
      throw new Error(errorMessage)
    }
    
    return await response.json()
  } catch (error) {
    console.error('删除用户失败:', error)
    throw error
  }
}

/**
 * 修改用户密码
 * @param {number} userId 用户ID
 * @param {Object} passwordData 密码数据
 * @param {string} passwordData.oldPassword 旧密码
 * @param {string} passwordData.newPassword 新密码
 * @returns {Promise} 修改结果
 */
export async function changeUserPassword(userId, passwordData) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/password`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(passwordData)
    })
    
    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('修改密码失败:', error)
    throw error
  }
}

/**
 * 重置用户密码
 * @param {number} userId 用户ID
 * @param {string} newPassword 新密码
 * @returns {Promise} 重置结果
 */
export async function resetUserPassword(userId, newPassword) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/password/reset`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ newPassword })
    })
    
    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('重置密码失败:', error)
    throw error
  }
}

/**
 * 切换用户状态（锁定/解锁）
 * @param {number} userId 用户ID
 * @param {boolean} locked 是否锁定
 * @returns {Promise} 切换结果
 */
export async function toggleUserStatus(userId, locked) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ locked })
    })
    
    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('切换用户状态失败:', error)
    throw error
  }
}



/**
 * 批量删除用户
 * @param {number[]} userIds 用户ID数组
 * @returns {Promise} 删除结果
 */
export async function batchDeleteUsers(userIds) {
  try {
    const response = await request({
      url: '/api/users/batch',
      method: 'delete',
      data: { userIds: userIds }
    })
    
    return response.data
  } catch (error) {
    console.error('批量删除用户失败:', error)
    throw error
  }
}

/**
 * 检查用户名是否存在
 * @param {string} userName 用户名
 * @returns {Promise} 检查结果
 */
export async function checkUserNameExists(userName) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/check-username?userName=${encodeURIComponent(userName)}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    return result.exists
  } catch (error) {
    console.error('检查用户名失败:', error)
    throw error
  }
}