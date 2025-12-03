<template>
  <div class="dashboard-view">
    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <h2 class="section-title">数据概览</h2>
      <div class="stats-grid">
        <div class="stat-card" v-for="stat in statistics" :key="stat.id" :style="{ '--card-color': stat.color, '--icon-color': stat.color, '--icon-color-light': stat.lightColor }">
          <div class="stat-icon">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-content">
            <h3>{{ stat.value }}</h3>
            <p>{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </div>



    <!-- 最近项目列表 -->
    <div class="recent-projects">
      <h2 class="section-title">最近项目</h2>
      <div class="project-list">
        <div class="project-item" v-for="project in recentProjects" :key="project.id">
          <div class="project-info">
            <h4>{{ project.name }}</h4>
            <p>{{ project.description }}</p>
            <span class="project-status" :class="project.status">{{ getStatusText(project.status) }}</span>
          </div>
          <div class="project-actions">
            <button class="btn-small" @click="viewProject(project.id)">查看</button>
            <button class="btn-small" @click="editProject(project.id)">编辑</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      // 统计数据
      statistics: [
        { id: 1, label: '在建项目', value: '12', icon: 'icon-building', color: '#1890ff', lightColor: '#40a9ff' },
        { id: 2, label: '运维项目', value: '8', icon: 'icon-tools', color: '#52c41a', lightColor: '#73d13d' },
        { id: 3, label: '客户数量', value: '25', icon: 'icon-users', color: '#fa8c16', lightColor: '#ffa940' },
        { id: 4, label: '团队成员', value: '15', icon: 'icon-user', color: '#722ed1', lightColor: '#9254de' }
      ],
      // 最近项目
      recentProjects: [
        { id: 1, name: '智慧城市建设项目', description: '某市智慧城市基础设施建设', status: 'active' },
        { id: 2, name: '企业ERP系统', description: '大型企业资源规划系统开发', status: 'planning' },
        { id: 3, name: '移动应用开发', description: '跨平台移动应用程序开发', status: 'completed' }
      ]
    }
  },
  mounted() {
    this.loadStatistics();
  },
  methods: {
    /**
     * 加载统计数据
     */
    loadStatistics() {
      // TODO: 从后端API获取实际统计数据
      console.log('加载统计数据');
    },

    /**
     * 获取状态文本
     */
    getStatusText(status) {
      const statusMap = {
        'active': '进行中',
        'planning': '规划中',
        'completed': '已完成',
        'paused': '已暂停'
      };
      return statusMap[status] || '未知状态';
    },

    /**
     * 查看项目
     */
    viewProject(projectId) {
      console.log('查看项目:', projectId);
      // TODO: 实现查看项目详情功能
    },

    /**
     * 编辑项目
     */
    editProject(projectId) {
      console.log('编辑项目:', projectId);
      // TODO: 实现编辑项目功能
    }
  }
}
</script>

<style scoped>
.dashboard-view {
  padding: 0;
  background: #f8f9fa;
  min-height: 100%;
}

.stats-section {
  margin-bottom: 20px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #1890ff;
  position: relative;
}

.section-title::before {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, #1890ff, #40a9ff);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--card-color, #1890ff), transparent);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #1890ff;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 20px;
  background: linear-gradient(135deg, var(--icon-color, #1890ff), var(--icon-color-light, #40a9ff));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content h3 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #333;
  background: linear-gradient(135deg, #333, #666);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-content p {
  font-size: 14px;
  color: #666;
  margin: 0;
  font-weight: 500;
}


.recent-projects {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  border-radius: 8px;
  overflow: hidden;
}

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  position: relative;
}

.project-item:last-child {
  border-bottom: none;
}

.project-item:hover {
  background: linear-gradient(135deg, #f8f9fa, #e6f7ff);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.project-info h4 {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.project-info p {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #666;
}

.project-status {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.project-status.active {
  background: linear-gradient(135deg, #f6ffed, #d9f7be);
  color: #389e0d;
  border: 1px solid #95de64;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
}

.project-status.planning {
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  color: #d46b08;
  border: 1px solid #ffbb96;
  box-shadow: 0 2px 4px rgba(250, 140, 22, 0.2);
}

.project-status.completed {
  background: linear-gradient(135deg, #f0f5ff, #d6e4ff);
  color: #0958d9;
  border: 1px solid #69b1ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.project-actions {
  display: flex;
  gap: 6px;
}

.btn-small {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 3px;
  background: white;
  color: #333;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.btn-small:hover {
  border-color: #1890ff;
  color: #1890ff;
}

  /* 响应式设计 */
  @media (max-width: 768px) {
    .stats-grid {
      grid-template-columns: 1fr;
    }
    
    .project-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
  }
  
  .project-actions {
    align-self: flex-end;
  }
}
</style>
