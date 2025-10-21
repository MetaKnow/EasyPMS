<template>
  <div class="home-page">
    <!-- 顶部Banner -->
    <header class="top-banner" v-if="!$route.meta.fullscreen">
      <div class="banner-left">
        <img src="/favicon.ico" alt="Logo" class="logo" />
        <h1 class="system-title">MissoftPMS</h1>
      </div>
      <div class="banner-right">
        <span class="welcome-text">欢迎，{{ userInfo.username }}</span>
        <button @click="logout" class="logout-btn">退出</button>
      </div>
    </header>

    <div class="main-container" :class="{ fullscreen: $route.meta.fullscreen }">
      <!-- 左侧功能菜单 -->
      <aside class="sidebar" v-if="!$route.meta.fullscreen">
        <nav class="menu">
          <template v-for="item in menuItems" :key="item.id">
            <!-- 普通菜单项 -->
            <div v-if="!item.isGroup" 
                 class="menu-item" 
                 :class="{ active: $route.path === item.path }"
                 @click="navigateToModule(item.path)">
              <i :class="item.icon"></i>
              <span>{{ item.name }}</span>
            </div>
            
            <!-- 菜单组 -->
            <div v-else class="menu-group">
              <div class="menu-group-header" 
                   :class="{ expanded: item.expanded }"
                   @click="toggleMenuGroup(item)">
                <i :class="item.icon"></i>
                <span>{{ item.name }}</span>
                <i class="expand-icon" :class="{ rotated: item.expanded }">▼</i>
              </div>
              
              <div class="menu-group-children" v-show="item.expanded">
                <div v-for="child in item.children" 
                     :key="child.id"
                     class="menu-item child-item" 
                     :class="{ active: $route.path === child.path }"
                     @click="navigateToModule(child.path)">
                  <i :class="child.icon"></i>
                  <span>{{ child.name }}</span>
                </div>
              </div>
            </div>
          </template>
        </nav>
      </aside>

      <!-- 右侧主内容区域 -->
      <main class="main-content">
        <router-view 
          :key="routerViewKey"
          @show-constructing-project-form="showConstructingProjectForm"
          @show-afterservice-project-form="showAfterserviceProjectForm"
        />
      </main>
    </div>

    <!-- 在建项目表单 -->
    <ConstructingProjectForm 
      :visible="constructingProjectFormVisible"
      :project-data="selectedConstructingProject"
      @close="closeConstructingProjectForm"
      @success="onConstructingProjectSuccess"
    />

    <!-- 运维项目表单 -->
    <AfterserviceProjectForm 
      :visible="afterserviceProjectFormVisible"
      :project-data="selectedAfterserviceProject"
      @close="closeAfterserviceProjectForm"
      @success="onAfterserviceProjectSuccess"
    />
  </div>
</template>

<script>
import ConstructingProjectForm from './ConstructingProjectForm.vue'
import AfterserviceProjectForm from './AfterserviceProjectForm.vue'
import { getConstructingProjectById } from '../api/constructingProject.js'

