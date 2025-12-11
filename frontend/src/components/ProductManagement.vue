<template>
  <div class="product-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">基础产品维护</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增产品
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedProducts.length === 0">
          <i class="icon-delete"></i>
          删除产品
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          v-model="searchForm.softName" 
          type="text" 
          placeholder="产品名称"
          class="search-input"
        />
        <input 
          v-model="searchForm.softVersion" 
          type="text" 
          placeholder="产品版本"
          class="search-input"
        />
        <button class="btn btn-primary" @click="searchProducts">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 产品列表 -->
    <div class="table-section">
      <div class="table-container">
        <table class="product-table">
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
              <th>产品名称</th>
              <th>产品类型</th>
              <th>产品版本</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(product, index) in products" 
              :key="product.softId"
              :class="{ selected: isSelected(product) }"
              @click="toggleSelect(product)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="isSelected(product)"
                  @change.stop="toggleSelect(product)"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ product.softName }}</td>
              <td>{{ product.softType || '-' }}</td>
              <td>{{ product.softVersion }}</td>
              <td>{{ formatDate(product.createTime) }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editProduct(product)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteProduct(product)">
                  删除
                </button>
              </td>
            </tr>
            <tr v-if="products.length === 0">
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

    <!-- 产品表单弹窗 -->
    <ProductForm
      v-if="showForm"
      :visible="showForm"
      :product="editingProduct"
      :mode="formMode"
      @close="closeForm"
      @save="saveProduct"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除产品 "{{ deletingProduct?.softName }}" 吗？此操作不可恢复。</p>
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
        <p>确定要删除选中的 {{ deletingProducts?.length || 0 }} 个产品吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProductForm from './ProductForm.vue'

export default {
  name: 'ProductManagement',
  components: {
    ProductForm
  },
  data() {
    return {
      API_BASE: __BACKEND_API_URL__ + '/api',
      // 产品列表数据
      products: [],
      
      // 选中的产品列表
      selectedProducts: [],
      
      // 编辑中的产品
      editingProduct: null,
      
      // 删除中的产品
      deletingProduct: null,
      
      // 表单显示状态
      showForm: false,
      
      // 删除确认弹窗显示状态
      showDeleteConfirm: false,
      
      // 批量删除中的产品
      deletingProducts: null,
      
      // 批量删除确认弹窗显示状态
      showBatchDeleteConfirm: false,
      
      // 表单模式：add 或 edit
      formMode: 'add',
      
      // 搜索表单
      searchForm: {
        softName: '',
        softVersion: ''
      },
      
      // 分页参数
      currentPage: 1,
      pageSize: 20,
      totalCount: 0,
      totalPages: 0,
      
      // 加载状态
      loading: false
    }
  },
  computed: {
    /**
     * 判断是否全选
     */
    isAllSelected() {
      return this.products.length > 0 && this.selectedProducts.length === this.products.length
    }
  },
  created() {
    // 组件创建时加载产品列表
    this.loadProducts()
  },
  methods: {
    /**
     * 加载产品列表
     */
    async loadProducts() {
      this.loading = true
      try {
        const params = new URLSearchParams({
          page: this.currentPage - 1, // 后端分页从0开始
          size: this.pageSize,
          sortBy: 'typeVersion',
          sortDir: 'desc'
        })
        
        // 添加搜索条件
        if (this.searchForm.softName) {
          params.append('softName', this.searchForm.softName)
        }
        if (this.searchForm.softVersion) {
          params.append('softVersion', this.searchForm.softVersion)
        }
        
        const response = await fetch(`${this.API_BASE}/products?${params}`)
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        this.products = data.products || []
        this.currentPage = data.currentPage + 1 // 转换为前端分页（从1开始）
        this.totalCount = data.totalItems
        this.totalPages = data.totalPages
        
        // 清除选中状态
        this.selectedProducts = []
        
      } catch (error) {
        console.error('加载产品列表失败:', error)
        this.$message?.error('加载产品列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 搜索产品
     */
    searchProducts() {
      this.currentPage = 1
      this.loadProducts()
    },
    
    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        softName: '',
        softVersion: ''
      }
      this.currentPage = 1
      this.loadProducts()
    },
    
    /**
     * 判断产品是否被选中
     */
    isSelected(product) {
      return this.selectedProducts.some(p => p.softId === product.softId)
    },
    
    /**
     * 切换产品选中状态
     */
    toggleSelect(product) {
      const index = this.selectedProducts.findIndex(p => p.softId === product.softId)
      if (index === -1) {
        this.selectedProducts.push(product)
      } else {
        this.selectedProducts.splice(index, 1)
      }
    },
    
    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        // 全选
        this.selectedProducts = [...this.products]
      } else {
        // 取消全选
        this.selectedProducts = []
      }
    },
    
    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingProduct = null
      this.showForm = true
    },
    

    
    /**
     * 编辑产品
     */
    editProduct(product) {
      this.formMode = 'edit'
      this.editingProduct = { ...product }
      this.showForm = true
    },
    
    /**
     * 删除选中的产品
     */
    deleteSelected() {
      if (this.selectedProducts.length > 0) {
        if (this.selectedProducts.length === 1) {
          // 单个删除
          this.deleteProduct(this.selectedProducts[0])
        } else {
          // 批量删除
          this.batchDeleteProducts()
        }
      }
    },
    
    /**
     * 删除产品
     */
    deleteProduct(product) {
      this.deletingProduct = product
      this.showDeleteConfirm = true
    },
    
    /**
     * 批量删除产品
     */
    batchDeleteProducts() {
      this.deletingProducts = [...this.selectedProducts]
      this.showBatchDeleteConfirm = true
    },
    
    /**
     * 确认批量删除产品
     */
    async confirmBatchDelete() {
      if (!this.deletingProducts || this.deletingProducts.length === 0) return
      
      try {
        // 调用批量删除API
        const response = await fetch(`${this.API_BASE}/products/batch`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.deletingProducts.map(p => p.softId))
        })
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const result = await response.json()
        this.$message?.success(`成功删除 ${result.deletedCount || this.deletingProducts.length} 个产品`)
        this.closeBatchDeleteConfirm()
        this.loadProducts()
        
      } catch (error) {
        console.error('批量删除产品失败:', error)
        this.$message?.error('批量删除产品失败: ' + error.message)
      }
    },
    
    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
      this.deletingProducts = null
    },
    
    /**
     * 确认删除产品
     */
    async confirmDelete() {
      if (!this.deletingProduct) return
      
      try {
        const response = await fetch(`${this.API_BASE}/products/${this.deletingProduct.softId}`, {
          method: 'DELETE'
        })
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        this.$message?.success('产品删除成功')
        this.closeDeleteConfirm()
        this.loadProducts()
        
      } catch (error) {
        console.error('删除产品失败:', error)
        this.$message?.error('删除产品失败: ' + error.message)
      }
    },
    
    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingProduct = null
    },
    
    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.editingProduct = null
    },
    
    /**
     * 保存产品
     */
    async saveProduct(productData) {
      try {
        let response
        
        if (this.formMode === 'add') {
          // 新增产品
          response = await fetch(`${this.API_BASE}/products`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(productData)
          })
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
          }
          
          this.$message?.success('产品新增成功')
          
        } else {
          // 更新产品
          response = await fetch(`${this.API_BASE}/products/${productData.softId}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(productData)
          })
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
          }
          
          this.$message?.success('产品更新成功')
        }
        
        // 重新加载列表
        this.loadProducts()
        this.closeForm()
        
      } catch (error) {
        console.error('保存产品失败:', error)
        this.$message?.error('保存产品失败: ' + error.message)
      }
    },
    
    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadProducts()
      }
    },
    
    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadProducts()
      }
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
.product-management {
  padding: 0px;
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

/* 操作按钮 */
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

.product-table {
  width: 100%;
  border-collapse: collapse;
}

.product-table th,
.product-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.product-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.product-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.product-table tbody tr:hover {
  background: #f5f5f5;
}

.product-table tbody tr.selected {
  background: #e6f7ff;
}

.no-data {
  text-align: center;
  color: #8c8c8c;
  padding: 20px;
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

/* 图标 */
.icon-plus::before { content: "➕"; }
.icon-edit::before { content: "✏️"; }
.icon-delete::before { content: "🗑️"; }
.icon-search::before { content: "🔍"; }
.icon-refresh::before { content: "🔄"; }

/* 响应式设计 */
@media (max-width: 768px) {
  .product-management {
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
