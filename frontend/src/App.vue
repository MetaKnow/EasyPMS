<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
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

/* 占位组件样式 */
.module-view {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.placeholder-content {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.placeholder-content h3 {
  color: #333;
  margin-bottom: 8px;
  font-size: 20px;
}

.placeholder-content p {
  color: #666;
  margin: 0;
  font-size: 14px;
}
</style>