export default {
  name: 'HomePage',
  components: {
    ConstructingProjectForm,
    AfterserviceProjectForm
  },
  data() {
    return {
      userInfo: {
        username: 'admin'
      },
      // 左侧菜单项
      menuItems: [
        { id: 'dashboard', name: '工作台', icon: 'icon-dashboard', path: '/home/dashboard' },
        { id: 'customers', name: '客户管理', icon: 'icon-users', path: '/home/customers' },
        { id: 'construction', name: '在建项目管理', icon: 'icon-building', path: '/home/construction' },
        { id: 'maintenance', name: '运维项目管理', icon: 'icon-tools', path: '/home/maintenance' },
        { 
          id: 'system', 
          name: '系统维护', 
          icon: 'icon-settings', 
          isGroup: true,
          expanded: false,
          children: [
            { id: 'organization', name: '机构用户管理', icon: 'icon-organization', path: '/home/system/organization' },
            { id: 'roles', name: '角色管理', icon: 'icon-shield', path: '/home/system/roles' },
            { id: 'milestones', name: '标准里程碑', icon: 'icon-milestone', path: '/home/system/milestones' },
            { id: 'steps', name: '标准交付步骤', icon: 'icon-steps', path: '/home/system/steps' },
            { id: 'deliverables', name: '标准交付物', icon: 'icon-deliverable', path: '/home/system/deliverables' },
            { id: 'products', name: '基础产品维护', icon: 'icon-product', path: '/home/system/products' },
            { id: 'partners', name: '渠道商维护', icon: 'icon-partner', path: '/home/system/partners' }
          ]
        }
      ],
      // 表单显示状态
      constructingProjectFormVisible: false,
      afterserviceProjectFormVisible: false,
      // 选中的项目数据
      selectedConstructingProject: null,
      selectedAfterserviceProject: null,
      // 路由视图刷新key
      routerViewKey: 0
    }
  },
  mounted() {
    this.loadUserInfo();
  },
  methods: {
    /**
     * 加载用户信息
     */
    loadUserInfo() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        this.userInfo = JSON.parse(userInfo);
      }
    },

    /**
     * 导航到指定模块
     */
    navigateToModule(path) {
      this.$router.push(path);
    },

    /**
     * 切换菜单组的展开/折叠状态
     */
    toggleMenuGroup(item) {
      item.expanded = !item.expanded;
    },

    /**
     * 退出登录
     */
    logout() {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      this.$router.push('/');
    },

    /**
     * 显示在建项目表单
     */
    async showConstructingProjectForm(projectData = null) {
      if (projectData && projectData.projectId) {
        // 编辑模式：获取完整的项目详情
        try {
          const response = await getConstructingProjectById(projectData.projectId);
          if (response.data.success) {
            this.selectedConstructingProject = response.data.data;
          } else {
            console.error('获取项目详情失败:', response.data.message);
            this.selectedConstructingProject = projectData; // 降级使用原数据
          }
        } catch (error) {
          console.error('获取项目详情失败:', error);
          this.selectedConstructingProject = projectData; // 降级使用原数据
        }
      } else {
        // 新建模式
        this.selectedConstructingProject = projectData;
      }
      this.constructingProjectFormVisible = true;
    },

    /**
     * 关闭在建项目表单
     */
    closeConstructingProjectForm() {
      this.constructingProjectFormVisible = false;
      this.selectedConstructingProject = null;
    },

    /**
     * 在建项目操作成功回调
     */
    onConstructingProjectSuccess() {
      this.closeConstructingProjectForm();
      // 如果当前不在在建项目页面，则跳转
      if (this.$route.path !== '/home/construction') {
        this.$router.push('/home/construction');
      } else {
        // 如果已经在在建项目页面，强制刷新组件
        this.routerViewKey++;
      }
    },

    /**
     * 显示运维项目表单
     */
    showAfterserviceProjectForm(projectData = null) {
      this.selectedAfterserviceProject = projectData;
      this.afterserviceProjectFormVisible = true;
    },

    /**
     * 关闭运维项目表单
     */
    closeAfterserviceProjectForm() {
      this.afterserviceProjectFormVisible = false;
      this.selectedAfterserviceProject = null;
    },

    /**
     * 运维项目操作成功回调
     */
    onAfterserviceProjectSuccess() {
      this.closeAfterserviceProjectForm();
      // 如果当前不在运维项目页面，则跳转
      if (this.$route.path !== '/home/maintenance') {
        this.$router.push('/home/maintenance');
      } else {
        // 如果已经在运维项目页面，强制刷新组件
        this.routerViewKey++;
      }
    }
  }
}
</script>

<style scoped>
/* 整体布局 */
.home-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

/* 顶部Banner */
.top-banner {
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.banner-left {
  display: flex;
  align-items: center;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  font-size: 14px;
}

.logout-btn {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.3);
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧边栏 */
.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #e8e8e8;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow-y: auto;
}

.menu {
  padding: 16px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  margin: 4px 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  position: relative;
}

.menu-item:hover {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  color: #2d3748;
  transform: translateX(4px);
}

.menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.menu-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #ffffff;
  border-radius: 0 2px 2px 0;
}

