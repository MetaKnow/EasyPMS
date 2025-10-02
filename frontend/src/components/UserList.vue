<template>
  <div class="user-list">
    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-form">
        <!-- 用户名筛选 -->
        <input 
          v-model="filterUserName" 
          type="text" 
          placeholder="按用户名筛选"
          class="search-input"
          @keyup.enter="searchUsers"
        />
        <!-- 姓名筛选 -->
        <input 
          v-model="filterName" 
          type="text" 
          placeholder="按姓名筛选"
          class="search-input"
          @keyup.enter="searchUsers"
        />
        <!-- 锁定状态筛选 -->
        <select v-model="filterLocked" class="btn">
          <option value="">全部状态</option>
          <option value="normal">正常</option>
          <option value="locked">已锁定</option>
        </select>
        <!-- 角色筛选 -->
        <select v-model="filterRoleId" class="btn">
          <option value="">全部角色</option>
          <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.roleName }}</option>
        </select>
        <button class="btn btn-primary" @click="searchUsers">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-section">
      <div class="table-container">
        <table class="user-table">
          <thead>
            <tr>
              <th width="50">
                <input 
                  type="checkbox" 
                  @change="selectAll" 
                  :checked="isAllSelected"
                />
              </th>
              <th width="60">序号</th>
              <th>用户名</th>
              <th>姓名</th>
              <th>所属机构</th>
              <th>角色</th>
              <th>状态</th>
              <th>创建时间</th>
              <th width="200">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(user, index) in filteredUsers" 
              :key="user.userId"
              :class="{ selected: selectedUsers.includes(user.userId) }"
            >
              <td @click.stop>
                <input 
                  type="checkbox" 
                  :value="user.userId"
                  v-model="selectedUsers"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ user.userName }}</td>
              <td>{{ user.name || '-' }}</td>
              <td>{{ user.organName || '未分配' }}</td>
              <td>
                <span class="role-badge" v-if="user.roleName">
                  {{ user.roleName }}
                </span>
                <span class="no-role" v-else>未分配</span>
              </td>
              <td>
                <span class="status-badge" :class="(user.locked === 1 || user.locked === true) ? 'locked' : 'normal'">
                  {{ (user.locked === 1 || user.locked === true) ? '已锁定' : '正常' }}
                </span>
              </td>
              <td>{{ formatDate(user.createTime) }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-small btn-primary" @click.stop="editUser(user)">
                    编辑
                  </button>
                  <button 
                    class="btn-small" 
                    :class="(user.locked === 1 || user.locked === true) ? 'btn-success' : 'btn-secondary'"
                    @click.stop="toggleUserStatus(user)"
                  >
                    {{ (user.locked === 1 || user.locked === true) ? '解锁' : '锁定' }}
                  </button>
                  <button class="btn-small btn-danger" @click.stop="deleteUser(user)">
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <div class="empty-icon">👤</div>
        <p v-if="users.length === 0">
          {{ showAllUsers ? '系统中暂无用户' : '该机构下暂无用户' }}
        </p>
        <p v-else>没有找到匹配的用户</p>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredUsers.length > 0">
        <button 
          class="btn btn-secondary" 
          @click="prevPage" 
          :disabled="currentPage <= 1"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ filteredUsers.length }} 条记录
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

    <!-- 批量删除确认对话框 -->
    <div v-if="showBatchDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认批量删除</h3>
        <p>确定要删除选中的 {{ selectedUsers.length }} 个用户吗？此操作不可恢复。</p>
        <div class="batch-delete-list">
          <div v-for="userId in selectedUsers" :key="userId" class="batch-delete-item">
            {{ getUserNameById(userId) }}
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserList',
  props: {
    organId: {
      type: Number,
      required: false,
      default: 0
    },
    users: {
      type: Array,
      default: () => []
    },
    // 角色列表，用于角色筛选下拉框（类级注释：该组件用于展示机构用户列表并提供多条件筛选功能）
    roles: {
      type: Array,
      default: () => []
    },
    // 是否显示所有用户模式
    showAllUsers: {
      type: Boolean,
      default: false
    }
  },
  emits: ['edit-user', 'delete-user', 'refresh', 'batch-delete-users', 'selection-change'],
  data() {
    return {
      // 选中用户ID集合
      selectedUsers: [],
      // 分页信息
      currentPage: 1,
      pageSize: 20,
      // 多条件筛选字段
      filterUserName: '',
      filterName: '',
      filterLocked: '', // '' | 'normal' | 'locked'
      filterRoleId: '', // '' | number
      // 批量删除相关
      showBatchDeleteConfirm: false
    }
  },
  computed: {
    /**
     * 基础过滤（函数级注释：按用户名、姓名、锁定状态、角色进行多条件过滤，不含分页）
     */
    filteredBase() {
      const nameKeyword = this.filterName.trim().toLowerCase()
      const userKeyword = this.filterUserName.trim().toLowerCase()
      return this.users.filter(u => {
        const byUser = !userKeyword || (u.userName || '').toLowerCase().includes(userKeyword)
        const byName = !nameKeyword || (u.name || '').toLowerCase().includes(nameKeyword)
        const byLocked = this.filterLocked === '' || (this.filterLocked === 'locked' ? (u.locked === 1 || u.locked === true) : (u.locked === 0 || u.locked === false))
        const byRole = this.filterRoleId === '' || Number(this.filterRoleId) === u.roleId
        return byUser && byName && byLocked && byRole
      })
    },
  
    /**
     * 过滤后的用户列表（含分页）
     */
    filteredUsers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredBase.slice(start, end)
    },
  
    /**
     * 总页数（基于过滤后的总数计算）
     */
    totalPages() {
      return Math.ceil(this.filteredBase.length / this.pageSize) || 1
    },
  
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.filteredUsers.length > 0 && 
             this.filteredUsers.every(user => this.selectedUsers.includes(user.userId))
    }
  },
  watch: {
    users() {
      this.selectedUsers = []
      this.currentPage = 1
      this.notifySelectionChange()
    },
    organId() {
      this.resetSearch()
    },
    selectedUsers: {
      handler() {
        this.notifySelectionChange()
      },
      deep: true
    }
  },
  methods: {
    /**
     * 搜索（函数级注释：触发分页重置以从第一页展示过滤结果）
     */
    searchUsers() {
      this.currentPage = 1
    },
  
    /**
     * 重置筛选（函数级注释：清空所有筛选条件并重置分页与选中状态）
     */
    resetSearch() {
      this.filterUserName = ''
      this.filterName = ''
      this.filterLocked = ''
      this.filterRoleId = ''
      this.selectedUsers = []
      this.currentPage = 1
      this.notifySelectionChange()
    },



    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        this.selectedUsers = this.filteredUsers.map(user => user.userId)
      } else {
        this.selectedUsers = []
      }
    },

    /**
     * 编辑用户
     */
    editUser(user) {
      this.$emit('edit-user', user)
    },

    /**
     * 删除用户（函数级注释：触发删除用户事件，由父组件处理确认对话框和API调用）
     */
    deleteUser(user) {
      this.$emit('delete-user', user)
    },

    /**
     * 切换用户状态（锁定/解锁）
     */
    async toggleUserStatus(user) {
      try {
        // 将Integer类型的locked字段转换为boolean，然后取反
        const currentLocked = user.locked === 1 || user.locked === true
        const newLocked = !currentLocked
        
        const response = await fetch(`http://localhost:8081/api/users/${user.userId}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            locked: newLocked
          })
        })

        if (response.ok) {
          this.$emit('refresh')
        }
      } catch (error) {
        console.error('切换用户状态失败:', error)
      }
    },

    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
      }
    },

    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
      }
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
     * 批量删除用户（函数级注释：触发批量删除事件，由父组件处理）
     */
    batchDeleteUsers() {
      if (this.selectedUsers.length === 0) {
        return
      }
      this.$emit('batch-delete-users', this.selectedUsers)
    },

    /**
     * 通知父组件选择变化（函数级注释：当用户选择发生变化时，将选中的用户数据传递给父组件）
     */
    notifySelectionChange() {
      const selectedUserData = this.filteredUsers.filter(user => 
        this.selectedUsers.includes(user.userId)
      )
      this.$emit('selection-change', selectedUserData)
    },

    /**
     * 关闭批量删除确认对话框
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
    },

    /**
     * 确认批量删除（函数级注释：执行批量删除API调用）
     */
    async confirmBatchDelete() {
      try {
        const response = await fetch('http://localhost:8081/api/users/batch', {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            userIds: this.selectedUsers
          })
        })

        if (response.ok) {
          this.selectedUsers = []
          this.showBatchDeleteConfirm = false
          this.$emit('refresh')
          alert('批量删除成功')
        } else {
          const errorData = await response.json()
          alert('批量删除失败: ' + (errorData.message || '未知错误'))
        }
      } catch (error) {
        console.error('批量删除用户失败:', error)
        alert('批量删除失败: ' + error.message)
      }
    },

    /**
     * 根据用户ID获取用户名（函数级注释：用于批量删除确认对话框中显示用户名）
     */
    getUserNameById(userId) {
      const user = this.users.find(u => u.userId === userId)
      return user ? user.name || user.userName : '未知用户'
    }
  }
}
</script>

<style scoped>
.user-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
}

.search-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 表格区域 */
.table-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-container {
  flex: 1;
  overflow: auto;
  max-height: calc(100vh - 270px);
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.user-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  position: sticky;
  top: 0;
  z-index: 1;
}

.user-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-table tbody tr:hover {
  background: #f5f5f5;
}

.user-table tbody tr.selected {
  background: #e6f7ff;
}

/* 徽章样式 */
.role-badge {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  background: #f0f5ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.no-role {
  color: #8c8c8c;
  font-size: 12px;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.normal {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.locked {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
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

.btn-secondary {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
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

.btn-success {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
}

.btn-success:hover {
  background: #73d13d;
  border-color: #73d13d;
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

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.page-info {
  font-size: 13px;
  color: #8c8c8c;
}

/* 图标 */
.icon-search::before {
  content: '🔍';
}

.icon-refresh::before {
  content: '↻';
}

.icon-delete::before {
  content: '🗑️';
}

/* 批量删除对话框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 18px;
}

.modal-content p {
  margin: 0 0 16px 0;
  color: #666;
  line-height: 1.5;
}

.batch-delete-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 20px;
}

.batch-delete-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.batch-delete-item:last-child {
  border-bottom: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
  }
}
</style>