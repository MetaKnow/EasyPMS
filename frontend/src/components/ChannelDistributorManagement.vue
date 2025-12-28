<template>
  <div class="channel-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2 class="page-title">渠道商维护</h2>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="showAddForm">
          <i class="icon-plus"></i>
          新增渠道商
        </button>
        <button class="btn btn-danger" @click="deleteSelected" :disabled="selectedChannels.length === 0">
          <i class="icon-delete"></i>
          删除渠道商
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
          v-model="searchForm.channelName" 
          type="text" 
          placeholder="渠道名称"
          class="search-input"
        />
        <input 
          v-model="searchForm.contactor" 
          type="text" 
          placeholder="联系人"
          class="search-input"
        />
        <input 
          v-model="searchForm.phoneNumber" 
          type="text" 
          placeholder="联系方式"
          class="search-input"
        />
        <button class="btn btn-primary" @click="searchChannels">
          <i class="icon-search"></i>
          搜索
        </button>
        <button class="btn btn-secondary" @click="resetSearch">
          <i class="icon-refresh"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 渠道商列表 -->
    <div class="table-section">
      <div class="table-container">
        <table class="channel-table">
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
              <th>渠道名称</th>
              <th>联系人</th>
              <th>联系方式</th>
              <th>销售负责人</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(channel, index) in channels" 
              :key="channel.channelId"
              :class="{ selected: isSelected(channel) }"
              @click="toggleSelectChannel(channel)"
            >
              <td>
                <input 
                  type="checkbox" 
                  :checked="isSelected(channel)"
                  @change.stop="toggleSelectChannel(channel)"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ channel.channelName }}</td>
              <td>{{ channel.contactor || '-' }}</td>
              <td>{{ channel.phoneNumber || '-' }}</td>
              <td>{{ getSaleDirectorName(channel.saleDirector) }}</td>
              <td>{{ formatDate(channel.createdAt) }}</td>
              <td>
                <button class="btn-small btn-primary" @click.stop="editChannel(channel)">
                  编辑
                </button>
                <button class="btn-small btn-danger" @click.stop="deleteChannel(channel)">
                  删除
                </button>
              </td>
            </tr>
            <tr v-if="channels.length === 0">
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

    <!-- 渠道商表单弹窗 -->
    <ChannelDistributorForm
      :visible="showForm"
      :mode="formMode"
      :channel-data="currentChannel"
      @close="closeForm"
      @success="handleFormSuccess"
      @error="handleFormError"
    />

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除渠道商 "{{ deleteTarget?.channelName }}" 吗？</p>
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
        <p>确定要删除选中的 {{ selectedChannels.length }} 个渠道商吗？</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeBatchDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="confirmBatchDelete">删除</button>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message.show" :class="['message', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script>
import ChannelDistributorForm from './ChannelDistributorForm.vue'
import { 
  getChannelDistributorList, 
  createChannelDistributor,
  deleteChannelDistributor, 
  batchDeleteChannelDistributors 
} from '../api/channelDistributor.js'
import { getAllUsers } from '../api/user.js'

export default {
  name: 'ChannelDistributorManagement',
  components: {
    ChannelDistributorForm
  },
  data() {
    return {
      /**
       * 渠道商列表数据
       */
      channels: [],
      /**
       * 用户列表数据（用于显示销售负责人姓名）
       */
      users: [],
      /**
       * 选中的渠道商
       */
      selectedChannels: [],
      /**
       * 搜索表单数据
       */
      searchForm: {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      },
      /**
       * 分页信息
       */
      currentPage: 1,
      pageSize: 20,
      totalElements: 0,
      totalPages: 0,
      /**
       * 表单相关状态
       */
      showForm: false,
      formMode: 'add', // 'add' | 'edit'
      currentChannel: null,
      /**
       * 删除确认相关状态
       */
      showDeleteConfirm: false,
      showBatchDeleteConfirm: false,
      deleteTarget: null,
      /**
       * 加载状态
       */
      loading: false,
      /**
       * 消息提示
       */
      message: {
        show: false,
        type: 'success', // 'success' | 'error'
        text: ''
      }
    }
  },
  computed: {
    /**
     * 是否全选
     */
    isAllSelected() {
      return this.channels.length > 0 && this.selectedChannels.length === this.channels.length
    },
    canExport() {
      try {
        const raw = sessionStorage.getItem('userInfo')
        const info = raw ? JSON.parse(raw) : null
        const role = info && info.roleName ? String(info.roleName).trim() : ''
        const roleLower = role.toLowerCase()
        const isSales = role === '销售' || role === '销售角色' || roleLower === 'sales'
        return !isSales
      } catch (_) {
        return true
      }
    }
  },
  mounted() {
    this.loadChannels()
    this.loadUsers()
  },
  methods: {
    /**
     * 加载用户列表
     */
    async loadUsers() {
      try {
        this.users = await getAllUsers()
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    },

    /**
     * 获取销售负责人姓名
     */
    getSaleDirectorName(saleDirectorId) {
      if (!saleDirectorId) return '-'
      const user = this.users.find(u => u.userId === saleDirectorId)
      return user ? (user.name || user.userName) : '-'
    },

    exportTable() {
      if (!Array.isArray(this.channels) || this.channels.length === 0) {
        this.showMessage('当前没有数据可导出', 'error')
        return
      }

      try {
        const exportData = this.channels.map((c) => ({
          '渠道名称': c.channelName || '',
          '联系人': c.contactor || '',
          '联系方式': c.phoneNumber || '',
          '销售负责人': this.getSaleDirectorName(c.saleDirector)
        }))
        const csvContent = this.convertToCSV(exportData)
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `渠道商维护_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        this.showMessage('表格导出成功', 'success')
      } catch (error) {
        console.error('导出表格失败:', error)
        this.showMessage('导出表格失败: ' + error.message, 'error')
      }
    },

    triggerImport() {
      this.$refs.fileInput?.click()
    },

    async handleFileImport(event) {
      const file = event.target.files && event.target.files[0]
      if (!file) return

      try {
        if (!Array.isArray(this.users) || this.users.length === 0) {
          await this.loadUsers()
        }
        const text = await this.readFileAsText(file)
        const importData = this.parseCSV(text)
        if (!Array.isArray(importData) || importData.length === 0) {
          this.showMessage('文件中没有有效数据', 'error')
          return
        }

        const headers = Object.keys(importData[0] || {})
        const requiredHeaders = ['渠道名称', '联系人', '联系方式']
        const missing = requiredHeaders.filter(h => !headers.includes(h))
        if (missing.length > 0) {
          this.showMessage(`文件缺少必须的列: ${missing.join(', ')}`, 'error')
          return
        }

        const validData = this.validateChannelImportData(importData)
        if (validData.length === 0) {
          this.showMessage('文件格式不正确或数据无效', 'error')
          return
        }

        if (confirm(`确定要导入 ${validData.length} 条数据吗？`)) {
          await this.importChannels(validData)
        }
      } catch (error) {
        console.error('导入表格失败:', error)
        this.showMessage('导入表格失败: ' + error.message, 'error')
      } finally {
        event.target.value = ''
      }
    },

    validateChannelImportData(rows) {
      const list = []
      for (const row of rows || []) {
        const channelName = (row['渠道名称'] || '').trim()
        const contactor = (row['联系人'] || '').trim()
        const phoneNumber = (row['联系方式'] || '').trim()
        if (!channelName || !contactor || !phoneNumber) continue

        const saleDirector = this.parseUserId(row['销售负责人'])
        list.push({
          channelName,
          contactor,
          phoneNumber,
          saleDirector: saleDirector || null
        })
      }
      return list
    },

    async importChannels(data) {
      let successCount = 0
      let errorCount = 0
      for (const item of data) {
        try {
          await createChannelDistributor(item)
          successCount++
        } catch (error) {
          console.error('导入渠道商失败:', error)
          errorCount++
        }
      }

      if (successCount > 0) {
        this.showMessage(`成功导入 ${successCount} 条数据${errorCount > 0 ? `，失败 ${errorCount} 条` : ''}`, 'success')
        this.loadChannels()
      } else {
        this.showMessage('导入失败，请检查数据格式', 'error')
      }
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
      const headerOk = tokens.includes('渠道名称') || tokens.includes('联系人') || tokens.includes('联系方式')
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
     * 加载渠道商列表
     */
    async loadChannels() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize,
          ...this.searchForm
        }

        try {
          const raw = sessionStorage.getItem('userInfo')
          const info = raw ? JSON.parse(raw) : null
          const roleName = info && info.roleName ? String(info.roleName).trim() : ''
          const roleLower = roleName.toLowerCase()
          const userNameLower = info && info.userName ? String(info.userName).trim().toLowerCase() : ''
          const uid = info && (info.userId ?? info.id)

          const isAdminUser = userNameLower === 'admin'
          const isPrivileged = (
            roleName.includes('管理员') ||
            roleName.includes('公司领导') ||
            roleName.includes('销售总监') ||
            roleName.includes('超级管理员') ||
            roleLower.includes('admin') ||
            roleLower.includes('leader') ||
            roleLower.includes('sales director') ||
            roleLower.includes('super admin') ||
            roleLower.includes('superadmin')
          )
          const isSalesRole = roleName.includes('销售') || roleLower.includes('sales')

          if (!isAdminUser && !isPrivileged && isSalesRole && uid != null) {
            params.saleDirector = Number(uid)
          }
        } catch (_) {}
        
        const response = await getChannelDistributorList(params)
        this.channels = response.content || []
        this.totalElements = response.totalElements || 0
        this.totalPages = response.totalPages || 0
        this.selectedChannels = []
      } catch (error) {
        console.error('加载渠道商列表失败:', error)
        this.showMessage('加载渠道商列表失败', 'error')
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索渠道商
     */
    searchChannels() {
      this.currentPage = 1
      this.loadChannels()
    },

    /**
     * 重置搜索
     */
    resetSearch() {
      this.searchForm = {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      }
      this.currentPage = 1
      this.loadChannels()
    },

    /**
     * 上一页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadChannels()
      }
    },

    /**
     * 下一页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadChannels()
      }
    },

    /**
     * 显示新增表单
     */
    showAddForm() {
      this.formMode = 'add'
      this.currentChannel = null
      this.showForm = true
    },

    /**
     * 编辑渠道商
     */
    editChannel(channel) {
      this.formMode = 'edit'
      this.currentChannel = channel
      this.showForm = true
    },



    /**
     * 关闭表单
     */
    closeForm() {
      this.showForm = false
      this.currentChannel = null
    },

    /**
     * 表单提交成功处理
     */
    handleFormSuccess(message) {
      this.showMessage(message, 'success')
      this.loadChannels()
    },

    /**
     * 表单提交错误处理
     */
    handleFormError(message) {
      this.showMessage(message, 'error')
    },

    /**
     * 删除渠道商
     */
    deleteChannel(channel) {
      this.deleteTarget = channel
      this.showDeleteConfirm = true
    },

    /**
     * 删除选中的渠道商
     */
    deleteSelected() {
      if (this.selectedChannels.length === 1) {
        this.deleteChannel(this.selectedChannels[0])
      } else if (this.selectedChannels.length > 1) {
        this.showBatchDeleteConfirm = true
      }
    },

    /**
     * 关闭删除确认弹窗
     */
    closeDeleteConfirm() {
      this.showDeleteConfirm = false
      this.deleteTarget = null
    },

    /**
     * 关闭批量删除确认弹窗
     */
    closeBatchDeleteConfirm() {
      this.showBatchDeleteConfirm = false
    },

    /**
     * 确认删除
     */
    async confirmDelete() {
      try {
        await deleteChannelDistributor(this.deleteTarget.channelId)
        this.showMessage('渠道商删除成功', 'success')
        this.loadChannels()
      } catch (error) {
        console.error('删除渠道商失败:', error)
        this.showMessage('删除渠道商失败', 'error')
      } finally {
        this.closeDeleteConfirm()
      }
    },

    /**
     * 确认批量删除
     */
    async confirmBatchDelete() {
      try {
        const channelIds = this.selectedChannels.map(channel => channel.channelId)
        await batchDeleteChannelDistributors(channelIds)
        this.showMessage(`成功删除 ${channelIds.length} 个渠道商`, 'success')
        this.loadChannels()
      } catch (error) {
        console.error('批量删除渠道商失败:', error)
        this.showMessage('批量删除渠道商失败', 'error')
      } finally {
        this.closeBatchDeleteConfirm()
      }
    },

    /**
     * 切换渠道商选中状态
     */
    toggleSelectChannel(channel) {
      const index = this.selectedChannels.findIndex(c => c.channelId === channel.channelId)
      if (index > -1) {
        this.selectedChannels.splice(index, 1)
      } else {
        this.selectedChannels.push(channel)
      }
    },

    /**
     * 全选/取消全选
     */
    selectAll(event) {
      if (event.target.checked) {
        this.selectedChannels = [...this.channels]
      } else {
        this.selectedChannels = []
      }
    },

    /**
     * 检查渠道商是否被选中
     */
    isSelected(channel) {
      return this.selectedChannels.some(c => c.channelId === channel.channelId)
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
     * 显示消息提示
     */
    showMessage(text, type = 'success') {
      this.message = {
        show: true,
        type,
        text
      }
      setTimeout(() => {
        this.message.show = false
      }, 3000)
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.channel-management {
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

.channel-table {
  width: 100%;
  border-collapse: collapse;
}

.channel-table th,
.channel-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.channel-table th {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.channel-table tbody tr {
  cursor: pointer;
  transition: background-color 0.3s;
}

.channel-table tbody tr:hover {
  background: #f5f5f5;
}

.channel-table tbody tr.selected {
  background: #e6f7ff;
}

.action-cell {
  display: flex;
  gap: 3px;
  justify-content: center;
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.pagination-info {
  font-size: 13px;
  color: #8c8c8c;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #262626;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-select {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  background: white;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  min-width: 300px;
  max-width: 500px;
}

.modal-content h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.modal-content p {
  margin: 0 0 24px 0;
  color: #666;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 消息提示 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  font-weight: 500;
  z-index: 1001;
  animation: slideIn 0.3s ease-out;
}

.message.success {
  background-color: #28a745;
}

.message.error {
  background-color: #dc3545;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 图标 */
.icon-plus::before { content: '+'; }
.icon-edit::before { content: '✏️'; }
.icon-delete::before { content: '🗑️'; }
.icon-search::before { content: '🔍'; }
.icon-refresh::before { content: '🔄'; }
.icon-download::before { content: "↓"; display: inline-block; margin-right: 4px; }
.icon-upload::before { content: "↑"; display: inline-block; margin-right: 4px; }

/* 响应式设计 */
@media (max-width: 768px) {
  .channel-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .search-select {
    min-width: auto;
  }
  
  .table-container {
    font-size: 12px;
  }
  
  .channel-table th,
  .channel-table td {
    padding: 8px 4px;
  }
}
</style>
