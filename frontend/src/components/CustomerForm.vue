<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ mode === 'add' ? '新增客户' : '修改客户' }}</h3>
        <button class="close-btn" @click="close">
          <span class="icon-close">✕</span>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="customer-form">
        <div class="form-row">
          <div class="form-group">
            <label for="customerName" class="required">客户名称</label>
            <input
              id="customerName"
              v-model="formData.customerName"
              type="text"
              class="form-input"
              placeholder="请输入客户名称"
              :class="{ error: errors.customerName }"
              @input="onCustomerNameInput"
              @blur="onCustomerNameBlur"
              maxlength="100"
            />
            <span v-if="errors.customerName" class="error-message">
              {{ errors.customerName }}
            </span>
          </div>

          <div class="form-group">
            <label for="contact" class="required">联系人</label>
            <input
              id="contact"
              v-model="formData.contact"
              type="text"
              class="form-input"
              placeholder="请输入联系人姓名"
              :class="{ error: errors.contact }"
              maxlength="50"
            />
            <span v-if="errors.contact" class="error-message">
              {{ errors.contact }}
            </span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="phoneNumber" class="required">联系方式</label>
            <input
              id="phoneNumber"
              v-model="formData.phoneNumber"
              type="tel"
              class="form-input"
              placeholder="请输入手机号码或电话号码"
              :class="{ error: errors.phoneNumber }"
              maxlength="20"
            />
            <span v-if="errors.phoneNumber" class="error-message">
              {{ errors.phoneNumber }}
            </span>
          </div>

          <div class="form-group">
            <label for="province" class="required">省份</label>
            <select
              id="province"
              v-model="formData.province"
              class="form-select"
              :class="{ error: errors.province }"
            >
              <option value="">请选择省份</option>
              <option v-for="province in provinces" :key="province" :value="province">
                {{ province }}
              </option>
            </select>
            <span v-if="errors.province" class="error-message">
              {{ errors.province }}
            </span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="saleLeader" class="required">销售负责人</label>
            <select
              id="saleLeader"
              v-model="formData.saleLeader"
              class="form-select"
              :class="{ error: errors.saleLeader }"
            >
              <option :value="null">请选择销售负责人</option>
              <option v-for="user in userList" :key="user.userId" :value="user.userId">
                {{ user.name }}
              </option>
            </select>
            <span v-if="errors.saleLeader" class="error-message">
              {{ errors.saleLeader }}
            </span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group full-width">
            <label for="customerRank" class="required">客户等级</label>
            <div class="radio-group">
              <label class="radio-item">
                <input
                  type="radio"
                  v-model="formData.customerRank"
                  value="战略客户"
                  name="customerRank"
                />
                <span class="radio-label">战略客户</span>
                <span class="radio-desc">长期合作的重要客户</span>
              </label>
              <label class="radio-item">
                <input
                  type="radio"
                  v-model="formData.customerRank"
                  value="重要客户"
                  name="customerRank"
                />
                <span class="radio-label">重要客户</span>
                <span class="radio-desc">业务量较大的客户</span>
              </label>
              <label class="radio-item">
                <input
                  type="radio"
                  v-model="formData.customerRank"
                  value="一般客户"
                  name="customerRank"
                />
                <span class="radio-label">一般客户</span>
                <span class="radio-desc">普通合作客户</span>
              </label>
            </div>
            <span v-if="errors.customerRank" class="error-message">
              {{ errors.customerRank }}
            </span>
          </div>
        </div>

        <!-- 如果是编辑模式，显示创建时间 -->
        <div v-if="mode === 'edit' && formData.createTime" class="form-row">
          <div class="form-group">
            <label>创建时间</label>
            <input
              type="text"
              class="form-input"
              :value="formatDate(formData.createTime)"
              readonly
              disabled
            />
          </div>
          <div class="form-group">
            <label>客户ID</label>
            <input
              type="text"
              class="form-input"
              :value="formData.customerId"
              readonly
              disabled
            />
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="close">
            取消
          </button>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            <span v-if="isSubmitting" class="loading-spinner">⏳</span>
            {{ isSubmitting ? '保存中...' : (mode === 'add' ? '新增客户' : '保存修改') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { checkCustomerNameAvailable } from '@/api/customer.js'
import { getAllUsers } from '@/api/user.js'

export default {
  name: 'CustomerForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    customer: {
      type: Object,
      default: null
    },
    mode: {
      type: String,
      default: 'add', // 'add' 或 'edit'
      validator: value => ['add', 'edit'].includes(value)
    }
  },
  emits: ['close', 'save'],
  data() {
    return {
      // 表单数据
      formData: {
        customerId: null,
        customerName: '',
        contact: '',
        phoneNumber: '',
        province: '',
        customerRank: '',
        createTime: null,
        saleLeader: null
      },
      
      // 用户列表
      userList: [],

      // 表单验证错误
      errors: {},
      
      // 提交状态
      isSubmitting: false,

      // 客户名称异步校验相关
      nameCheckTimer: null,
      nameChecking: false,
      lastCheckedNameAvailable: true,
      
      // 省份列表
      provinces: [
        '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
        '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
        '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
        '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆', '台湾',
        '香港', '澳门'
      ]
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.initForm()
      }
    },
    customer: {
      handler() {
        if (this.visible) {
          this.initForm()
        }
      },
      deep: true
    }
  },
  mounted() {
    if (this.visible) {
      this.initForm()
    }
    this.fetchUserList()
  },
  methods: {
    /**
     * 获取用户列表
     */
    async fetchUserList() {
      try {
        this.userList = await getAllUsers()
      } catch (error) {
        console.error('获取用户列表失败', error)
      }
    },

    /**
     * 初始化表单数据
     */
    initForm() {
      if (this.mode === 'edit' && this.customer) {
        // 编辑模式，填充现有数据
        this.formData = {
          customerId: this.customer.customerId,
          customerName: this.customer.customerName || '',
          contact: this.customer.contact || '',
          phoneNumber: this.customer.phoneNumber || '',
          province: this.customer.province || '',
          customerRank: this.customer.customerRank || '',
          createTime: this.customer.createTime,
          saleLeader: this.customer.saleLeader || null
        }
      } else {
        // 新增模式，重置表单
        this.formData = {
          customerId: null,
          customerName: '',
          contact: '',
          phoneNumber: '',
          province: '',
          customerRank: '',
          createTime: null,
          saleLeader: null
        }
      }
      
      // 清空错误信息
      this.errors = {}
    },

    /**
     * 表单验证
     */
    async validateForm() {
      const errors = {}

      // 客户名称验证
      if (!this.formData.customerName.trim()) {
        errors.customerName = '请输入客户名称'
      } else if (this.formData.customerName.trim().length < 2) {
        errors.customerName = '客户名称至少2个字符'
      } else if (this.formData.customerName.trim().length > 100) {
        errors.customerName = '客户名称不能超过100个字符'
      }

      // 联系人验证
      if (!this.formData.contact.trim()) {
        errors.contact = '请输入联系人姓名'
      } else if (this.formData.contact.trim().length < 2) {
        errors.contact = '联系人姓名至少2个字符'
      } else if (this.formData.contact.trim().length > 50) {
        errors.contact = '联系人姓名不能超过50个字符'
      }

      // 联系方式验证
      if (!this.formData.phoneNumber.trim()) {
        errors.phoneNumber = '请输入联系方式'
      } else {
        const phonePattern = /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$|^400-?\d{3}-?\d{4}$/
        if (!phonePattern.test(this.formData.phoneNumber.trim())) {
          errors.phoneNumber = '请输入正确的手机号码或电话号码'
        }
      }

      // 省份验证
      if (!this.formData.province) {
        errors.province = '请选择省份'
      }

      // 销售负责人验证
      if (!this.formData.saleLeader) {
        errors.saleLeader = '请选择销售负责人'
      }

      // 客户等级验证
      if (!this.formData.customerRank) {
        errors.customerRank = '请选择客户等级'
      }

      // 基础校验通过后做异步判重
      const baseValid = Object.keys(errors).length === 0
      if (baseValid) {
        try {
          this.nameChecking = true
          const available = await checkCustomerNameAvailable(
            this.formData.customerName.trim(),
            this.mode === 'edit' ? this.formData.customerId : null
          )
          this.lastCheckedNameAvailable = available
          if (!available) {
            errors.customerName = '客户名称已存在，请更换后再保存'
          }
        } catch (e) {
          // 网络异常不阻断提交，但不覆盖已有错误
        } finally {
          this.nameChecking = false
        }
      }

      this.errors = errors
      return Object.keys(errors).length === 0
    },

    onCustomerNameInput() {
      // 先做基础长度校验以即时反馈
      const name = this.formData.customerName || ''
      if (!name.trim()) {
        this.errors.customerName = '请输入客户名称'
      } else if (name.trim().length < 2) {
        this.errors.customerName = '客户名称至少2个字符'
      } else if (name.trim().length > 100) {
        this.errors.customerName = '客户名称不能超过100个字符'
      } else if (!this.nameChecking && this.errors.customerName && this.errors.customerName.includes('客户名称已存在')) {
        // 清除之前的重复提示，等待异步校验结果
        this.errors.customerName = ''
      }

      // 防抖异步判重
      if (this.nameCheckTimer) {
        clearTimeout(this.nameCheckTimer)
      }
      this.nameCheckTimer = setTimeout(() => {
        this.checkCustomerNameDuplicate()
      }, 300)
    },

    onCustomerNameBlur() {
      // 失焦即时判重
      this.checkCustomerNameDuplicate(true)
    },

    async checkCustomerNameDuplicate(force = false) {
      const name = (this.formData.customerName || '').trim()
      if (!name || name.length < 2 || name.length > 100) {
        return
      }
      try {
        this.nameChecking = true
        const available = await checkCustomerNameAvailable(
          name,
          this.mode === 'edit' ? this.formData.customerId : null
        )
        this.lastCheckedNameAvailable = available
        if (!available) {
          this.errors.customerName = '客户名称已存在，请更换后再保存'
        } else if (force || (this.errors.customerName && this.errors.customerName.includes('客户名称已存在'))) {
          // 清理重复提示
          this.errors.customerName = ''
        }
      } catch (e) {
        // 忽略网络错误
      } finally {
        this.nameChecking = false
      }
    },

    /**
     * 处理表单提交
     */
    async handleSubmit() {
      if (!(await this.validateForm())) {
        return
      }

      this.isSubmitting = true

      try {
        // 准备提交数据
        const submitData = {
          customerName: this.formData.customerName.trim(),
          contact: this.formData.contact.trim(),
          phoneNumber: this.formData.phoneNumber.trim(),
          province: this.formData.province,
          customerRank: this.formData.customerRank,
          saleLeader: this.formData.saleLeader
        }

        // 如果是编辑模式，添加ID
        if (this.mode === 'edit') {
          submitData.customerId = this.formData.customerId
        }

        // 触发保存事件
        this.$emit('save', submitData)

      } catch (error) {
        console.error('表单提交失败:', error)
        this.$message?.error('保存失败，请重试')
      } finally {
        this.isSubmitting = false
      }
    },

    /**
     * 关闭表单
     */
    close() {
      this.$emit('close')
    },

    /**
     * 处理遮罩层点击
     */
    handleOverlayClick() {
      this.close()
    },

    /**
     * 格式化日期
     */
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString()
    }
  }
}
</script>

