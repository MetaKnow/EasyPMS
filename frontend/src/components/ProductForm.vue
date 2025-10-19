<template>
  <div class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ mode === 'add' ? '新增产品' : '修改产品' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label for="softName">产品名称 <span class="required">*</span></label>
            <input
              id="softName"
              v-model="formData.softName"
              type="text"
              :class="['form-control', errors.softName ? 'is-invalid' : '']"
              placeholder="请输入产品名称"
              maxlength="200"
              @input="onSoftNameInput"
              @blur="onSoftNameBlur"
            />
            <div v-if="errors.softName" class="invalid-feedback">
              {{ errors.softName }}
            </div>
          </div>
          
          <div class="form-group">
            <label for="softVersion">产品版本 <span class="required">*</span></label>
            <input
              id="softVersion"
              v-model="formData.softVersion"
              type="text"
              :class="['form-control', errors.softVersion ? 'is-invalid' : '']"
              placeholder="请输入产品版本"
              maxlength="50"
              @input="onSoftVersionInput"
              @blur="onSoftVersionBlur"
            />
            <div v-if="errors.softVersion" class="invalid-feedback">
              {{ errors.softVersion }}
            </div>
          </div>
          
          <div class="form-group">
            <label for="softType">产品类型 <span class="required">*</span></label>
            <select
              id="softType"
              v-model="formData.softType"
              :class="['form-control', errors.softType ? 'is-invalid' : '']"
            >
              <option value="">请选择产品类型</option>
              <option value="标准产品">标准产品</option>
              <option value="综合档案馆产品体系">综合档案馆产品体系</option>
            </select>
            <div v-if="errors.softType" class="invalid-feedback">
              {{ errors.softType }}
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="close">
              取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { checkProductNameExists } from '../api/product.js'
