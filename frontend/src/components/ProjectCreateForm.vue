<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content" @click.stop>
      <!-- 固定标题区域 -->
      <div class="modal-header">
        <h3>新建项目</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      
      <!-- 可滚动内容区域 -->
      <div class="modal-body">
        <form @submit.prevent="submitForm" class="project-form">
        <!-- 项目基本信息分组 -->
        <div class="form-section">
          <h4 class="section-title">项目基本信息</h4>
          <div class="form-grid">
            <div class="form-group">
              <label for="projectNum">项目编号 <span class="required">*</span></label>
              <input 
                type="text" 
                id="projectNum" 
                v-model="form.projectNum" 
                required 
                readonly
                placeholder="自动生成"
                class="readonly-field"
              />
            </div>

            <div class="form-group">
              <label for="year">年度 <span class="required">*</span></label>
              <input 
                type="number" 
                id="year" 
                v-model="form.year" 
                required 
                :min="2020" 
                :max="2030"
                placeholder="请输入年度"
              />
            </div>

            <div class="form-group full-width">
              <label for="projectName">项目名称 <span class="required">*</span></label>
              <input 
                type="text" 
                id="projectName" 
                v-model="form.projectName" 
                required 
                placeholder="请输入项目名称"
              />
            </div>

            <div class="form-group">
              <label for="projectType">项目类型 <span class="required">*</span></label>
              <select id="projectType" v-model="form.projectType" required>
                <option value="">请选择项目类型</option>
                <option value="软件开发">软件开发</option>
                <option value="系统集成">系统集成</option>
                <option value="技术咨询">技术咨询</option>
                <option value="运维服务">运维服务</option>
              </select>
            </div>

            <div class="form-group">
              <label for="projectState">项目状态</label>
              <select id="projectState" v-model="form.projectState">
                <option v-for="state in statusOptions" :key="state" :value="state">{{ state }}</option>
              </select>
            </div>

            <div class="form-group">
               <label for="softId">软件系统 <span class="required">*</span></label>
               <select id="softId" v-model="form.softId" required>
                 <option value="">请选择软件系统</option>
                 <option v-for="product in products" :key="product.softId" :value="product.softId">
                   {{ product.softName }} {{ product.softVersion ? `(${product.softVersion})` : '' }}
                 </option>
               </select>
             </div>

            <div class="form-group" style="position: relative;">
              <label for="customerId">客户 <span class="required">*</span></label>
              <input 
                type="text" 
                id="customerId" 
                v-model="customerSearchText" 
                @focus="showCustomerDropdown = true"
                @input="onCustomerInput"
                @blur="onCustomerBlur"
                placeholder="请输入或选择客户"
                autocomplete="off"
              />
              <ul v-if="showCustomerDropdown" class="dropdown-list">
                 <li v-for="c in filteredCustomers" :key="c.customerId" @mousedown.prevent="selectCustomer(c)">
                   {{ c.customerName }}
                 </li>
                 <li v-if="filteredCustomers.length === 0" class="no-result">无匹配客户</li>
              </ul>
            </div>

            <div class="form-group">
              <label for="projectLeader">项目负责人 <span class="required">*</span></label>
              <select id="projectLeader" v-model="form.projectLeader" required>
                <option value="">请选择项目负责人</option>
                <option v-for="user in users" :key="user.userId" :value="user.userId">
                  {{ user.name || user.userName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="saleLeader">销售负责人 <span class="required">*</span></label>
              <select id="saleLeader" v-model="form.saleLeader" required>
                <option value="">请选择销售负责人</option>
                <option v-for="user in users" :key="user.userId" :value="user.userId">
                  {{ user.name || user.userName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="startDate">开始日期 <span class="required">*</span></label>
              <input 
                type="date" 
                id="startDate" 
                v-model="form.startDate"
                required
              />
            </div>

            <div class="form-group">
              <label for="planEndDate">计划结束日期 <span class="required">*</span></label>
              <input 
                type="date" 
                id="planEndDate" 
                v-model="form.planEndDate"
                required
              />
            </div>

            <div class="form-group">
              <label for="actualEndDate">实际结束日期</label>
              <input 
                type="date" 
                id="actualEndDate" 
                v-model="form.actualEndDate"
              />
            </div>

            <div class="form-group">
              <label for="acceptanceDate">项目验收日期</label>
              <input 
                type="date" 
                id="acceptanceDate" 
                v-model="form.acceptanceDate"
              />
            </div>

            <div class="form-group">
              <label for="planPeriod">项目预计工期（天）</label>
              <input 
                type="number" 
                id="planPeriod" 
                v-model="form.planPeriod" 
                min="1"
                placeholder="自动计算"
                readonly
              />
            </div>

            <div class="form-group">
              <label for="actualPeriod">项目实际工期（天）</label>
              <input 
                type="number" 
                id="actualPeriod" 
                v-model="form.actualPeriod" 
                min="1"
                placeholder="自动计算"
                readonly
              />
            </div>

            <div class="form-group">
              <label for="value">项目金额</label>
              <input 
                type="number" 
                id="value" 
                v-model="form.value" 
                step="0.01"
                min="0"
                placeholder="请输入项目金额"
              />
            </div>

            <div class="form-group">
              <label for="receivedMoney">已回款金额</label>
              <input 
                type="number" 
                id="receivedMoney" 
                v-model="form.receivedMoney" 
                step="0.01"
                min="0"
                placeholder="请输入已回款金额"
              />
            </div>

            <div class="form-group">
              <label for="unreceiveMoney">未回款金额</label>
              <input 
                type="number" 
                id="unreceiveMoney" 
                v-model="form.unreceiveMoney" 
                step="0.01"
                min="0"
                placeholder="自动计算"
                readonly
              />
            </div>

            <div class="form-group">
              <label for="isAgent">是否渠道项目</label>
              <select id="isAgent" v-model="form.isAgent">
                <option :value="0">否</option>
                <option :value="1">是</option>
              </select>
            </div>

            <div class="form-group" v-if="form.isAgent === 1" style="position: relative;">
              <label for="channelId">渠道名称 <span class="required">*</span></label>
              <input 
                type="text"
                id="channelId" 
                v-model="channelSearchText" 
                @focus="showChannelDropdown = true"
                @input="onChannelInput"
                @blur="onChannelBlur"
                placeholder="请输入或选择渠道名称"
                autocomplete="off"
              />
              <ul v-if="showChannelDropdown" class="dropdown-list">
                 <li v-for="c in filteredChannels" :key="c.channelId" @mousedown.prevent="selectChannel(c)">
                   {{ c.channelName }}
                 </li>
                 <li v-if="filteredChannels.length === 0" class="no-result">无匹配渠道</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 项目建设内容分组 -->
        <div class="form-section">
          <h4 class="section-title">项目建设内容</h4>
          <div class="construction-content">
            <p class="section-description">请选择项目主要建设内容：</p>
            <div class="checkbox-grid">
              <div class="checkbox-item">
                <input 
                  type="checkbox" 
                  id="standardProduct" 
                  v-model="constructionContent.standardProduct"
                />
                <label for="standardProduct">标准产品</label>
              </div>
              
              <div class="checkbox-item">
                <input 
                  type="checkbox" 
                  id="interfaceDevelopment" 
                  v-model="constructionContent.interfaceDevelopment"
                />
                <label for="interfaceDevelopment">接口开发</label>
              </div>
              
              <div class="checkbox-item">
                <input 
                  type="checkbox" 
                  id="dataMigration" 
                  v-model="constructionContent.dataMigration"
                />
                <label for="dataMigration">数据迁移</label>
              </div>
              
              <div class="checkbox-item">
                <input 
                  type="checkbox" 
                  id="customDevelopment" 
                  v-model="constructionContent.customDevelopment"
                />
                <label for="customDevelopment">个性化功能开发</label>
              </div>

              <div class="checkbox-item">
                <input
                  type="checkbox"
                  id="userTraining"
                  v-model="constructionContent.userTraining"
                />
                <label for="userTraining">用户培训</label>
              </div>

              <div class="checkbox-item">
                <input
                  type="checkbox"
                  id="systemTrialRun"
                  v-model="constructionContent.systemTrialRun"
                />
                <label for="systemTrialRun">系统上线试运行</label>
              </div>
            </div>
          </div>
        </div>

        </form>
      </div>
      
      <!-- 固定按钮区域 -->
      <div class="modal-footer">
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting" @click="submitForm">
            {{ isSubmitting ? '创建中...' : '创建项目' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getAllProducts } from '@/api/product'
import { getAllCustomers } from '@/api/customer'
import { getAllUsers } from '@/api/user'
import { getAllChannelDistributors } from '@/api/channelDistributor'

export default {
  name: 'ProjectCreateForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    /**
     * 函数级注释：项目状态下拉选项（新建不含“已完成”）
     */
    statusOptions() {
      return ['待开始', '进行中', '已暂停']
    },
    filteredCustomers() {
      if (!this.customerSearchText) return this.customers
      const lower = this.customerSearchText.toLowerCase()
      return this.customers.filter(c => c.customerName && c.customerName.toLowerCase().includes(lower))
    },
    filteredChannels() {
      if (!this.channelSearchText) return this.channels
      const lower = this.channelSearchText.toLowerCase()
      return this.channels.filter(c => c.channelName && c.channelName.toLowerCase().includes(lower))
    }
  },
  data() {
     return {
       isSubmitting: false,
       customerSearchText: '',
       showCustomerDropdown: false,
       
       channelSearchText: '',
       showChannelDropdown: false,
       customers: [],
       users: [],
       products: [],
       channels: [],
      form: {
        projectNum: '',
        year: new Date().getFullYear(),
        projectName: '',
        projectType: '',
        customerId: '',
        projectLeader: '',
        saleLeader: '',
        projectState: '待开始',
        softId: '',
        startDate: '',
        planEndDate: '',
        planPeriod: '',
        isAgent: 0,
        channelId: '',
        value: '',
        actualEndDate: '',
        actualPeriod: '',
        receivedMoney: '',
        unreceiveMoney: '',
        acceptanceDate: ''
      },
      constructionContent: {
        standardProduct: true,
        interfaceDevelopment: false,
        dataMigration: false,
        customDevelopment: false,
        userTraining: true,
        systemTrialRun: true
      }
    }
  },
  watch: {
     visible(newVal) {
       if (newVal) {
         this.loadCustomers()
         this.loadUsers()
         this.loadProducts()
         this.resetForm()
       }
     },
     // 监听开始日期和计划结束日期变化，自动计算预计工期
     'form.startDate'() {
       this.calculatePlanPeriod()
     },
     'form.planEndDate'() {
       this.calculatePlanPeriod()
     },
     // 监听开始日期和实际结束日期变化，自动计算实际工期
     'form.actualEndDate'() {
       this.calculateActualPeriod()
     },
     // 监听项目金额和已回款金额变化，自动计算未回款金额
     'form.value'() {
       this.calculateOutstandingAmount()
     },
     'form.receivedMoney'() {
       this.calculateOutstandingAmount()
     },
     // 监听是否渠道项目变化，当选择"否"时清空渠道名称
     'form.isAgent'(newVal) {
       if (newVal === 0) {
         this.form.channelId = ''
       }
     }
   },
  methods: {
    onCustomerInput() {
      this.form.customerId = ''
      const match = this.customers.find(c => c.customerName === this.customerSearchText)
      if (match) {
        this.form.customerId = match.customerId
      }
    },
    selectCustomer(customer) {
      this.form.customerId = customer.customerId
      this.customerSearchText = customer.customerName
      this.showCustomerDropdown = false
    },
    onCustomerBlur() {
      this.showCustomerDropdown = false
      if (!this.form.customerId && this.customerSearchText) {
        const match = this.customers.find(c => c.customerName === this.customerSearchText)
        if (match) {
          this.form.customerId = match.customerId
        }
      }
    },
    updateCustomerSearchText() {
      if (this.form.customerId && this.customers.length > 0) {
        const c = this.customers.find(x => x.customerId === this.form.customerId)
        if (c) {
          this.customerSearchText = c.customerName
        }
      } else if (!this.form.customerId) {
        this.customerSearchText = ''
      }
    },

    onChannelInput() {
      this.form.channelId = ''
      const match = this.channels.find(c => c.channelName === this.channelSearchText)
      if (match) {
        this.form.channelId = match.channelId
      }
    },
    selectChannel(channel) {
      this.form.channelId = channel.channelId
      this.channelSearchText = channel.channelName
      this.showChannelDropdown = false
    },
    onChannelBlur() {
      this.showChannelDropdown = false
      if (!this.form.channelId && this.channelSearchText) {
        const match = this.channels.find(c => c.channelName === this.channelSearchText)
        if (match) {
          this.form.channelId = match.channelId
        }
      }
    },
    updateChannelSearchText() {
      if (this.form.channelId && this.channels.length > 0) {
        const c = this.channels.find(x => x.channelId === this.form.channelId)
        if (c) {
          this.channelSearchText = c.channelName
        }
      } else if (!this.form.channelId) {
        this.channelSearchText = ''
      }
    },
    /**
     * 生成项目编号
     * 格式：MS-YYYYMMDDHHMMSS
     */
    generateProjectNumber() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      
      return `MS-${year}${month}${day}${hours}${minutes}${seconds}`
    },
    
    /**
     * 重置表单
     */
    resetForm() {
      this.form = {
        projectNum: this.generateProjectNumber(),
        year: new Date().getFullYear(),
        projectName: '',
        projectType: '',
        customerId: '',
        projectLeader: '',
        saleLeader: '',
        projectState: '待开始',
        softId: '',
        startDate: '',
        planEndDate: '',
        planPeriod: '',
        isAgent: 0,
        channelId: '',
        value: '',
        actualEndDate: '',
        actualPeriod: '',
        receivedMoney: '',
        unreceiveMoney: '',
        acceptanceDate: ''
      }
      // 新建模式默认将项目负责人设置为当前登录用户
      try {
        const raw = sessionStorage.getItem('userInfo')
        const info = raw ? JSON.parse(raw) : null
        const uid = info && (info.userId ?? info.id)
        if (uid != null) {
          this.form.projectLeader = Number(uid)
        }
      } catch (_) {}
      this.constructionContent = {
        standardProduct: true,
        interfaceDevelopment: false,
        dataMigration: false,
        customDevelopment: false,
        userTraining: true,
        systemTrialRun: true
      }
      this.updateCustomerSearchText()
      this.updateChannelSearchText()
    },

    /**
     * 加载客户列表
     */
    async loadCustomers() {
      try {
        this.customers = await getAllCustomers()
        this.updateCustomerSearchText()
      } catch (error) {
        console.error('加载客户列表失败:', error)
        this.customers = []
      }
    },

    /**
      * 加载用户列表
      */
     async loadUsers() {
       try {
         this.users = await getAllUsers()
       } catch (error) {
         console.error('加载用户列表失败:', error)
         this.users = []
       }
     },

     /**
      * 加载产品列表
      */
     async loadProducts() {
       try {
         this.products = await getAllProducts()
       } catch (error) {
         console.error('加载产品列表失败:', error)
       }
     },

     /**
      * 加载渠道商列表
      */
     async loadChannels() {
       try {
         this.channels = await getAllChannelDistributors()
         this.updateChannelSearchText()
       } catch (error) {
         console.error('加载渠道商列表失败:', error)
         this.channels = []
         this.updateChannelSearchText()
       }
     },

    /**
     * 验证表单
     */
    validateForm() {
      const errors = []
      
      // 必填字段验证
      if (!this.form.projectNum.trim()) {
        errors.push('项目编号不能为空')
      }
      
      if (!this.form.projectName.trim()) {
        errors.push('项目名称不能为空')
      }
      
      if (!this.form.projectType.trim()) {
        errors.push('项目类型不能为空')
      }
      
      if (!this.form.customerId) {
        errors.push('客户名称不能为空')
      }
      
      if (!this.form.projectLeader) {
        errors.push('项目负责人不能为空')
      }
      
      if (!this.form.saleLeader) {
        errors.push('销售负责人不能为空')
      }
      
      if (!this.form.startDate) {
        errors.push('开始日期不能为空')
      }
      
      if (!this.form.planEndDate) {
        errors.push('计划结束日期不能为空')
      }
      
      // 项目金额为非必填字段，但如果填写了则必须大于0
      if (this.form.value && this.form.value <= 0) {
        errors.push('项目金额必须大于0')
      }
      
      // 渠道项目条件验证
      if (this.form.isAgent === 1 && !this.form.channelId) {
        errors.push('渠道项目必须选择渠道名称')
      }
      
      // 日期逻辑验证
      if (this.form.startDate && this.form.planEndDate) {
        const startDate = new Date(this.form.startDate)
        const planEndDate = new Date(this.form.planEndDate)
        if (startDate >= planEndDate) {
          errors.push('计划结束日期必须晚于开始日期')
        }
      }
      
      if (this.form.startDate && this.form.actualEndDate) {
        const startDate = new Date(this.form.startDate)
        const actualEndDate = new Date(this.form.actualEndDate)
        if (startDate >= actualEndDate) {
          errors.push('实际结束日期必须晚于开始日期')
        }
      }
      
      return errors
    },

    /**
     * 格式化建设内容为字符串
     */
    formatConstructionContent() {
      const selectedContent = []
      
      if (this.constructionContent.standardProduct) {
        selectedContent.push('标准产品')
      }
      if (this.constructionContent.interfaceDevelopment) {
        selectedContent.push('接口开发')
      }
      if (this.constructionContent.dataMigration) {
        selectedContent.push('数据迁移')
      }
      if (this.constructionContent.customDevelopment) {
        selectedContent.push('个性化功能开发')
      }
      if (this.constructionContent.userTraining) {
        selectedContent.push('用户培训')
      }
      if (this.constructionContent.systemTrialRun) {
        selectedContent.push('系统上线试运行')
      }
      
      return selectedContent.join('/')
    },

    /**
     * 提交表单
     */
    async submitForm() {
      // 表单验证
      const errors = this.validateForm()
      if (errors.length > 0) {
        alert('请修正以下错误：\n' + errors.join('\n'))
        return
      }
      
      this.isSubmitting = true
      try {
        // 格式化建设内容为字符串
        const constructContent = this.formatConstructionContent()
        
        // 合并基本信息和建设内容
        const projectData = {
          ...this.form,
          constructContent: constructContent
        }
        
        const API_BASE = __BACKEND_API_URL__ + '/api'
        const response = await axios.post(`${API_BASE}/constructing-projects`, projectData)
        
        if (response.data.success) {
          this.$emit('success', response.data.data)
          this.closeModal()
          alert('项目创建成功！')
        } else {
          alert(response.data.message || '创建失败')
        }
      } catch (error) {
        console.error('创建项目失败:', error)
        alert('创建失败，请稍后重试')
      } finally {
        this.isSubmitting = false
      }
    },

    /**
     * 计算项目预计工期
     */
    calculatePlanPeriod() {
      if (this.form.startDate && this.form.planEndDate) {
        const startDate = new Date(this.form.startDate)
        const endDate = new Date(this.form.planEndDate)
        
        if (endDate >= startDate) {
          // 计算天数差
          const timeDiff = endDate.getTime() - startDate.getTime()
          const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24))
          this.form.planPeriod = daysDiff
        } else {
          this.form.planPeriod = ''
        }
      } else {
        this.form.planPeriod = ''
      }
    },

    /**
     * 计算项目实际工期
     */
    calculateActualPeriod() {
      if (this.form.startDate && this.form.actualEndDate) {
        const startDate = new Date(this.form.startDate)
        const endDate = new Date(this.form.actualEndDate)
        
        if (endDate >= startDate) {
          // 计算天数差
          const timeDiff = endDate.getTime() - startDate.getTime()
          const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24))
          this.form.actualPeriod = daysDiff
        } else {
          this.form.actualPeriod = ''
        }
      } else {
        this.form.actualPeriod = ''
      }
    },

    /**
     * 计算未回款金额
     */
    calculateOutstandingAmount() {
      if (this.form.value && this.form.receivedMoney) {
        const projectValue = parseFloat(this.form.value) || 0
        const receivedAmount = parseFloat(this.form.receivedMoney) || 0
        this.form.unreceiveMoney = Math.max(0, projectValue - receivedAmount)
      } else if (this.form.value) {
        this.form.unreceiveMoney = parseFloat(this.form.value) || 0
      } else {
        this.form.unreceiveMoney = ''
      }
    },

    /**
     * 关闭弹窗
     */
    closeModal() {
      this.$emit('close')
    }
  },
  mounted() {
    this.loadCustomers()
    this.loadUsers()
    this.loadProducts()
    this.loadChannels()
  }
}
</script>

