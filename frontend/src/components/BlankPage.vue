<template>
  <div class="blank-page">
    <div class="content">
      <div class="welcome-message">
        <h1>欢迎使用 MissoftPMS</h1>
        <p>软件项目管理系统</p>
        <div class="user-info" v-if="userInfo">
          <p>欢迎您，{{ userInfo.name || userInfo.userName }}！</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const userInfo = ref(null)

/**
 * 组件挂载时获取用户信息
 */
onMounted(() => {
  // 从sessionStorage获取用户信息
  const storedUserInfo = sessionStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
})
</script>

<style scoped>
.blank-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.content {
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.welcome-message h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-message p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 30px;
  font-weight: 500;
}

.user-info {
  margin-top: 30px;
  padding: 20px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.user-info p {
  margin: 0;
  font-size: 1.1rem;
  color: #667eea;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content {
    padding: 40px 20px;
  }
  
  .welcome-message h1 {
    font-size: 2rem;
  }
  
  .welcome-message p {
    font-size: 1rem;
  }
}

/* 动画效果 */
.content {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>