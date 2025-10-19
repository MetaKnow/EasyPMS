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
          accept=".csv" 
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
              <select v-model="sortDir" class="search-select sort-dir-select" @change="onSortChange">
                <option value="desc">倒序</option>
                <option value="asc">正序</option>
              </select>
            </div>
          </div>

          <!-- 步骤列表 -->
          <div class="table-section">
            <div class="table-container" @scroll="onTableScroll">
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
                <tbody @mouseover="onTableMouseOver" @mousemove="onTableMouseMove" @mouseout="onTableMouseOut">
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
                    <td>{{ index + 1 }}</td>
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
            <div v-if="tooltipVisible" ref="cellTooltip" class="cell-tooltip" :style="tooltipStyle">{{ tooltipText }}</div>
            
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
      sortBy: 'sstepName',
      sortDir: 'asc',
      
      // 加载状态
      loading: false,
      
      // 单元格悬浮提示
      tooltipVisible: false,
      tooltipText: '',
      tooltipStyle: { top: '0px', left: '0px' },
      tooltipCell: null,
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
          page: 0, // 加载全部数据时固定为第一页
          size: 100000, // 加载全量数据
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
    },

    /**
     * 导出表格（仅导出：序号、步骤名称、步骤类型、所属里程碑）
     */
    exportTable() {
      if (!this.selectedProduct) {
        this.$message?.warning('请先选择产品')
        return
      }

      if (this.steps.length === 0) {
        this.$message?.warning('当前没有数据可导出')
        return
      }

      try {
        // 准备导出数据（仅四列）
        const exportData = this.steps.map((step, index) => ({
          '序号': index + 1,
          '步骤名称': step.sstepName || '',
          '步骤类型': step.type || '',
          '所属里程碑': this.getMilestoneName(step.smilestoneId)
        }))

        // 转换为CSV格式
        const headers = Object.keys(exportData[0])
        const csvContent = [
          headers.join(','),
          ...exportData.map(row => headers.map(header => `"${row[header] || ''}"`).join(','))
        ].join('\n')

        // 创建下载链接
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `标准交付步骤_${this.selectedProduct}_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        this.$message?.success('表格导出成功')
      } catch (error) {
        console.error('导出表格失败:', error)
        this.$message?.error('导出表格失败: ' + error.message)
      }
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
     * 解析CSV内容（更宽容：允许行列数不一致，按可用列映射）
     */
    parseCSV(text) {
      const lines = text.split(/\r?\n/).filter(line => line.trim())
      if (lines.length < 2) return []

      // 自动检测分隔符（逗号/分号/Tab/中文逗号）
      const delimiter = lines[0].includes(',')
        ? ','
        : lines[0].includes(';')
          ? ';'
          : lines[0].includes('\t')
            ? '\t'
            : lines[0].includes('，')
              ? '，'
              : ','

      // 解析并标准化表头
      const rawHeaders = this.parseCSVLine(lines[0], delimiter)
      const headers = rawHeaders.map(h => this.normalizeHeader(h))

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

    /** 标准化列名，去除BOM/引号/特殊空格，并映射常见别名 */
    normalizeHeader(h) {
      const clean = (h || '')
        .replace(/^\ufeff/, '')
        .replace(/["“”]/g, '')
        .replace(/\u00A0/g, ' ')
        .trim()
        .replace(/\s+/g, '')

      switch (clean) {
        case '步骤名称':
        case '步骤名':
        case '名称':
          return '步骤名称'
        case '步骤类型':
        case '类型':
          return '步骤类型'
        case '所属里程碑':
        case '里程碑':
        case '标准里程碑':
          return '所属里程碑'
        default:
          return (h || '').trim().replace(/^\ufeff/, '').replace(/["“”]/g, '')
      }
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

        // 校验必须的表头
        const headers = Object.keys(importData[0] || {})
        console.log('解析到的表头:', headers)
        const requiredHeaders = ['步骤名称', '步骤类型']
        const missing = requiredHeaders.filter(h => !headers.includes(h))
        if (missing.length > 0) {
          this.$message?.error(`文件缺少必须的列: ${missing.join(', ')}`) || alert(`文件缺少必须的列: ${missing.join(', ')}`)
          return
        }

        // 验证导入数据格式
        const validData = this.validateImportData(importData)
        if (validData.length === 0) {
          this.$message?.error('文件格式不正确或数据无效') || alert('文件格式不正确或数据无效')
          return
        }

        // 确认导入
        if (confirm(`确定要导入 ${validData.length} 条数据吗？`)) {
          await this.importSteps(validData)
        }

      } catch (error) {
        console.error('导入文件失败:', error)
        this.$message?.error('导入文件失败: ' + error.message) || alert('导入文件失败: ' + error.message)
      } finally {
        // 清空文件输入
        event.target.value = ''
      }
    },

    /**
     * 读取文件内容（支持中文编码：UTF-8 / GBK / GB18030 自动回退）
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
      const delimiter = firstLine.includes(',') ? ',' : firstLine.includes(';') ? ';' : firstLine.includes('\t') ? '\t' : firstLine.includes('，') ? '，' : ','
      const tokens = this.parseCSVLine(firstLine, delimiter).map(h => this.normalizeHeader(h))
      const replacementCount = (text.match(/\uFFFD/g) || []).length
      const hasChinese = /[\u4e00-\u9fa5]/.test(text)
      const headerOk = tokens.includes('步骤名称') || tokens.includes('步骤类型') || tokens.includes('所属里程碑')
      return (headerOk && replacementCount === 0) || (hasChinese && replacementCount < 5)
    },

    /**
     * 验证导入数据
     */
    validateImportData(data) {
      const validData = []
      
      for (const row of data) {
        // 检查必填字段
        if (row['步骤名称'] && row['步骤类型']) {
          // 查找对应的里程碑ID
          let milestoneId = null
          if (row['所属里程碑']) {
            const milestone = this.milestones.find(m => m.milestoneName === row['所属里程碑'])
            if (milestone) {
              milestoneId = milestone.milestoneId
            }
          }

          validData.push({
            sstepName: row['步骤名称'],
            type: row['步骤类型'],
            systemName: this.selectedProduct,
            smilestoneId: milestoneId
          })
        }
      }

      return validData
    },

    /**
     * 导入步骤数据
     */
    async importSteps(data) {
      let successCount = 0
      let errorCount = 0

      for (const stepData of data) {
        try {
          await createStandardConstructStep(stepData)
          successCount++
        } catch (error) {
          console.error('导入步骤失败:', error)
          errorCount++
        }
      }

      if (successCount > 0) {
        this.$message?.success(`成功导入 ${successCount} 条数据${errorCount > 0 ? `，失败 ${errorCount} 条` : ''}`)
        this.loadSteps() // 重新加载列表
      } else {
        this.$message?.error('导入失败，请检查数据格式')
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
  table-layout: fixed;
}

.step-table th,
.step-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
}

.step-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  position: sticky;
  top: 0;
  z-index: 5;
}

/* 固定列宽与平均分配 */
.step-table th:nth-child(1),
.step-table td:nth-child(1) { width: 40px; }

.step-table th:nth-child(2),
.step-table td:nth-child(2) { width: 60px; }

.step-table th:nth-child(8),
.step-table td:nth-child(8) { width: 140px; }

.step-table th:nth-child(3),
.step-table th:nth-child(4),
.step-table th:nth-child(5),
.step-table th:nth-child(6),
.step-table th:nth-child(7),
.step-table td:nth-child(3),
.step-table td:nth-child(4),
.step-table td:nth-child(5),
.step-table td:nth-child(6),
.step-table td:nth-child(7) {
  width: calc((100% - (40px + 60px + 140px)) / 5);
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

.btn-info {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-info:hover:not(:disabled) {
  background: #40a9ff;
  border-color: #40a9ff;
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
  /* 排序选择框：遵循通用select样式，避免与其他模块不一致 */
  .sort-dir-select {
    width: auto;
    min-width: auto;
    padding: 4px 8px;
    flex: 0 0 auto;
    white-space: nowrap;
  }
</style>

// ... 悬浮提示：表格单元格 =====
onTableMouseOver(e) {
  const td = e.target.closest('td')
  if (!td) return
  const idx = td.cellIndex
  if (idx <= 1 || idx === 7) {
    this.tooltipVisible = false
    return
  }
  if (!this.isOverflowed(td)) {
    this.tooltipVisible = false
    return
  }
  this.tooltipText = (td.innerText || '').trim()
  this.tooltipVisible = true
  this.positionTooltip(td)
},
onTableMouseMove(e) {
  if (!this.tooltipVisible) return
  const td = e.target.closest('td')
  if (!td) return
  this.positionTooltip(td)
},
onTableMouseOut(e) {
  const related = e.relatedTarget
  const leavingTd = e.target.closest('td')
  if (related && (related.closest?.('td') === leavingTd || related.classList?.contains('cell-tooltip'))) {
    return
  }
  this.tooltipVisible = false
},
onTableScroll() {
  this.tooltipVisible = false
},
isOverflowed(el) {
  return el && el.scrollWidth > el.clientWidth
},
positionTooltip(td) {
  const rect = td.getBoundingClientRect()
  let top = rect.bottom + 8
  let left = rect.left + 8
  this.$nextTick(() => {
    const tip = this.$el.querySelector('.cell-tooltip')
    const tipH = tip?.offsetHeight || 0
    const tipW = tip?.offsetWidth || 0
    if (window.innerHeight - rect.bottom < tipH + 12) {
      top = rect.top - tipH - 8
    }
    left = Math.min(left, window.innerWidth - tipW - 8)
    left = Math.max(8, left)
    this.tooltipStyle = { top: `${top}px`, left: `${left}px` }
  })
},