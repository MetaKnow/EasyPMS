<template>
  <div class="organization-user-management">
    <div class="management-header">
      <h2>机构用户管理</h2>
      <div class="header-actions">
        <button 
          class="btn btn-primary" 
          @click="addUser"
          :disabled="!selectedOrgan"
        >
          <i class="icon-plus"></i>
          新增用户
        </button>
        <button 
          class="btn btn-danger" 
          @click="handleBatchDeleteUsers"
          :disabled="selectedUserIds.length === 0"
        >
          <i class="icon-delete"></i>
          删除用户 ({{ selectedUserIds.length }})
        </button>
        <button class="btn btn-secondary" @click="refreshData">
          <i class="icon-refresh"></i>
          刷新
        </button>
      </div>
    </div>

    <div class="management-content">
      <!-- 左侧机构树 -->
      <div class="left-panel">
        <!-- 移除面板头部标签 -->
        <div class="panel-content">
          <OrganizationTree 
            ref="organTree"
            :organizations="organs"
            :selected-organ="selectedOrgan"
            @select-organ="handleOrganSelect"
            @add-child="handleAddOrgan"
            @rename-organ="handleEditOrgan"
            @delete-organ="handleDeleteOrgan"
          />
        </div>
      </div>

      <!-- 右侧用户列表 -->
      <div class="right-panel">
        <div class="panel-content">
          <UserList 
            :organ-id="selectedOrgan?.organId || 0"
            :users="users"
            :roles="roles"
            :show-all-users="!selectedOrgan"
            @edit-user="handleEditUser"
            @delete-user="handleDeleteUser"
            @refresh="selectedOrgan ? loadUsers : loadAllUsers"
            @batch-delete-users="handleBatchDeleteUsers"
            @selection-change="handleSelectionChange"
          />
        </div>
      </div>
    </div>

    <!-- 用户表单弹窗 -->
    <UserDialog
      v-if="userDialogVisible"
      :visible="userDialogVisible"
      :user="editingUser"
      :organ-id="selectedOrgan?.organId"
      :roles="roles"
      :organs="organs"
      @close="closeUserDialog"
      @submit="handleUserSubmit"
    />



    <!-- 机构对话框 -->
    <OrganDialog
      v-if="organDialogVisible"
      :visible="organDialogVisible"
      :organ="editingOrgan"
      :parent-organ="parentOrgan"
      :is-edit="isEditOrgan"
      @close="closeOrganDialog"
      @submit="handleOrganSubmit"
    />

    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-if="confirmDialogVisible"
      :visible="confirmDialogVisible"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :details="confirmDialog.details"
      :confirm-text="confirmDialog.confirmText"
      @close="closeConfirmDialog"
      @confirm="handleConfirmAction"
    />
  </div>
</template>

<script>
import OrganizationTree from './OrganizationTree.vue'
import UserList from './UserList.vue'
import UserDialog from './UserDialog.vue'
import OrganDialog from './OrganDialog.vue'
import ConfirmDialog from './ConfirmDialog.vue'

// API imports
import { getOrganTree, createOrgan, updateOrgan, deleteOrgan, checkOrganHasUsers, checkOrganAndChildrenUsersDetail } from '../api/organ.js'
import { getUsersByOrganId, getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, checkUserNameExists } from '../api/user.js'
import { getRoleList } from '../api/role.js'

