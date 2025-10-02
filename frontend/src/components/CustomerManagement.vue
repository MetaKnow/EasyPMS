<template>
  <div class="customer-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">客户管理</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增客户
        </button>

        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedCustomers.length === 0">
          <i class="icon-delete"></i>
          删除客户
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          v-model="searchForm.customerName" 
          type="text" 
          placeholder="客户名称"
          class="search-input"
        />
        <input 
          v-model="searchForm.contact" 
          type="text" 
          placeholder="联系人"
          class="search-input"
        />
        <select v-model="searchForm.province" class="search-select">
          <option value="">全部省份</option>
          <option v-for="province in provinces" :key="province" :value="province">
            {{ province }}
          </option>
        </select>
        <select v-model="searchForm.customerRank" class="search-select">
          <option value="">全部等级</option>
          <option value="战略客户">战略客户</option>
          <option value="重要客户">重要客户</option>
          <option value="一般客户">一般客户</option>
        </select>
        <button class="btn btn-primary" @click="searchCustomers">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 客户列表表格 -->
    <div class="table-section">
      <div class="table-container">
        <table class="customer-table">
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
              <th>客户名称</th>
              <th>联系人</th>
              <th>联系方式</th>
              <th>省份</th>
              <th>客户等级</th>
              <th>创建时间</th>
              <th width="120">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(customer, index) in customers" 
              :key="customer.customerId"
              :class="{ selected: isSelected(customer) }"
              @click="toggleSelectCustomer(customer)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="isSelected(customer)"
                  @change.stop="toggleSelectCustomer(customer)"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ customer.customerName }}</td>
              <td>{{ customer.contact || '-' }}</td>
              <td>{{ customer.phoneNumber || '-' }}</td>
              <td>{{ customer.province || '-' }}</td>
              <td>
                <span class="rank-badge" :class="getRankClass(customer.customerRank)">
                  {{ customer.customerRank || '-' }}
                </span>
              </td>
              <td>{{ formatDate(customer.createTime) }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editCustomer(customer)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteCustomer(customer)">
                  删除
                </button>
              </td>
            </tr>
          <!-- 暂无数据 -->
              <tr v-if="customers.length === 0" class="empty-row">
                <td colspan="9" class="empty-cell">
                  暂无数据
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

    <!-- 客户表单弹窗 -->
    <CustomerForm
      v-if="showForm"
      :visible="showForm"
      :customer="editingCustomer"
      :mode="formMode"
      @close="closeForm"
      @save="saveCustomer"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除客户 "{{ deletingCustomer?.customerName }}" 吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmDelete">确认删除</button>
        </div>
      </div>
    </div>

    <!-- 批量删除确认弹窗 -->
    <div v-if="showBatchDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认批量删除</h3>
        <p>确定要删除选中的 {{ deletingCustomers.length }} 个客户吗？此操作不可恢复。</p>
        <div class="batch-delete-list">
          <div v-for="customer in deletingCustomers" :key="customer.customerId" class="batch-delete-item">
            {{ customer.customerName }}
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
import CustomerForm from './CustomerForm.vue'
import { getCustomerList, createCustomer, updateCustomer, deleteCustomer, batchDeleteCustomers, checkCustomerNameAvailable } from '../api/customer.js'

export default {
  name: 'CustomerManagement',
  components: {
    CustomerForm
  },
  data() {
    return {
      // 客户列表数据
      customers: [],
      selectedCustomers: [], // 改为数组，支持多选
      
      // 搜索表单
      searchForm: {
        customerName: '',
        contact: '',
        province: '',
        customerRank: ''
      },
      
      // 分页
      currentPage: 1,
      pageSize: 20,
      totalCount: 0,
      totalPages: 0,
      
      // 表单相关
      showForm: false,
      formMode: 'add', // 'add' 或 'edit'
      
      // 加载状态
      loading: false,
      editingCustomer: null,
      
      // 删除确认
      showDeleteConfirm: false,
      deletingCustomer: null,
      
      // 批量删除确认
      showBatchDeleteConfirm: false,
      deletingCustomers: [],
      
      // 省份列表
      provinces: [
        '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
        '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
        '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
        '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆', '台湾',
        '香港', '澳门'
      ]
    }
  },
  computed: {
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.customers.length > 0 && this.selectedCustomers.length === this.customers.length
    }
  },
  mounted() {
    this.loadCustomers()
  },
  methods: {
    /**
     * 加载客户列表
     */
    async loadCustomers() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        };

        // 添加搜索条件
        if (this.searchForm.customerName) {
          params.customerName = this.searchForm.customerName;
        }
        if (this.searchForm.contact) {
          params.contact = this.searchForm.contact;
        }
        if (this.searchForm.province) {
          params.province = this.searchForm.province;
        }
        if (this.searchForm.customerRank) {
          params.customerRank = this.searchForm.customerRank;
        }

        const data = await getCustomerList(params);
        this.customers = data.customers || [];
        this.totalCount = data.totalItems || 0;
        this.totalPages = data.totalPages || 0;
        
        // 清空选中状态
        this.selectedCustomers = [];
      } catch (error) {
        console.error('加载客户列表失败:', error);
        this.$message?.error('加载客户列表失败: ' + error.message);
        this.customers = [];
        this.totalCount = 0;
        this.totalPages = 0;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 搜索客户
     */
    searchCustomers() {
      this.currentPage = 1
      this.loadCustomers()
    },

    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        customerName: '',
        contact: '',
        province: '',
        customerRank: ''
      }
      this.currentPage = 1
      this.loadCustomers()
    },

    /**
     * 判断客户是否被选中
     */
    isSelected(customer) {
      return this.selectedCustomers.some(c => c.customerId === customer.customerId);
    },
    
    /**
     * 切换客户选中状态
     */
    toggleSelectCustomer(customer) {
      const index = this.selectedCustomers.findIndex(c => c.customerId === customer.customerId);
      if (index === -1) {
        // 如果未选中，则添加到选中数组
        this.selectedCustomers.push(customer);
      } else {
        // 如果已选中，则从选中数组中移除
        this.selectedCustomers.splice(index, 1);
      }
    },

    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (this.isAllSelected) {
        // 如果已全选，则清空选中
        this.selectedCustomers = [];
      } else {
        // 如果未全选，则选中所有
        this.selectedCustomers = [...this.customers];
      }
    },

    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingCustomer = null
      this.showForm = true
    },



    /**
     * 编辑客户
     */
    editCustomer(customer) {
      this.formMode = 'edit'
      this.editingCustomer = { ...customer }
      this.showForm = true
    },

    /**
     * 删除选中的客户
     */
    deleteSelected() {
      if (this.selectedCustomers.length > 0) {
        if (this.selectedCustomers.length === 1) {
          // 单个删除
          this.deleteCustomer(this.selectedCustomers[0]);
        } else {
          // 批量删除
          this.deletingCustomers = [...this.selectedCustomers];
          this.showBatchDeleteConfirm = true;
        }
      }
    },

    /**
     * 删除客户
     */
    deleteCustomer(customer) {
      this.deletingCustomer = customer
      this.showDeleteConfirm = true
    },

    /**
     * 确认删除
     */
    async confirmDelete() {
      try {
        await deleteCustomer(this.deletingCustomer.customerId);
        
        // 重新加载列表
        this.loadCustomers();
        
        // 清空选择
        this.selectedCustomers = [];
        
        this.closeDeleteConfirm();
        this.$message?.success('客户删除成功');
        
      } catch (error) {
        console.error('删除客户失败:', error);
        this.$message?.error('删除客户失败: ' + error.message);
      }
    },

    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingCustomer = null
    },

    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
      this.deletingCustomers = []
    },

    /**
     * 确认批量删除
     */
    async confirmBatchDelete() {
      try {
        // 获取所有要删除的客户ID
        const customerIds = this.deletingCustomers.map(customer => customer.customerId);
        
        // 调用批量删除API
        await batchDeleteCustomers(customerIds);
        
        // 重新加载列表
        this.loadCustomers();
        
        // 清空选择
        this.selectedCustomers = [];
        
        this.closeBatchDeleteConfirm();
        this.$message?.success(`成功删除 ${customerIds.length} 个客户`);
        
      } catch (error) {
        console.error('批量删除客户失败:', error);
        this.$message?.error('批量删除客户失败: ' + error.message);
      }
    },

    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.editingCustomer = null
    },

    /**
     * 保存客户
     */
    async saveCustomer(customerData) {
      try {
        // 保存前进行客户名称判重（正常逻辑，不以错误处理）
        const available = await checkCustomerNameAvailable(
          customerData.customerName,
          this.formMode === 'edit' ? customerData.customerId : null
        )
        if (!available) {
          this.$message?.warning('客户名称已存在，请更换后再保存')
          return
        }

        if (this.formMode === 'add') {
          await createCustomer(customerData);
          this.$message?.success('客户新增成功');
        } else {
          await updateCustomer(customerData.customerId, customerData);
          this.$message?.success('客户更新成功');
        }
        
        // 重新加载列表
        this.loadCustomers();
        this.closeForm();
        
      } catch (error) {
        console.error('保存客户失败:', error);
        this.$message?.error('保存客户失败: ' + error.message);
      }
    },

    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadCustomers()
      }
    },

    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadCustomers()
      }
    },

    /**
     * 获取等级样式类
     */
    getRankClass(rank) {
      const classMap = {
        '战略客户': 'rank-strategic',
        '重要客户': 'rank-important',
        '一般客户': 'rank-normal'
      }
      return classMap[rank] || 'rank-normal'
    },

    /**
     * 格式化日期
     */
    formatDate(dateString) {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.customer-management {
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

.search-input, .search-select {
  min-width: 200px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus, .search-select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 表格区域 */
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

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th,
.customer-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.customer-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.customer-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.customer-table tbody tr:hover {
  background: #f5f5f5;
}

.customer-table tbody tr.selected {
  background: #e6f7ff;
}

.empty-cell {
  text-align: center;
  color: #8c8c8c;
  padding: 20px;
}

/* 等级徽章 */
.rank-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.rank-strategic {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.rank-important {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.rank-normal {
  background: #f0f5ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
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
  padding: 18px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 380px;
  width: 90%;
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

/* 批量删除列表 */
.batch-delete-list {
  max-height: 200px;
  overflow-y: auto;
  margin: 12px 0;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.batch-delete-item {
  padding: 6px 8px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  color: #595959;
}

.batch-delete-item:last-child {
  border-bottom: none;
}



/* 图标 */
.icon-plus::before { content: "➕"; }
.icon-edit::before { content: "✏️"; }
.icon-delete::before { content: "🗑️"; }
.icon-search::before { content: "🔍"; }
.icon-refresh::before { content: "🔄"; }

/* 响应式设计 */
@media (max-width: 768px) {
  .customer-management {
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
  }
  
  .search-section {
    padding: 8px 12px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }
  
  .search-input {
    min-width: auto;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px;
  }
}
</style>