.menu-item i {
  margin-right: 12px;
  font-size: 18px;
  width: 20px;
  text-align: center;
  opacity: 0.8;
}

.menu-item.active i {
  opacity: 1;
}

/* 菜单组样式 */
.menu-group {
  margin: 8px 0;
}

.menu-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  margin: 4px 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #2d3748;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
}

.menu-group-header:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.menu-group-header.expanded {
  background: #f8f9fa;
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.menu-group-header i {
  margin-right: 12px;
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.menu-group-header .expand-icon {
  font-size: 12px;
  transition: transform 0.3s ease;
  margin-left: auto;
  opacity: 0.7;
}

.menu-group-header.expanded .expand-icon {
  transform: rotate(180deg);
  opacity: 1;
}

.menu-group-children {
  padding: 8px 0;
  margin: 0 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 0 0 12px 12px;
  border: 1px solid #e9ecef;
  border-top: none;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.menu-item.child-item {
  padding: 10px 24px;
  margin: 2px 8px;
  font-size: 13px;
  font-weight: 400;
  color: #6c757d;
  border-radius: 6px;
  position: relative;
}

.menu-item.child-item::before {
  content: '';
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: #dee2e6;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.menu-item.child-item:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #f8f9fa 100%);
  color: #495057;
  transform: translateX(6px);
}

.menu-item.child-item:hover::before {
  background: #6c757d;
  transform: translateY(-50%) scale(1.2);
}

.menu-item.child-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.3);
}

.menu-item.child-item.active::before {
  background: white;
  transform: translateY(-50%) scale(1.3);
}

.menu-item.child-item i {
  margin-right: 10px;
  font-size: 16px;
  width: 18px;
}

/* 图标样式 */
.icon-dashboard::before { content: '📊'; }
.icon-users::before { content: '👥'; }
.icon-building::before { content: '🏗️'; }
.icon-tools::before { content: '🔧'; }
.icon-settings::before { content: '⚙️'; }
.icon-organization::before { content: '🏢'; }
.icon-shield::before { content: '🛡️'; }
.icon-milestone::before { content: '🎯'; }
.icon-steps::before { content: '📋'; }
.icon-deliverable::before { content: '📦'; }
.icon-product::before { content: '📱'; }
.icon-partner::before { content: '🤝'; }

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 8px;
  overflow: hidden;
  height: calc(100vh - 60px);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #262626;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.stat-card {
  background: white;
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-content h3 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #262626;
}

.stat-content p {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* 快速操作 */
.actions-section {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.action-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.action-btn.primary {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.action-btn.primary:hover {
  background: #40a9ff;
}

/* 最近项目 */
.recent-projects {
  background: white;
  border-radius: 6px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  transition: all 0.3s;
}

.project-item:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.project-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #262626;
}

.project-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #8c8c8c;
}

.project-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.project-status.active {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.project-status.planning {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.project-status.completed {
  background: #f0f5ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.project-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-small:hover {
  border-color: #1890ff;
  color: #1890ff;
}

/* 图标样式 */
.icon-dashboard::before { content: "📊"; }
.icon-users::before { content: "👥"; }
.icon-building::before { content: "🏗️"; }
.icon-tools::before { content: "🔧"; }
.icon-user::before { content: "👤"; }
.icon-organization::before { content: "🏢"; }
.icon-shield::before { content: "🛡️"; }
.icon-plus::before { content: "➕"; }
.icon-user-plus::before { content: "👤➕"; }
.icon-chart::before { content: "📈"; }

/* 模块视图样式 */
.module-view {
  height: 100%;
  overflow: hidden;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  text-align: center;
  color: #8c8c8c;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.placeholder-content h3 {
  font-size: 24px;
  margin: 0 0 12px 0;
  color: #262626;
}

.placeholder-content p {
  font-size: 16px;
  margin: 0;
  color: #8c8c8c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
  }
  
  .menu {
    display: flex;
    overflow-x: auto;
    padding: 10px;
  }
  
  .menu-item {
    flex-shrink: 0;
    min-width: 120px;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
.main-container.fullscreen .main-content { height: 100vh; }
</style>