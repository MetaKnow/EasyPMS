<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ isViewMode ? '查看运维项目' : (isEdit ? '编辑运维项目' : '创建运维项目') }}</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="submitForm" class="project-form">
          <div class="form-grid">
            <!-- 只读项目编号 -->
          <div class="form-group full-width">
            <label for="projectNum">项目编号 <span class="required">*</span></label>
            <input
              type="text"
              id="projectNum"
              v-model="form.projectNum"
              readonly
              placeholder="系统自动生成"
            />
          </div>

          <!-- 基本信息 -->
          <div class="form-group full-width">
            <label for="projectName">项目名称 <span class="required">*</span></label>
            <input 
              type="text" 
              id="projectName" 
              v-model="form.projectName" 
              required 
              :disabled="isViewMode"
              placeholder="请输入项目名称"
            />
          </div>

          <div class="form-group full-width" style="position: relative;">
            <label for="customerId">客户名称</label>
            <input 
              type="text" 
              id="customerId" 
              v-model="customerSearchText" 
              @focus="showCustomerDropdown = true"
              @input="onCustomerInput"
              @blur="onCustomerBlur"
              placeholder="请输入或选择客户"
              :disabled="isViewMode"
              autocomplete="off"
            />
            <ul v-if="showCustomerDropdown" class="dropdown-list">
               <li v-for="c in filteredCustomers" :key="c.customerId" @mousedown.prevent="selectCustomer(c)">
                 {{ c.customerName }}
               </li>
               <li v-if="filteredCustomers.length === 0" class="no-result">无匹配客户</li>
            </ul>
          </div>

          <div class="form-group full-width">
            <label for="arcSystem">档案系统 <span class="required">*</span></label>
            <select id="arcSystem" v-model="form.arcSystem" required :disabled="isViewMode">
              <option value="">请选择档案系统</option>
              <option v-for="product in products" :key="product.softId" :value="product.softName + (product.softVersion ? ' (' + product.softVersion + ')' : '')">
                {{ product.softName }} {{ product.softVersion ? `(${product.softVersion})` : '' }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="director">销售负责人 <span class="required">*</span></label>
            <select id="director" v-model="form.saleDirector" required :disabled="isViewMode">
              <option value="">请选择销售负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.name || user.userName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceDirector">运维负责人 <span class="required">*</span></label>
            <select id="serviceDirector" v-model="form.serviceDirector" required :disabled="isViewMode">
              <option value="">请选择运维负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.name || user.userName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceType">运维类型 <span class="required">*</span></label>
            <select id="serviceType" v-model="form.serviceType" required :disabled="isViewMode">
              <option value="">请选择运维类型</option>
              <option value="我公司全权运维">我公司全权运维</option>
              <option value="我公司配合远程运维">我公司配合远程运维</option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceState">运维状态 <span class="required">*</span></label>
            <select id="serviceState" v-model="form.serviceState" required :disabled="isViewMode">
              <option value="">请选择运维状态</option>
              <option value="免费运维期">免费运维期</option>
              <option value="付费运维">付费运维</option>
              <option value="无付费运维">无付费运维</option>
              <option value="暂停运维">暂停运维</option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceYear">运维年数 <span class="required">*</span></label>
            <input 
              type="number" 
              id="serviceYear" 
              v-model="form.serviceYear" 
              min="1"
              max="10"
              required
              :disabled="isViewMode"
              placeholder="请输入运维年数"
            />
          </div>

          <div class="form-group">
            <label for="totalHours">总工时</label>
            <input 
              type="number" 
              id="totalHours" 
              v-model="form.totalHours" 
              step="0.5"
              min="0"
              :disabled="true"
              readonly
              placeholder="请输入总工时"
            />
          </div>

          <!-- 日期信息 -->
          <div class="form-group">
            <label for="startDate">开始日期 <span class="required">*</span></label>
            <input 
              type="date" 
              id="startDate" 
              v-model="form.startDate"
              required
              :disabled="isViewMode"
            />
          </div>

          <div class="form-group">
            <label for="endDate">结束日期 <span class="required">*</span></label>
            <input 
              type="date" 
              id="endDate" 
              v-model="form.endDate"
              required
              :disabled="isViewMode"
            />
          </div>
        </div>
      </form>
    </div>
    
    <div class="modal-footer">
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="closeModal">{{ isViewMode ? '关闭' : '取消' }}</button>
        <button v-if="!isViewMode" type="button" class="btn btn-primary" @click="submitForm" :disabled="isSubmitting">
          {{ isSubmitting ? '提交中...' : '确定' }}
        </button>
      </div>
    </div>
  </div>
  </div>
</template>

<script>
import axios from 'axios'
import { createAfterserviceProject, updateAfterserviceProject } from '../api/afterserviceProject'

export default {
  name: 'AfterserviceProjectForm',
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    projectData: {
      type: Object,
      default: null
    },
    isViewMode: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // eslint-disable-next-line no-undef
      API_BASE: __BACKEND_API_URL__ + '/api',
      isSubmitting: false,
      customerSearchText: '',
      showCustomerDropdown: false,
      users: [],
      customers: [],
      products: [],
      form: {
        projectNum: '',
        projectName: '',
        customerId: '',
        arcSystem: '',
        saleDirector: '',
        serviceYear: '',
        startDate: '',
        endDate: '',
        serviceState: '',
        totalHours: '',
        serviceType: '',
        serviceDirector: ''
      }
    }
  },
  computed: {
    /**
     * 是否为编辑模式
     */
    isEdit() {
      return this.projectData && this.projectData.projectId
    },
    filteredCustomers() {
      if (!this.customerSearchText) return this.customers
      const lower = this.customerSearchText.toLowerCase()
      return this.customers.filter(c => c.customerName && c.customerName.toLowerCase().includes(lower))
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.loadFormData()
        this.loadUsers()
        this.loadCustomers()
        this.loadProducts()
        // 新建模式时获取一个新的项目编号用于展示
        if (!this.isEdit) {
          this.loadProjectNum()
        }
      }
    },
    projectData: {
      handler(newVal) {
        if (newVal) {
          this.loadFormData()
        }
      },
      deep: true
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
      // 如果失焦时没有ID，且当前文本能匹配到，尝试匹配
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
    /**
     * 加载表单数据
     */
    loadFormData() {
      if (this.projectData) {
        Object.keys(this.form).forEach(key => {
          if (this.projectData[key] !== undefined) {
            this.form[key] = this.projectData[key]
          }
        })
      } else {
        this.resetForm()
      }
      this.updateCustomerSearchText()
    },

    /**
     * 加载新项目编号（仅新建时）
     */
    async loadProjectNum() {
      try {
        const resp = await axios.get(`${this.API_BASE}/afterservice-projects/new-project-num`)
        const num = resp?.data?.data?.projectNum
        if (num) {
          this.form.projectNum = num
        }
      } catch (e) {
        console.error('加载项目编号失败:', e)
      }
    },

    /**
     * 重置表单
     */
    resetForm() {
      this.form = {
        projectNum: '',
        projectName: '',
        customerId: '',
        arcSystem: '',
        saleDirector: '',
        serviceYear: '',
        startDate: '',
        endDate: '',
        serviceState: '',
        totalHours: '',
        serviceType: '',
        serviceDirector: ''
      }
    },

    /**
     * 加载用户列表，并在新建时默认设置运维负责人为当前用户
     */
    async loadUsers() {
      try {
        const response = await axios.get(`${this.API_BASE}/users?size=1000`)
        if (response.data && response.data.users) {
          this.users = response.data.users
        }
        // 新建模式默认将运维负责人设置为当前登录用户
        try {
          const raw = sessionStorage.getItem('userInfo')
          const info = raw ? JSON.parse(raw) : null
          const uid = info && (info.userId ?? info.id)
          if (!this.isEdit && uid != null) {
            this.form.serviceDirector = Number(uid)
          }
        } catch (_) {}
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    },

    /**
     * 加载客户列表
     */
    async loadCustomers() {
      try {
        const response = await axios.get(`${this.API_BASE}/customers?size=1000`)
        if (response.data && response.data.customers) {
          this.customers = response.data.customers
          this.updateCustomerSearchText()
        }
      } catch (error) {
        console.error('加载客户列表失败:', error)
      }
    },

    /**
     * 加载产品列表
     */
    async loadProducts() {
      try {
        const response = await axios.get(`${this.API_BASE}/products?size=1000`)
        if (response.data && response.data.products) {
          this.products = response.data.products
        }
      } catch (error) {
        console.error('加载产品列表失败:', error)
      }
    },

    /**
     * 提交表单
     */
    async submitForm() {
      this.isSubmitting = true
      try {
        const url = this.isEdit 
          ? `${this.API_BASE}/afterservice-projects/${this.projectData.projectId}`
          : `${this.API_BASE}/afterservice-projects`
        
        const method = this.isEdit ? 'put' : 'post'
        // 函数级注释：提交时排除 totalHours，防止用户修改统计值
        const payload = { ...this.form }
        delete payload.totalHours
        const response = await axios[method](url, payload)
        
        if (response.data.success) {
          this.$emit('success', response.data.data)
          this.closeModal()
        } else {
          alert(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存运维项目失败:', error)
        alert('保存失败，请稍后重试')
      } finally {
        this.isSubmitting = false
      }
    },

    /**
     * 关闭模态框
     */
    closeModal() {
      this.$emit('close')
      this.resetForm()
    }
  }
}
</script>

<style scoped>
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.required {
  color: #ff4d4f;
}

.form-group input,
.form-group select {
  padding: 8px 11px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
  background: #fff;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-group input:hover,
.form-group select:hover {
  border-color: #40a9ff;
}

.form-group input:disabled,
.form-group select:disabled {
  color: #000000;
  -webkit-text-fill-color: #000000;
  background-color: #f5f5f5;
  cursor: default;
  opacity: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 6px 15px;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 400;
  transition: all 0.2s;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #1890ff;
  color: white;
  border-color: #1890ff;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
  border-color: #40a9ff;
}

.btn-primary:disabled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: rgba(0, 0, 0, 0.25);
  cursor: not-allowed;
}

.btn-secondary {
  background: #fff;
  color: #595959;
  border-color: #d9d9d9;
}

.btn-secondary:hover {
  color: #40a9ff;
  border-color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
    max-height: 95vh;
  }
  
  .project-form {
    padding: 16px;
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .form-actions {
    flex-direction: column-reverse;
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
