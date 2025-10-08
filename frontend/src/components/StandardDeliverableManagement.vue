<template>
  <div class="deliverable-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">标准交付物</h2>
      <div class="header-actions" v-if="selectedProduct">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增交付物
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedDeliverables.length === 0">
          <i class="icon-delete"></i>
          删除交付物
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

      <!-- 右侧交付物管理区域 -->
      <div class="deliverable-content">
        <!-- 未选择产品时的提示 -->
        <div v-if="!selectedProduct" class="no-selection">
          <div class="no-selection-content">
            <i class="icon-info"></i>
            <p>请从左侧选择一个产品来管理其交付物</p>
          </div>
        </div>

        <!-- 选择产品后的交付物管理界面 -->
        <div v-else class="deliverable-management-content">

          <!-- 搜索和筛选 -->
          <div class="search-section">
            <div class="search-form">
              <input 
                v-model="searchForm.deliverableName" 
                type="text" 
                placeholder="交付物名称"
                class="search-input"
              />
              <select v-model="searchForm.deliverableType" class="search-select">
                <option value="">全部类型</option>
                <option value="步骤交付物">步骤交付物</option>
                <option value="里程碑交付物">里程碑交付物</option>
              </select>
              <select v-model="searchForm.milestoneId" class="search-select">
                <option value="">全部里程碑</option>
                <option v-for="milestone in milestones" :key="milestone.milestoneId" :value="milestone.milestoneId">
                  {{ milestone.milestoneName }}
                </option>
              </select>
              <button class="btn btn-primary" @click="searchDeliverables">
                <i class="icon-search"></i>
                搜索
              </button>
              <button class="btn btn-secondary" @click="resetSearch">
                <i class="icon-refresh"></i>
                重置
              </button>
              <select v-model="sortBy" class="search-select" @change="onSortChange">
                <option value="deliverableId">按ID排序</option>
                <option value="createTime">按创建时间排序</option>
                <option value="updateTime">按更新时间排序</option>
                <option value="deliverableName">按交付物名称排序</option>
              </select>
              <select v-model="sortDir" class="search-select" @change="onSortChange">
                <option value="desc">倒序</option>
                <option value="asc">正序</option>
              </select>
            </div>
          </div>

          <!-- 交付物列表 -->
          <div class="table-section">
            <div class="table-container">
              <table class="deliverable-table">
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
                    <th>交付物名称</th>
                    <th>交付物类型</th>
                    <th>里程碑名称</th>
                    <th>步骤名称</th>
                    <th>是否必须</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(deliverable, index) in deliverables" 
                    :key="deliverable.deliverableId"
                    :class="{ selected: isSelected(deliverable) }"
                    @click="toggleSelect(deliverable)"
                  >
                    <td>
                      <input 
                        type="checkbox" 
                        :checked="isSelected(deliverable)"
                        @change.stop="toggleSelect(deliverable)"
                      />
                    </td>
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>{{ deliverable.deliverableName }}</td>
                    <td>{{ deliverable.deliverableType }}</td>
                    <td>{{ deliverable.milestoneName || '-' }}</td>
                    <td>{{ deliverable.sstepName || '-' }}</td>
                    <td>
                      <span :class="['must-load-tag', deliverable.isMustLoad ? 'required' : 'optional']">
                        {{ deliverable.isMustLoad ? '是' : '否' }}
                      </span>
                    </td>
                    <td>
                      <button class="btn-small btn-primary" @click.stop="editDeliverable(deliverable)">
                        编辑
                      </button>
                      <button class="btn-small btn-danger" @click.stop="deleteDeliverable(deliverable)">
                        删除
                      </button>
                    </td>
                  </tr>
                  <tr v-if="deliverables.length === 0">
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
        </div>
      </div>
    </div>

    <!-- 交付物表单弹窗 -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal-content form-modal" @click.stop>
        <div class="modal-header">
          <h3>{{ formMode === 'add' ? '新增交付物' : '编辑交付物' }}</h3>
          <button class="close-btn" @click="closeForm">×</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveDeliverable">
            <div class="form-group">
              <label>交付物名称 <span class="required">*</span></label>
              <input
                v-model="formData.deliverableName"
                type="text"
                class="form-control"
                placeholder="请输入交付物名称"
                required
              />
            </div>
            
            <div class="form-group">
              <label>系统名称 <span class="required">*</span></label>
              <input
                v-model="formData.systemName"
                type="text"
                class="form-control"
                placeholder="请输入系统名称"
                required
              />
            </div>
            
            <div class="form-group">
              <label>交付物类型 <span class="required">*</span></label>
              <select v-model="formData.deliverableType" class="form-control" required>
                <option value="">请选择交付物类型</option>
                <option value="步骤交付物">步骤交付物</option>
                <option value="里程碑交付物">里程碑交付物</option>
              </select>
            </div>
            
            <!-- 步骤交付物：先选择里程碑，再选择步骤 -->
            <template v-if="formData.deliverableType === '步骤交付物'">
              <div class="form-group">
                <label>关联里程碑 <span class="required">*</span></label>
                <select v-model="formData.selectedMilestoneId" class="form-control" required>
                  <option value="">请选择里程碑</option>
                  <option v-for="milestone in milestones" :key="milestone.milestoneId" :value="milestone.milestoneId">
                    {{ milestone.milestoneName }}
                  </option>
                </select>
              </div>
              
              <div class="form-group" v-if="formData.selectedMilestoneId">
                <label>关联步骤 <span class="required">*</span></label>
                <select v-model="formData.sstepId" class="form-control" required>
                  <option value="">请选择步骤</option>
                  <option v-for="step in availableSteps" :key="step.sstepId" :value="step.sstepId">
                    {{ step.sstepName }}
                  </option>
                </select>
              </div>
            </template>
            
            <!-- 里程碑交付物：直接选择里程碑 -->
            <div class="form-group" v-if="formData.deliverableType === '里程碑交付物'">
              <label>关联里程碑 <span class="required">*</span></label>
              <select v-model="formData.milestoneId" class="form-control" required>
                <option value="">请选择里程碑</option>
                <option v-for="milestone in milestones" :key="milestone.milestoneId" :value="milestone.milestoneId">
                  {{ milestone.milestoneName }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>
                <input 
                  v-model="formData.isMustLoad" 
                  type="checkbox"
                  class="form-checkbox"
                />
                必须上传
              </label>
            </div>
            
            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeForm">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除交付物 "{{ deletingDeliverable?.deliverableName }}" 吗？</p>
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
        <p>确定要删除选中的 {{ deletingDeliverables.length }} 个交付物吗？</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 标准交付物组件
 * 功能：管理项目标准交付物模板，支持按产品分类查看和管理
 */
import {
  getStandardDeliverables,
  getStandardDeliverableById,
  createStandardDeliverable,
  updateStandardDeliverable,
  deleteStandardDeliverable,
  deleteStandardDeliverables,
  getDistinctSystemNames,
  getDistinctProductNames
} from '../api/standardDeliverable.js'

import { getAllStandardMilestones } from '../api/standardMilestone.js'
import { getStandardConstructSteps, getStandardConstructStepsBySystemName, getStandardConstructStepsByMilestoneId, getStandardConstructStepById } from '../api/standardConstructStep.js'

export default {
  name: 'StandardDeliverableManagement',
  data() {
    return {
      // 产品名称列表
      productNames: [],
      
      // 当前选中的产品
      selectedProduct: '',
      
      // 里程碑列表
      milestones: [],
      
      // 交付物列表数据
      deliverables: [],
      
      // 选中的交付物列表
      selectedDeliverables: [],
      
      // 编辑中的交付物
      editingDeliverable: null,
      
      // 删除中的交付物
      deletingDeliverable: null,
      
      // 表单显示状态
      showForm: false,
      
      // 删除确认弹窗显示状态
      showDeleteConfirm: false,
      
      // 批量删除中的交付物
      deletingDeliverables: null,
      
      // 批量删除确认弹窗显示状态
      showBatchDeleteConfirm: false,
      
      // 表单模式：add 或 edit
      formMode: 'add',
      
      // 表单数据
      formData: {
        deliverableName: '',
        systemName: '',
        deliverableType: '',
        isMustLoad: false,
        sstepId: null,
        milestoneId: null,
        selectedMilestoneId: null // 新增：步骤交付物选择的里程碑ID
      },
      
      // 搜索表单
      searchForm: {
        deliverableName: '',
        deliverableType: '',
        milestoneId: ''
      },
      
      // 分页参数
      currentPage: 1,
      pageSize: 20,
      totalElements: 0,
      totalPages: 1,
      
      // 排序参数
      sortBy: 'deliverableId',
      sortDir: 'desc',
      
      // 可用的标准步骤列表
      availableSteps: [],
      
      // 加载状态
      loading: false
    }
  },
  computed: {
    /**
     * 判断是否全选
     */
    isAllSelected() {
      return this.deliverables.length > 0 && this.selectedDeliverables.length === this.deliverables.length
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
        // 如果有产品且当前没有选中产品，自动选择第一个
        if (this.productNames.length > 0 && !this.selectedProduct) {
          this.selectedProduct = this.productNames[0]
        }
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
      this.loadDeliverables()
    },
    
    /**
     * 加载交付物列表
     */
    async loadDeliverables() {
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
        if (this.searchForm.deliverableName) {
          params.deliverableName = this.searchForm.deliverableName
        }
        if (this.searchForm.deliverableType) {
          params.deliverableType = this.searchForm.deliverableType
        }
        if (this.searchForm.milestoneId) {
          params.milestoneId = this.searchForm.milestoneId
        }
        
        const response = await getStandardDeliverables(params)
        this.deliverables = response.deliverables || []
        this.currentPage = (response.currentPage || 0) + 1 // 转换为前端分页（从1开始）
        this.totalElements = response.totalItems || 0
        this.totalPages = Math.max(response.totalPages || 0, 1) // 确保至少为1页，以显示分页器
        
        // 清除选中状态
        this.selectedDeliverables = []
        
      } catch (error) {
        console.error('加载交付物列表失败:', error)
        this.$message?.error('加载交付物列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 排序变化处理
     */
    onSortChange() {
      this.currentPage = 1
      this.loadDeliverables()
    },
    
    /**
     * 搜索交付物
     */
    searchDeliverables() {
      this.currentPage = 1
      this.loadDeliverables()
    },
    
    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        deliverableName: '',
        deliverableType: '',
        milestoneId: ''
      }
      this.currentPage = 1
      if (this.selectedProduct) {
        this.loadDeliverables()
      }
    },
    
    /**
     * 判断交付物是否被选中
     */
    isSelected(deliverable) {
      return this.selectedDeliverables.some(d => d.deliverableId === deliverable.deliverableId)
    },
    
    /**
     * 切换交付物选中状态
     */
    toggleSelect(deliverable) {
      const index = this.selectedDeliverables.findIndex(d => d.deliverableId === deliverable.deliverableId)
      if (index === -1) {
        this.selectedDeliverables.push(deliverable)
      } else {
        this.selectedDeliverables.splice(index, 1)
      }
    },
    
    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        // 全选
        this.selectedDeliverables = [...this.deliverables]
      } else {
        // 取消全选
        this.selectedDeliverables = []
      }
    },
    
    /**
     * 显示添加表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingDeliverable = null
      this.formData = {
        deliverableName: '',
        systemName: this.selectedProduct || '',
        deliverableType: '',
        isMustLoad: false,
        sstepId: null,
        milestoneId: null,
        selectedMilestoneId: null
      }
      this.availableSteps = []
      this.showForm = true
    },
    
    /**
     * 编辑交付物
     */
    async editDeliverable(deliverable) {
      this.formMode = 'edit'
      this.editingDeliverable = { ...deliverable }
      this.formData = {
        deliverableName: deliverable.deliverableName,
        systemName: deliverable.systemName,
        deliverableType: deliverable.deliverableType,
        isMustLoad: deliverable.isMustLoad,
        sstepId: deliverable.sstepId ? Number(deliverable.sstepId) : null,
        milestoneId: deliverable.milestoneId ? Number(deliverable.milestoneId) : null,
        selectedMilestoneId: null // 初始化为null，后面会根据步骤类型设置
      }
      
      // 清空步骤列表
      this.availableSteps = []
      
      // 如果是步骤交付物且有步骤ID，获取步骤信息以设置关联的里程碑
      if (deliverable.deliverableType === '步骤交付物' && deliverable.sstepId) {
        try {
          console.log('正在获取步骤信息，sstepId:', deliverable.sstepId)
          const stepResponse = await getStandardConstructStepById(deliverable.sstepId)
          console.log('步骤信息响应:', stepResponse)
          if (stepResponse && stepResponse.step && stepResponse.step.smilestoneId) {
            console.log('设置selectedMilestoneId为:', stepResponse.step.smilestoneId)
            this.formData.selectedMilestoneId = Number(stepResponse.step.smilestoneId)
            // 加载该里程碑下的步骤列表
            await this.loadStepsByMilestoneId(stepResponse.step.smilestoneId)
          }
        } catch (error) {
          console.error('获取步骤信息失败:', error)
          // 如果获取步骤信息失败，则加载系统下的所有步骤
          if (deliverable.systemName) {
            await this.loadStepsBySystemName(deliverable.systemName)
          }
        }
      } else if (deliverable.deliverableType === '步骤交付物' && deliverable.systemName) {
        // 如果是步骤交付物但没有步骤ID，加载对应的步骤列表
        await this.loadStepsBySystemName(deliverable.systemName)
      }
      
      this.showForm = true
    },
    
    /**
     * 根据系统名称加载标准步骤
     */
    async loadStepsBySystemName(systemName) {
      try {
        console.log('正在根据系统名称加载步骤列表，systemName:', systemName)
        const response = await getStandardConstructStepsBySystemName(systemName)
        console.log('根据系统名称获取的步骤列表:', response)
        this.availableSteps = response.steps || []
        console.log('设置availableSteps为:', this.availableSteps)
        console.log('当前formData.sstepId:', this.formData.sstepId)
      } catch (error) {
        console.error('加载标准步骤失败:', error)
        this.availableSteps = []
      }
    },
    
    /**
     * 根据里程碑ID和系统名称加载标准步骤
     */
    async loadStepsByMilestoneId(milestoneId) {
      try {
        console.log('正在加载步骤列表，milestoneId:', milestoneId, 'systemName:', this.formData.systemName)
        // 使用主查询API，同时按系统名称和里程碑ID过滤
        const response = await getStandardConstructSteps({
          systemName: this.formData.systemName,
          smilestoneId: milestoneId,
          page: 0,
          size: 1000 // 获取所有匹配的步骤
        })
        this.availableSteps = response.steps || []
        console.log('加载的步骤列表:', this.availableSteps)
        console.log('当前formData.sstepId:', this.formData.sstepId)
      } catch (error) {
        console.error('根据里程碑和系统名称加载标准步骤失败:', error)
        this.availableSteps = []
      }
    },
    
    /**
     * 删除选中的交付物
     */
    deleteSelected() {
      if (this.selectedDeliverables.length > 0) {
        if (this.selectedDeliverables.length === 1) {
          // 单个删除
          this.deleteDeliverable(this.selectedDeliverables[0])
        } else {
          // 批量删除
          this.batchDeleteDeliverables()
        }
      }
    },
    
    /**
     * 删除交付物
     */
    deleteDeliverable(deliverable) {
      this.deletingDeliverable = deliverable
      this.showDeleteConfirm = true
    },
    
    /**
     * 批量删除交付物
     */
    batchDeleteDeliverables() {
      this.deletingDeliverables = [...this.selectedDeliverables]
      this.showBatchDeleteConfirm = true
    },
    
    /**
     * 确认删除交付物
     */
    async confirmDelete() {
      if (!this.deletingDeliverable) return
      
      try {
        await deleteStandardDeliverable(this.deletingDeliverable.deliverableId)
        this.$message?.success('交付物删除成功')
        
        // 重新加载列表
        this.loadDeliverables()
        this.closeDeleteConfirm()
        
      } catch (error) {
        console.error('删除交付物失败:', error)
        this.$message?.error('删除交付物失败: ' + error.message)
      }
    },
    
    /**
     * 确认批量删除交付物
     */
    async confirmBatchDelete() {
      if (!this.deletingDeliverables || this.deletingDeliverables.length === 0) return
      
      try {
        // 调用批量删除API
        const deliverableIds = this.deletingDeliverables.map(d => d.deliverableId)
        await deleteStandardDeliverables(deliverableIds)
        
        this.$message?.success(`成功删除 ${this.deletingDeliverables.length} 个交付物`)
        
        // 重新加载列表
        this.loadDeliverables()
        this.closeBatchDeleteConfirm()
        
      } catch (error) {
        console.error('批量删除交付物失败:', error)
        this.$message?.error('批量删除交付物失败: ' + error.message)
      }
    },
    
    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingDeliverable = null
    },
    
    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
      this.deletingDeliverables = null
    },
    
    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.editingDeliverable = null
      this.availableSteps = []
    },
    
    /**
     * 保存交付物
     */
    async saveDeliverable() {
      try {
        // 准备发送到后端的数据，过滤掉前端特有的字段
        const submitData = {
          deliverableName: this.formData.deliverableName,
          systemName: this.formData.systemName,
          deliverableType: this.formData.deliverableType,
          isMustLoad: this.formData.isMustLoad,
          sstepId: this.formData.sstepId,
          milestoneId: this.formData.milestoneId
        }
        
        if (this.formMode === 'add') {
          // 新增交付物
          await createStandardDeliverable(submitData)
          this.$message?.success('交付物新增成功')
          
        } else {
          // 更新交付物
          await updateStandardDeliverable(this.editingDeliverable.deliverableId, submitData)
          this.$message?.success('交付物更新成功')
        }
        
        // 重新加载列表
        this.loadDeliverables()
        this.closeForm()
        
      } catch (error) {
        console.error('保存交付物失败:', error)
        this.$message?.error('保存交付物失败: ' + error.message)
      }
    },
    
    /**
     * 获取关联信息
     */
    getRelatedInfo(deliverable) {
      if (deliverable.deliverableType === '步骤交付物') {
        return deliverable.sstepName || '-'
      } else if (deliverable.deliverableType === '里程碑交付物') {
        return deliverable.milestoneName || '-'
      }
      return '-'
    },
    
    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadDeliverables()
      }
    },
    
    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadDeliverables()
      }
    },
    
    /**
     * 格式化日期
     */
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
  },
  watch: {
    /**
     * 监听产品选择变化，自动加载交付物列表
     */
    selectedProduct(newProduct) {
      if (newProduct) {
        // 重置分页到第一页
        this.currentPage = 1
        // 加载交付物列表
        this.loadDeliverables()
      } else {
        // 清空交付物列表
        this.deliverables = []
        this.totalElements = 0
        this.totalPages = 0
      }
    },

    /**
     * 监听交付物类型变化，动态加载相关数据
     */
     'formData.deliverableType'(newType) {
       if (newType === '步骤交付物' && this.formData.systemName) {
         this.loadStepsBySystemName(this.formData.systemName)
       } else {
         this.availableSteps = []
       }
       
       // 清空不相关的字段
       if (newType === '步骤交付物') {
         this.formData.milestoneId = null
       } else if (newType === '里程碑交付物') {
         this.formData.sstepId = null
         this.formData.selectedMilestoneId = null
       }
     },

     /**
      * 监听系统名称变化，动态加载步骤
      */
     'formData.systemName'(newSystemName) {
       if (this.formData.deliverableType === '步骤交付物' && newSystemName) {
         this.loadStepsBySystemName(newSystemName)
       }
     },

     /**
      * 监听步骤交付物选择的里程碑变化，动态加载步骤
      */
     'formData.selectedMilestoneId'(newMilestoneId) {
       if (this.formData.deliverableType === '步骤交付物' && newMilestoneId) {
         this.loadStepsByMilestoneId(newMilestoneId)
         // 新增模式下清空之前选择的步骤；编辑模式保留原有选择用于回显
         if (this.formMode === 'add') {
           this.formData.sstepId = null
         }
       } else {
         this.availableSteps = []
       }
     }
   }
}
</script>

