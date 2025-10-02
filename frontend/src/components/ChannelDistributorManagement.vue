<template>
  <div class="channel-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">渠道商维护</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增渠道商
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedChannels.length === 0">
          <i class="icon-delete"></i>
          删除渠道商
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          v-model="searchForm.channelName" 
          type="text" 
          placeholder="渠道名称"
          class="search-input"
        />
        <input 
          v-model="searchForm.contactor" 
          type="text" 
          placeholder="联系人"
          class="search-input"
        />
        <input 
          v-model="searchForm.phoneNumber" 
          type="text" 
          placeholder="联系方式"
          class="search-input"
        />
        <button class="btn btn-primary" @click="searchChannels">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 渠道商列表 -->
    <div class="table-section">
      <div class="table-container">
        <table class="channel-table">
          <thead>
            <tr>
              <th width="40">
                <input 
                  type="checkbox" 
                  :checked="isAllSelected"
                  @change="selectAll"
                />
              </th>
              <th width="60">序号</th>
              <th>渠道名称</th>
              <th>联系人</th>
              <th>联系方式</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(channel, index) in channels" 
              :key="channel.channelId"
              :class="{ selected: isSelected(channel) }"
              @click="toggleSelectChannel(channel)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="isSelected(channel)"
                  @change.stop="toggleSelectChannel(channel)"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ channel.channelName }}</td>
              <td>{{ channel.contactor || '-' }}</td>
              <td>{{ channel.phoneNumber || '-' }}</td>
              <td>{{ formatDate(channel.createdAt) }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editChannel(channel)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteChannel(channel)">
                  删除
                </button>
              </td>
            </tr>
            <tr v-if="channels.length === 0">
              <td colspan="7" class="no-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <button 
          class="btn btn-secondary" 
          @click="prevPage" 
          :disabled="currentPage <= 1"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ totalElements }} 条记录
        </span>
        <button 
          class="btn btn-secondary" 
          @click="nextPage" 
          :disabled="currentPage >= totalPages"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 渠道商表单弹窗 -->
    <ChannelDistributorForm
      :visible="showForm"
      :mode="formMode"
      :channel-data="currentChannel"
      @close="closeForm"
      @success="handleFormSuccess"
      @error="handleFormError"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除渠道商 "{{ deleteTarget?.channelName }}" 吗？</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmDelete">删除</button>
        </div>
      </div>
    </div>

    <!-- 批量删除确认弹窗 -->
    <div v-if="showBatchDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认批量删除</h3>
        <p>确定要删除选中的 {{ selectedChannels.length }} 个渠道商吗？</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">删除</button>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message.show" :class="['message', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script>
import ChannelDistributorForm from './ChannelDistributorForm.vue'
import { 
  getChannelDistributorList, 
  deleteChannelDistributor, 
  batchDeleteChannelDistributors 
} from '../api/channelDistributor.js'

