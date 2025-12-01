<template>
  <div class="construction-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">在建项目管理</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showCreateForm">
          <i class="icon-plus"></i>
          新建项目
        </button>
        <button class="btn btn-danger" @click="batchDelete" :disabled="selectedProjects.length === 0">
          <i class="icon-delete"></i>
          删除项目
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          v-model="searchForm.projectName" 
          type="text" 
          placeholder="项目名称"
          class="search-input"
          @keyup.enter="searchProjects"
        />
        <input 
          v-model="searchForm.projectLeaderName" 
          type="text" 
          placeholder="项目负责人"
          class="search-input"
        />
        <select v-model="searchForm.projectState" class="search-select">
          <option value="">全部状态</option>
          <option value="待开始">待开始</option>
          <option value="进行中">进行中</option>
          <option value="已完成">已完成</option>
          <option value="已暂停">已暂停</option>
        </select>
        <select v-model="searchForm.year" class="search-select">
          <option value="">全部年度</option>
          <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}</option>
        </select>
        <button class="btn btn-primary" @click="searchProjects">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 项目列表表格 -->
    <div class="table-section">
      <div class="table-container" @mouseover="onTableMouseOver" @mousemove="onTableMouseMove" @mouseout="onTableMouseOut" @scroll="onTableScroll">
        <table class="construction-table">
            <colgroup>
               <col style="width: 40px" />
               <col style="width: 60px" />
               <col style="width: 170px" />
               <col style="width: calc((100% - var(--fixed-total)) / 2)" />
               <col style="width: 250px" />
               <col style="width: calc((100% - var(--fixed-total)) / 2)" />
               <col style="width: 100px" />
               <col style="width: 100px" />
               <col style="width: 170px" />
             </colgroup>
          <thead>
              <tr>
                <th width="40">
                  <input 
                    type="checkbox" 
                    @change="selectAll"
                    :checked="isAllSelected"
                  />
                </th>
                <th width="60">序号</th>
                <th width="180">项目编号</th>
                <th>项目名称</th>
                <th>软件系统</th>
                <th>客户</th>
                <th width="100">项目负责人</th>
                <th width="100">项目状态</th>
                <th width="120">操作</th>
              </tr>
            </thead>
          <tbody>
            <tr 
              v-for="(project, index) in projectList" 
              :key="project.projectId"
              :class="{ selected: selectedProjects.includes(project.projectId) }"
              @click="selectProject(project)"
              @dblclick="openProjectDetail(project)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="selectedProjects.includes(project.projectId)"
                  @change="toggleProjectSelection(project.projectId)"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                <td>{{ project.projectNum }}</td>
                <td>{{ project.projectName }}</td>
                <td>{{ project.softName ? (project.softVersion ? project.softName + ' (' + project.softVersion + ')' : project.softName) : '未选择' }}</td>
                <td>{{ project.customerName || '未知客户' }}</td>
                <td>{{ project.projectLeaderName || project.projectLeader }}</td>
                <td>
                  <span class="status-badge" :class="getStatusClass(project.projectState)">
                    {{ project.projectState }}
                  </span>
                </td>
                <td>
                  <button class="btn-small btn-info" @click.stop="viewProject(project)">
                    查看
                  </button>
                  <button class="btn-small btn-primary" @click.stop="editProject(project)">
                    编辑
                  </button>
                  <button class="btn-small btn-danger" @click.stop="deleteProject(project)">
                    删除
                  </button>
                </td>
            </tr>
          </tbody>
        </table>
        <div v-if="tooltipVisible" class="cell-tooltip" :style="tooltipStyle" ref="cellTooltip">{{ tooltipText }}</div>
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
          第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ total }} 条记录
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

  <!-- 新建项目表单 -->
  <ProjectCreateForm 
    :visible="createFormVisible"
    @close="closeCreateForm"
    @success="onCreateSuccess"
  />
</template>

<script>
import { 
  getConstructingProjects, 
  deleteConstructingProject, 
  batchDeleteConstructingProjects 
} from '../api/constructingProject'
import ProjectCreateForm from './ProjectCreateForm.vue'

