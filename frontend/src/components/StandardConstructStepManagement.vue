<template>
  <div class="step-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">标准交付步骤</h2>
      <div class="header-actions" v-if="selectedProduct">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增步骤
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedSteps.length === 0">
          <i class="icon-delete"></i>
          删除步骤
        </button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧产品名称标签区域 -->
      <div class="product-sidebar">
        <div class="sidebar-header">
          <h3>产品名称</h3>
          <button class="btn btn-secondary btn-small" @click="refreshProducts">
            <i class="icon-refresh"></i>
            刷新
          </button>
        </div>
        <div class="product-tags">
          <div 
            v-for="productName in productNames" 
            :key="productName"
            :class="['product-tag', { active: selectedProduct === productName }]"
            @click="selectProduct(productName)"
          >
            {{ productName }}
          </div>
          <div v-if="productNames.length === 0" class="no-products">
            暂无产品数据
          </div>
        </div>
      </div>

      <!-- 右侧步骤管理区域 -->
      <div class="step-content">
        <!-- 未选择产品时的提示 -->
        <div v-if="!selectedProduct" class="no-selection">
          <div class="no-selection-content">
            <i class="icon-info"></i>
            <p>请从左侧选择一个产品来管理其交付步骤</p>
          </div>
        </div>

        <!-- 选择产品后的步骤管理界面 -->
        <div v-else class="step-management-content">

          <!-- 搜索和筛选 -->
          <div class="search-section">
            <div class="search-form">
              <input 
                v-model="searchForm.stepName" 
                type="text" 
                placeholder="步骤名称"
                class="search-input"
              />
              <select v-model="searchForm.type" class="search-select">
                <option value="">全部类型</option>
                <option value="标准产品">标准产品</option>
                <option value="接口开发">接口开发</option>
                <option value="数据迁移">数据迁移</option>
                <option value="个性化功能开发">个性化功能开发</option>
                <option value="用户培训">用户培训</option>
                <option value="系统上线试运行">系统上线试运行</option>
              </select>
              <select v-model="searchForm.milestoneId" class="search-select">
                <option value="">全部里程碑</option>
                <option v-for="milestone in milestones" :key="milestone.milestoneId" :value="milestone.milestoneId">
                  {{ milestone.milestoneName }}
                </option>
              </select>
              <button class="btn btn-primary" @click="searchSteps">
                <i class="icon-search"></i>
                搜索
              </button>
              <button class="btn btn-secondary" @click="resetSearch">
                <i class="icon-refresh"></i>
                重置
              </button>
              <select v-model="sortBy" class="search-select" @change="onSortChange">
                <option value="sstepId">按ID排序</option>
                <option value="createTime">按创建时间排序</option>
                <option value="updateTime">按更新时间排序</option>
                <option value="sstepName">按步骤名称排序</option>
              </select>
              <select v-model="sortDir" class="search-select" @change="onSortChange">
                <option value="desc">倒序</option>
                <option value="asc">正序</option>
              </select>
            </div>
          </div>

          <!-- 步骤列表 -->
          <div class="table-section">
            <div class="table-container">
              <table class="step-table">
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
                    <th>步骤名称</th>
                    <th>步骤类型</th>
                    <th>所属里程碑</th>
                    <th>创建时间</th>
                    <th>更新时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(step, index) in steps" 
                    :key="step.sstepId"
                    :class="{ selected: isSelected(step) }"
                    @click="toggleSelect(step)"
                  >
                    <td>
                      <input 
                        type="checkbox" 
                        :checked="isSelected(step)"
                        @change.stop="toggleSelect(step)"
                      />
                    </td>
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>{{ step.sstepName }}</td>
                    <td>{{ step.type }}</td>
                    <td>{{ getMilestoneName(step.smilestoneId) }}</td>
                    <td>{{ formatDate(step.createTime) }}</td>
                    <td>{{ formatDate(step.updateTime) }}</td>
                    <td>
                      <button class="btn-small btn-primary" @click.stop="editStep(step)">
                        编辑
                      </button>
                      <button class="btn-small btn-danger" @click.stop="deleteStep(step)">
                        删除
                      </button>
                    </td>
                  </tr>
                  <tr v-if="steps.length === 0">
                    <td colspan="8" class="no-data">暂无数据</td>
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
        </div>
      </div>
    </div>

    <!-- 步骤表单弹窗 -->
    <StandardConstructStepForm
      v-if="showForm"
      :visible="showForm"
      :step="editingStep"
      :mode="formMode"
      :selectedProductName="selectedProduct"
      @close="closeForm"
      @save="saveStep"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除步骤 "{{ deletingStep?.sstepName }}" 吗？此操作不可恢复。</p>
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
        <p>确定要删除选中的 {{ deletingSteps?.length || 0 }} 个步骤吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import StandardConstructStepForm from './StandardConstructStepForm.vue'
