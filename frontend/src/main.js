import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const originalFetch = window.fetch.bind(window)
window.fetch = (input, init = {}) => {
  const nextInit = { ...init }
  const headers = new Headers(init.headers || {})
  const token = sessionStorage.getItem('token')
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const userInfoRaw = sessionStorage.getItem('userInfo')
  if (userInfoRaw && !headers.has('X-User-Id')) {
    try {
      const userInfo = JSON.parse(userInfoRaw)
      if (userInfo && userInfo.userId != null) {
        headers.set('X-User-Id', String(userInfo.userId))
      }
    } catch (e) {
      console.error('解析userInfo失败:', e)
    }
  }
  nextInit.headers = headers
  return originalFetch(input, nextInit)
}

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
