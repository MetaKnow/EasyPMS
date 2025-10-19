<template>
  <div class="milestone-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">标准里程碑</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增里程碑
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedMilestones.length === 0">
          <i class="icon-delete"></i>
          删除里程碑
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

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          v-model="searchForm.milestoneName" 
          type="text" 
          placeholder="里程碑名称"
          class="search-input"
        />
        <button class="btn btn-primary" @click="searchMilestones">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 里程碑列表 -->
    <div class="table-section">
      <div class="table-container" @scroll="onTableScroll">
        <table class="milestone-table">
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
              <th>里程碑名称</th>
              <th>创建时间</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody @mouseover="onTableMouseOver" @mousemove="onTableMouseMove" @mouseout="onTableMouseOut">
            <tr 
              v-for="(milestone, index) in milestones" 
              :key="milestone.milestoneId"
              :class="{ selected: isSelected(milestone) }"
              @click="toggleSelect(milestone)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="isSelected(milestone)"
                  @change.stop="toggleSelect(milestone)"
                />
              </td>
              <td>{{ index + 1 }}</td>
              <td>{{ milestone.milestoneName }}</td>
              <td>{{ formatDate(milestone.createTime) }}</td>
              <td>{{ formatDate(milestone.updateTime) }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editMilestone(milestone)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteMilestone(milestone)">
                  删除
                </button>
              </td>
            </tr>
            <tr v-if="milestones.length === 0">
              <td colspan="6" class="no-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="tooltipVisible" ref="cellTooltip" class="cell-tooltip" :style="tooltipStyle">{{ tooltipText }}</div>
      
    </div>

    <!-- 里程碑表单弹窗 -->
    <StandardMilestoneForm
      v-if="showForm"
      :visible="showForm"
      :milestone="editingMilestone"
      :mode="formMode"
      @close="closeForm"
      @save="saveMilestone"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除里程碑 "{{ deletingMilestone?.milestoneName }}" 吗？此操作不可恢复。</p>
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
        <p>确定要删除选中的 {{ deletingMilestones?.length || 0 }} 个里程碑吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import StandardMilestoneForm from './StandardMilestoneForm.vue'
import { 
  getStandardMilestoneList, 
  createStandardMilestone, 
  updateStandardMilestone, 
  deleteStandardMilestone, 
  batchDeleteStandardMilestones 
} from '../api/standardMilestone.js'

/**
 * 标准里程碑管理组件
 * 用于管理标准里程碑信息
 */
export default {
  name: 'StandardMilestoneManagement',
  components: {
    StandardMilestoneForm
  },
  data() {
    return {
      // 里程碑列表数据
      milestones: [],
      
      // 选中的里程碑列表
      selectedMilestones: [],
      
      // 编辑中的里程碑
      editingMilestone: null,
      
      // 删除中的里程碑
      deletingMilestone: null,
      
      // 表单显示状态
      showForm: false,
      
      // 删除确认弹窗显示状态
      showDeleteConfirm: false,
      
      // 批量删除中的里程碑
      deletingMilestones: null,
      
      // 批量删除确认弹窗显示状态
      showBatchDeleteConfirm: false,
      
      // 表单模式：add 或 edit
      formMode: 'add',
      
      // 搜索表单
      searchForm: {
        milestoneName: ''
      },
      
      // 分页参数
      currentPage: 1,
      pageSize: 20,
      totalCount: 0,
      totalPages: 0,
      
      // 加载状态
      loading: false,

      // 悬浮提示状态
      tooltipVisible: false,
      tooltipText: '',
      tooltipStyle: { top: '0px', left: '0px' },
      tooltipCell: null
    }
  },
  computed: {
    /**
     * 判断是否全选
     */
    isAllSelected() {
      return this.milestones.length > 0 && this.selectedMilestones.length === this.milestones.length
    }
  },
  created() {
    // 组件创建时加载里程碑列表
    this.loadMilestones()
  },
  methods: {
    /**
     * 加载里程碑列表
     */
    async loadMilestones() {
      this.loading = true
      try {
        const params = {
          page: 0, // 加载全部数据时固定为第一页
          size: 100000, // 加载全量数据
          sortBy: 'milestoneName',
          sortDir: 'asc'
        }
        
        // 添加搜索条件
        if (this.searchForm.milestoneName) {
          params.milestoneName = this.searchForm.milestoneName
        }
        
        const data = await getStandardMilestoneList(params)
        this.milestones = data.milestones || []
        this.currentPage = (data.currentPage || 0) + 1 // 转换为前端分页（从1开始）
        this.totalCount = data.totalItems || 0
        this.totalPages = data.totalPages || 0
        
        // 清除选中状态
        this.selectedMilestones = []
        
      } catch (error) {
        console.error('加载里程碑列表失败:', error)
        this.$message?.error('加载里程碑列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 搜索里程碑
     */
    searchMilestones() {
      this.currentPage = 1
      this.loadMilestones()
    },
    
    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        milestoneName: ''
      }
      this.currentPage = 1
      this.loadMilestones()
    },
    
    /**
     * 判断里程碑是否被选中
     */
    isSelected(milestone) {
      return this.selectedMilestones.some(m => m.milestoneId === milestone.milestoneId)
    },
    
    /**
     * 切换里程碑选中状态
     */
    toggleSelect(milestone) {
      const index = this.selectedMilestones.findIndex(m => m.milestoneId === milestone.milestoneId)
      if (index === -1) {
        this.selectedMilestones.push(milestone)
      } else {
        this.selectedMilestones.splice(index, 1)
      }
    },
    
    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        // 全选
        this.selectedMilestones = [...this.milestones]
      } else {
        // 取消全选
        this.selectedMilestones = []
      }
    },
    
    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.editingMilestone = null
      this.showForm = true
    },
    
    /**
     * 编辑里程碑
     */
    editMilestone(milestone) {
      this.formMode = 'edit'
      this.editingMilestone = { ...milestone }
      this.showForm = true
    },
    
    /**
     * 删除选中的里程碑
     */
    deleteSelected() {
      if (this.selectedMilestones.length > 0) {
        if (this.selectedMilestones.length === 1) {
          // 单个删除
          this.deleteMilestone(this.selectedMilestones[0])
        } else {
          // 批量删除
          this.batchDeleteMilestones()
        }
      }
    },
    
    /**
     * 删除里程碑
     */
    deleteMilestone(milestone) {
      this.deletingMilestone = milestone
      this.showDeleteConfirm = true
    },
    
    /**
     * 批量删除里程碑
     */
    batchDeleteMilestones() {
      this.deletingMilestones = [...this.selectedMilestones]
      this.showBatchDeleteConfirm = true
    },
    
    /**
     * 确认删除里程碑
     */
    async confirmDelete() {
      if (!this.deletingMilestone) return
      
      try {
        await deleteStandardMilestone(this.deletingMilestone.milestoneId)
        this.$message?.success('里程碑删除成功')
        
        // 重新加载列表
        this.loadMilestones()
        this.closeDeleteConfirm()
        
      } catch (error) {
        console.error('删除里程碑失败:', error)
        this.$message?.error('删除里程碑失败: ' + error.message)
      }
    },
    
    /**
     * 确认批量删除里程碑
     */
    async confirmBatchDelete() {
      if (!this.deletingMilestones || this.deletingMilestones.length === 0) return
      
      try {
        // 调用批量删除API
        const milestoneIds = this.deletingMilestones.map(m => m.milestoneId)
        await batchDeleteStandardMilestones(milestoneIds)
        
        this.$message?.success(`成功删除 ${this.deletingMilestones.length} 个里程碑`)
        
        // 重新加载列表
        this.loadMilestones()
        this.closeBatchDeleteConfirm()
        
      } catch (error) {
        console.error('批量删除里程碑失败:', error)
        this.$message?.error('批量删除里程碑失败: ' + error.message)
      }
    },
    
    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deletingMilestone = null
    },
    
    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
      this.deletingMilestones = null
    },
    
    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.editingMilestone = null
    },
    
    /**
     * 保存里程碑
     */
    async saveMilestone(milestoneData) {
      try {
        if (this.formMode === 'add') {
          // 新增里程碑
          await createStandardMilestone(milestoneData)
          this.$message?.success('里程碑新增成功')
          
        } else {
          // 更新里程碑
          await updateStandardMilestone(milestoneData.milestoneId, milestoneData)
          this.$message?.success('里程碑更新成功')
        }
        
        // 重新加载列表
        this.loadMilestones()
        this.closeForm()
        
      } catch (error) {
        console.error('保存里程碑失败:', error)
        this.$message?.error('保存里程碑失败: ' + error.message)
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
    },

    /**
     * 导出表格
     */
    exportTable() {
      if (this.milestones.length === 0) {
        this.$message?.warning('当前没有数据可导出')
        return
      }
      try {
        const exportData = this.milestones.map((m, index) => ({
          '序号': index + 1,
          '里程碑名称': m.milestoneName || '',
          '创建时间': this.formatDate(m.createTime),
          '更新时间': this.formatDate(m.updateTime)
        }))
        const headers = Object.keys(exportData[0])
        const csvContent = [
          headers.join(','),
          ...exportData.map(row => headers.map(header => `"${row[header] || ''}"`).join(','))
        ].join('\n')

        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `标准里程碑_${new Date().toISOString().slice(0, 10)}.csv`)
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
      this.$refs.fileInput?.click()
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
          this.$message?.warning('文件中没有有效数据')
          return
        }

        const validData = this.validateImportData(importData)
        if (validData.length === 0) {
          this.$message?.error('文件格式不正确或数据无效')
          return
        }

        if (confirm(`确定要导入 ${validData.length} 条数据吗？`)) {
          await this.importMilestones(validData)
        }
      } catch (error) {
        console.error('导入文件失败:', error)
        this.$message?.error('导入文件失败: ' + error.message)
      } finally {
        event.target.value = ''
      }
    },

    /**
     * 读取文件内容
     */
    readFileAsText(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsText(file, 'utf-8')
      })
    },

    /**
     * 解析CSV内容
     */
    parseCSV(text) {
      const lines = text.split('\n').filter(line => line.trim())
      if (lines.length < 2) return []

      const headers = lines[0].split(',').map(h => h.replace(/"/g, '').trim())
      const data = []

      for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(',').map(v => v.replace(/"/g, '').trim())
        const row = {}
        headers.forEach((header, index) => {
          row[header] = values[index] || ''
        })
        data.push(row)
      }

      return data
    },

    /**
     * 验证导入数据
     */
    validateImportData(data) {
      const validData = []
      for (const row of data) {
        if (row['里程碑名称']) {
          validData.push({
            milestoneName: row['里程碑名称'].trim()
          })
        }
      }
      return validData
    },

    /**
     * 导入里程碑数据
     */
    async importMilestones(data) {
      let successCount = 0
      let errorCount = 0

      for (const milestoneData of data) {
        try {
          await createStandardMilestone(milestoneData)
          successCount++
        } catch (error) {
          console.error('导入里程碑失败:', error)
          errorCount++
        }
      }

      if (successCount > 0) {
        this.$message?.success(`成功导入 ${successCount} 条数据${errorCount > 0 ? `，失败 ${errorCount} 条` : ''}`)
        this.loadMilestones()
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
.milestone-management {
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

.milestone-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.milestone-table th,
.milestone-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
}

.milestone-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

/* 固定列宽与平均分配：1=40px, 2=60px, 6=操作(140px) */
.milestone-table th:nth-child(1),
.milestone-table td:nth-child(1) { width: 40px; }

.milestone-table th:nth-child(2),
.milestone-table td:nth-child(2) { width: 60px; }

.milestone-table th:nth-child(6),
.milestone-table td:nth-child(6) { width: 140px; }

.milestone-table th:nth-child(3),
.milestone-table th:nth-child(4),
.milestone-table th:nth-child(5),
.milestone-table td:nth-child(3),
.milestone-table td:nth-child(4),
.milestone-table td:nth-child(5) {
  width: calc((100% - (40px + 60px + 140px)) / 3);
}

.milestone-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.milestone-table tbody tr:hover {
  background: #f5f5f5;
}

.milestone-table tbody tr.selected {
  background: #e6f7ff;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .milestone-management {
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
  /* 导入/导出按钮样式与图标，保持与步骤模块一致 */
  .btn-success {
    background-color: #28a745;
    color: #fff;
  }
  .btn-success:hover {
    background-color: #218838;
  }
  .btn-warning {
    background-color: #ffc107;
    color: #212529;
  }
  .btn-warning:hover {
    background-color: #e0a800;
    color: #212529;
  }
  .icon-download::before {
    content: "\2193";
    display: inline-block;
    margin-right: 4px;
  }
  .icon-upload::before {
    content: "\2191";
    display: inline-block;
    margin-right: 4px;
  }
</style>