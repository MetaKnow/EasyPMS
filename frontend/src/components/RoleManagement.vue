<template>
  <div class="role-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增角色
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="!selectedRole">
          <i class="icon-delete"></i>
          删除角色
        </button>
        <button class="btn btn-info" @click="viewUsers" :disabled="!selectedRole">
          <i class="icon-users"></i>
          查看用户
        </button>
      </div>
    </div>

    <!-- 角色列表表格 -->
    <div class="table-section">
      <div class="table-container">
        <table class="role-table">
          <thead>
            <tr>
              <th width="50">
                <input 
                  type="checkbox" 
                  @change="selectAll" 
                  :checked="isAllSelected"
                />
              </th>
              <th>角色名称</th>
              <th>角色描述</th>
              <th>用户数量</th>
              <th width="120">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="role in roles" 
              :key="role.roleId"
              :class="{ selected: selectedRole && selectedRole.roleId === role.roleId }"
              @click="selectRole(role)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="selectedRole && selectedRole.roleId === role.roleId"
                  @change="selectRole(role)"
                />
              </td>
              <td>{{ role.roleName }}</td>
              <td>{{ role.description || '暂无描述' }}</td>
              <td>{{ role.userCount || 0 }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editRole(role)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteRole(role)">
                  删除
                </button>
              </td>
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
          第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ totalCount }} 条记录
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

    <!-- 角色表单弹窗 -->
    <RoleForm
      v-if="showForm"
      :visible="showForm"
      :role="editingRole"
      :mode="formMode"
      @close="closeForm"
      @save="saveRole"
    />

    <!-- 用户列表弹窗 -->
    <RoleUsersDialog
      v-if="showUsersDialog"
      :visible="showUsersDialog"
      :role="selectedRole"
      @close="closeUsersDialog"
      @refresh="loadRoles"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="closeDeleteConfirm">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p v-if="deletingRole && deletingRole.userCount > 0">
          角色 "{{ deletingRole?.roleName }}" 已被 {{ deletingRole.userCount }} 个用户使用，无法删除。
          <br>请先取消所有用户的该角色授权后再删除。
        </p>
        <p v-else>
          确定要删除角色 "{{ deletingRole?.roleName }}" 吗？此操作不可恢复。
        </p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
          <button 
            v-if="deletingRole && deletingRole.userCount === 0"
            class="btn btn-danger" 
            @click="confirmDelete"
          >
            确认删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import RoleForm from './RoleForm.vue'
import RoleUsersDialog from './RoleUsersDialog.vue'
import { getRoleList, deleteRole, checkRoleInUse } from '../api/role.js'

/**
 * 角色管理组件（类级注释：负责角色的增删改查和用户管理）
 */
