/**
 * 客户管理相关API接口
 */

import request from './request.js'

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 获取客户列表（分页查询）
 * @param {Object} params 查询参数
 * @param {number} params.page 页码（从0开始）
 * @param {number} params.size 每页大小
 * @param {string} params.customerName 客户名称（可选）
 * @param {string} params.contact 联系人（可选）
 * @param {string} params.province 省份（可选）
 * @param {string} params.customerRank 客户等级（可选）
 * @returns {Promise} 客户分页数据
 */
export async function getCustomerList(params = {}) {
  try {
    const queryParams = new URLSearchParams()
    
    if (params.page !== undefined) {
      queryParams.append('page', params.page)
    }
    if (params.size !== undefined) {
      queryParams.append('size', params.size)
    }
    if (params.customerName) {
      queryParams.append('customerName', params.customerName)
    }
    if (params.contact) {
      queryParams.append('contact', params.contact)
    }
    if (params.province) {
      queryParams.append('province', params.province)
    }
    if (params.customerRank) {
      queryParams.append('customerRank', params.customerRank)
    }
    
    const response = await request({
      url: `/api/customers${queryParams.toString() ? '?' + queryParams.toString() : ''}`,
      method: 'GET'
    })
    
    return response.data
  } catch (error) {
    console.error('获取客户列表失败:', error)
    throw error
  }
}

/**
 * 获取所有客户列表（不分页）
 * @returns {Promise<Array>} 所有客户数据
 */
export async function getAllCustomers() {
  try {
    const response = await request({
      url: '/api/customers/all',
      method: 'GET'
    })
    
    // 后端返回格式：{success: true, data: [...]}
    return response.data?.data || []
  } catch (error) {
    console.error('获取所有客户列表失败:', error)
    return []
  }
}

/**
 * 检查客户名称是否可用（用于保存前判重）
 * @param {string} customerName 客户名称
 * @param {number} [customerId] 编辑模式下排除的客户ID，可选
 * @returns {Promise<boolean>} true 表示可用，false 表示已存在
 */
export async function checkCustomerNameAvailable(customerName, customerId) {
  try {
    const queryParams = new URLSearchParams()
    if (customerName) {
      queryParams.append('customerName', customerName)
    }
    if (customerId !== undefined && customerId !== null) {
      queryParams.append('customerId', customerId)
    }

    const response = await request({
      url: `/api/customers/check-name${queryParams.toString() ? '?' + queryParams.toString() : ''}`,
      method: 'GET'
    })

    return !!(response.data && response.data.available)
  } catch (error) {
    console.error('检查客户名称失败:', error)
    // 网络或其他错误不阻断提交逻辑，返回可用以免误伤用户流程
    return true
  }
}

/**
 * 创建新客户
 * @param {Object} customerData 客户数据
 * @returns {Promise} 创建结果
 */
export async function createCustomer(customerData) {
  try {
    const response = await request({
      url: '/api/customers',
      method: 'POST',
      data: customerData
    })
    
    return response.data
  } catch (error) {
    console.error('创建客户失败:', error)
    // 提取后端返回的明确错误信息（如：客户名称已存在）
    let message = error.message
    if (error.response && error.response.data) {
      const data = error.response.data
      message = data.message || data.error || message
    }
    const err = new Error(message)
    err.response = error.response
    throw err
  }
}

/**
 * 更新客户信息
 * @param {number} customerId 客户ID
 * @param {Object} customerData 客户数据
 * @returns {Promise} 更新结果
 */
export async function updateCustomer(customerId, customerData) {
  try {
    const response = await request({
      url: `/api/customers/${customerId}`,
      method: 'PUT',
      data: customerData
    })
    
    return response.data
  } catch (error) {
    console.error('更新客户失败:', error)
    // 提取后端返回的明确错误信息（如：客户名称已被其他客户使用）
    let message = error.message
    if (error.response && error.response.data) {
      const data = error.response.data
      message = data.message || data.error || message
    }
    const err = new Error(message)
    err.response = error.response
    throw err
  }
}

/**
 * 删除客户
 * @param {number} customerId 客户ID
 * @returns {Promise} 删除结果
 */
export async function deleteCustomer(customerId) {
  try {
    const response = await request({
      url: `/api/customers/${customerId}`,
      method: 'DELETE'
    })
    
    return response.data
  } catch (error) {
    console.error('删除客户失败:', error)
    throw error
  }
}

/**
 * 批量删除客户
 * @param {Array} customerIds 客户ID数组
 * @returns {Promise} 删除结果
 */
export async function batchDeleteCustomers(customerIds) {
  try {
    const response = await request({
      url: '/api/customers/batch',
      method: 'DELETE',
      data: customerIds
    })
    
    return response.data
  } catch (error) {
    console.error('批量删除客户失败:', error)
    throw error
  }
}