<template>
  <div class="maintenance-management">
    <div class="page-header">
      <h2 class="page-title">运维项目管理</h2>
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

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <div class="search-form">
        <input 
          type="text" 
          v-model="searchForm.projectName" 
          placeholder="项目名称"
          class="search-input"
          @keyup.enter="searchProjects"
        />
        <select v-model="searchForm.status" class="search-select">
          <option value="">全部运维状态</option>
          <option value="免费运维期">免费运维期</option>
          <option value="付费运维">付费运维</option>
          <option value="无付费运维">无付费运维</option>
          <option value="暂停运维">暂停运维</option>
        </select>
        <input 
          type="text" 
          v-model="searchForm.saleDirector" 
          placeholder="销售负责人"
          class="search-input"
        />
        <button @click="searchProjects" class="btn btn-primary">搜索</button>
        <button @click="resetSearch" class="btn btn-secondary">重置</button>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="table-section">
      <div class="table-container" @mouseover="onTableMouseOver" @mousemove="onTableMouseMove" @mouseout="onTableMouseOut" @scroll="onTableScroll">
        <table class="maintenance-table">
          <colgroup>
            <col style="width: 50px" />
            <col style="width: 60px" />
            <col style="width: 170px" />
            <col style="width: calc((100% - var(--fixed-total)) / 3)" />
            <col style="width: 120px" />
            <col style="width: 100px" />
            <col style="width: 100px" />
            <col style="width: 100px" />
            <col style="width: 100px" />
            <col style="width: 120px" />
            <col style="width: 160px" />
          </colgroup>
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
              <th width="160">项目编号</th>
              <th>项目名称</th>
              <th>客户名称</th>
              <th>档案系统</th>
              <th width="100">总工时</th>
              <th width="100">销售负责人</th>
              <th width="100">运维负责人</th>
              <th width="120">运维状态</th>
              <th width="100">运维类型</th>
              <th width="160">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(project, index) in projectList" 
              :key="project.projectId"
              @click="selectProject(project)"
              @dblclick="openMaintenanceRecord(project)"
              :class="{ selected: selectedProject && selectedProject.projectId === project.projectId }"
            >
              <td>
                <input 
                  type="checkbox" 
                  :value="project.projectId"
                  v-model="selectedProjects"
                  @click.stop
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ project.projectNum }}</td>
              <td>{{ project.projectName }}</td>
              <td>{{ project.customerName || '-' }}</td>
              <td>{{ project.arcSystem }}</td>
              <td>{{ project.totalHours || '-' }}</td>
              <td>{{ project.saleDirectorName || '-' }}</td>
              <td>{{ project.serviceDirectorName || '-' }}</td>
              <td>
                <span :class="getStatusClass(project.serviceState)">
                  {{ project.serviceState || '-' }}
                </span>
              </td>
              <td>{{ project.serviceType || '-' }}</td>
              <td>
                <button class="btn-small btn-info" @click.stop="viewProject(project)" style="margin-right: 5px;">查看</button>
                <button class="btn-small btn-primary" @click.stop="editProject(project)">编辑</button>
                <button class="btn-small btn-danger" @click.stop="deleteProject(project)">删除</button>
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
</template>

<script>
import { 
  getAfterserviceProjects, 
  deleteAfterserviceProject, 
  batchDeleteAfterserviceProjects 
} from '../api/afterserviceProject'