<style scoped>
/* 模态框遮罩 */
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
  padding: 20px;
}

/* 模态框内容 */
.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
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
  font-size: 18px;
  color: #8c8c8c;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

/* 表单样式 */
.customer-form {
  padding: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  min-width: 0;
}

.form-group.full-width {
  flex: none;
  width: 100%;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.form-group label.required::after {
  content: " *";
  color: #ff4d4f;
}

/* 输入框样式 */
.form-input,
.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-input.error,
.form-select.error {
  border-color: #ff4d4f;
}

.form-input:disabled {
  background: #f5f5f5;
  color: #8c8c8c;
  cursor: not-allowed;
}

/* 单选按钮组 */
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.radio-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.radio-item:hover {
  border-color: #1890ff;
  background: #f6f8ff;
}

.radio-item input[type="radio"] {
  margin: 0;
  margin-top: 2px;
}

.radio-item input[type="radio"]:checked + .radio-label {
  color: #1890ff;
  font-weight: 500;
}

.radio-label {
  font-weight: 500;
  color: #262626;
  min-width: 80px;
}

.radio-desc {
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.4;
}

/* 错误信息 */
.error-message {
  display: block;
  margin-top: 4px;
  color: #ff4d4f;
  font-size: 12px;
  line-height: 1.4;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #262626;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 80px;
  justify-content: center;
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

.btn-primary:hover:not(:disabled) {
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

/* 加载动画 */
.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 10px;
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .customer-form {
    padding: 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
  
  .radio-group {
    gap: 8px;
  }
  
  .radio-item {
    padding: 8px;
  }
  
  .form-actions {
    flex-direction: column-reverse;
    gap: 8px;
  }
  
  .btn {
    width: 100%;
  }
}

/* 小屏幕优化 */
@media (max-width: 480px) {
  .modal-header h3 {
    font-size: 16px;
  }
  
  .form-input,
  .form-select {
    font-size: 16px; /* 防止iOS缩放 */
  }
}
</style>