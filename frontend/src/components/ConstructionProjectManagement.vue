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
        <button class="btn btn-transfer" @click="openHandover" :disabled="!handoverEnabled">
          <i class="icon-transfer"></i>
          移交运维
        </button>
        <button v-if="canExport" class="btn btn-success" @click="exportTable">
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
        <input 
          v-model="searchForm.customerName" 
          type="text" 
          placeholder="客户名称"
          class="search-input"
        />
        <input 
          v-model="searchForm.softName" 
          type="text" 
          placeholder="软件系统"
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
  <ProjectHandoverForm
    :visible="handoverFormVisible"
    :project-data="handoverProject"
    :users="users"
    @close="closeHandover"
    @success="onHandoverSuccess"
  />
</template>

<script>
import { 
  getConstructingProjects, 
  getConstructingProjectById,
  createConstructingProject,
  deleteConstructingProject, 
  batchDeleteConstructingProjects,
  getProjectSummary
} from '../api/constructingProject'
import ProjectCreateForm from './ProjectCreateForm.vue'
import ProjectHandoverForm from './ProjectHandoverForm.vue'
import { getAllUsers } from '../api/user'
import { getAllCustomers } from '../api/customer.js'
import { getAllChannelDistributors } from '../api/channelDistributor.js'
import { getAllProducts } from '../api/product.js'

