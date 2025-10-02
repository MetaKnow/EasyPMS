<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content large" @click.stop>
      <div class="modal-header">
        <h3>角色用户管理 - {{ role?.roleName }}</h3>
        <button class="close-btn" @click="close">
          <span class="icon-close">✕</span>
        </button>
      </div>

      <div class="modal-body">
        <!-- 操作按钮 -->
        <div class="action-bar">
          <div class="info-text">
            共 {{ users.length }} 个用户拥有此角色
          </div>
          <div class="action-buttons">
            <button 
              class="btn btn-warning" 
              @click="selectAll"
              :disabled="users.length === 0"
            >
              {{ isAllSelected ? '取消全选' : '全选' }}
            </button>
            <button 
              class="btn btn-danger" 
              @click="batchRemoveRole"
              :disabled="selectedUsers.length === 0 || loading"
            >
              {{ loading ? '处理中...' : `批量取消授权 (${selectedUsers.length})` }}
            </button>
          </div>
        </div>

        <!-- 用户列表 -->
        <div class="table-section">
          <div class="table-container">
            <table class="users-table">
              <thead>
                <tr>
                  <th width="50">
                    <input 
                      type="checkbox" 
                      @change="toggleSelectAll" 
                      :checked="isAllSelected"
                      :indeterminate="isPartialSelected"
                    />
                  </th>
                  <th>用户名</th>
                  <th>真实姓名</th>
                  <th>机构</th>
                  <th>状态</th>
                  <th width="100">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="users.length === 0">
                  <td colspan="6" class="no-data">
                    暂无用户拥有此角色
                  </td>
                </tr>
                <tr 
                  v-for="user in users" 
                  :key="user.userId"
                  :class="{ selected: selectedUsers.includes(user.userId) }"
                >
                  <td>
                    <input 
                      type="checkbox" 
                      :checked="selectedUsers.includes(user.userId)"
                      @change="toggleUserSelection(user.userId)"
                    />
                  </td>
                  <td>{{ user.userName }}</td>
                  <td>{{ user.name || '未设置' }}</td>
                  <td>{{ user.organName || '未分配' }}</td>
                  <td>
                    <span class="status-badge" :class="user.locked ? 'locked' : 'active'">
                      {{ user.locked ? '已锁定' : '正常' }}
                    </span>
                  </td>
                  <td>
                    <button 
                      class="btn-small btn-danger" 
                      @click="removeUserRole(user)"
                      :disabled="loading"
                    >
                      取消授权
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="close">
          关闭
        </button>
      </div>
    </div>

    <!-- 确认取消授权弹窗 -->
    <div v-if="showConfirm" class="modal-overlay confirm-overlay">
      <div class="modal-content confirm-dialog" @click.stop>
        <div class="confirm-header">
          <div class="confirm-icon">
            <span class="icon-warning">⚠️</span>
          </div>
          <h3 class="confirm-title">确认取消授权</h3>
        </div>
        
        <div class="confirm-body">
          <p v-if="confirmType === 'single'" class="confirm-message">
            您即将取消一个用户的角色授权，此操作将立即生效。
          </p>
          <p v-else class="confirm-message">
            您即将取消 <strong>{{ selectedUsers.length }}</strong> 个用户的角色授权，此操作将立即生效。
          </p>
          <div class="confirm-warning">
            <span class="warning-icon">💡</span>
            <span class="warning-text">取消授权后，用户将失去该角色的相关权限</span>
          </div>
        </div>
        
        <div class="confirm-actions">
          <button class="btn btn-secondary" @click="closeConfirm">
            <span class="btn-icon">✕</span>
            取消
          </button>
          <button class="btn btn-danger confirm-btn" @click="confirmRemoveRole" :disabled="loading">
            <span class="btn-icon">{{ loading ? '⏳' : '✓' }}</span>
            {{ loading ? '处理中...' : '确认取消授权' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getUsersByRoleId, batchRemoveRoleFromUsers } from '../api/role.js'

/**
 * 角色用户列表对话框组件（类级注释：管理角色下的用户列表和批量操作）
 */
export default {
  name: 'RoleUsersDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    role: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'refresh'],
  data() {
    return {
      users: [],
      selectedUsers: [],
      loading: false,
      
      // 确认弹窗
      showConfirm: false,
      confirmType: 'single', // 'single' 或 'batch'
      confirmUser: null
    }
  },
  computed: {
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.users.length > 0 && this.selectedUsers.length === this.users.length
    },
    
    /**
     * 是否部分选中
     */
    isPartialSelected() {
      return this.selectedUsers.length > 0 && this.selectedUsers.length < this.users.length
    }
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal && this.role) {
          this.loadUsers()
        }
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 加载用户列表（函数级注释：获取角色下的所有用户）
     */
    async loadUsers() {
      if (!this.role) return
      
      this.loading = true
      try {
        this.users = await getUsersByRoleId(this.role.roleId)
        this.selectedUsers = []
      } catch (error) {
        console.error('加载用户列表失败:', error)
        this.$message?.error('加载用户列表失败: ' + error.message)
        this.users = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 切换用户选择状态（函数级注释：选择或取消选择单个用户）
     */
    toggleUserSelection(userId) {
      const index = this.selectedUsers.indexOf(userId)
      if (index > -1) {
        this.selectedUsers.splice(index, 1)
      } else {
        this.selectedUsers.push(userId)
      }
    },

    /**
     * 切换全选状态（函数级注释：全选或取消全选所有用户）
     */
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedUsers = []
      } else {
        this.selectedUsers = this.users.map(user => user.userId)
      }
    },

    /**
     * 全选按钮（函数级注释：全选按钮的点击处理）
     */
    selectAll() {
      this.toggleSelectAll()
    },

    /**
     * 取消单个用户角色（函数级注释：取消单个用户的角色授权）
     */
    removeUserRole(user) {
      this.confirmType = 'single'
      this.confirmUser = user
      this.showConfirm = true
    },

    /**
     * 批量取消角色授权（函数级注释：批量取消多个用户的角色授权）
     */
    batchRemoveRole() {
      if (this.selectedUsers.length === 0) return
      
      this.confirmType = 'batch'
      this.confirmUser = null
      this.showConfirm = true
    },

    /**
     * 确认取消角色授权（函数级注释：执行取消角色授权操作）
     */
    async confirmRemoveRole() {
      this.loading = true
      try {
        let userIds = []
        
        if (this.confirmType === 'single') {
          userIds = [this.confirmUser.userId]
        } else {
          userIds = [...this.selectedUsers]
        }
        
        const result = await batchRemoveRoleFromUsers(userIds)
        
        this.$message?.success(result.message || '角色授权取消成功')
        
        // 重新加载用户列表
        await this.loadUsers()
        
        // 通知父组件刷新
        this.$emit('refresh')
        
        this.closeConfirm()
        
      } catch (error) {
        console.error('取消角色授权失败:', error)
        this.$message?.error('取消角色授权失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    /**
     * 关闭确认弹窗（函数级注释：关闭确认对话框）
     */
    closeConfirm() {
      this.showConfirm = false
      this.confirmType = 'single'
      this.confirmUser = null
    },

    /**
     * 关闭对话框（函数级注释：关闭用户列表对话框）
     */
    close() {
      this.$emit('close')
    },

    /**
     * 处理遮罩层点击（函数级注释：点击遮罩层关闭对话框）
     */
    handleOverlayClick() {
      this.close()
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 6px;
  min-width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.modal-content.large {
  min-width: 800px;
  min-height: 600px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.close-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  color: #8c8c8c;
  border-radius: 2px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

.modal-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.info-text {
  color: #595959;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.table-section {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.table-container {
  flex: 1;
  overflow: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.users-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  position: sticky;
  top: 0;
  z-index: 1;
}

.users-table tbody tr {
  transition: background-color 0.3s;
}

.users-table tbody tr:hover {
  background: #f5f5f5;
}

.users-table tbody tr.selected {
  background: #e6f7ff;
}

.no-data {
  text-align: center;
  color: #8c8c8c;
  font-style: italic;
  padding: 40px 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #1890ff;
  color: white;
  border-color: #1890ff;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
  border-color: #40a9ff;
}

.btn-warning {
  background: #fa8c16;
  color: white;
  border-color: #fa8c16;
}

.btn-warning:hover:not(:disabled) {
  background: #ffa940;
  border-color: #ffa940;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
  border-color: #ff4d4f;
}

.btn-danger:hover:not(:disabled) {
  background: #ff7875;
  border-color: #ff7875;
}

.btn-secondary {
  background: #f5f5f5;
  color: #595959;
  border-color: #d9d9d9;
}

.btn-secondary:hover:not(:disabled) {
  background: #fafafa;
  border-color: #40a9ff;
  color: #1890ff;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
}

/* 状态徽章样式 */
.status-badge {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.locked {
  background: #fff2e8;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.icon-close {
  font-size: 14px;
}

/* 确认对话框样式 */
.confirm-overlay {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(2px);
}

.confirm-dialog {
  min-width: 420px;
  max-width: 500px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: confirmSlideIn 0.3s ease-out;
}

@keyframes confirmSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 24px 16px 24px;
  border-bottom: none;
}

.confirm-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff2e8 0%, #ffd591 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.confirm-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.confirm-body {
  padding: 0 24px 24px 24px;
}

.confirm-message {
  margin: 0 0 16px 0;
  font-size: 15px;
  line-height: 1.6;
  color: #595959;
}

.confirm-message strong {
  color: #fa8c16;
  font-weight: 600;
}

.confirm-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  margin-top: 16px;
}

.warning-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.warning-text {
  font-size: 13px;
  color: #52c41a;
  line-height: 1.4;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 24px 24px 24px;
  border-top: 1px solid #f0f0f0;
}

.confirm-actions .btn {
  min-width: 100px;
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.confirm-btn {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.confirm-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff7875 0%, #ffa39e 100%);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
  transform: translateY(-1px);
}

.confirm-btn:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  box-shadow: none;
  transform: none;
}

.btn-icon {
  margin-right: 4px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content.large {
    min-width: auto;
    margin: 20px;
    min-height: auto;
    max-height: calc(100vh - 40px);
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .action-bar {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
    padding: 12px 16px;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .modal-footer {
    padding: 12px 16px;
  }
  
  /* 确认对话框移动端样式 */
  .confirm-dialog {
    min-width: auto;
    margin: 20px;
    max-width: calc(100vw - 40px);
  }
  
  .confirm-header {
    padding: 20px 20px 12px 20px;
  }
  
  .confirm-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .confirm-title {
    font-size: 16px;
  }
  
  .confirm-body {
    padding: 0 20px 20px 20px;
  }
  
  .confirm-actions {
    padding: 12px 20px 20px 20px;
    flex-direction: column;
    gap: 8px;
  }
  
  .confirm-actions .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>