export default {
  name: 'RoleManagement',
  components: {
    RoleForm,
    RoleUsersDialog
  },
  data() {
    return {
      // 角色列表数据
      roles: [],
      selectedRole: null,
      
      // 分页相关
      currentPage: 1,
      pageSize: 15,
      totalCount: 0,
      totalPages: 0,
      
      // 表单相关
      showForm: false,
      formMode: 'add', // 'add' 或 'edit'
      editingRole: null,
      
      // 用户列表弹窗
      showUsersDialog: false,
      
      // 删除确认
      showDeleteConfirm: false,
      deletingRole: null,
      
      // 加载状态
      loading: false
    }
  },
  computed: {
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.roles.length > 0 && this.selectedRole !== null
    }
  },
  mounted() {
    this.loadRoles()
  },
  methods: {
    /**
     * 加载角色列表（函数级注释：获取所有角色并统计用户数量）
     */
    async loadRoles() {
      this.loading = true;
      try {
        const response = await getRoleList();
        // 后端返回格式：{ data: Role[], message: string }
        const allRoles = response.data || [];
        
        // 为每个角色获取用户数量
        for (const role of allRoles) {
          try {
            const result = await checkRoleInUse(role.roleId);
            role.userCount = result.count || 0;
          } catch (error) {
            console.error(`获取角色 ${role.roleId} 用户数量失败:`, error);
            role.userCount = 0;
          }
        }
        
        // 设置总数和总页数
        this.totalCount = allRoles.length;
        this.totalPages = Math.ceil(this.totalCount / this.pageSize) || 1;
        
        // 前端分页处理
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        this.roles = allRoles.slice(startIndex, endIndex);
        
        // 如果当前页超出范围，重置到第一页
        if (this.currentPage > this.totalPages && this.totalPages > 0) {
          this.currentPage = 1;
          this.loadRoles();
          return;
        }
        
      } catch (error) {
        console.error('加载角色列表失败:', error);
        this.$message?.error('加载角色列表失败: ' + error.message);
        this.roles = [];
        this.totalCount = 0;
        this.totalPages = 0;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 选择角色（函数级注释：切换角色选中状态）
     */
    selectRole(role) {
      this.selectedRole = this.selectedRole?.roleId === role.roleId ? null : role
    },

    /**
     * 全选/取消全选（函数级注释：批量选择操作）
     */
    selectAll(event) {
      if (event.target.checked && this.roles.length > 0) {
        this.selectedRole = this.roles[0]
      } else {
        this.selectedRole = null
      }
    },

    /**
     * 显示新增表单（函数级注释：打开新增角色对话框）
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingRole = null
      this.showForm = true
    },



    /**
     * 编辑角色（函数级注释：打开编辑角色对话框）
     */
    editRole(role) {
      this.formMode = 'edit'
      this.editingRole = { ...role }
      this.showForm = true
    },

    /**
     * 删除选中的角色（函数级注释：删除当前选中的角色）
     */
    deleteSelected() {
      if (this.selectedRole) {
        this.deleteRole(this.selectedRole)
      }
    },

    /**
     * 删除角色（函数级注释：显示删除确认对话框）
     */
    deleteRole(role) {
      this.deletingRole = role
      this.showDeleteConfirm = true
    },

    /**
     * 确认删除（函数级注释：执行角色删除操作）
     */
    async confirmDelete() {
      try {
        await deleteRole(this.deletingRole.roleId);
        
        // 重新加载列表
        this.loadRoles();
        
        // 清空选择
        this.selectedRole = null;
        
        this.closeDeleteConfirm();
        this.$message?.success('角色删除成功');
        
      } catch (error) {
        console.error('删除角色失败:', error);
        this.$message?.error('删除角色失败: ' + error.message);
      }
    },

    /**
     * 关闭删除确认弹窗（函数级注释：关闭删除确认对话框）
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingRole = null
    },

    /**
     * 查看用户（函数级注释：显示角色下的用户列表）
     */
    viewUsers() {
      if (this.selectedRole) {
        this.showUsersDialog = true
      }
    },

    /**
     * 关闭用户列表弹窗（函数级注释：关闭用户列表对话框）
     */
    closeUsersDialog() {
      this.showUsersDialog = false
    },

    /**
     * 关闭表单（函数级注释：关闭角色表单对话框）
     */
    closeForm() {
      this.showForm = false
      this.editingRole = null
    },

    /**
     * 保存角色（函数级注释：保存角色数据并刷新列表）
     */
    async saveRole() {
      // 重新加载列表
      this.loadRoles();
      this.closeForm();
    },

    /**
     * 上一页（函数级注释：切换到上一页）
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadRoles()
      }
    },

    /**
     * 下一页（函数级注释：切换到下一页）
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadRoles()
      }
    }
  }
}
</script>

<style scoped>
.role-management {
  padding: 8px;
  background: #f5f5f5;
  min-height: 100%;
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

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.role-table {
  width: 100%;
  border-collapse: collapse;
}

.role-table th,
.role-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.role-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.role-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.role-table tbody tr:hover {
  background: #f5f5f5;
}

.role-table tbody tr.selected {
  background: #e6f7ff;
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

.btn-info {
  background: #13c2c2;
  color: white;
  border-color: #13c2c2;
}

.btn-info:hover:not(:disabled) {
  background: #36cfc9;
  border-color: #36cfc9;
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
  margin-right: 4px;
}

/* 模态框样式 */
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
  padding: 20px;
  min-width: 400px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-content h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #262626;
}

.modal-content p {
  margin: 0 0 16px 0;
  color: #595959;
  line-height: 1.5;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 图标 */
.icon-plus::before { content: "➕"; }
.icon-edit::before { content: "✏️"; }
.icon-delete::before { content: "🗑️"; }
.icon-users::before { content: "👥"; }

/* 分页器样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e8e8e8;
  margin-top: 0;
}

.page-info {
  color: #666;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .role-management {
    padding: 4px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
    padding: 8px 12px;
  }
  
  .action-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}
</style>