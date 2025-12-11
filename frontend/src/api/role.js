/**
 * 角色管理相关API接口
 */
import request from './request.js'

const API_BASE_URL = __BACKEND_API_URL__ + '/api'

/**
 * 获取所有角色列表
 * @returns {Promise} 角色列表
 */
export async function getRoleList() {
  try {
    const response = await fetch(`${API_BASE_URL}/roles`, {
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
    console.error('获取角色列表失败:', error)
    throw error
  }
}

/**
 * 根据ID获取角色详情
 * @param {number} roleId 角色ID
 * @returns {Promise} 角色详情
 */
export async function getRoleById(roleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
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
    console.error('获取角色详情失败:', error)
    throw error
  }
}

/**
 * 创建新角色
 * @param {Object} role 角色对象
 * @param {string} role.roleName 角色名称
 * @param {string} [role.description] 角色描述（可选）
 * @returns {Promise} 创建结果
 */
export async function createRole(role) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(role)
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('创建角色失败:', error)
    throw error
  }
}

/**
 * 更新角色信息
 * @param {number} roleId 角色ID
 * @param {Object} updates 更新数据
 * @returns {Promise} 更新结果
 */
export async function updateRole(roleId, updates) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updates)
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('更新角色失败:', error)
    throw error
  }
}

/**
 * 删除角色
 * @param {number} roleId 角色ID
 * @returns {Promise} 删除结果
 */
export async function deleteRole(roleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('删除角色失败:', error)
    throw error
  }
}

/**
 * 检查角色是否被使用
 * @param {number} roleId 角色ID
 * @returns {Promise<{count:number}>} 用户数量对象
 */
export async function checkRoleInUse(roleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/${roleId}/users/count`, {
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
    console.error('检查角色使用情况失败:', error)
    throw error
  }
}

/**
 * 根据角色ID获取用户列表
 * @param {number} roleId 角色ID
 * @returns {Promise<Array>} 用户列表
 */
export async function getUsersByRoleId(roleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/${roleId}/users`, {
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
    console.error('获取角色下用户列表失败:', error)
    throw error
  }
}

/**
 * 检查角色名称是否存在
 * @param {string} roleName 角色名称
 * @returns {Promise<{exists:boolean}>} 是否存在
 */
export async function checkRoleNameExists(roleName) {
  try {
    const encoded = encodeURIComponent(roleName || '')
    const response = await fetch(`${API_BASE_URL}/roles/check-name?roleName=${encoded}`, {
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
    console.error('检查角色名称是否存在失败:', error)
    // 发生网络错误时，返回 {exists:false} 以不阻塞用户输入，但提交前仍有最终校验
    return { exists: false }
  }
}

/**
 * 批量取消用户的角色授权
 * @param {Array<number>} userIds 用户ID列表
 * @returns {Promise} 操作结果
 */
export async function batchRemoveRoleFromUsers(userIds) {
  try {
    const response = await fetch(`${API_BASE_URL}/roles/batch-remove-users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userIds })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('批量取消角色授权失败:', error)
    throw error
  }
}

/**
 * 批量删除角色
 * @param {Array<number>} roleIds 角色ID列表
 * @returns {Promise} 操作结果
 */
export async function batchDeleteRoles(roleIds) {
  try {
    const response = await request({
      url: '/api/roles/batch',
      method: 'DELETE',
      data: roleIds
    })
    
    return response.data
  } catch (error) {
    console.error('批量删除角色失败:', error)
    throw error
  }
}