export default {
  name: 'OrganizationUserManagement',
  components: {
    OrganizationTree,
    UserList,
    UserDialog,
    OrganDialog,
    ConfirmDialog
  },
  data() {
    return {
      // 机构相关数据
      organs: [],
      selectedOrgan: null,
      selectedOrganId: null,
      
      // 用户相关数据
      users: [],
      selectedUserIds: [],
      
      // 角色数据
      roles: [],
      
      // 对话框状态
      userDialogVisible: false,
      organDialogVisible: false,
      confirmDialogVisible: false,
      
      // 编辑状态
      editingUser: null,
      editingOrgan: null,
      parentOrgan: null,
      isEditOrgan: false,
      
      // 确认对话框配置
      confirmDialog: {
        title: '',
        message: '',
        details: '',
        confirmText: '',
        action: null
      },
      
      // 加载状态
      loading: false
    }
  },
  async mounted() {
    await this.initializeData()
  },
  methods: {
    /**
     * 初始化数据
     */
    async initializeData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadOrgans(),
          this.loadRoles(),
          this.loadAllUsers() // 默认加载所有用户
        ])
      } catch (error) {
        console.error('初始化数据失败:', error)
        this.$message?.error('初始化数据失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 加载机构数据
     */
    async loadOrgans() {
      try {
        const response = await getOrganTree()
        this.organs = response.data || []
        
        // 不再自动选择第一个机构，保持机构树无选中状态
        // 用户可以手动选择机构来查看特定机构的用户
      } catch (error) {
        console.error('加载机构数据失败:', error)
        this.$message?.error('加载机构数据失败')
      }
    },

    /**
     * 递归查找第一个机构节点
     */
    findFirstOrgan(organs) {
      if (!organs || organs.length === 0) return null
      
      // 返回第一个机构
      const firstOrgan = organs[0]
      
      // 如果第一个机构有子机构，返回第一个子机构，否则返回第一个机构本身
      if (firstOrgan.children && firstOrgan.children.length > 0) {
        return this.findFirstOrgan(firstOrgan.children)
      }
      
      return firstOrgan
    },

    /**
     * 加载角色数据
     */
    async loadRoles() {
      try {
        const response = await getRoleList()
        this.roles = response.data || []
      } catch (error) {
        console.error('加载角色数据失败:', error)
        this.$message?.error('加载角色数据失败')
      }
    },

    /**
     * 加载用户数据
     */
    async loadUsers() {
      if (!this.selectedOrganId) return
      
      try {
        const response = await getUsersByOrganId(this.selectedOrganId)
        this.users = response.data || []
      } catch (error) {
        console.error('加载用户数据失败:', error)
        this.$message?.error('加载用户数据失败')
      }
    },

    /**
     * 加载所有用户数据（函数级注释：获取系统中的所有用户，不限制机构）
     */
    async loadAllUsers() {
      try {
        const response = await getUserList()
        this.users = response.users || []
      } catch (error) {
        console.error('加载所有用户数据失败:', error)
        this.$message?.error('加载所有用户数据失败')
      }
    },

    /**
     * 选择机构（函数级注释：支持点击已选中机构来取消选择，回到显示所有用户模式）
     */
    handleOrganSelect(organ) {
      // 如果点击的是已选中的机构，则取消选择，显示所有用户
      if (this.selectedOrgan && this.selectedOrgan.organId === organ.organId) {
        this.selectedOrgan = null
        this.selectedOrganId = null
        this.loadAllUsers()
      } else {
        // 选择新的机构，显示该机构的用户
        this.selectedOrgan = organ
        this.selectedOrganId = organ.organId
        this.loadUsers()
      }
    },

    /**
     * 新增机构
     */
    handleAddOrgan(parentOrgan) {
      this.parentOrgan = parentOrgan
      this.editingOrgan = null
      this.isEditOrgan = false
      this.organDialogVisible = true
    },

    /**
     * 编辑机构
     */
    handleEditOrgan(organ) {
      this.editingOrgan = organ
      this.parentOrgan = null
      this.isEditOrgan = true
      this.organDialogVisible = true
    },

    /**
     * 删除机构（函数级注释：检查机构及其子机构的用户情况，显示详细的确认提示）
     */
    async handleDeleteOrgan(organ) {
      try {
        // 获取机构及其子机构的详细用户信息
        const userCheckResult = await checkOrganAndChildrenUsersDetail(organ.organId)
        
        if (userCheckResult.hasUsers) {
          // 如果有用户，显示详细的阻止信息对话框
          const organUserInfo = userCheckResult.organUserInfo
          const totalUsers = userCheckResult.totalUsers
          
          let blockMessage = `无法删除机构"${organ.organName}"，以下机构中存在用户：`
          let blockDetails = ''
          
          organUserInfo.forEach(info => {
            blockDetails += `• ${info.organName}（${info.userCount}个用户）\n`
          })
          
          blockDetails += `\n总计：${totalUsers}个用户\n\n请先转移或删除这些用户后再删除机构。`
          
          // 使用确认对话框显示详细信息
          this.confirmDialog = {
            title: '无法删除机构',
            message: blockMessage,
            details: blockDetails,
            confirmText: '我知道了',
            action: null // 不需要执行任何操作，只是显示信息
          }
          this.confirmDialogVisible = true
          return
        }

        // 如果没有用户，显示确认对话框
        let confirmMessage = `确定要删除机构"${organ.organName}"吗？`
        let confirmDetails = '删除后将无法恢复，请谨慎操作。'
        
        // 检查是否有子机构需要一起删除
        try {
          const organTree = await this.getOrganSubtree(organ.organId)
          if (organTree && organTree.length > 1) {
            const childrenNames = organTree.slice(1).map(child => child.organName).join('、')
            confirmMessage = `确定要删除机构"${organ.organName}"及其子机构吗？`
            confirmDetails = `将同时删除以下子机构：${childrenNames}\n\n删除后将无法恢复，请谨慎操作。`
          }
        } catch (error) {
          console.warn('获取子机构信息失败，使用默认提示:', error)
        }

        this.confirmDialog = {
          title: '删除机构',
          message: confirmMessage,
          details: confirmDetails,
          confirmText: '确认删除',
          action: () => this.doDeleteOrgan(organ)
        }
        this.confirmDialogVisible = true
        
      } catch (error) {
        console.error('检查机构状态失败:', error)
        this.$message?.error('检查机构状态失败，请稍后重试')
      }
    },

    /**
     * 获取机构子树（函数级注释：递归获取指定机构及其所有子机构的信息）
     * @param {number} organId 机构ID
     * @returns {Array} 机构及其子机构的数组
     */
    getOrganSubtree(organId) {
      const result = []
      
      const findOrganAndChildren = (organs, targetId) => {
        for (const organ of organs) {
          if (organ.organId === targetId) {
            result.push(organ)
            if (organ.children && organ.children.length > 0) {
              const addChildren = (children) => {
                for (const child of children) {
                  result.push(child)
                  if (child.children && child.children.length > 0) {
                    addChildren(child.children)
                  }
                }
              }
              addChildren(organ.children)
            }
            return true
          }
          if (organ.children && organ.children.length > 0) {
            if (findOrganAndChildren(organ.children, targetId)) {
              return true
            }
          }
        }
        return false
      }
      
      findOrganAndChildren(this.organs, organId)
      return result
    },

    /**
     * 执行删除机构
     */
    async doDeleteOrgan(organ) {
      try {
        await deleteOrgan(organ.organId)
        
        this.$message?.success('删除机构成功')
        await this.loadOrgans()
        
        // 如果删除的是当前选中的机构，清空选中状态
        if (this.selectedOrganId === organ.organId) {
          this.selectedOrgan = null
          this.selectedOrganId = null
          this.users = []
        }
      } catch (error) {
        console.error('删除机构失败:', error)
        this.$message?.error(error.message || '删除机构失败')
      }
    },

    /**
     * 新增用户
     */
    addUser() {
      if (!this.selectedOrgan) {
        this.$message?.warning('请先选择机构')
        return
      }
      
      this.editingUser = null
      this.userDialogVisible = true
    },

    /**
     * 编辑用户
     */
    handleEditUser(user) {
      this.editingUser = user
      this.userDialogVisible = true
    },

    /**
     * 删除用户（函数级注释：显示确认对话框，确认后调用删除API并刷新用户列表）
     */
    handleDeleteUser(user) {
      // 显示确认对话框
      this.confirmDialog = {
        title: '删除用户',
        message: `确定要删除用户"${user.userName}"吗？`,
        details: '删除后将无法恢复，请谨慎操作。',
        confirmText: '确认删除',
        action: () => this.doDeleteUser(user)
      }
      this.confirmDialogVisible = true
    },

    /**
     * 执行删除用户（函数级注释：调用删除API并处理结果）
     */
    async doDeleteUser(user) {
      try {
        await deleteUser(user.userId)
        this.$message?.success('删除用户成功')
        await this.loadUsers()
      } catch (error) {
        console.error('删除用户失败:', error)
        this.$message?.error(error.message || '删除用户失败')
      }
    },

    /**
     * 处理用户提交
     */
    async handleUserSubmit(userData) {
      try {
        if (this.editingUser) {
          // 编辑用户
          await updateUser(this.editingUser.userId, userData)
          this.$message?.success('更新用户成功')
        } else {
          // 新增用户
          // 保存前进行用户名兜底判重，避免并发或网络异常导致重复
          if (userData && userData.userName) {
            const exists = await checkUserNameExists(userData.userName)
            if (exists) {
              this.$message?.warning('用户名已存在，请更换后再试')
              return
            }
          }
          await createUser(userData)
          this.$message?.success('创建用户成功')
        }
        
        this.closeUserDialog()
        // 根据当前模式选择合适的刷新方法
        if (this.selectedOrgan) {
          await this.loadUsers()
        } else {
          await this.loadAllUsers()
        }
      } catch (error) {
        console.error('保存用户失败:', error)
        this.$message?.error(error.message || '保存用户失败')
      }
    },

    /**
     * 关闭用户对话框
     */
    closeUserDialog() {
      this.userDialogVisible = false
      this.editingUser = null
    },

    /**
     * 刷新数据
     */
    async refreshData() {
      await this.loadOrgans()
      if (this.selectedOrganId) {
        await this.loadUsers()
      }
    },

    /**
     * 处理机构对话框提交
     */
    async handleOrganSubmit(organData) {
      try {
        if (this.isEditOrgan) {
          // 编辑机构
          await updateOrgan(this.editingOrgan.organId, {
            organName: organData.organName,
            parentOrganId: this.editingOrgan.parentOrganId
          })
          this.$message?.success('重命名机构成功')
          
          // 如果当前选中的机构被重命名，更新选中状态
          if (this.selectedOrganId === this.editingOrgan.organId) {
            this.selectedOrgan.organName = organData.organName
          }
        } else {
          // 新增机构
          await createOrgan({
            organName: organData.organName,
            parentOrganId: this.parentOrgan.organId
          })
          this.$message?.success('新增机构成功')
        }
        
        await this.loadOrgans()
        this.organDialogVisible = false
      } catch (error) {
        console.error('机构操作失败:', error)
        this.$message?.error(error.message || '机构操作失败')
      }
    },

    /**
     * 关闭机构对话框
     */
    closeOrganDialog() {
      this.organDialogVisible = false
      this.editingOrgan = null
      this.parentOrgan = null
      this.isEditOrgan = false
    },

    /**
     * 关闭确认对话框
     */
    closeConfirmDialog() {
      this.confirmDialogVisible = false
      this.confirmDialog = {
        title: '',
        message: '',
        details: '',
        confirmText: '确认',
        action: null
      }
    },

    /**
     * 处理确认对话框的确认操作
     */
    async handleConfirmAction() {
      if (this.confirmDialog.action) {
        await this.confirmDialog.action()
      }
      this.closeConfirmDialog()
    },

    /**
     * 处理选择变化
     */
    handleSelectionChange(selectedUsers) {
      this.selectedUserIds = selectedUsers.map(user => user.userId)
    },

    /**
     * 处理批量删除用户
     */
    handleBatchDeleteUsers() {
      // 获取选中的用户数据
      const selectedUsers = this.users.filter(user => 
        this.selectedUserIds.includes(user.userId)
      )
      
      if (selectedUsers.length === 0) {
        this.$message?.warning('请先选择要删除的用户')
        return
      }
      
      // 显示确认对话框
      this.confirmDialog = {
        title: '批量删除用户',
        message: `确定要删除以下 ${selectedUsers.length} 个用户吗？`,
        details: selectedUsers.map(user => user.userName).join('、'),
        confirmText: '删除',
        action: this.confirmBatchDelete
      }
      this.confirmDialogVisible = true
    },

    /**
     * 确认批量删除用户
     */
    async confirmBatchDelete() {
      try {
        await batchDeleteUsers(this.selectedUserIds)

        this.$message?.success(`成功删除 ${this.selectedUserIds.length} 个用户`)
        
        // 刷新用户列表
        if (this.selectedOrgan) {
          await this.loadUsers()
        } else {
          await this.loadAllUsers()
        }
        
        // 清空选中的用户ID
        this.selectedUserIds = []
      } catch (error) {
        console.error('批量删除用户失败:', error)
        this.$message?.error('批量删除用户失败: ' + (error.message || '未知错误'))
      }
    }
  }
}
</script>

