<template>
  <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ isEdit ? '重命名机构' : '新增机构' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="organName">机构名称 <span class="required">*</span></label>
            <input
              id="organName"
              v-model="formData.organName"
              type="text"
              class="form-control"
              :class="{ error: errors.organName }"
              placeholder="请输入机构名称"
              maxlength="200"
              @input="clearError('organName')"
            />
            <div v-if="errors.organName" class="error-message">
              {{ errors.organName }}
            </div>
          </div>

          <div v-if="!isEdit" class="form-group">
            <label for="parentOrgan">上级机构</label>
            <div class="parent-organ-info">
              <i class="icon-organ">🏢</i>
              <span>{{ parentOrganName || '无上级机构' }}</span>
            </div>
          </div>
        </form>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="close">
          取消
        </button>
        <button 
          type="button" 
          class="btn btn-primary" 
          @click="handleSubmit"
          :disabled="loading"
        >
          <span v-if="loading" class="loading-spinner">⟳</span>
          {{ isEdit ? '确认重命名' : '确认新增' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 机构对话框组件
 * 用于新增和编辑机构
 */
export default {
  name: 'OrganDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    organ: {
      type: Object,
      default: null
    },
    parentOrgan: {
      type: Object,
      default: null
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'submit'],
  data() {
    return {
      formData: {
        organName: ''
      },
      errors: {},
      loading: false
    }
  },
  computed: {
    /**
     * 父机构名称
     */
    parentOrganName() {
      return this.parentOrgan?.organName || ''
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.initForm()
      }
    }
  },
  methods: {
    /**
     * 初始化表单
     */
    initForm() {
      if (this.isEdit && this.organ) {
        this.formData.organName = this.organ.organName
      } else {
        this.formData.organName = ''
      }
      this.errors = {}
    },

    /**
     * 验证表单
     */
    validateForm() {
      this.errors = {}
      
      if (!this.formData.organName?.trim()) {
        this.errors.organName = '机构名称不能为空'
        return false
      }
      
      if (this.formData.organName.trim().length > 200) {
        this.errors.organName = '机构名称不能超过200个字符'
        return false
      }
      
      return true
    },

    /**
     * 清除字段错误
     */
    clearError(field) {
      if (this.errors[field]) {
        delete this.errors[field]
      }
    },

    /**
     * 处理表单提交
     */
    async handleSubmit() {
      if (!this.validateForm()) {
        return
      }
      
      this.loading = true
      try {
        const submitData = {
          organName: this.formData.organName.trim()
        }
        
        if (!this.isEdit && this.parentOrgan) {
          submitData.parentOrganId = this.parentOrgan.organId
        }
        
        this.$emit('submit', submitData)
      } catch (error) {
        console.error('提交表单失败:', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * 关闭对话框
     */
    close() {
      this.$emit('close')
    },

    /**
     * 处理遮罩层点击
     */
    handleOverlayClick() {
      this.close()
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
  border-radius: 8px;
  min-width: 400px;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
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
  color: #8c8c8c;
  cursor: pointer;
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
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 表单样式 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #262626;
}

.required {
  color: #ff4d4f;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-control.error {
  border-color: #ff4d4f;
}

.error-message {
  margin-top: 4px;
  font-size: 12px;
  color: #ff4d4f;
}

.parent-organ-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  color: #595959;
}

.icon-organ {
  font-size: 14px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #262626;
  font-size: 13px;
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
  opacity: 0.6;
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

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>