/**
 * 基础产品维护相关API接口
 */

const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 获取所有产品列表（不分页）
 * @returns {Promise<Array>} 产品列表
 */
export async function getAllProducts() {
  try {
    const response = await fetch(`${API_BASE_URL}/products/all`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    // 后端返回格式：{products: [...]}
    return result.products || []
  } catch (error) {
    console.error('获取产品列表失败:', error)
    return []
  }
}

/**
 * 检查产品名称+版本是否存在（支持编辑场景排除当前ID）
 * @param {string} softName 产品名称
 * @param {string} softVersion 产品版本
 * @param {number} [softId] 编辑时的产品ID，用于排除自身
 * @returns {Promise<{exists:boolean}>} 是否存在
 */
export async function checkProductNameExists(softName, softVersion, softId) {
  try {
    const nameEncoded = encodeURIComponent(softName || '')
    const versionEncoded = encodeURIComponent(softVersion || '')
    const url = `${API_BASE_URL}/products/check-name?softName=${nameEncoded}&softVersion=${versionEncoded}${softId ? `&softId=${softId}` : ''}`

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
    console.error('检查产品名称与版本是否存在失败:', error)
    // 网络错误时返回 {exists:false}，避免阻塞输入；提交前仍进行最终校验
    return { exists: false }
  }
}