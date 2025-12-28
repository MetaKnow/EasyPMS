<template>
  <div class="system-backup-view">
    <div class="page-header">
      <h2 class="page-title">系统备份</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="triggerBackup" :disabled="loading">手动备份</button>
      </div>
    </div>

    <div class="table-section">
      <div class="table-container">
        <table class="backup-table">
          <thead>
            <tr>
              <th width="80">序号</th>
              <th>备份文件名</th>
              <th width="200">备份时间</th>
              <th width="100">备份状态</th>
              <th width="120">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center">加载中...</td>
            </tr>
            <tr v-else-if="backups.length === 0">
              <td colspan="5" class="text-center">暂无备份记录</td>
            </tr>
            <tr v-for="(backup, index) in backups" :key="backup.id">
              <td>{{ index + 1 }}</td>
              <td>{{ backup.fileName }}</td>
              <td>{{ formatTime(backup.backupDate) }}</td>
              <td>
                <span :class="getStatusClass(backup.backupState)">{{ backup.backupState }}</span>
              </td>
              <td>
                <button 
                  class="btn btn-sm btn-outline-primary" 
                  @click="downloadBackup(backup)"
                  v-if="backup.backupState === '成功'"
                >
                  下载
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="tips-section">
      <h4>备份策略说明：</h4>
      <ul>
        <li>备份方式：数据库完整备份</li>
        <li>备份时间：每天凌晨 01:00 自动执行</li>
        <li>保留策略：保留最近 15 天的成功备份记录</li>
        <li>存储路径：项目根目录 database/backup</li>
      </ul>
    </div>
  </div>
</template>

<script>
import request from '../api/request'
import moment from 'moment'

export default {
  name: 'SystemBackup',
  data() {
    return {
      backups: [],
      loading: false
    }
  },
  mounted() {
    this.loadBackups()
  },
  methods: {
    async loadBackups(showLoading = true) {
      if (showLoading) this.loading = true
      try {
        const response = await request.get('/api/backups/list')
        if (response.data && response.data.success) {
          this.backups = response.data.data || []
        } else {
          console.error('Failed to load backups')
        }
      } catch (error) {
        console.error('Error loading backups:', error)
      } finally {
        if (showLoading) this.loading = false
      }
    },
    async downloadBackup(backup) {
      if (!backup || !backup.id) return
      
      try {
        const response = await request.get(`/api/backups/download/${backup.id}`, {
          responseType: 'blob'
        })
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', backup.fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Download failed:', error)
        alert('下载失败')
      }
    },
    async triggerBackup() {
      if (!confirm('确定要立即执行备份吗？')) return
      this.loading = true
      try {
        const resp = await request.post('/api/backups/trigger')
        if (resp && resp.data && resp.data.success === false) {
          throw new Error(resp.data.message || '触发备份失败')
        }
        await this.loadBackups(false)
        alert('备份完成')
      } catch (error) {
        console.error('Trigger failed:', error)
        alert('触发备份失败')
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return '-'
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    getStatusClass(state) {
      return state === '成功' ? 'text-success' : 'text-danger'
    }
  }
}
</script>

<style scoped>
.system-backup-view {
  padding: 0px;
  background: #f5f5f5;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.table-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  overflow: hidden;
}

.table-container {
  overflow: auto;
  flex: 1;
  max-height: calc(100vh - 260px);
}

.backup-table {
  width: 100%;
  border-collapse: collapse;
}

.backup-table th,
.backup-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.backup-table th {
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
  position: sticky;
  top: 0;
  z-index: 1;
}

.backup-table tr:hover {
  background-color: #f5f5f5;
}

.text-center {
  text-align: center;
}

.text-success {
  color: #67c23a;
  font-weight: 500;
}

.text-danger {
  color: #f56c6c;
  font-weight: 500;
}

.tips-section {
  margin-top: 8px;
  padding: 12px 16px;
  background-color: #fdf6ec;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  color: #e6a23c;
}

.tips-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.tips-section ul {
  margin: 0;
  padding-left: 20px;
}

.tips-section li {
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.btn {
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #66b1ff;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
}

.btn-outline-primary {
  background-color: transparent;
  border: 1px solid #409eff;
  color: #409eff;
}

.btn-outline-primary:hover {
  background-color: #ecf5ff;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
