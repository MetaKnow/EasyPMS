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
        <button class="btn btn-warning" @click="editSelected" :disabled="!selectedCustomer">
          <i class="icon-edit"></i>
          修改客户
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="!selectedCustomer">
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
              v-for="customer in customers" 
              :key="customer.customerId"
              :class="{ selected: selectedCustomer && selectedCustomer.customerId === customer.customerId }"
              @click="selectCustomer(customer)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="selectedCustomer && selectedCustomer.customerId === customer.customerId"
                  @change="selectCustomer(customer)"
                />
              </td>
              <td>{{ customer.customerName }}</td>
              <td>{{ customer.contact }}</td>
              <td>{{ customer.phoneNumber }}</td>
              <td>{{ customer.province }}</td>
              <td>
                <span class="rank-badge" :class="getRankClass(customer.customerRank)">
                  {{ customer.customerRank }}
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
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="closeDeleteConfirm">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除客户 "{{ deletingCustomer?.customerName }}" 吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CustomerForm from './CustomerForm.vue'

export default {
  name: 'CustomerManagement',
  components: {
    CustomerForm
  },
  data() {
    return {
      // 客户列表数据
      customers: [],
      selectedCustomer: null,
      
      // 搜索表单
      searchForm: {
        customerName: '',
        contact: '',
        province: '',
        customerRank: ''
      },
      
      // 分页
      currentPage: 1,
      pageSize: 15,
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
      return this.customers.length > 0 && this.selectedCustomer !== null
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
        const params = new URLSearchParams({
          page: (this.currentPage - 1).toString(),
          size: this.pageSize.toString(),
          sortBy: 'customerId',
          sortDir: 'desc'
        });

        // 添加搜索条件
        if (this.searchForm.customerName) {
          params.append('customerName', this.searchForm.customerName);
        }
        if (this.searchForm.contact) {
          params.append('contact', this.searchForm.contact);
        }
        if (this.searchForm.province) {
          params.append('province', this.searchForm.province);
        }
        if (this.searchForm.customerRank) {
          params.append('customerRank', this.searchForm.customerRank);
        }

        const response = await fetch(`http://localhost:8081/api/customers?${params}`);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        this.customers = data.customers || [];
        this.totalCount = data.totalItems || 0;
        this.totalPages = data.totalPages || 0;
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
     * 选择客户
     */
    selectCustomer(customer) {
      this.selectedCustomer = this.selectedCustomer?.customerId === customer.customerId ? null : customer
    },

    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked && this.customers.length > 0) {
        this.selectedCustomer = this.customers[0]
      } else {
        this.selectedCustomer = null
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
     * 编辑选中的客户
     */
    editSelected() {
      if (this.selectedCustomer) {
        this.editCustomer(this.selectedCustomer)
      }
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
      if (this.selectedCustomer) {
        this.deleteCustomer(this.selectedCustomer)
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
        const response = await fetch(`http://localhost:8081/api/customers/${this.deletingCustomer.customerId}`, {
          method: 'DELETE'
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // 重新加载列表
        this.loadCustomers();
        
        // 清空选择
        this.selectedCustomer = null;
        
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
        let response;
        
        if (this.formMode === 'add') {
          response = await fetch('http://localhost:8081/api/customers', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(customerData)
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          this.$message?.success('客户新增成功');
          
        } else {
          response = await fetch(`http://localhost:8081/api/customers/${customerData.customerId}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(customerData)
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
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
      return new Date(dateString).toLocaleString()
    }
  }
}
</script>

<style scoped>
.customer-management {
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
  flex-wrap: wrap;
}

.search-input, .search-select {
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  min-width: 130px;
}

.search-input:focus, .search-select:focus {
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
}

.table-container {
  overflow-x: auto;
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
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
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
  
  .search-input, .search-select {
    min-width: auto;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px;
  }
}
</style>