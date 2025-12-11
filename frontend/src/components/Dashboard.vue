<template>
  <div class="dashboard-view">
    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <h2 class="section-title">数据概览</h2>
      <div class="stats-grid">
        <div class="stat-card" v-for="stat in statistics" :key="stat.id" :style="{ '--card-color': stat.color }">
          <div class="stat-content">
            <h3>{{ stat.value }}</h3>
            <p>{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </div>



    
  </div>
</template>

<script>
import { getAfterserviceProjects } from '../api/afterserviceProject'
export default {
  name: 'Dashboard',
  /**
   * 类级注释：Dashboard（工作台）组件
   * 展示“数据概览”统计，分两行显示8项：
   * 1) 建设中项目、2) 已完成项目（来源：在建项目管理）；
   * 3) 免费运维、4) 付费运维、5) 运维期满、6) 暂停运维（来源：运维项目管理，按运维状态统计）；
   * 7) 自主运维、8) 配合运维（来源：运维项目管理，按运维类型统计）。
   */
  data() {
    return {
      // 统计数据（两行共8项）
      statistics: [
        { id: 1, label: '建设中项目', value: '0', icon: 'icon-building', color: '#1890ff', lightColor: '#40a9ff' },
        { id: 2, label: '已完成项目', value: '0', icon: 'icon-building', color: '#52c41a', lightColor: '#73d13d' },
        { id: 3, label: '免费运维项目', value: '0', icon: 'icon-tools', color: '#fa8c16', lightColor: '#ffa940' },
        { id: 4, label: '付费运维项目', value: '0', icon: 'icon-tools', color: '#d46b08', lightColor: '#ffbb96' },
        { id: 5, label: '运维期满项目', value: '0', icon: 'icon-tools', color: '#0958d9', lightColor: '#69b1ff' },
        { id: 6, label: '暂停运维项目', value: '0', icon: 'icon-tools', color: '#722ed1', lightColor: '#9254de' },
        { id: 7, label: '自主运维项目', value: '0', icon: 'icon-user', color: '#13c2c2', lightColor: '#36cfc9' },
        { id: 8, label: '配合运维项目', value: '0', icon: 'icon-user', color: '#2f54eb', lightColor: '#597ef7' }
      ]
    }
  },
  mounted() {
    this.loadStatistics();
  },
  methods: {
    /**
     * 加载统计数据（函数级注释）
     * - 在建项目：调用后端状态统计与分页接口获取总数；
     * - 运维项目：按运维状态通过分页接口获取总数；
     * - 运维类型：由于后端列表不支持类型筛选，分页遍历累加统计。
     */
    async loadStatistics() {
      try {
        // 在建项目状态统计（使用后端计数接口）
        const [constructRunning, constructCompleted] = await Promise.all([
          this.countConstructingByState('进行中'),
          this.countConstructingByState('已完成')
        ])

        // 运维项目按状态统计（接口支持 status 参数，读取 total）
        const [freeMaint, paidMaint, expiredMaint, pausedMaint] = await Promise.all([
          this.countAfterserviceByStatus('免费运维期'),
          this.countAfterserviceByStatus('付费运维'),
          this.countAfterserviceByStatus('无付费运维'),
          this.countAfterserviceByStatus('暂停运维')
        ])

        // 运维项目按类型统计（分页遍历累加）
        const [fullMaint, coopMaint] = await this.countAfterserviceByTypes(['我公司全权运维', '我公司配合远程运维'])

        // 写入统计值
        this.setStatValue(1, constructRunning)
        this.setStatValue(2, constructCompleted)
        this.setStatValue(3, freeMaint)
        this.setStatValue(4, paidMaint)
        this.setStatValue(5, expiredMaint)
        this.setStatValue(6, pausedMaint)
        this.setStatValue(7, fullMaint)
        this.setStatValue(8, coopMaint)
      } catch (e) {
        console.error('加载统计数据失败:', e)
      }
    },

    /**
     * 设置统计项值（函数级注释）
     * @param {number} id 统计项ID（1..8）
     * @param {number} value 数值
     */
    setStatValue(id, value) {
      const idx = this.statistics.findIndex(s => s.id === id)
      if (idx !== -1) this.statistics[idx].value = String(value ?? 0)
    },

    /**
     * 统计在建项目（函数级注释：按项目状态计数）
     * @param {string} state 项目状态（如：进行中、已完成）
     * @returns {Promise<number>} 计数
     */
    async countConstructingByState(state) {
      // eslint-disable-next-line no-undef
      const backendBase = typeof __BACKEND_API_URL__ !== 'undefined' ? __BACKEND_API_URL__ : 'http://localhost:8081'
      const url = `${backendBase}/api/constructing-projects/count/state/${encodeURIComponent(state)}`
      const resp = await fetch(url)
      if (!resp.ok) throw new Error('在建项目状态统计失败: ' + resp.status)
      const data = await resp.json()
      return (data && data.success) ? Number(data.data || 0) : 0
    },

    /**
     * 统计运维项目（函数级注释：按运维状态读取分页总数）
     * @param {string} status 运维状态（免费运维期/付费运维/无付费运维/暂停运维）
     * @returns {Promise<number>} 计数
     */
    async countAfterserviceByStatus(status) {
      const resp = await getAfterserviceProjects({ page: 1, size: 1, status })
      const ok = resp && resp.data && resp.data.success
      return ok ? Number((resp.data.data && resp.data.data.total) || 0) : 0
    },

    /**
     * 统计运维项目（函数级注释：按运维类型分页遍历累加）
     * @param {string[]} types 类型数组，传入顺序即返回顺序
     * @returns {Promise<[number, number]>} 对应类型计数数组
     */
    async countAfterserviceByTypes(types) {
      const pageSize = 200
      let page = 1
      let pages = 1
      let counts = types.map(() => 0)
      // 首次请求以获得总页数
      let resp = await getAfterserviceProjects({ page, size: pageSize })
      if (!(resp && resp.data && resp.data.success)) return counts
      pages = Number((resp.data.data && resp.data.data.pages) || 1)
      const accumulate = list => {
        for (const p of (list || [])) {
          const t = p && p.serviceType
          const idx = types.indexOf(t)
          if (idx !== -1) counts[idx]++
        }
      }
      accumulate((resp.data.data && resp.data.data.list) || [])
      for (page = 2; page <= pages; page++) {
        resp = await getAfterserviceProjects({ page, size: pageSize })
        if (!(resp && resp.data && resp.data.success)) break
        accumulate((resp.data.data && resp.data.data.list) || [])
      }
      return counts
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
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
}

.stat-card {
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
  min-width: 0;
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
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #2d3748;
}

.stat-content p {
  font-size: 14px;
  color: #4a5568;
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
    .stats-grid { grid-template-columns: repeat(2, 1fr); }
    
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
