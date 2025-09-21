<template>
  <div id="app">
    <!-- 根据登录状态显示不同页面 -->
    <BlankPage v-if="isLoggedIn" />
    <LoginPage v-else @login-success="handleLoginSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LoginPage from './components/LoginPage.vue'
import BlankPage from './components/BlankPage.vue'

const isLoggedIn = ref(false)

/**
 * 处理登录成功事件
 */
const handleLoginSuccess = () => {
  isLoggedIn.value = true
}

/**
 * 检查用户是否已登录
 */
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  
  if (token && userInfo) {
    isLoggedIn.value = true
  }
}

/**
 * 组件挂载时检查登录状态
 */
onMounted(() => {
  checkLoginStatus()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.6;
  color: #333;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
