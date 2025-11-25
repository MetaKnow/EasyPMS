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
        <button class="btn btn-success" @click="exportTable">
          <i class="icon-download"></i>
          导出表格
        </button>
        <button class="btn btn-warning" @click="triggerImport">
          <i class="icon-upload"></i>
          导入表格
        </button>
        <input 
          ref="fileInput" 
          type="file" 
          accept=".xlsx,.xls,.csv" 
          style="display: none" 
          @change="handleFileImport"
        />
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
              <span class="sort-info">排序：先按里程碑顺序，再按步骤名称</span>
            </div>
          </div>

          <!-- 交付物列表 -->
          <div class="table-section">
            <div class="table-container" @scroll="onTableScroll">
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
                <tbody @mouseover="onTableMouseOver" @mousemove="onTableMouseMove" @mouseout="onTableMouseOut">
                  <tr 
                    v-for="(deliverable, index) in deliverables" 
                    :key="deliverable.deliverableId"
                    :class="[{ selected: isSelected(deliverable) }, deliverable.deliverableType === '里程碑交付物' ? 'milestone-row' : '']"
                    @click="toggleSelect(deliverable)"
                  >
                    <td>
                      <input 
                        type="checkbox" 
                        :checked="isSelected(deliverable)"
                        @change.stop="toggleSelect(deliverable)"
                      />
                    </td>
                    <td>{{ index + 1 }}</td>
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
                      <button class="btn-small btn-primary edit-btn" @click.stop="editDeliverable(deliverable)">
                        编辑
                        <span v-if="hasTemplatesForDeliverable(deliverable)" class="t-badge">T</span>
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
            <div v-if="tooltipVisible" ref="cellTooltip" class="cell-tooltip" :style="tooltipStyle">{{ tooltipText }}</div>
            
          </div>
        </div>
      </div>
    </div>

    <!-- 交付物表单弹窗 -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal-content form-modal" ref="formDragModal" :style="formDragStyle" @click.stop>
        <div class="modal-header" @mousedown="startFormDrag">
          <h3>{{ formMode === 'add' ? '新增交付物' : '编辑交付物' }}</h3>
          <button class="close-btn" @click="closeForm">×</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveDeliverable">
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
              <label>
                <input 
                  v-model="formData.isMustLoad" 
                  type="checkbox"
                  class="form-checkbox"
                />
                必须上传
              </label>
            </div>

            <!-- 模板文件上传与列表（仅编辑模式显示） -->
            <div class="form-group" v-if="formMode === 'edit'">
              <label>模板文件</label>
              <div class="template-upload">
                <div
                  class="dropzone"
                  :class="{ active: dropActive }"
                  @click="openFileSelector"
                  @dragover.prevent="handleDragOver"
                  @dragleave.prevent="handleDragLeave"
                  @drop.prevent="handleDrop"
                >
                  <i class="icon-upload"></i>
                  拖拽文件到此或点击选择
                  <!--<div class="hint">支持所有文件类型，无大小限制</div>-->
                </div>
                <input
                  ref="templateUploadInput"
                  type="file"
                  multiple
                  @change="onTemplateFileSelect"
                  style="display:none"
                />
                <div class="actions">
                  <span v-if="templateUploading" class="uploading-tip">正在上传...</span>
                </div>
              </div>
              <div class="template-list" v-if="existingTemplates && existingTemplates.length > 0">
                <div class="template-item" v-for="f in existingTemplates" :key="f.name || f.filename">
                  <span class="file-link" @click="downloadTemplate((f.name || f.filename))">
                    {{ f.name || f.filename }}
                  </span>
                  <span class="file-meta">
                    {{ formatSize(f.size) }}
                  </span>
                  <button type="button" class="btn-icon btn-danger" title="删除" @click="deleteTemplate((f.name || f.filename))">
                    <i class="icon-delete"></i>
                  </button>
                </div>
              </div>
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
  getDistinctProductNames,
  uploadDeliverableTemplates,
  listDeliverableTemplates,
  downloadDeliverableTemplate,
  deleteDeliverableTemplate
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
      
      // 表单拖动状态
      formDragStyle: { position: 'fixed', top: '0px', left: '0px' },
      formDragging: false,
      formDragStart: { mouseX: 0, mouseY: 0, top: 0, left: 0 },
      
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
      
      // 注意：排序已在后端SQL中固定实现，先按里程碑创建时间，再按步骤名称
      
      // 可用的标准步骤列表
      availableSteps: [],
      
      // 加载状态
      loading: false,

      // 单元格悬浮提示
      tooltipVisible: false,
      tooltipText: '',
      tooltipStyle: { top: '0px', left: '0px' },
      tooltipCell: null,
      
      // 模板文件上传/列表状态
      templateFiles: [],
      existingTemplates: [],
      templateUploading: false,
      dropActive: false,
      templateAccept: '.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.zip,.rar,.7z,.txt',
      maxUploadSizeMB: 20,
      // 列表中交付物是否存在已上传模板的缓存映射：{ [deliverableId]: boolean }
      hasTemplatesByDeliverableId: {}
    }
  },
  computed: {
    /**
     * 判断是否全选
     */
    isAllSelected() {
      return this.deliverables.length > 0 && this.selectedDeliverables.length === this.deliverables.length
    },
    /**
     * 已选文件总大小（bytes）
     */
    totalSelectedSize() {
      return (this.templateFiles || []).reduce((sum, f) => sum + (f.size || 0), 0)
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
          page: 0, // 加载全部数据时固定为第一页
          size: 100000, // 加载全量数据
          systemName: this.selectedProduct // 按产品名称过滤
          // 注意：排序已在后端SQL中固定实现，先按里程碑创建时间，再按步骤名称
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

        // 预拉取模板存在性以在“编辑”按钮右上角显示“T”标识
        if (this.deliverables && this.deliverables.length > 0) {
          this.prefetchTemplatesPresence()
        } else {
          this.hasTemplatesByDeliverableId = {}
        }
        
      } catch (error) {
        console.error('加载交付物列表失败:', error)
        this.$message?.error('加载交付物列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
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
     * 预拉取交付物的模板存在性
     * 用途：在交付物列表的“编辑”按钮右上角显示“T”标识
     * 实现：对当前列表中的每个交付物调用后端模板列表接口并缓存结果，限制并发以避免压测后端
     */
    async prefetchTemplatesPresence() {
      try {
        const ids = (this.deliverables || []).map(d => d && d.deliverableId).filter(Boolean)
        // 清空旧缓存
        this.hasTemplatesByDeliverableId = {}
        if (ids.length === 0) return
        const maxConcurrency = 4
        let idx = 0
        const worker = async () => {
          while (idx < ids.length) {
            const id = ids[idx++]
            try {
              const list = await listDeliverableTemplates(id)
              const files = Array.isArray(list) ? list : (list && list.files) || []
              if (this.$set) {
                this.$set(this.hasTemplatesByDeliverableId, id, Array.isArray(files) ? files.length > 0 : false)
              } else {
                this.hasTemplatesByDeliverableId[id] = Array.isArray(files) ? files.length > 0 : false
              }
            } catch (_) {
              if (this.$set) {
                this.$set(this.hasTemplatesByDeliverableId, id, false)
              } else {
                this.hasTemplatesByDeliverableId[id] = false
              }
            }
          }
        }
        await Promise.all(Array.from({ length: Math.min(maxConcurrency, ids.length) }, () => worker()))
      } catch (error) {
        console.error('预拉取模板存在性失败:', error)
      }
    },

    /**
     * 判断交付物是否存在模板（供模板中显示“T”标识）
     * 返回：true 表示存在已上传的模板文件
     */
    hasTemplatesForDeliverable(deliverable) {
      const did = deliverable && deliverable.deliverableId
      return did ? this.hasTemplatesByDeliverableId[did] === true : false
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
      // 清空模板文件选择与列表
      this.existingTemplates = []
      this.templateFiles = []

      this.showForm = true
      this.$nextTick(() => this.centerFormModal())
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

      // 加载并回显已上传模板文件
      this.existingTemplates = []
      this.templateFiles = []
      await this.loadDeliverableTemplates(deliverable.deliverableId)
      
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
      this.$nextTick(() => this.centerFormModal())
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
        // 使用主查询API，同时按系统名称和里程碑ID过滤，按步骤名称正序排序
        const response = await getStandardConstructSteps({
          systemName: this.formData.systemName,
          smilestoneId: milestoneId,
          page: 0,
          size: 1000, // 获取所有匹配的步骤
          sortBy: 'sstepName', // 按步骤名称排序
          sortDir: 'asc' // 正序排序
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
      // 重置模板相关状态
      this.templateFiles = []
      this.existingTemplates = []
      this.templateUploading = false
      this.endFormDrag()
    },

    // 表单弹窗居中
    centerFormModal() {
      const modal = this.$refs.formDragModal
      if (!modal) return
      const w = modal.offsetWidth
      const h = modal.offsetHeight
      const left = Math.max((window.innerWidth - w) / 2, 8)
      const top = Math.max((window.innerHeight - h) / 2, 20)
      this.formDragStyle = { position: 'fixed', top: `${top}px`, left: `${left}px` }
    },

    // 开始拖动表单弹窗
    startFormDrag(e) {
      if (e.button !== 0) return
      const modal = this.$refs.formDragModal
      if (!modal) return
      const top = parseInt((this.formDragStyle.top || '0').toString().replace('px', '')) || 0
      const left = parseInt((this.formDragStyle.left || '0').toString().replace('px', '')) || 0
      this.formDragStart = { mouseX: e.clientX, mouseY: e.clientY, top, left }
      this.formDragging = true
      document.addEventListener('mousemove', this.onFormDragMove)
      document.addEventListener('mouseup', this.endFormDrag)
    },

    // 拖动中
    onFormDragMove(e) {
      if (!this.formDragging) return
      const modal = this.$refs.formDragModal
      if (!modal) return
      const dx = e.clientX - this.formDragStart.mouseX
      const dy = e.clientY - this.formDragStart.mouseY
      const w = modal.offsetWidth
      const h = modal.offsetHeight
      let left = this.formDragStart.left + dx
      let top = this.formDragStart.top + dy
      const maxLeft = Math.max(window.innerWidth - w, 0)
      const maxTop = Math.max(window.innerHeight - h, 0)
      left = Math.min(Math.max(0, left), maxLeft)
      top = Math.min(Math.max(0, top), maxTop)
      this.formDragStyle = { position: 'fixed', top: `${top}px`, left: `${left}px` }
    },

    // 结束拖动
    endFormDrag() {
      if (!this.formDragging) return
      this.formDragging = false
      document.removeEventListener('mousemove', this.onFormDragMove)
      document.removeEventListener('mouseup', this.endFormDrag)
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
     * 格式化日期
     */
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },

    /**
     * 导出表格
     */
    exportTable() {
      if (!this.selectedProduct) {
        this.$message?.warning('请先选择产品')
        return
      }
      
      if (this.deliverables.length === 0) {
        this.$message?.warning('当前没有可导出的数据')
        return
      }

      try {
        const exportData = this.deliverables.map(item => ({
          '交付物名称': item.deliverableName || '',
          '交付物类型': item.deliverableType || '',
          '里程碑名称': item.milestoneName || '',
          '步骤名称': item.sstepName || '',
          '是否必须': item.isMustLoad ? '是' : '否'
        }))

        const csvContent = this.convertToCSV(exportData)
        
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `标准交付物_${this.selectedProduct}_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        this.$message?.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message?.error('导出失败: ' + error.message)
      }
    },

    /**
     * 转换数据为CSV格式
     */
    convertToCSV(data) {
      if (!data || data.length === 0) return ''
      
      const headers = Object.keys(data[0])
      const csvRows = []
      
      // 添加表头
      csvRows.push(headers.join(','))
      
      // 添加数据行
      for (const row of data) {
        const values = headers.map(header => {
          const value = row[header] || ''
          // 处理包含逗号、引号或换行符的值
          if (value.toString().includes(',') || value.toString().includes('"') || value.toString().includes('\n')) {
            return `"${value.toString().replace(/"/g, '""')}"`
          }
          return value
        })
        csvRows.push(values.join(','))
      }
      
      return csvRows.join('\n')
    },

    /**
     * 触发文件导入
     */
    triggerImport() {
      if (!this.selectedProduct) {
        this.$message?.warning('请先选择产品')
        return
      }
      this.$refs.fileInput.click()
    },

    /**
     * 处理文件导入
     */
    async handleFileImport(event) {
      const file = event.target.files[0]
      if (!file) return

      try {
        const text = await this.readFileAsText(file)
        const importData = this.parseCSV(text)
        
        if (importData.length === 0) {
          this.$message?.warning('文件中没有有效数据') || alert('文件中没有有效数据')
          return
        }

        // 校验必须的表头（直接从首行解析并标准化，避免误判）
        const firstLine = (text.split(/\r?\n/).find(line => line.trim().length > 0) || '')
        const stripped = firstLine.replace(/"[^"]*"/g, '')
        const candidates = [',', ';', '\t', '，', '；', '|']
        let delimiter = ','
        let bestCount = -1
        for (const d of candidates) {
          const count = stripped.split(d).length - 1
          if (count > bestCount) {
            bestCount = count
            delimiter = d
          }
        }
        if (bestCount <= 0) {
          delimiter = firstLine.includes(',') ? ',' : firstLine.includes(';') ? ';' : firstLine.includes('\t') ? '\t' : firstLine.includes('，') ? '，' : firstLine.includes('；') ? '；' : firstLine.includes('|') ? '|' : ','
        }
        let rawHeaders = this.parseCSVLine(firstLine, delimiter)
        if (rawHeaders.length === 1) {
          for (const d of candidates) {
            const tokens = this.parseCSVLine(firstLine, d)
            if (tokens.length > 1) {
              delimiter = d
              rawHeaders = tokens
              break
            }
          }
        }
        const headers = rawHeaders.map(h => this.normalizeDeliverableHeader(h))
        console.log('解析到的表头:', headers)
        const requiredHeaders = ['交付物名称', '交付物类型', '里程碑名称', '步骤名称', '是否必须']
        const missing = requiredHeaders.filter(h => !headers.includes(h))
        if (missing.length > 0) {
          this.$message?.error(`文件缺少必须的列: ${missing.join(', ')}`) || alert(`文件缺少必须的列: ${missing.join(', ')}`)
          return
        }

        // 验证与归一化导入数据
        const validData = this.validateImportData(importData)
        if (validData.length === 0) {
          this.$message?.error('文件格式不正确或数据无效') || alert('文件格式不正确或数据无效')
          return
        }

        // 确认导入
        if (confirm(`确定要导入 ${validData.length} 条数据吗？`)) {
          await this.importDeliverables(validData)
        }
        
      } catch (error) {
        console.error('导入失败:', error)
        this.$message?.error('导入失败: ' + error.message) || alert('导入失败: ' + error.message)
      } finally {
        // 清空文件输入
        event.target.value = ''
      }
    },

    /**
     * 读取文件内容
     */
    async readFileAsText(file) {
      try {
        const buffer = await this.readFileAsArrayBuffer(file)
        // 尝试UTF-8
        let utf8 = ''
        try {
          utf8 = new TextDecoder('utf-8').decode(buffer)
        } catch (_) {}
        if (this.isLikelyChineseCSV(utf8)) {
          console.log('检测到CSV编码: UTF-8')
          return utf8
        }
        // 尝试GB18030（GBK超集）
        try {
          const gb18030 = new TextDecoder('gb18030').decode(buffer)
          if (this.isLikelyChineseCSV(gb18030)) {
            console.log('检测到CSV编码: GB18030')
            return gb18030
          }
        } catch (_) {}
        // 尝试GBK
        try {
          const gbk = new TextDecoder('gbk').decode(buffer)
          if (this.isLikelyChineseCSV(gbk)) {
            console.log('检测到CSV编码: GBK')
            return gbk
          }
        } catch (_) {}
        console.warn('无法可靠判断编码，默认使用UTF-8')
        return utf8 || await this.readAsTextLegacy(file, 'utf-8')
      } catch (e) {
        console.warn('TextDecoder不可用，回退到FileReader', e)
        const tryUtf8 = await this.readAsTextLegacy(file, 'utf-8')
        if (this.isLikelyChineseCSV(tryUtf8)) return tryUtf8
        const tryGbk = await this.readAsTextLegacy(file, 'gbk')
        if (this.isLikelyChineseCSV(tryGbk)) return tryGbk
        const tryGb18030 = await this.readAsTextLegacy(file, 'gb18030').catch(() => '')
        if (tryGb18030 && this.isLikelyChineseCSV(tryGb18030)) return tryGb18030
        return tryUtf8
      }
    },

    /** 读取为ArrayBuffer */
    readFileAsArrayBuffer(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsArrayBuffer(file)
      })
    },

    /** 使用FileReader按指定编码读取（兼容旧环境） */
    readAsTextLegacy(file, encoding) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsText(file, encoding)
      })
    },

    /** 粗略判断文本是否为有效的中文CSV（用于编码选择） */
    isLikelyChineseCSV(text) {
      if (!text || typeof text !== 'string') return false
      const firstLine = (text.split(/\r?\n/).find(line => line.trim().length > 0) || '')
      const stripped = firstLine.replace(/"[^"]*"/g, '')
      const delimiter = firstLine.includes(',')
        ? ','
        : firstLine.includes(';')
          ? ';'
          : firstLine.includes('\t')
            ? '\t'
            : firstLine.includes('，')
              ? '，'
              : firstLine.includes('；')
                ? '；'
                : firstLine.includes('|')
                  ? '|'
                  : ','
      let tokens = this.parseCSVLine(firstLine, delimiter).map(h => this.normalizeDeliverableHeader(h))
      if (tokens.length === 1) {
        const candidates = [',', ';', '\t', '，', '；', '|']
        for (const d of candidates) {
          const t = this.parseCSVLine(firstLine, d).map(h => this.normalizeDeliverableHeader(h))
          if (t.length > 1) {
            tokens = t
            break
          }
        }
      }
      const replacementCount = (text.match(/\uFFFD/g) || []).length
      const hasChinese = /[\u4e00-\u9fa5]/.test(text)
      const headerOk = tokens.includes('交付物名称') || tokens.includes('交付物类型') || tokens.includes('里程碑名称') || tokens.includes('步骤名称') || tokens.includes('是否必须')
      return (headerOk && replacementCount === 0) || (hasChinese && replacementCount < 5)
    },

    /**
     * 解析CSV数据（自动检测分隔符并归一化表头）
     */
    parseCSV(text) {
      const lines = text.split(/\r?\n/).filter(line => line.trim())
      if (lines.length < 2) return []

      const headerLine = lines[0]
      // 去除引号内内容用于分隔符计数，避免误判
      const stripped = headerLine.replace(/"[^"]*"/g, '')
      const candidates = [',', ';', '\t', '，', '；', '|']

      // 选择出现次数最多的分隔符
      let delimiter = ','
      let bestCount = -1
      for (const d of candidates) {
        const count = stripped.split(d).length - 1
        if (count > bestCount) {
          bestCount = count
          delimiter = d
        }
      }
      if (bestCount <= 0) {
        // 兜底：按包含关系检测
        delimiter = headerLine.includes(',')
          ? ','
          : headerLine.includes(';')
            ? ';'
            : headerLine.includes('\t')
              ? '\t'
              : headerLine.includes('，')
                ? '，'
                : headerLine.includes('；')
                  ? '；'
                  : headerLine.includes('|')
                    ? '|'
                    : ','
      }

      const normalize = (h) => {
        const clean = (h || '')
          .replace(/^\ufeff/, '')
          .replace(/["“”]/g, '')
          .replace(/\u00A0/g, ' ')
          .trim()
          .replace(/\s+/g, '')
        switch (clean) {
          case '交付物名称':
          case '交付物名':
          case '名称':
            return '交付物名称'
          case '交付物类型':
          case '类型':
            return '交付物类型'
          case '里程碑名称':
          case '所属里程碑':
          case '里程碑':
          case '标准里程碑':
            return '里程碑名称'
          case '步骤名称':
          case '步骤名':
            return '步骤名称'
          case '是否必须':
          case '是否必须加载':
          case '是否必须装载':
          case '必须':
          case 'IsMust':
          case 'isMustLoad':
            return '是否必须'
          default:
            return (h || '').trim().replace(/^\ufeff/, '').replace(/["“”]/g, '')
        }
      }

      // 解析表头，若仅解析出一个字段，尝试其他分隔符回退
      let rawHeaders = this.parseCSVLine(headerLine, delimiter)
      if (rawHeaders.length === 1) {
        for (const d of candidates) {
          const tokens = this.parseCSVLine(headerLine, d)
          if (tokens.length > 1) {
            delimiter = d
            rawHeaders = tokens
            break
          }
        }
      }
      const headers = rawHeaders.map(h => normalize(h))

      const data = []
      for (let i = 1; i < lines.length; i++) {
        const values = this.parseCSVLine(lines[i], delimiter)
        const row = {}
        headers.forEach((header, index) => {
          const v = values[index]
          row[header] = v !== undefined ? v.trim().replace(/^\ufeff/, '') : ''
        })
        data.push(row)
      }

      return data
    },

    /**
     * 解析CSV行，支持引号与指定分隔符
     */
    parseCSVLine(line, delimiter = ',') {
      const result = []
      let current = ''
      let inQuotes = false
      
      for (let i = 0; i < line.length; i++) {
        const char = line[i]
        
        if (char === '"') {
          if (inQuotes && line[i + 1] === '"') {
            current += '"'
            i++
          } else {
            inQuotes = !inQuotes
          }
        } else if (char === delimiter && !inQuotes) {
          result.push(current.trim())
          current = ''
        } else {
          current += char
        }
      }
      
      result.push(current.trim())
      return result
    },

    /**
     * 标准化交付物导入列名
     */
    normalizeDeliverableHeader(h) {
      const clean = (h || '')
        .replace(/^\ufeff/, '')
        .replace(/["“”]/g, '')
        .replace(/\u00A0/g, ' ')
        .trim()
        .replace(/\s+/g, '')
      switch (clean) {
        case '交付物名称':
        case '交付物名':
        case '名称':
          return '交付物名称'
        case '交付物类型':
        case '类型':
          return '交付物类型'
        case '里程碑名称':
        case '所属里程碑':
        case '里程碑':
        case '标准里程碑':
          return '里程碑名称'
        case '步骤名称':
        case '步骤名':
          return '步骤名称'
        case '是否必须':
        case '是否必须加载':
        case '是否必须装载':
        case '必须':
        case 'IsMust':
        case 'isMustLoad':
          return '是否必须'
        default:
          return (h || '').trim().replace(/^\ufeff/, '').replace(/["“”]/g, '')
      }
    },

    /**
     * 验证并归一化导入数据（返回有效行）
     */
    validateImportData(data) {
      const validData = []
      for (const row of data) {
        const name = (row['交付物名称'] || '').trim()
        const type = (row['交付物类型'] || '').trim()
        const milestoneName = (row['里程碑名称'] || '').trim()
        const stepName = (row['步骤名称'] || '').trim()
        const isMustRaw = (row['是否必须'] || '').trim()
        if (!name || !type) continue
        const typeNormalized = type === '里程碑' ? '里程碑交付物' : type === '步骤' ? '步骤交付物' : type
        if (!['步骤交付物', '里程碑交付物'].includes(typeNormalized)) continue
        const bool = /^(是|Y|y|true|TRUE|1)$/i.test(isMustRaw) ? true : /^(否|N|n|false|FALSE|0)$/i.test(isMustRaw) ? false : null
        if (bool === null) continue
        if (typeNormalized === '里程碑交付物' && !milestoneName) continue
        if (typeNormalized === '步骤交付物' && !stepName) continue
        validData.push({
          deliverableName: name,
          deliverableType: typeNormalized,
          isMustLoad: bool,
          milestoneName,
          stepName
        })
      }
      return validData
    },

    /**
     * 导入交付物数据（按名称解析里程碑/步骤）
     */
    async importDeliverables(rows) {
      let successCount = 0
      let errorCount = 0
      const errors = []

      for (let i = 0; i < rows.length; i++) {
        const row = rows[i]
        try {
          const type = row.deliverableType
          const name = row.deliverableName
          const isMustLoad = row.isMustLoad
          const systemName = this.selectedProduct
          let milestoneId = null
          let sstepId = null

          if (type === '里程碑交付物') {
            const milestone = this.milestones.find(m => m.milestoneName === row.milestoneName)
            if (!milestone) {
              throw new Error(`未找到里程碑: ${row.milestoneName || '（空）'}`)
            }
            milestoneId = milestone.milestoneId
          } else if (type === '步骤交付物') {
            let candidateSteps = []
            if (row.milestoneName) {
              const milestone = this.milestones.find(m => m.milestoneName === row.milestoneName)
              const mid = milestone ? milestone.milestoneId : null
              const resp = await getStandardConstructSteps({
                systemName,
                smilestoneId: mid,
                page: 0,
                size: 1000,
                sortBy: 'sstepName',
                sortDir: 'asc'
              })
              candidateSteps = resp.steps || []
            } else {
              const resp = await getStandardConstructStepsBySystemName(systemName)
              candidateSteps = resp.steps || []
            }
            const step = candidateSteps.find(s => s.sstepName === row.stepName)
            if (!step) {
              throw new Error(`未找到步骤: ${row.stepName || '（空）'}`)
            }
            sstepId = step.sstepId
          }

          const deliverableData = {
            deliverableName: name,
            systemName,
            deliverableType: type,
            isMustLoad,
            sstepId,
            milestoneId
          }

          await createStandardDeliverable(deliverableData)
          successCount++
        } catch (error) {
          errorCount++
          errors.push(`第${i + 1}行导入失败: ${error.message}`)
          console.error(`导入第${i + 1}行失败:`, error)
        }
      }

      await this.loadDeliverables()

      if (successCount > 0) {
        this.$message?.success(`导入完成: 成功${successCount}条${errorCount > 0 ? `, 失败${errorCount}条` : ''}`)
      }
      
      if (errors.length > 0 && errors.length <= 5) {
        this.$message?.warning(errors.join('\n'))
      } else if (errors.length > 5) {
        this.$message?.warning(`导入过程中发生${errors.length}个错误，请检查数据格式`)
      }
    },

    // 悬浮提示事件与定位
    onTableMouseOver(e) {
      const cell = e.target.closest('td')
      if (!cell) return
      if (cell.querySelector('button')) return
      if (!this.isOverflowed(cell)) return
      this.tooltipText = cell.textContent.trim()
      this.tooltipVisible = true
      this.tooltipCell = cell
      this.positionTooltip(cell, e)
    },
    onTableMouseMove(e) {
      if (!this.tooltipVisible || !this.tooltipCell) return
      this.positionTooltip(this.tooltipCell, e)
    },
    onTableMouseOut(e) {
      const toEl = e.relatedTarget
      if (toEl && this.tooltipCell && this.tooltipCell.contains(toEl)) return
      this.tooltipVisible = false
      this.tooltipCell = null
    },
    onTableScroll() {
      this.tooltipVisible = false
      this.tooltipCell = null
    },
    isOverflowed(el) {
      if (!el) return false
      const style = getComputedStyle(el)
      if (style.whiteSpace !== 'nowrap') return false
      return el.scrollWidth > el.clientWidth || el.scrollHeight > el.clientHeight
    },
    positionTooltip(cell, e) {
      const rect = cell.getBoundingClientRect()
      this.tooltipStyle = { top: '0px', left: '0px' }
      this.$nextTick(() => {
        const tip = this.$refs.cellTooltip
        const tipRect = tip ? tip.getBoundingClientRect() : { width: 300, height: 80 }
        const margin = 8
        const showAbove = rect.bottom + tipRect.height + margin > window.innerHeight
        const top = showAbove ? rect.top - tipRect.height - margin : rect.bottom + margin
        let left = e.clientX + 12
        const maxLeft = window.innerWidth - tipRect.width - margin
        if (left > maxLeft) left = maxLeft
        if (left < margin) left = margin
        this.tooltipStyle = { top: `${top}px`, left: `${left}px` }
      })
    },

    // 模板文件：加载/选择/上传/下载/删除
    async loadDeliverableTemplates(deliverableId) {
      try {
        const list = await listDeliverableTemplates(deliverableId)
        this.existingTemplates = Array.isArray(list) ? list : (list.files || [])
      } catch (error) {
        console.error('加载模板文件失败:', error)
        this.$message?.error('加载模板文件失败: ' + (error.message || error))
        this.existingTemplates = []
      }
    },
    onTemplateFileSelect(e) {
      const files = Array.from((e.target && e.target.files) || [])
      const accepted = this.processSelectedFiles(files)
      if (accepted.length > 0) {
        this.autoUploadTemplates(accepted)
      } else {
        if (this.$refs.templateUploadInput) this.$refs.templateUploadInput.value = ''
      }
    },
    processSelectedFiles(files) {
      const accepted = Array.from(files || [])
      this.templateFiles = accepted
      return accepted
    },
    openFileSelector() {
      if (this.$refs.templateUploadInput) this.$refs.templateUploadInput.click()
    },
    handleDragOver() {
      this.dropActive = true
    },
    handleDragLeave() {
      this.dropActive = false
    },
    handleDrop(e) {
      this.dropActive = false
      const files = Array.from((e.dataTransfer && e.dataTransfer.files) || [])
      const accepted = this.processSelectedFiles(files)
      if (accepted.length > 0) {
        this.autoUploadTemplates(accepted)
      }
    },
    async autoUploadTemplates(files) {
      if (!this.editingDeliverable || !this.editingDeliverable.deliverableId) {
        this.$message?.warning('请先选择交付物')
        return
      }
      if (!files || files.length === 0) {
        return
      }
      this.templateUploading = true
      try {
        await uploadDeliverableTemplates(this.editingDeliverable.deliverableId, files)
        this.$message?.success('模板上传成功')
        this.templateFiles = []
        if (this.$refs.templateUploadInput) this.$refs.templateUploadInput.value = ''
        await this.loadDeliverableTemplates(this.editingDeliverable.deliverableId)
        // 更新列表缓存以显示“T”标识
        const id = this.editingDeliverable.deliverableId
        if (this.$set) {
          this.$set(this.hasTemplatesByDeliverableId, id, true)
        } else {
          this.hasTemplatesByDeliverableId[id] = true
        }
      } catch (error) {
        console.error('模板上传失败:', error)
        this.$message?.error('模板上传失败: ' + (error.message || error))
      } finally {
        this.templateUploading = false
      }
    },
    async downloadTemplate(filename) {
      if (!this.editingDeliverable) return
      try {
        const blob = await downloadDeliverableTemplate(this.editingDeliverable.deliverableId, filename)
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
      } catch (error) {
        console.error('下载模板失败:', error)
        this.$message?.error('下载模板失败: ' + (error.message || error))
      }
    },
    async deleteTemplate(filename) {
      if (!this.editingDeliverable) return
      if (!confirm(`确定要删除模板文件 "${filename}" 吗？`)) return
      try {
        await deleteDeliverableTemplate(this.editingDeliverable.deliverableId, filename)
        this.$message?.success('模板删除成功')
        await this.loadDeliverableTemplates(this.editingDeliverable.deliverableId)
        this.showForm = true
        // 同步更新列表缓存中的模板存在性
        const id = this.editingDeliverable.deliverableId
        const has = Array.isArray(this.existingTemplates) ? this.existingTemplates.length > 0 : false
        if (this.$set) {
          this.$set(this.hasTemplatesByDeliverableId, id, has)
        } else {
          this.hasTemplatesByDeliverableId[id] = has
        }
      } catch (error) {
        console.error('删除模板失败:', error)
        this.$message?.error('删除模板失败: ' + (error.message || error))
      }
    },
    formatSize(bytes) {
      const b = Number(bytes || 0)
      if (b < 1024) return `${b} B`
      const kb = b / 1024
      if (kb < 1024) return `${kb.toFixed(1)} KB`
      const mb = kb / 1024
      if (mb < 1024) return `${mb.toFixed(1)} MB`
      const gb = mb / 1024
      return `${gb.toFixed(1)} GB`
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
  table-layout: fixed;
}

.deliverable-table th,
.deliverable-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
}

.deliverable-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  position: sticky;
  top: 0;
  z-index: 5;
}

/* 固定列宽与平均分配：1=40px, 2=60px, 7=是否必须(100px), 8=操作(140px) */
.deliverable-table th:nth-child(1),
.deliverable-table td:nth-child(1) { width: 40px; }

.deliverable-table th:nth-child(2),
.deliverable-table td:nth-child(2) { width: 60px; }

.deliverable-table th:nth-child(7),
.deliverable-table td:nth-child(7) { width: 100px; }

.deliverable-table th:nth-child(8),
.deliverable-table td:nth-child(8) { width: 140px; }

.deliverable-table th:nth-child(3),
.deliverable-table th:nth-child(4),
.deliverable-table th:nth-child(5),
.deliverable-table th:nth-child(6),
.deliverable-table td:nth-child(3),
.deliverable-table td:nth-child(4),
.deliverable-table td:nth-child(5),
.deliverable-table td:nth-child(6) {
  width: calc((100% - (40px + 60px + 100px + 140px)) / 4);
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

.deliverable-table tbody tr.milestone-row {
  background: rgba(255, 0, 0, 0.1);
}

.deliverable-table tbody tr.milestone-row:hover {
  background: rgba(255, 0, 0, 0.1);
}

.deliverable-table tbody tr.milestone-row.selected {
  background: rgba(255, 0, 0, 0.1);
}

/* 悬浮提示样式 */
.cell-tooltip {
  position: fixed;
  z-index: 2000;
  background: rgba(0,0,0,0.88);
  color: #fff;
  padding: 10px 12px;
  border-radius: 6px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.3);
  max-width: 600px;
  font-size: 14px;
  line-height: 1.5;
  pointer-events: none;
  white-space: normal;
  word-break: break-word;
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

/* 编辑按钮右上角“T”标识 */
.edit-btn {
  position: relative;
}
.t-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fa8c16;
  color: #fff;
  font-size: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 1px rgba(0,0,0,0.08);
}

.btn-success {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #73d13d;
  border-color: #73d13d;
  color: white;
}

.btn-warning {
  background: #fa8c16;
  border-color: #fa8c16;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background: #ffa940;
  border-color: #ffa940;
  color: white;
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
  cursor: move;
  user-select: none;
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
.icon-download::before { content: "⬇️"; }
.icon-upload::before { content: "⬆️"; }

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
/* 模板上传区样式 */
.template-upload { margin-top: 4px; }
.dropzone {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 12px;
  text-align: center;
  color: #8c8c8c;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}
.dropzone:hover { border-color: #40a9ff; color: #40a9ff; background: #f5faff; }
.dropzone.active { border-color: #1890ff; color: #1890ff; background: #e6f7ff; }
.dropzone .hint { margin-top: 4px; font-size: 12px; color: #bfbfbf; }
.actions { margin-top: 8px; display: flex; align-items: center; gap: 6px; }
.uploading-tip { color: #fa8c16; font-size: 12px; }
.selected-files { margin-top: 8px; background: #fff; border: 1px solid #f0f0f0; border-radius: 6px; padding: 8px; }
.selected-file-item { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.template-list { margin-top: 8px; }
.template-item { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.file-link { color: #1890ff; cursor: pointer; }
.file-link:hover { text-decoration: underline; }
.file-meta { color: #8c8c8c; font-size: 12px; }
.btn-icon { border: none; background: transparent; cursor: pointer; padding: 0 4px; }
.btn-icon.btn-danger { color: #ff4d4f; }
.btn-icon.btn-danger:hover { color: #ff7875; }
</style>