<style scoped>
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
  border-radius: 12px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  border-radius: 12px 12px 0 0;
  flex-shrink: 0;
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
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #666;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.modal-footer {
  flex-shrink: 0;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
  border-radius: 0 0 12px 12px;
}

.project-form {
  padding: 24px;
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #1890ff;
  display: inline-block;
}

.form-subsection {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.form-subsection:last-child {
  margin-bottom: 0;
}

.subsection-title {
  margin: 0 0 16px 0;
  color: #495057;
  font-size: 14px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
  position: relative;
}

.subsection-title::before {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 30px;
  height: 1px;
  background: #1890ff;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 500;
  margin-bottom: 6px;
  color: #262626;
  font-size: 14px;
}

.required {
  color: #ff4d4f;
}

.form-group input,
.form-group select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-group input:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.readonly-field {
  background-color: #f8f9fa !important;
  color: #6c757d !important;
  border-color: #e9ecef !important;
  cursor: not-allowed !important;
}

.readonly-field:focus {
  border-color: #e9ecef !important;
  box-shadow: none !important;
}
.construction-content {
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.section-description {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 14px;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.checkbox-item label {
  cursor: pointer;
  font-size: 14px;
  color: #262626;
  margin: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin: 0;
  padding: 0;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 80px;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-primary:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .checkbox-grid {
    grid-template-columns: 1fr;
  }

  .btn {
    width: 100%;
  }
}

.dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  margin-top: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  list-style: none;
  padding: 0;
}
.dropdown-list li {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
}
.dropdown-list li:hover {
  background-color: #f5f5f5;
}
.no-result {
  color: #999;
  cursor: default;
  text-align: center;
  padding: 12px !important;
}
</style>
