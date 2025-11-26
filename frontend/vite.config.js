import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// 读取 pms-config.json
const configPath = path.resolve(__dirname, '../pms-config.json')
let pmsConfig = { backend: { host: 'localhost', port: 8081 }, frontend: { host: 'localhost', port: 5173 } }

if (fs.existsSync(configPath)) {
  try {
    pmsConfig = JSON.parse(fs.readFileSync(configPath, 'utf-8'))
    console.log('Loaded pms-config.json:', pmsConfig)
  } catch (e) {
    console.error('Failed to load pms-config.json:', e)
  }
}

// https://vite.dev/config/
export default defineConfig({
  server: {
    host: pmsConfig.frontend.host,
    port: pmsConfig.frontend.port
  },
  define: {
    // 注入全局变量供前端代码使用
    __BACKEND_API_URL__: JSON.stringify(`http://${pmsConfig.backend.host}:${pmsConfig.backend.port}`)
  },
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