<style scoped>
.organization-user-management {
  padding: 8px;
  background: #f5f5f5;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 管理页面头部 */
.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.management-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

/* 主内容区域 */
.management-content {
  flex: 1;
  display: flex;
  gap: 8px;
  overflow: hidden;
}

/* 左右面板 */
.left-panel, .right-panel {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-panel {
  width: 260px; /* 原 300, 缩小以贴合右侧 */
  min-width: 240px; /* 原 250, 更紧凑 */
}

/* 移除面板头部的显示影响，无需 .panel-header */
.panel-header { display: none; }

/* 调整 panel-content 边距使左右更贴合 */
.panel-content {
  flex: 1;
  overflow: auto;
  padding: 6px; /* 原 8px，缩小 */
}

.right-panel {
  flex: 1;
}

/* 面板头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 面板内容 */
.panel-content {
  flex: 1;
  overflow: auto;
  padding: 8px;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-organ {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
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

.btn-primary {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.btn:disabled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-primary:disabled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
}

.btn:disabled:hover {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
}

.btn-secondary {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
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

/* 图标 */
.icon-refresh::before {
  content: '↻';
}

.icon-plus::before {
  content: '+';
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  min-width: 400px;
  max-width: 500px;
}

.modal-content h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.modal-content p {
  margin: 0 0 24px 0;
  color: #595959;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .organization-user-management {
    padding: 4px;
  }
  
  .management-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
    padding: 8px 12px;
  }
  
  .management-content {
    flex-direction: column;
  }
  
  .left-panel {
    width: 100%;
    height: 300px;
  }
  
  .right-panel {
    height: 400px;
  }
}
</style>