export default {
  name: 'MaintenanceProjectManagement',
  data() {
    return {
      // 项目列表
      projectList: [],
      // 搜索表单
      searchForm: {
        projectName: '',
        status: '',
        saleDirector: ''
      },
      // 分页信息
      currentPage: 1,
      pageSize: 20,
      total: 0,
      // 选中的项目
      selectedProjects: [],
      // 当前选中的单个项目
      selectedProject: null,
      // 加载状态
      loading: false,
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
    this.loadProjects()
  },
  methods: {
    /**
     * 加载项目列表
     */
    async loadProjects() {
      try {
        this.loading = true
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          ...this.searchForm
        }
        try {
          const raw = localStorage.getItem('userInfo')
          const info = raw ? JSON.parse(raw) : null
          const name = info && info.roleName ? String(info.roleName).trim() : ''
          const lower = name.toLowerCase()
          const uid = info && (info.userId ?? info.id)
          const isPrivileged = (
            ['管理员', '公司领导', '超级管理员', '销售总监', '项目总监'].some(r => name.includes(r)) ||
            ['admin', 'leader', 'super admin', 'superadmin', 'sales director', 'project director'].some(r => lower === r)
          )
          if (!isPrivileged && uid != null) {
            const isPM = (name.includes('项目经理') || lower === 'project manager' || lower === 'pm')
            const isAfter = (name.includes('售后') || name.includes('运维'))
            const isSales = (name.includes('销售') || lower === 'sales')
            if (isPM || isAfter) {
              params.serviceDirector = Number(uid)
            } else if (isSales) {
              params.saleDirector = Number(uid)
            }
          }
        } catch (_) {}
        
        const response = await getAfterserviceProjects(params)
        if (response.data.success) {
          this.projectList = response.data.data.list || []
          this.total = response.data.data.total || 0
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
        status: '',
        saleDirector: ''
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
      this.$emit('show-afterservice-project-form')
    },

    /**
     * 双击打开运维记录界面
     */
    openMaintenanceRecord(project) {
      if (!project?.projectId) return
      this.$router.push({ name: 'MaintenanceRecord', params: { projectId: project.projectId } })
    },

    /**
     * 编辑项目
     */
    editProject(project) {
      this.$emit('show-afterservice-project-form', project)
    },

    /**
     * 删除项目
     */
    async deleteProject(project) {
      if (!confirm(`确定要删除项目"${project.projectName}"吗？`)) {
        return
      }

      try {
        const response = await deleteAfterserviceProject(project.projectId)
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
        const response = await batchDeleteAfterserviceProjects(this.selectedProjects)
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
     * 查看项目
     */
    viewProject(project) {
      this.$emit('show-afterservice-project-form', project, true)
    },

    /**
     * 编辑项目
     */
    editProject(project) {
      this.$emit('show-afterservice-project-form', project)
    },

    /**
     * 删除项目
     */
    async deleteProject(project) {
      if (!confirm(`确定要删除项目"${project.projectName}"吗？`)) {
        return
      }

      try {
        const response = await deleteAfterserviceProject(project.projectId)
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
     * 双击打开运维记录界面
     */
    openMaintenanceRecord(project) {
      if (!project?.projectId) return
      this.$router.push({ name: 'MaintenanceRecord', params: { projectId: project.projectId } })
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
        '免费运维期': 'status-free',
        '付费运维': 'status-paid',
        '无付费运维': 'status-none',
        '暂停运维': 'status-paused'
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

    // 悬浮提示事件与定位（与在建项目管理模块一致）
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
.maintenance-management {
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
  margin: 0;
  color: #262626;
  font-size: 20px;
  font-weight: 600;
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

.maintenance-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  --fixed-total: 900px;
}

.maintenance-table th,
.maintenance-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.maintenance-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.maintenance-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.maintenance-table tbody tr:hover {
  background: #f5f5f5;
}

.maintenance-table tbody tr.selected {
  background: #e6f7ff;
}

.action-buttons {
  display: flex;
  gap: 3px;
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
    background: #138496;
    border-color: #117a8b;
  }

  .btn-sm {
    padding: 3px 6px;
    font-size: 11px;
  }

  /* 与在建项目管理一致的小号按钮样式 */
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
.status-active {
  color: #52c41a;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-free {
  color: #52c41a; /* 绿色，表示免费期 */
  background: #f6ffed;
  border: 1px solid #b7eb8f;
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

.status-paid {
  color: #1890ff; /* 蓝色，表示付费运维 */
  background: #f0f5ff;
  border: 1px solid #91d5ff;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-none {
  color: #8c8c8c; /* 灰色，表示无付费运维 */
  background: #fafafa;
  border: 1px solid #d9d9d9;
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

  .icon-delete::before {
    content: '🗑';
  }

/* 响应式设计 */
@media (max-width: 768px) {
  .maintenance-management {
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

/* 单元格悬浮提示样式 */
.cell-tooltip {
  position: fixed;
  z-index: 1000;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  max-width: 600px;
  line-height: 1.4;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
</style>
