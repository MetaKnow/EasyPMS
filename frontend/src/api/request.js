import axios from 'axios'

/**
 * 创建axios实例
 */
const request = axios.create({
  baseURL: 'http://localhost:8081', // 后端API基础URL（已改为8081）
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/'
    }
    
    return Promise.reject(error)
  }
)

export default request