<style scoped>
/* 主容器 */
.deliverable-management {
  height: 100%;
  padding: 0px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f5;
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
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 主内容区域 */
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
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.product-tags {
  padding: 8px;
  overflow-y: auto;
  flex: 1;
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
  border-color: #91d5ff;
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

/* 右侧交付物内容区域 */
.deliverable-content {
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
  margin: 0;
  font-size: 16px;
}

/* 交付物管理内容区域 */
.deliverable-management-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  overflow: hidden;
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

.deliverable-table {
  width: 100%;
  border-collapse: collapse;
}

.deliverable-table th,
.deliverable-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.deliverable-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.deliverable-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.deliverable-table tbody tr:hover {
  background: #f5f5f5;
}

.deliverable-table tbody tr.selected {
  background: #e6f7ff;
}

.no-data {
  text-align: center;
  color: #8c8c8c;
  padding: 20px;
}

/* 必须加载标签 */
.must-load-tag {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.must-load-tag.required {
  background: #fff2e8;
  color: #fa8c16;
  border: 1px solid #ffbb96;
}

.must-load-tag.optional {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
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

.form-modal {
  max-width: 500px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #8c8c8c;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #262626;
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
  margin-top: 16px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #262626;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-checkbox {
  margin-right: 8px;
}

.required {
  color: #ff4d4f;
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
  .deliverable-management {
    padding: 4px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
    padding: 8px 12px;
  }
  
  .header-actions {
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