export default {
  name: 'ConstructionProjectManagement',
  components: {
    ProjectCreateForm
  },
  data() {
    return {
      // 项目列表
      projectList: [],
      // 搜索表单
      searchForm: {
        projectName: '',
        projectLeaderName: '',
        projectState: '',
        year: ''
      },
      // 分页信息
      currentPage: 1,
      pageSize: 20,
      total: 0,
      // 选中的项目
      selectedProjects: [],
      selectedProject: null,
      // 加载状态
      loading: false,
      // 年度选项
      yearOptions: [],
      // 表单显示状态
      createFormVisible: false,
      // 单元格悬浮提示
      tooltipVisible: false,
      tooltipText: '',
      tooltipStyle: { top: '0px', left: '0px' },
      tooltipCell: null
    }
  },
  computed: {
    /**
     * 总页数
     */
    totalPages() {
      return Math.ceil(this.total / this.pageSize)
    },
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.projectList.length > 0 && 
             this.selectedProjects.length === this.projectList.length
    }
  },
  mounted() {
    this.initYearOptions()
    this.loadProjects()
  },
  methods: {
    /**
     * 初始化年度选项
     */
    initYearOptions() {
      const currentYear = new Date().getFullYear()
      for (let i = currentYear - 5; i <= currentYear + 2; i++) {
        this.yearOptions.push(i)
      }
    },

    /**
     * 加载项目列表
     */
    async loadProjects() {
      try {
        this.loading = true
        const params = {
          // 后端分页从0开始，前端从1开始，这里转换索引
          page: this.currentPage - 1,
          size: this.pageSize,
          ...this.searchForm
        }
        
        console.log('正在加载项目列表，参数:', params)
        const response = await getConstructingProjects(params)
        console.log('API响应:', response.data)
        
        if (response.data.success) {
          this.projectList = response.data.data.list || []
          this.total = response.data.data.total || 0
          console.log('项目列表数据:', this.projectList)
          console.log('总数:', this.total)
        } else {
          console.error('API返回失败:', response.data.message)
        }
      } catch (error) {
        console.error('加载项目列表失败:', error)
        alert('加载项目列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索项目
     */
    searchProjects() {
      this.currentPage = 1
      this.loadProjects()
    },

    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        projectName: '',
        projectLeaderName: '',
        projectState: '',
        year: ''
      }
      this.searchProjects()
    },

    /**
     * 选择项目
     */
    selectProject(project) {
      this.selectedProject = project
    },

    /**
     * 切换项目选择状态
     */
    toggleProjectSelection(projectId) {
      const index = this.selectedProjects.indexOf(projectId)
      if (index > -1) {
        this.selectedProjects.splice(index, 1)
      } else {
        this.selectedProjects.push(projectId)
      }
    },

    /**
     * 编辑选中的项目
     */
    editSelected() {
      if (this.selectedProject) {
        this.editProject(this.selectedProject)
      }
    },

    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadProjects()
      }
    },

    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadProjects()
      }
    },

    /**
     * 刷新列表
     */
    refreshList() {
      this.loadProjects()
    },

    /**
     * 显示创建表单
     */
    showCreateForm() {
      this.createFormVisible = true
    },

    /**
     * 关闭创建表单
     */
    closeCreateForm() {
      this.createFormVisible = false
    },

    /**
     * 创建成功回调
     */
    onCreateSuccess() {
      this.createFormVisible = false
      this.loadProjects()
    },

    /**
     * 查看项目
     */
    viewProject(project) {
      this.$emit('show-constructing-project-form', project, true)
    },

    /**
     * 编辑项目
     */
    editProject(project) {
      this.$emit('show-constructing-project-form', project, false)
    },

    /**
     * 打开项目详情页（双击行）
     */
    openProjectDetail(project) {
      if (!project || !project.projectId) return
      this.$router.push({ name: 'ProjectDetail', params: { projectId: project.projectId } })
    },

    /**
     * 删除项目
     */
    async deleteProject(project) {
      if (!confirm(`确定要删除项目"${project.projectName}"吗？`)) {
        return
      }

      try {
        const response = await deleteConstructingProject(project.projectId)
        if (response.data.success) {
          alert('删除成功')
          this.loadProjects()
        } else {
          alert(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除项目失败:', error)
        alert('删除失败')
      }
    },

    /**
     * 批量删除
     */
    async batchDelete() {
      if (this.selectedProjects.length === 0) {
        alert('请选择要删除的项目')
        return
      }

      if (!confirm(`确定要删除选中的 ${this.selectedProjects.length} 个项目吗？`)) {
        return
      }

      try {
        const response = await batchDeleteConstructingProjects(this.selectedProjects)
        if (response.data.success) {
          alert('批量删除成功')
          this.selectedProjects = []
          this.loadProjects()
        } else {
          alert(response.data.message || '批量删除失败')
        }
      } catch (error) {
        console.error('批量删除失败:', error)
        alert('批量删除失败')
      }
    },

    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        this.selectedProjects = this.projectList.map(p => p.projectId)
      } else {
        this.selectedProjects = []
      }
    },

    /**
     * 切换页码
     */
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadProjects()
      }
    },

    /**
     * 获取状态样式类
     */
    getStatusClass(status) {
      const statusMap = {
        '待开始': 'status-pending',
        '进行中': 'status-active',
        '已完成': 'status-completed',
        '已暂停': 'status-paused'
      }
      return statusMap[status] || ''
    },

    /**
     * 格式化日期
     */
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleDateString()
    },

    /**
     * 格式化金额
     */
    formatMoney(amount) {
      if (!amount) return '-'
      return '¥' + Number(amount).toLocaleString()
    },

    // 悬浮提示事件与定位（与其他模块一致）
    onTableMouseOver(e) {
      const cell = e.target.closest('td')
      if (!cell) return
      if (cell.querySelector('button')) return
      if (!this.isOverflowed(cell)) {
        this.tooltipVisible = false
        this.tooltipCell = null
        return
      }
      this.tooltipText = (cell.textContent || '').trim()
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
.construction-management {
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

.construction-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  --fixed-total: 600px;
}

.construction-table th,
.construction-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.construction-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.construction-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.construction-table tbody tr:hover {
  background: #f5f5f5;
}

.construction-table tbody tr.selected {
  background: #e6f7ff;
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

.btn-info {
  background: #17a2b8;
  border-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background: #20c0db;
  border-color: #20c0db;
}

.btn-sm {
  padding: 3px 6px;
  font-size: 11px;
}

/* 与客户管理模块一致的行内小按钮样式 */
.btn-small {
  padding: 3px 6px;
  font-size: 11px;
  margin-right: 3px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 状态徽章 */
.status-pending {
  color: #fa8c16;
  background: #fff7e6;
  border: 1px solid #ffd591;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  color: #52c41a;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-completed {
  color: #1890ff;
  background: #f0f5ff;
  border: 1px solid #91d5ff;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-paused {
  color: #ff4d4f;
  background: #fff2f0;
  border: 1px solid #ffb3b3;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
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
  color: #8c8c8c;
  font-size: 13px;
}

/* 图标 */
.icon-plus::before {
  content: '+';
}

.icon-refresh::before {
  content: '↻';
}

/* 悬浮提示样式：与其他模块一致 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .construction-management {
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
  
  .form-group {
    min-width: auto;
  }
  
  .pagination {
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px;
  }
}
</style>