import { 
  getStandardConstructSteps, 
  createStandardConstructStep, 
  updateStandardConstructStep, 
  deleteStandardConstructStep, 
  deleteStandardConstructSteps,
  getDistinctProductNames
} from '../api/standardConstructStep.js'
import { getAllStandardMilestones } from '../api/standardMilestone.js'

/**
 * 标准交付步骤管理组件
 * 用于管理标准交付步骤信息，支持按产品分类管理
 */
export default {
  name: 'StandardConstructStepManagement',
  components: {
    StandardConstructStepForm
  },
  data() {
    return {
      // 产品名称列表
      productNames: [],
      
      // 当前选中的产品
      selectedProduct: '',
      
      // 里程碑列表
      milestones: [],
      
      // 步骤列表数据
      steps: [],
      
      // 选中的步骤列表
      selectedSteps: [],
      
      // 编辑中的步骤
      editingStep: null,
      
      // 删除中的步骤
      deletingStep: null,
      
      // 表单显示状态
      showForm: false,
      
      // 删除确认弹窗显示状态
      showDeleteConfirm: false,
      
      // 批量删除中的步骤
      deletingSteps: null,
      
      // 批量删除确认弹窗显示状态
      showBatchDeleteConfirm: false,
      
      // 表单模式：add 或 edit
      formMode: 'add',
      
      // 搜索表单
      searchForm: {
        stepName: '',
        type: '',
        milestoneId: ''
      },
      
      // 分页参数
      currentPage: 1,
      pageSize: 20,
      totalCount: 0,
      totalPages: 0,
      
      // 排序参数
      sortBy: 'sstepId',
      sortDir: 'desc',
      
      // 加载状态
      loading: false
    }
  },
  computed: {
    /**
     * 判断是否全选
     */
    isAllSelected() {
      return this.steps.length > 0 && this.selectedSteps.length === this.steps.length
    }
  },
  created() {
    // 组件创建时加载产品名称和里程碑列表
    this.loadProductNames()
    this.loadMilestones()
  },
  methods: {
    /**
     * 加载产品名称列表
     */
    async loadProductNames() {
      try {
        this.productNames = await getDistinctProductNames()
      } catch (error) {
        console.error('加载产品名称列表失败:', error)
        this.$message?.error('加载产品名称列表失败: ' + error.message)
      }
    },
    
    /**
     * 刷新产品名称列表
     */
    refreshProducts() {
      this.loadProductNames()
    },
    
    /**
     * 加载里程碑列表
     */
    async loadMilestones() {
      try {
        const response = await getAllStandardMilestones()
        // 后端返回的数据格式是 { milestones: [...] }
        this.milestones = response.milestones || []
      } catch (error) {
        console.error('加载里程碑列表失败:', error)
        this.$message?.error('加载里程碑列表失败: ' + error.message)
        this.milestones = [] // 确保在错误情况下milestones是数组
      }
    },
    
    /**
     * 选择产品
     */
    selectProduct(productName) {
      this.selectedProduct = productName
      this.currentPage = 1
      this.resetSearch()
      this.loadSteps()
    },
    
    /**
     * 加载步骤列表
     */
    async loadSteps() {
      if (!this.selectedProduct) return
      
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1, // 后端分页从0开始
          size: this.pageSize,
          sortBy: this.sortBy,
          sortDir: this.sortDir,
          systemName: this.selectedProduct // 按产品名称过滤
        }
        
        // 添加搜索条件
        if (this.searchForm.stepName) {
          params.sstepName = this.searchForm.stepName
        }
        if (this.searchForm.type) {
          params.type = this.searchForm.type
        }
        if (this.searchForm.milestoneId) {
          params.smilestoneId = this.searchForm.milestoneId
        }
        
        const data = await getStandardConstructSteps(params)
        this.steps = data.steps || []
        this.currentPage = (data.currentPage || 0) + 1 // 转换为前端分页（从1开始）
        this.totalCount = data.totalItems || 0
        this.totalPages = data.totalPages || 0
        
        // 清除选中状态
        this.selectedSteps = []
        
      } catch (error) {
        console.error('加载步骤列表失败:', error)
        this.$message?.error('加载步骤列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 排序变化处理
     */
    onSortChange() {
      this.currentPage = 1
      this.loadSteps()
    },
    
    /**
     * 搜索步骤
     */
    searchSteps() {
      this.currentPage = 1
      this.loadSteps()
    },
    
    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        stepName: '',
        type: '',
        milestoneId: ''
      }
      this.currentPage = 1
      if (this.selectedProduct) {
        this.loadSteps()
      }
    },
    
    /**
     * 判断步骤是否被选中
     */
    isSelected(step) {
      return this.selectedSteps.some(s => s.sstepId === step.sstepId)
    },
    
    /**
     * 切换步骤选中状态
     */
    toggleSelect(step) {
      const index = this.selectedSteps.findIndex(s => s.sstepId === step.sstepId)
      if (index === -1) {
        this.selectedSteps.push(step)
      } else {
        this.selectedSteps.splice(index, 1)
      }
    },
    
    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        // 全选
        this.selectedSteps = [...this.steps]
      } else {
        // 取消全选
        this.selectedSteps = []
      }
    },
    
    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingStep = null
      this.showForm = true
    },
    
    /**
     * 编辑步骤
     */
    editStep(step) {
      this.formMode = 'edit'
      this.editingStep = { ...step }
      this.showForm = true
    },
    
    /**
     * 删除选中的步骤
     */
    deleteSelected() {
      if (this.selectedSteps.length > 0) {
        if (this.selectedSteps.length === 1) {
          // 单个删除
          this.deleteStep(this.selectedSteps[0])
        } else {
          // 批量删除
          this.batchDeleteSteps()
        }
      }
    },
    
    /**
     * 删除步骤
     */
    deleteStep(step) {
      this.deletingStep = step
      this.showDeleteConfirm = true
    },
    
    /**
     * 批量删除步骤
     */
    batchDeleteSteps() {
      this.deletingSteps = [...this.selectedSteps]
      this.showBatchDeleteConfirm = true
    },
    
    /**
     * 确认删除步骤
     */
    async confirmDelete() {
      if (!this.deletingStep) return
      
      try {
        await deleteStandardConstructStep(this.deletingStep.sstepId)
        this.$message?.success('步骤删除成功')
        
        // 重新加载列表
        this.loadSteps()
        this.closeDeleteConfirm()
        
      } catch (error) {
        console.error('删除步骤失败:', error)
        this.$message?.error('删除步骤失败: ' + error.message)
      }
    },
    
    /**
     * 确认批量删除步骤
     */
    async confirmBatchDelete() {
      if (!this.deletingSteps || this.deletingSteps.length === 0) return
      
      try {
        // 调用批量删除API
        const stepIds = this.deletingSteps.map(s => s.sstepId)
        await deleteStandardConstructSteps(stepIds)
        
        this.$message?.success(`成功删除 ${this.deletingSteps.length} 个步骤`)
        
        // 重新加载列表
        this.loadSteps()
        this.closeBatchDeleteConfirm()
        
      } catch (error) {
        console.error('批量删除步骤失败:', error)
        this.$message?.error('批量删除步骤失败: ' + error.message)
      }
    },
    
    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingStep = null
    },
    
    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
      this.deletingSteps = null
    },
    
    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.editingStep = null
    },
    
    /**
     * 保存步骤
     */
    async saveStep(stepData) {
      try {
        if (this.formMode === 'add') {
          // 新增步骤
          await createStandardConstructStep(stepData)
          this.$message?.success('步骤新增成功')
          
        } else {
          // 更新步骤
          await updateStandardConstructStep(stepData.sstepId, stepData)
          this.$message?.success('步骤更新成功')
        }
        
        // 重新加载列表
        this.loadSteps()
        this.closeForm()
        
      } catch (error) {
        console.error('保存步骤失败:', error)
        this.$message?.error('保存步骤失败: ' + error.message)
      }
    },
    
    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadSteps()
      }
    },
    
    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadSteps()
      }
    },
    
    /**
     * 根据里程碑ID获取里程碑名称
     */
    getMilestoneName(milestoneId) {
      if (!milestoneId) return ''
      if (!Array.isArray(this.milestones)) return ''
      const milestone = this.milestones.find(m => m.milestoneId === milestoneId)
      return milestone ? milestone.milestoneName : ''
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
.step-management {
  padding: 0px;
  background: #f5f5f5;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 页面头部 */
.page-header {
  margin-bottom: 8px;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

/* 左侧产品标签区域 */
.product-sidebar {
  width: 250px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.product-tags {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.product-tag {
  display: block;
  padding: 8px 12px;
  margin-bottom: 4px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #262626;
  text-align: center;
}

.product-tag:hover {
  background: #e6f7ff;
  border-color: #40a9ff;
  color: #1890ff;
}

.product-tag.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.no-products {
  text-align: center;
  color: #8c8c8c;
  padding: 20px;
  font-size: 14px;
}

/* 右侧步骤管理区域 */
.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 未选择产品时的提示 */
.no-selection {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.no-selection-content {
  text-align: center;
  color: #8c8c8c;
}

.no-selection-content i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.no-selection-content p {
  font-size: 16px;
  margin: 0;
}

/* 步骤管理内容区域 */
.step-management-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

/* 操作头部 */
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.selected-product-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-label {
  font-size: 14px;
  color: #8c8c8c;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  padding: 4px 8px;
  background: #e6f7ff;
  border-radius: 4px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
}

/* 搜索区域 */
.search-section {
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

.search-input,
.search-select {
  min-width: 150px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus,
.search-select:focus {
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
}

.step-table {
  width: 100%;
  border-collapse: collapse;
}

.step-table th,
.step-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.step-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.step-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.step-table tbody tr:hover {
  background: #f5f5f5;
}

.step-table tbody tr.selected {
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
  margin-top: auto;
  flex-shrink: 0;
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
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  background: white;
  color: #262626;
}

.btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn:disabled:hover {
  border-color: #d9d9d9;
  color: #262626;
}

.btn-primary {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
  border-color: #40a9ff;
  color: white;
}

.btn-secondary {
  background: white;
  border-color: #d9d9d9;
  color: #262626;
}

.btn-secondary:hover:not(:disabled) {
  border-color: #40a9ff;
  color: #40a9ff;
}

.btn-danger {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #ff7875;
  border-color: #ff7875;
  color: white;
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 20px;
  width: 90%;
  max-width: 400px;
}

.modal-content h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.modal-content p {
  margin: 0 0 16px 0;
  color: #595959;
  line-height: 1.5;
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
.icon-info::before { content: "ℹ️"; }

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    flex-direction: column;
  }
  
  .product-sidebar {
    width: 100%;
    max-height: 200px;
  }
  
  .product-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  
  .product-tag {
    flex: 0 0 auto;
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .step-management {
    padding: 4px;
  }
  
  .action-header {
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
  
  .search-input,
  .search-select {
    min-width: auto;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px;
  }
}
</style>