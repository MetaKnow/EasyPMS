<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ mode === 'add' ? '新增渠道商' : '修改渠道商' }}</h3>
        <button class="close-btn" @click="close">
          <span class="icon-close">✕</span>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="channel-form">
        <div class="form-row">
          <div class="form-group">
            <label for="channelName" class="required">渠道名称</label>
            <input
              id="channelName"
              v-model="formData.channelName"
              type="text"
              class="form-input"
              placeholder="请输入渠道名称"
              :class="{ error: errors.channelName }"
              maxlength="100"
              @input="onChannelNameInput"
              @blur="onChannelNameBlur"
            />
            <span v-if="errors.channelName" class="error-message">
              {{ errors.channelName }}
            </span>
          </div>

          <div class="form-group">
            <label for="contactor" class="required">联系人</label>
            <input
              id="contactor"
              v-model="formData.contactor"
              type="text"
              class="form-input"
              placeholder="请输入联系人姓名"
              :class="{ error: errors.contactor }"
              maxlength="50"
            />
            <span v-if="errors.contactor" class="error-message">
              {{ errors.contactor }}
            </span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group full-width">
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
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="close">
            取消
          </button>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { createChannelDistributor, updateChannelDistributor, checkChannelNameAvailable } from '../api/channelDistributor.js'

export default {
  name: 'ChannelDistributorForm',
  props: {
    /**
     * 弹窗是否可见
     */
    visible: {
      type: Boolean,
      default: false
    },
    /**
     * 表单模式：'add' 新增，'edit' 编辑
     */
    mode: {
      type: String,
      default: 'add',
      validator: value => ['add', 'edit'].includes(value)
    },
    /**
     * 编辑时的渠道商数据
     */
    channelData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      /**
       * 表单数据
       */
      formData: {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      },
      /**
       * 表单验证错误信息
       */
      errors: {},
      /**
       * 是否正在提交
       */
      isSubmitting: false,
      // 判重防抖定时器
      channelNameCheckTimer: null
    }
  },
  watch: {
    /**
     * 监听弹窗显示状态变化
     */
    visible(newVal) {
      if (newVal) {
        this.initForm()
      } else {
        this.resetForm()
      }
    },
    /**
     * 监听渠道商数据变化
     */
    channelData: {
      handler() {
        if (this.visible) {
          this.initForm()
        }
      },
      deep: true
    }
  },
  methods: {
    /**
     * 输入时触发判重（防抖）
     */
    onChannelNameInput() {
      if (this.channelNameCheckTimer) {
        clearTimeout(this.channelNameCheckTimer)
      }
      this.channelNameCheckTimer = setTimeout(() => {
        this.checkChannelNameAsync(false)
      }, 400)
    },

    /**
     * 失焦立即判重
     */
    onChannelNameBlur() {
      this.checkChannelNameAsync(true)
    },

    /**
     * 初始化表单数据
     */
    initForm() {
      if (this.mode === 'edit' && this.channelData) {
        this.formData = {
          channelName: this.channelData.channelName || '',
          contactor: this.channelData.contactor || '',
          phoneNumber: this.channelData.phoneNumber || ''
        }
      } else {
        this.resetForm()
      }
      this.errors = {}
    },

    /**
     * 重置表单数据
     */
    resetForm() {
      this.formData = {
        channelName: '',
        contactor: '',
        phoneNumber: ''
      }
      this.errors = {}
      this.isSubmitting = false
    },

    /**
     * 验证表单数据
     */
    async validateForm() {
      this.errors = {}

      // 验证渠道名称
      if (!this.formData.channelName.trim()) {
        this.errors.channelName = '请输入渠道名称'
      } else if (this.formData.channelName.length > 100) {
        this.errors.channelName = '渠道名称不能超过100个字符'
      }

      // 本地校验通过后进行最终一次异步判重
      if (!this.errors.channelName) {
        const available = await checkChannelNameAvailable(
          this.formData.channelName.trim(),
          this.mode === 'edit' ? this.channelData.channelId : undefined
        )
        if (!available) {
          this.errors.channelName = '渠道名称已存在'
        }
      }

      // 验证联系人
      if (!this.formData.contactor.trim()) {
        this.errors.contactor = '请输入联系人'
      } else if (this.formData.contactor.length > 50) {
        this.errors.contactor = '联系人姓名不能超过50个字符'
      }

      // 验证联系方式
      if (!this.formData.phoneNumber.trim()) {
        this.errors.phoneNumber = '请输入联系方式'
      } else if (this.formData.phoneNumber.length > 20) {
        this.errors.phoneNumber = '联系方式不能超过20个字符'
      } else if (!/^[\d\-\+\(\)\s]+$/.test(this.formData.phoneNumber)) {
        this.errors.phoneNumber = '请输入有效的联系方式'
      }

      return Object.keys(this.errors).length === 0
    },

    /**
     * 异步判重核心逻辑
     */
    async checkChannelNameAsync(force = false) {
      const name = (this.formData.channelName || '').trim()
      if (!name) return

      // 若已有非重复类的错误，且非强制检查，则跳过远程判重
      if (this.errors.channelName && this.errors.channelName !== '渠道名称已存在' && !force) {
        return
      }

      const available = await checkChannelNameAvailable(
        name,
        this.mode === 'edit' ? this.channelData.channelId : undefined
      )
      if (!available) {
        this.errors.channelName = '渠道名称已存在'
      } else if (this.errors.channelName === '渠道名称已存在') {
        delete this.errors.channelName
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
        if (this.mode === 'add') {
          await createChannelDistributor(this.formData)
          this.$emit('success', '渠道商创建成功')
        } else {
          await updateChannelDistributor(this.channelData.channelId, this.formData)
          this.$emit('success', '渠道商更新成功')
        }
        this.close()
      } catch (error) {
        console.error('保存渠道商失败:', error)
        this.$emit('error', error.message || '保存失败，请重试')
      } finally {
        this.isSubmitting = false
      }
    },

    /**
     * 关闭弹窗
     */
    close() {
      this.$emit('close')
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
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 模态框内容 */
.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
  border-radius: 8px 8px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #f3f4f6;
  color: #374151;
}

/* 表单样式 */
.channel-form {
  padding: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  flex: 1 1 100%;
}

.form-group label {
  font-weight: 500;
  margin-bottom: 6px;
  color: #374151;
  font-size: 14px;
}

.form-group label.required::after {
  content: ' *';
  color: #ef4444;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #ef4444;
}

.error-message {
  color: #ef4444;
  font-size: 12px;
  margin-top: 4px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 80px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .btn {
    width: 100%;
  }
}
</style>