export default {
  name: 'ConstructionProjectManagement',
  components: {
    ProjectCreateForm,
    ProjectHandoverForm
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
        year: '',
        customerName: '',
        softName: ''
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
      handoverFormVisible: false,
      handoverProject: null,
      users: [],
      customers: [],
      channels: [],
      products: [],
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
    },
    /**
     * 函数级注释：移交按钮是否可用（仅选中且状态为“进行中”的项目）
     */
    handoverEnabled() {
      if (this.selectedProjects.length !== 1) return false
      const p = this.projectList.find(x => x.projectId === this.selectedProjects[0])
      return !!p && p.projectState === '进行中'
    },
    canExport() {
      try {
        const raw = sessionStorage.getItem('userInfo')
        const info = raw ? JSON.parse(raw) : null
        const role = info && info.roleName ? String(info.roleName).trim() : ''
        const roleLower = role.toLowerCase()
        const isSales = role === '销售' || role === '销售角色' || roleLower === 'sales'
        const isAfterSales = role === '售后' || role.includes('售后') || roleLower === 'afterservice' || roleLower === 'after service'
        const isPM = role === '项目经理' || role.includes('项目经理') || roleLower === 'project manager' || roleLower === 'pm'
        return !(isSales || isAfterSales || isPM)
      } catch (_) {
        return true
      }
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
        try {
          const raw = sessionStorage.getItem('userInfo')
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
              params.projectLeader = Number(uid)
            } else if (isSales) {
              params.saleLeader = Number(uid)
            }
          }
        } catch (_) {}
        
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
        year: '',
        customerName: '',
        softName: ''
      }
      this.searchProjects()
    },

    async exportTable() {
      if (!Array.isArray(this.projectList) || this.projectList.length === 0) {
        alert('当前没有数据可导出')
        return
      }

      try {
        await this.ensureImportLookups()

        const details = await Promise.all(
          (this.projectList || []).map(async (p) => {
            const id = p && p.projectId != null ? p.projectId : null
            if (!id) return null
            try {
              const resp = await getConstructingProjectById(id)
              const data = resp && resp.data ? resp.data : null
              if (data && data.success === true) return data.data || null
              return data && data.data ? data.data : null
            } catch (_) {
              return null
            }
          })
        )

        const userNameById = (id) => {
          if (!id) return ''
          const u = (this.users || []).find(x => x && x.userId === id)
          return u ? (u.name || u.userName || '') : ''
        }
        const customerNameById = (id) => {
          if (!id) return ''
          const c = (this.customers || []).find(x => x && x.customerId === id)
          return c ? (c.customerName || '') : ''
        }
        const channelNameById = (id) => {
          if (!id) return ''
          const c = (this.channels || []).find(x => x && x.channelId === id)
          return c ? (c.channelName || '') : ''
        }
        const softLabelById = (id) => {
          if (!id) return ''
          const p = (this.products || []).find(x => x && x.softId === id)
          if (!p) return ''
          const name = p.softName || ''
          const ver = p.softVersion || ''
          return name ? (name + (ver ? ` (${ver})` : '')) : ''
        }
        const boolToCN = (v) => (v === true ? '是' : v === false ? '否' : '')
        const dateStr = (v) => (v == null ? '' : String(v))

        const exportData = details
          .filter(Boolean)
          .map((d) => ({
            '项目ID': d.projectId != null ? String(d.projectId) : '',
            '项目编号': d.projectNum || '',
            '年度': d.year != null ? String(d.year) : '',
            '项目名称': d.projectName || '',
            '项目类型': d.projectType || '',
            '项目状态': d.projectState || '',
            '项目负责人ID': d.projectLeader != null ? String(d.projectLeader) : '',
            '项目负责人': userNameById(d.projectLeader),
            '商务负责人ID': d.saleLeader != null ? String(d.saleLeader) : '',
            '商务负责人': userNameById(d.saleLeader),
            '客户ID': d.customerId != null ? String(d.customerId) : '',
            '客户名称': customerNameById(d.customerId),
            '档案系统ID': d.softId != null ? String(d.softId) : '',
            '档案系统': softLabelById(d.softId),
            '开始日期': dateStr(d.startDate),
            '计划结束日期': dateStr(d.planEndDate),
            '实际结束日期': dateStr(d.actualEndDate),
            '预计工期(天)': d.planPeriod != null ? String(d.planPeriod) : '',
            '实际工期(天)': d.actualPeriod != null ? String(d.actualPeriod) : '',
            '是否渠道项目': d.isAgent != null ? (Number(d.isAgent) === 1 ? '是' : '否') : '',
            '渠道ID': d.channelId != null ? String(d.channelId) : '',
            '渠道名称': channelNameById(d.channelId),
            '项目金额': d.value != null ? String(d.value) : '',
            '已回款金额': d.receivedMoney != null ? String(d.receivedMoney) : '',
            '未回款金额': d.unreceiveMoney != null ? String(d.unreceiveMoney) : '',
            '验收日期': dateStr(d.acceptanceDate),
            '建设内容': d.constructContent || '',
            '是否移交运维': boolToCN(d.isCommitAfterSale),
            '创建时间': dateStr(d.createTime),
            '更新时间': dateStr(d.updateTime)
          }))

        const csvContent = this.convertToCSV(exportData)
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `在建项目管理_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        alert('表格导出成功')
      } catch (error) {
        console.error('导出表格失败:', error)
        alert('导出表格失败: ' + error.message)
      }
    },

    triggerImport() {
      this.$refs.fileInput?.click()
    },

    async handleFileImport(event) {
      const file = event.target.files && event.target.files[0]
      if (!file) return

      try {
        await this.ensureImportLookups()

        const text = await this.readFileAsText(file)
        const importData = this.parseCSV(text)
        if (!Array.isArray(importData) || importData.length === 0) {
          alert('文件中没有有效数据')
          return
        }

        const headers = Object.keys(importData[0] || {})
        const requiredHeaders = ['项目名称', '年度', '项目类型']
        const missing = requiredHeaders.filter(h => !headers.includes(h))
        if (missing.length > 0) {
          alert(`文件缺少必须的列: ${missing.join(', ')}`)
          return
        }

        const validData = this.validateConstructingProjectImportData(importData)
        if (validData.length === 0) {
          alert('文件格式不正确或数据无效')
          return
        }

        if (confirm(`确定要导入 ${validData.length} 条数据吗？`)) {
          await this.importConstructingProjects(validData)
        }
      } catch (error) {
        console.error('导入表格失败:', error)
        alert('导入表格失败: ' + error.message)
      } finally {
        event.target.value = ''
      }
    },

    async ensureImportLookups() {
      if (!Array.isArray(this.users) || this.users.length === 0) {
        try {
          this.users = await getAllUsers()
        } catch (_) {
          this.users = []
        }
      }
      if (!Array.isArray(this.customers) || this.customers.length === 0) {
        try {
          this.customers = await getAllCustomers()
        } catch (_) {
          this.customers = []
        }
      }
      if (!Array.isArray(this.channels) || this.channels.length === 0) {
        try {
          this.channels = await getAllChannelDistributors()
        } catch (_) {
          this.channels = []
        }
      }
      if (!Array.isArray(this.products) || this.products.length === 0) {
        try {
          this.products = await getAllProducts()
        } catch (_) {
          this.products = []
        }
      }
    },

    validateConstructingProjectImportData(rows) {
      const list = []
      for (const row of rows || []) {
        const projectName = (row['项目名称'] || '').trim()
        const year = this.parseYear(row['年度'])
        const projectType = (row['项目类型'] || '').trim()
        if (!projectName || !year || !projectType) continue

        const projectNum = (row['项目编号'] || '').trim()
        const projectState = (row['项目状态'] || '').trim()
        if (projectState === '已完成') continue

        const projectLeader = this.parseUserId(row['项目负责人ID']) || this.parseUserId(row['项目负责人'])
        const saleLeader = this.parseUserId(row['商务负责人ID']) || this.parseUserId(row['商务负责人'])
        const customerId = this.parseCustomerId(row['客户ID']) || this.parseCustomerId(row['客户名称'])
        const softId = this.parseSoftId(row['档案系统ID']) || this.parseSoftId(row['档案系统']) || this.parseSoftId(row['软件系统'])

        const isAgent = this.parseBoolean(row['是否渠道项目'])
        const channelId = this.parseChannelId(row['渠道ID']) || this.parseChannelId(row['渠道名称'])
        const startDate = this.parseDate(row['开始日期'])
        const planEndDate = this.parseDate(row['计划结束日期'])
        const actualEndDate = this.parseDate(row['实际结束日期'])
        const acceptanceDate = this.parseDate(row['验收日期'])
        const planPeriod = this.parseInt(row['预计工期(天)']) ?? this.parseInt(row['预计工期'])
        const actualPeriod = this.parseInt(row['实际工期(天)']) ?? this.parseInt(row['实际工期'])
        const value = this.parseDecimal(row['项目金额'])
        const receivedMoney = this.parseDecimal(row['已回款金额'])
        const unreceiveMoney = this.parseDecimal(row['未回款金额'])
        const constructContent = (row['建设内容'] || '').trim()
        const isCommitAfterSale = this.parseBoolean(row['是否移交运维'])

        if (isAgent === true && !channelId) continue

        const payload = {
          projectName,
          year,
          projectType
        }

        if (projectNum) payload.projectNum = projectNum
        if (projectState) payload.projectState = projectState
        if (projectLeader) payload.projectLeader = projectLeader
        if (saleLeader) payload.saleLeader = saleLeader
        if (customerId) payload.customerId = customerId
        if (softId) payload.softId = softId
        if (startDate) payload.startDate = startDate
        if (planEndDate) payload.planEndDate = planEndDate
        if (actualEndDate) payload.actualEndDate = actualEndDate
        if (acceptanceDate) payload.acceptanceDate = acceptanceDate
        if (planPeriod != null) payload.planPeriod = planPeriod
        if (actualPeriod != null) payload.actualPeriod = actualPeriod
        if (value != null) payload.value = value
        if (receivedMoney != null) payload.receivedMoney = receivedMoney
        if (unreceiveMoney != null) payload.unreceiveMoney = unreceiveMoney
        if (constructContent) payload.constructContent = constructContent
        if (isCommitAfterSale !== null) payload.isCommitAfterSale = isCommitAfterSale
        if (isAgent !== null) payload.isAgent = isAgent ? 1 : 0
        if (channelId) payload.channelId = channelId

        list.push(payload)
      }
      return list
    },

    async importConstructingProjects(data) {
      let successCount = 0
      let errorCount = 0
      for (const item of data) {
        try {
          const resp = await createConstructingProject(item)
          if (resp?.data?.success === false) throw new Error(resp?.data?.error || resp?.data?.message || '创建失败')
          successCount++
        } catch (error) {
          console.error('导入在建项目失败:', error)
          errorCount++
        }
      }

      if (successCount > 0) {
        alert(`成功导入 ${successCount} 条数据${errorCount > 0 ? `，失败 ${errorCount} 条` : ''}`)
        this.loadProjects()
      } else {
        alert('导入失败，请检查数据格式')
      }
    },

    parseYear(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const y = Number(raw)
      if (Number.isNaN(y) || !Number.isFinite(y)) return null
      return Math.trunc(y)
    },

    parseInt(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const n = Number(raw)
      if (Number.isNaN(n) || !Number.isFinite(n)) return null
      return Math.trunc(n)
    },

    parseDecimal(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const n = Number(raw)
      if (Number.isNaN(n) || !Number.isFinite(n)) return null
      return n
    },

    parseDate(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const m = raw.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})$/)
      if (!m) return null
      const y = String(m[1]).padStart(4, '0')
      const mm = String(m[2]).padStart(2, '0')
      const dd = String(m[3]).padStart(2, '0')
      return `${y}-${mm}-${dd}`
    },

    parseUserId(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const asNum = Number(raw)
      if (!Number.isNaN(asNum) && Number.isFinite(asNum) && asNum > 0) return asNum
      const user = (this.users || []).find(u => (u?.name && String(u.name).trim() === raw) || (u?.userName && String(u.userName).trim() === raw))
      return user ? user.userId : null
    },

    parseCustomerId(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const asNum = Number(raw)
      if (!Number.isNaN(asNum) && Number.isFinite(asNum) && asNum > 0) return asNum
      const c = (this.customers || []).find(x => x?.customerName && String(x.customerName).trim() === raw)
      return c ? c.customerId : null
    },

    parseChannelId(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const asNum = Number(raw)
      if (!Number.isNaN(asNum) && Number.isFinite(asNum) && asNum > 0) return asNum
      const c = (this.channels || []).find(x => x?.channelName && String(x.channelName).trim() === raw)
      return c ? c.channelId : null
    },

    parseSoftId(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const asNum = Number(raw)
      if (!Number.isNaN(asNum) && Number.isFinite(asNum) && asNum > 0) return asNum

      const match = raw.match(/^(.+?)(?:\s*\((.+)\))?$/)
      const softName = match ? String(match[1] || '').trim() : raw
      const softVersion = match && match[2] != null ? String(match[2]).trim() : ''
      const p = (this.products || []).find(x => {
        const n = x?.softName ? String(x.softName).trim() : ''
        const v = x?.softVersion ? String(x.softVersion).trim() : ''
        if (!n) return false
        if (softVersion) return n === softName && v === softVersion
        return n === softName
      })
      return p ? p.softId : null
    },

    parseBoolean(value) {
      if (value === undefined || value === null) return null
      const raw = String(value).trim()
      if (!raw) return null
      const lower = raw.toLowerCase()
      if (raw === '是' || raw === '1' || lower === 'true' || lower === 'yes') return true
      if (raw === '否' || raw === '0' || lower === 'false' || lower === 'no') return false
      return null
    },

    convertToCSV(data) {
      if (!Array.isArray(data) || data.length === 0) return ''
      const headers = Object.keys(data[0])
      const rows = [headers.join(',')]
      for (const row of data) {
        const values = headers.map(h => {
          const v = row[h] == null ? '' : String(row[h])
          if (/[",\n\r]/.test(v)) return `"${v.replace(/"/g, '""')}"`
          return v
        })
        rows.push(values.join(','))
      }
      return rows.join('\n')
    },

    async readFileAsText(file) {
      const buffer = await this.readFileAsArrayBuffer(file)
      let utf8 = ''
      try {
        utf8 = new TextDecoder('utf-8').decode(buffer)
      } catch (_) {}
      if (this.isLikelyChineseCSV(utf8)) return utf8
      try {
        const gb18030 = new TextDecoder('gb18030').decode(buffer)
        if (this.isLikelyChineseCSV(gb18030)) return gb18030
      } catch (_) {}
      try {
        const gbk = new TextDecoder('gbk').decode(buffer)
        if (this.isLikelyChineseCSV(gbk)) return gbk
      } catch (_) {}
      try {
        return await this.readAsTextLegacy(file, 'utf-8')
      } catch (_) {}
      return utf8
    },

    readFileAsArrayBuffer(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsArrayBuffer(file)
      })
    },

    readAsTextLegacy(file, encoding) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsText(file, encoding)
      })
    },

    isLikelyChineseCSV(text) {
      if (!text || typeof text !== 'string') return false
      const firstLine = (text.split(/\r?\n/).find(line => line.trim().length > 0) || '')
      const delimiter = firstLine.includes(',') ? ',' : firstLine.includes(';') ? ';' : firstLine.includes('\t') ? '\t' : firstLine.includes('，') ? '，' : ','
      const tokens = this.parseCSVLine(firstLine, delimiter).map(h => (h || '').replace(/^\ufeff/, '').replace(/["“”]/g, '').trim())
      const replacementCount = (text.match(/\uFFFD/g) || []).length
      const hasChinese = /[\u4e00-\u9fa5]/.test(text)
      const headerOk = tokens.includes('项目名称') || tokens.includes('项目类型') || tokens.includes('年度')
      return (headerOk && replacementCount === 0) || (hasChinese && replacementCount < 5)
    },

    parseCSV(text) {
      const lines = (text || '').split(/\r?\n/).filter(line => line.trim())
      if (lines.length < 2) return []

      const headerLine = lines[0]
      const candidates = [',', ';', '\t', '，', '；', '|']
      let delimiter = ','
      let bestCount = -1
      const stripped = headerLine.replace(/"[^"]*"/g, '')
      for (const d of candidates) {
        const count = stripped.split(d).length - 1
        if (count > bestCount) {
          bestCount = count
          delimiter = d
        }
      }
      const headers = this.parseCSVLine(headerLine, delimiter).map(h => (h || '').replace(/^\ufeff/, '').replace(/["“”]/g, '').trim())

      const data = []
      for (let i = 1; i < lines.length; i++) {
        const values = this.parseCSVLine(lines[i], delimiter)
        const row = {}
        headers.forEach((h, idx) => {
          row[h] = values[idx] !== undefined ? String(values[idx]).trim().replace(/^\ufeff/, '') : ''
        })
        data.push(row)
      }
      return data
    },

    parseCSVLine(line, delimiter = ',') {
      const result = []
      let current = ''
      let inQuotes = false
      const s = String(line || '')
      for (let i = 0; i < s.length; i++) {
        const char = s[i]
        if (char === '"') {
          if (inQuotes && s[i + 1] === '"') {
            current += '"'
            i++
          } else {
            inQuotes = !inQuotes
          }
        } else if (char === delimiter && !inQuotes) {
          result.push(current)
          current = ''
        } else {
          current += char
        }
      }
      result.push(current)
      return result.map(x => String(x).trim())
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
      if (this.selectedProjects.length === 1) {
        this.selectedProject = this.projectList.find(x => x.projectId === this.selectedProjects[0]) || null
      } else {
        this.selectedProject = null
      }
    },

    /**
     * 函数级注释：打开移交运维弹窗
     */
    async openHandover() {
      if (!this.handoverEnabled) return
      if (this.selectedProjects.length !== 1) return
      const p = this.projectList.find(x => x.projectId === this.selectedProjects[0])
      if (!p) return
      try {
        const resp = await getProjectSummary(p.projectId)
        const data = resp?.data?.data || {}
        const milestones = Array.isArray(data.milestones) ? data.milestones : []
        const allCompleted = milestones.length === 0 ? true : milestones.every(m => m && (m.iscomplete === true))
        if (!allCompleted) {
          alert('该项目有未完成的里程碑，请查看项目步骤及里程碑数据')
          return
        }
        this.handoverProject = p
        try {
          this.users = await getAllUsers()
        } catch (e) {
          this.users = []
        }
        this.handoverFormVisible = true
      } catch (e) {
        alert('无法加载项目里程碑数据，请稍后再试')
      }
    },

    /**
     * 函数级注释：关闭移交运维弹窗
     */
    closeHandover() {
      this.handoverFormVisible = false
      this.handoverProject = null
    },

    /**
     * 函数级注释：移交成功后刷新列表并重置选择
     */
    onHandoverSuccess() {
      this.closeHandover()
      this.selectedProjects = []
      this.selectedProject = null
      this.loadProjects()
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
      this.selectedProject = null
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
  background-color: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
  border-color: #e0a800;
  color: #212529;
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
  color: #fff;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #218838;
  color: #fff;
}

.btn-transfer {
  background: #fa8c16;
  border-color: #fa8c16;
  color: white;
}

.btn-transfer:hover {
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

.icon-download::before {
  content: "↓";
  display: inline-block;
  margin-right: 4px;
}

.icon-upload::before {
  content: "↑";
  display: inline-block;
  margin-right: 4px;
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