export default {
  name: 'ChannelDistributorManagement',
  components: {
    ChannelDistributorForm
  },
  data() {
    return {
      /**
       * 渠道商列表数据
       */
      channels: [],
      /**
       * 选中的渠道商
       */
      selectedChannels: [],
      /**
       * 搜索表单数据
       */
      searchForm: {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      },
      /**
       * 分页信息
       */
      currentPage: 1,
      pageSize: 20,
      totalElements: 0,
      totalPages: 0,
      /**
       * 表单相关状态
       */
      showForm: false,
      formMode: 'add', // 'add' | 'edit'
      currentChannel: null,
      /**
       * 删除确认相关状态
       */
      showDeleteConfirm: false,
      showBatchDeleteConfirm: false,
      deleteTarget: null,
      /**
       * 加载状态
       */
      loading: false,
      /**
       * 消息提示
       */
      message: {
        show: false,
        type: 'success', // 'success' | 'error'
        text: ''
      }
    }
  },
  computed: {
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.channels.length > 0 && this.selectedChannels.length === this.channels.length
    }
  },
  mounted() {
    this.loadChannels()
  },
  methods: {
    /**
     * 加载渠道商列表
     */
    async loadChannels() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize,
          ...this.searchForm
        }
        
        const response = await getChannelDistributorList(params)
        this.channels = response.content || []
        this.totalElements = response.totalElements || 0
        this.totalPages = response.totalPages || 0
        this.selectedChannels = []
      } catch (error) {
        console.error('加载渠道商列表失败:', error)
        this.showMessage('加载渠道商列表失败', 'error')
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索渠道商
     */
    searchChannels() {
      this.currentPage = 1
      this.loadChannels()
    },

    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      }
      this.currentPage = 1
      this.loadChannels()
    },

    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadChannels()
      }
    },

    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadChannels()
      }
    },

    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.currentChannel = null
      this.showForm = true
    },

    /**
     * 编辑渠道商
     */
    editChannel(channel) {
      this.formMode = 'edit'
      this.currentChannel = channel
      this.showForm = true
    },



    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.currentChannel = null
    },

    /**
     * 表单提交成功处理
     */
    handleFormSuccess(message) {
      this.showMessage(message, 'success')
      this.loadChannels()
    },

    /**
     * 表单提交错误处理
     */
    handleFormError(message) {
      this.showMessage(message, 'error')
    },

    /**
     * 删除渠道商
     */
    deleteChannel(channel) {
      this.deleteTarget = channel
      this.showDeleteConfirm = true
    },

    /**
     * 删除选中的渠道商
     */
    deleteSelected() {
      if (this.selectedChannels.length === 1) {
        this.deleteChannel(this.selectedChannels[0])
      } else if (this.selectedChannels.length > 1) {
        this.showBatchDeleteConfirm = true
      }
    },

    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deleteTarget = null
    },

    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
    },

    /**
     * 确认删除
     */
    async confirmDelete() {
      try {
        await deleteChannelDistributor(this.deleteTarget.channelId)
        this.showMessage('渠道商删除成功', 'success')
        this.loadChannels()
      } catch (error) {
        console.error('删除渠道商失败:', error)
        this.showMessage('删除渠道商失败', 'error')
      } finally {
        this.closeDeleteConfirm()
      }
    },

    /**
     * 确认批量删除
     */
    async confirmBatchDelete() {
      try {
        const channelIds = this.selectedChannels.map(channel => channel.channelId)
        await batchDeleteChannelDistributors(channelIds)
        this.showMessage(`成功删除 ${channelIds.length} 个渠道商`, 'success')
        this.loadChannels()
      } catch (error) {
        console.error('批量删除渠道商失败:', error)
        this.showMessage('批量删除渠道商失败', 'error')
      } finally {
        this.closeBatchDeleteConfirm()
      }
    },

    /**
     * 切换渠道商选中状态
     */
    toggleSelectChannel(channel) {
      const index = this.selectedChannels.findIndex(c => c.channelId === channel.channelId)
      if (index > -1) {
        this.selectedChannels.splice(index, 1)
      } else {
        this.selectedChannels.push(channel)
      }
    },

    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        this.selectedChannels = [...this.channels]
      } else {
        this.selectedChannels = []
      }
    },

    /**
     * 检查渠道商是否被选中
     */
    isSelected(channel) {
      return this.selectedChannels.some(c => c.channelId === channel.channelId)
    },

    /**
     * 格式化日期
     */
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },

    /**
     * 显示消息提示
     */
    showMessage(text, type = 'success') {
      this.message = {
        show: true,
        type,
        text
      }
      setTimeout(() => {
        this.message.show = false
      }, 3000)
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.channel-management {
  padding: 8px;
  background: #f5f5f5;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 页面头部 */
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
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 8px;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.search-form {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  min-width: 200px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-container {
  overflow: auto;
  flex: 1;
  max-height: calc(100vh - 260px);
}

.channel-table {
  width: 100%;
  border-collapse: collapse;
}

.channel-table th,
.channel-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.channel-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.channel-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.channel-table tbody tr:hover {
  background: #f5f5f5;
}

.channel-table tbody tr.selected {
  background: #e6f7ff;
}

.action-cell {
  display: flex;
  gap: 3px;
  justify-content: center;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #262626;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.btn-warning {
  background: #fa8c16;
  border-color: #fa8c16;
  color: white;
}

.btn-warning:hover {
  background: #ffa940;
  border-color: #ffa940;
}

.btn-danger {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: white;
}

.btn-danger:hover {
  background: #ff7875;
  border-color: #ff7875;
}

.btn-secondary {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.btn-secondary:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.btn-small {
  padding: 3px 6px;
  font-size: 11px;
  margin-right: 3px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.pagination-info {
  font-size: 13px;
  color: #8c8c8c;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #262626;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-select {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  background: white;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  min-width: 300px;
  max-width: 500px;
}

.modal-content h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.modal-content p {
  margin: 0 0 24px 0;
  color: #666;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 消息提示 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  font-weight: 500;
  z-index: 1001;
  animation: slideIn 0.3s ease-out;
}

.message.success {
  background-color: #28a745;
}

.message.error {
  background-color: #dc3545;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 图标 */
.icon-plus::before { content: '+'; }
.icon-edit::before { content: '✏️'; }
.icon-delete::before { content: '🗑️'; }
.icon-search::before { content: '🔍'; }
.icon-refresh::before { content: '🔄'; }

/* 响应式设计 */
@media (max-width: 768px) {
  .channel-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .search-select {
    min-width: auto;
  }
  
  .table-container {
    font-size: 12px;
  }
  
  .channel-table th,
  .channel-table td {
    padding: 8px 4px;
  }
}
</style>