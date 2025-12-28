/**
 * 渠道商管理相关API接口
 */

import request from './request.js'

/**
 * 获取渠道商列表（分页查询）
 * @param {Object} params 查询参数
 * @param {number} params.page 页码（从0开始）
 * @param {number} params.size 每页大小
 * @param {string} params.channelName 渠道名称（可选）
 * @param {string} params.contactor 联系人（可选）
 * @param {string} params.phoneNumber 联系方式（可选）
 * @returns {Promise} 渠道商分页数据
 */
export async function getChannelDistributorList(params = {}) {
  try {
    const queryParams = new URLSearchParams()
    
    if (params.page !== undefined) {
      queryParams.append('page', params.page)
    }
    if (params.size !== undefined) {
      queryParams.append('size', params.size)
    }
    if (params.channelName) {
      queryParams.append('channelName', params.channelName)
    }
    if (params.contactor) {
      queryParams.append('contactor', params.contactor)
    }
    if (params.phoneNumber) {
      queryParams.append('phoneNumber', params.phoneNumber)
    }
    if (params.saleDirector !== undefined && params.saleDirector !== null && params.saleDirector !== '') {
      queryParams.append('saleDirector', params.saleDirector)
    }
    
    const response = await request({
      url: `/api/channel-distributors${queryParams.toString() ? '?' + queryParams.toString() : ''}`,
      method: 'GET'
    })
    
    return response.data
  } catch (error) {
    console.error('获取渠道商列表失败:', error)
    throw error
  }
}

/**
 * 创建渠道商
 * @param {Object} channelData 渠道商数据
 * @param {string} channelData.channelName 渠道名称
 * @param {string} channelData.contactor 联系人
 * @param {string} channelData.phoneNumber 联系方式
 * @returns {Promise} 创建结果
 */
export async function createChannelDistributor(channelData) {
  try {
    const response = await request({
      url: '/api/channel-distributors',
      method: 'POST',
      data: channelData
    })
    
    return response.data
  } catch (error) {
    console.error('创建渠道商失败:', error)
    throw error
  }
}

/**
 * 更新渠道商信息
 * @param {number} channelId 渠道商ID
 * @param {Object} channelData 渠道商数据
 * @param {string} channelData.channelName 渠道名称
 * @param {string} channelData.contactor 联系人
 * @param {string} channelData.phoneNumber 联系方式
 * @returns {Promise} 更新结果
 */
export async function updateChannelDistributor(channelId, channelData) {
  try {
    const response = await request({
      url: `/api/channel-distributors/${channelId}`,
      method: 'PUT',
      data: channelData
    })
    
    return response.data
  } catch (error) {
    console.error('更新渠道商失败:', error)
    throw error
  }
}

/**
 * 删除渠道商
 * @param {number} channelId 渠道商ID
 * @returns {Promise} 删除结果
 */
export async function deleteChannelDistributor(channelId) {
  try {
    const response = await request({
      url: `/api/channel-distributors/${channelId}`,
      method: 'DELETE'
    })
    
    return response.data
  } catch (error) {
    console.error('删除渠道商失败:', error)
    throw error
  }
}

/**
 * 批量删除渠道商
 * @param {Array<number>} channelIds 渠道商ID数组
 * @returns {Promise} 批量删除结果
 */
export async function batchDeleteChannelDistributors(channelIds) {
  try {
    const response = await request({
      url: '/api/channel-distributors/batch',
      method: 'DELETE',
      data: { ids: channelIds }
    })
    
    return response.data
  } catch (error) {
    console.error('批量删除渠道商失败:', error)
    throw error
  }
}

/**
 * 检查渠道名称是否可用（判重）
 * @param {string} channelName 渠道名称
 * @param {number} [channelId] 编辑时排除的渠道ID（可选）
 * @returns {Promise<boolean>} 可用返回true，重复返回false
 */
export async function checkChannelNameAvailable(channelName, channelId) {
  try {
    const params = new URLSearchParams()
    params.append('channelName', channelName)
    if (channelId !== undefined && channelId !== null) {
      params.append('channelId', channelId)
    }

    const response = await request({
      url: `/api/channel-distributors/check-name?${params.toString()}`,
      method: 'GET'
    })

    // 后端返回 { available: boolean }
    return Boolean(response?.data?.available)
  } catch (error) {
    console.error('检查渠道名称可用性失败:', error)
    // 判重检查失败时，返回true以避免误杀（提交前还有最终校验）
    return true
  }
}

/**
 * 获取所有渠道商列表（不分页）
 * @returns {Promise<Array>} 渠道商列表
 */
export async function getAllChannelDistributors() {
  try {
    const response = await request({
      url: '/api/channel-distributors/all',
      method: 'GET'
    })
    
    return response.data.data || []
  } catch (error) {
    console.error('获取所有渠道商列表失败:', error)
    return []
  }
}