export default {
  name: 'ProductForm',
  props: {
    /**
     * 是否显示表单
     */
    visible: {
      type: Boolean,
      default: false
    },
    
    /**
     * 产品数据
     */
    product: {
      type: Object,
      default: null
    },
    
    /**
     * 表单模式：add 或 edit
     */
    mode: {
      type: String,
      default: 'add',
      validator: value => ['add', 'edit'].includes(value)
    }
  },
  data() {
    return {
      /**
       * 表单数据
       */
      formData: {
        softId: null,
        softName: '',
        softVersion: '',
        softType: ''
      },
      
      /**
       * 表单验证错误
       */
      errors: {
        softName: '',
        softVersion: '',
        softType: ''
      },
      
      /**
       * 提交状态
       */
      submitting: false
    }
  },
  watch: {
    /**
     * 监听产品数据变化，更新表单
     */
    product: {
      handler(newProduct) {
        this.initializeFormData(newProduct)
      },
      immediate: true
    },
    
    /**
     * 监听表单显示状态变化
     */
    visible(newVisible) {
      if (newVisible) {
        // 表单显示时，重新初始化数据
        this.initializeFormData(this.product)
        // 清除之前的验证错误
        this.clearErrors()
      }
    }
  },
  methods: {
    /**
     * 关闭表单
     */
    close() {
      this.$emit('close')
    },
    
    /**
     * 初始化表单数据
     */
    initializeFormData(product) {
      if (product && this.mode === 'edit') {
        // 编辑模式：回显产品数据
        this.formData = {
          softId: product.softId || null,
          softName: product.softName || '',
          softVersion: product.softVersion || '',
          softType: product.softType || ''
        }
      } else {
        // 新增模式：重置表单
        this.resetForm()
      }
    },
    
    /**
     * 清除验证错误
     */
    clearErrors() {
      this.errors = {
        softName: '',
        softVersion: '',
        softType: ''
      }
    },
    
    /**
     * 重置表单
     */
    resetForm() {
      this.formData = {
        softId: null,
        softName: '',
        softVersion: '',
        softType: ''
      }
      this.clearErrors()
    },
    
    /**
     * 验证表单
     */
    async validateForm() {
      let isValid = true
      
      // 重置错误信息
      this.errors = {
        softName: '',
        softVersion: '',
        softType: ''
      }
      
      // 验证产品名称
      if (!this.formData.softName) {
        this.errors.softName = '产品名称不能为空'
        isValid = false
      } else if (this.formData.softName.length < 2) {
        this.errors.softName = '产品名称至少需要2个字符'
        isValid = false
      } else if (this.formData.softName.length > 200) {
        this.errors.softName = '产品名称不能超过200个字符'
        isValid = false
      } else {
        // 异步判重（提交前最终校验）
        try {
          const { exists } = await checkProductNameExists(
            this.formData.softName,
            this.formData.softVersion,
            this.mode === 'edit' ? this.formData.softId : undefined
          )
          if (exists) {
            this.errors.softName = '该产品名称与版本组合已存在'
            isValid = false
          }
        } catch (e) {
          // 网络异常时不阻塞提交，但记录错误
          console.error('产品名称判重校验失败:', e)
        }
      }
      
      // 验证产品版本
      if (!this.formData.softVersion) {
        this.errors.softVersion = '产品版本不能为空'
        isValid = false
      } else if (this.formData.softVersion.length > 50) {
        this.errors.softVersion = '产品版本不能超过50个字符'
        isValid = false
      }

      // 验证产品类型
      const allowedTypes = ['标准产品', '综合档案馆产品体系']
      if (!this.formData.softType) {
        this.errors.softType = '产品类型不能为空'
        isValid = false
      } else if (!allowedTypes.includes(this.formData.softType)) {
        this.errors.softType = '产品类型不合法'
        isValid = false
      }
      
      return isValid
    },
    
    /**
     * 提交表单
     */
    async submitForm() {
      // 验证表单
      if (!(await this.validateForm())) {
        return
      }
      
      this.submitting = true
      
      try {
        // 准备提交数据
        const submitData = {
          softName: this.formData.softName,
          softVersion: this.formData.softVersion,
          softType: this.formData.softType
        }
        
        // 如果是编辑模式，添加ID
        if (this.mode === 'edit' && this.formData.softId) {
          submitData.softId = this.formData.softId
        }
        
        // 触发保存事件
        this.$emit('save', submitData)
        
      } catch (error) {
        console.error('提交表单失败:', error)
        this.$message?.error('提交表单失败: ' + error.message)
      } finally {
        this.submitting = false
      }
    }
    ,
    /**
     * 输入时进行去抖的异步判重
     */
    async onSoftNameInput() {
      this.clearSoftCheckDebounce()
      this._softCheckDebounceTimer = setTimeout(async () => {
        await this.checkSoftNameVersionAsync()
      }, 300)
    },
    
    /**
     * 失焦时立即进行异步判重
     */
    async onSoftNameBlur() {
      this.clearSoftCheckDebounce()
      await this.checkSoftNameVersionAsync()
    },

    async onSoftVersionInput() {
      this.clearSoftCheckDebounce()
      this._softCheckDebounceTimer = setTimeout(async () => {
        await this.checkSoftNameVersionAsync()
      }, 300)
    },

    async onSoftVersionBlur() {
      this.clearSoftCheckDebounce()
      await this.checkSoftNameVersionAsync()
    },
    
    /**
     * 清理产品名称输入的去抖定时器
     */
    clearSoftCheckDebounce() {
      if (this._softCheckDebounceTimer) {
        clearTimeout(this._softCheckDebounceTimer)
        this._softCheckDebounceTimer = null
      }
    },
    
    /**
     * 异步检查产品名称是否重复并更新错误提示
     */
    async checkSoftNameVersionAsync() {
      const name = (this.formData.softName || '').trim()
      const version = (this.formData.softVersion || '').trim()
      if (!name || name.length < 2 || name.length > 200) {
        return
      }
      if (!version || version.length > 50) {
        return
      }
      try {
        const { exists } = await checkProductNameExists(
          name,
          version,
          this.mode === 'edit' ? this.formData.softId : undefined
        )
        if (exists) {
          this.errors.softName = '该产品名称与版本组合已存在'
        } else if (this.errors.softName === '该产品名称与版本组合已存在') {
          this.errors.softName = ''
        }
      } catch (e) {
        console.error('产品名称与版本异步检查失败:', e)
      }
    }
  }
}
</script>

<style scoped>
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
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #8c8c8c;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

.modal-body {
  padding: 20px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.required {
  color: #ff4d4f;
  margin-left: 2px;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-control.is-invalid {
  border-color: #ff4d4f;
}

.invalid-feedback {
  margin-top: 4px;
  font-size: 13px;
  color: #ff4d4f;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #262626;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 80px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .modal-body {
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .btn {
    width: 100%;
  }
}
</style>