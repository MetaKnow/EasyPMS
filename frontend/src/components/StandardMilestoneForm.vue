<template>
  <div class="modal-overlay">
    <div class="modal-content" ref="dragModal" :style="dragStyle" @click.stop>
      <div class="modal-header" @mousedown="startDrag">
        <h3>{{ mode === 'add' ? '新增里程碑' : '修改里程碑' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label for="milestoneName">里程碑名称 <span class="required">*</span></label>
            <input
              id="milestoneName"
              v-model="formData.milestoneName"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errors.milestoneName }"
              placeholder="请输入里程碑名称"
              maxlength="100"
              @input="onMilestoneNameInput"
              @blur="onMilestoneNameBlur"
            />
            <div v-if="errors.milestoneName" class="invalid-feedback">
              {{ errors.milestoneName }}
            </div>
          </div>
        </form>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="close">
          取消
        </button>
        <button type="button" class="btn btn-primary" @click="submitForm" :disabled="isSubmitting">
          {{ isSubmitting ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { checkMilestoneNameExists } from '../api/standardMilestone.js'

/**
 * 标准里程碑表单组件
 * 用于新增和编辑标准里程碑
 */
export default {
  name: 'StandardMilestoneForm',
  props: {
    /**
     * 表单显示状态
     */
    visible: {
      type: Boolean,
      default: false
    },
    
    /**
     * 里程碑数据（编辑模式时传入）
     */
    milestone: {
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
        milestoneId: null,
        milestoneName: ''
      },
      
      /**
       * 验证错误信息
       */
      errors: {
        milestoneName: ''
      },
      
      /**
       * 提交状态
       */
      isSubmitting: false,
      
      /**
       * 防抖定时器
       */
      debounceTimer: null,
      // 拖动状态
      dragStyle: { position: 'fixed', top: '0px', left: '0px' },
      dragging: false,
      dragStart: { mouseX: 0, mouseY: 0, top: 0, left: 0 }
    }
  },
  watch: {
    /**
     * 监听里程碑数据变化，更新表单
     */
    milestone: {
      handler(newMilestone) {
        this.initializeFormData(newMilestone)
      },
      immediate: true
    },
    
    /**
     * 监听表单显示状态变化
     */
    visible(newVisible) {
      if (newVisible) {
        // 表单显示时，重新初始化数据
        this.initializeFormData(this.milestone)
        // 清除之前的验证错误
        this.clearErrors()
        this.$nextTick(() => this.centerModal())
      }
    }
  },
  mounted() {
    this.$nextTick(() => this.centerModal())
  },
  methods: {
    /**
     * 关闭表单
     */
    close() {
      this.endDrag()
      this.$emit('close')
    },
    
    // 弹窗居中
    centerModal() {
      const modal = this.$refs.dragModal
      if (!modal) return
      const w = modal.offsetWidth
      const h = modal.offsetHeight
      const left = Math.max((window.innerWidth - w) / 2, 8)
      const top = Math.max((window.innerHeight - h) / 2, 20)
      this.dragStyle = { position: 'fixed', top: `${top}px`, left: `${left}px` }
    },
    
    // 开始拖动
    startDrag(e) {
      if (e.button !== 0) return
      const modal = this.$refs.dragModal
      if (!modal) return
      const top = parseInt((this.dragStyle.top || '0').toString().replace('px', '')) || 0
      const left = parseInt((this.dragStyle.left || '0').toString().replace('px', '')) || 0
      this.dragStart = { mouseX: e.clientX, mouseY: e.clientY, top, left }
      this.dragging = true
      document.addEventListener('mousemove', this.onDragMove)
      document.addEventListener('mouseup', this.endDrag)
    },
    
    // 拖动中
    onDragMove(e) {
      if (!this.dragging) return
      const modal = this.$refs.dragModal
      if (!modal) return
      const dx = e.clientX - this.dragStart.mouseX
      const dy = e.clientY - this.dragStart.mouseY
      const w = modal.offsetWidth
      const h = modal.offsetHeight
      let left = this.dragStart.left + dx
      let top = this.dragStart.top + dy
      const maxLeft = Math.max(window.innerWidth - w, 0)
      const maxTop = Math.max(window.innerHeight - h, 0)
      left = Math.min(Math.max(0, left), maxLeft)
      top = Math.min(Math.max(0, top), maxTop)
      this.dragStyle = { position: 'fixed', top: `${top}px`, left: `${left}px` }
    },
    
    // 结束拖动
    endDrag() {
      if (!this.dragging) return
      this.dragging = false
      document.removeEventListener('mousemove', this.onDragMove)
      document.removeEventListener('mouseup', this.endDrag)
    },
    
    /**
     * 初始化表单数据
     */
    initializeFormData(milestone) {
      if (milestone && this.mode === 'edit') {
        // 编辑模式：回显里程碑数据
        this.formData = {
          milestoneId: milestone.milestoneId || null,
          milestoneName: milestone.milestoneName || ''
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
        milestoneName: ''
      }
    },
    
    /**
     * 重置表单
     */
    resetForm() {
      this.formData = {
        milestoneId: null,
        milestoneName: ''
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
        milestoneName: ''
      }
      
      // 验证里程碑名称
      if (!this.formData.milestoneName || !this.formData.milestoneName.trim()) {
        this.errors.milestoneName = '请输入里程碑名称'
        isValid = false
      } else if (this.formData.milestoneName.trim().length > 100) {
        this.errors.milestoneName = '里程碑名称不能超过100个字符'
        isValid = false
      } else {
        // 检查里程碑名称是否重复
        try {
          const isDuplicate = await this.checkMilestoneNameDuplicate()
          if (isDuplicate) {
            this.errors.milestoneName = '里程碑名称已存在，请使用其他名称'
            isValid = false
          }
        } catch (error) {
          console.error('检查里程碑名称重复失败:', error)
          // 网络错误时不阻止提交，但给出提示
          this.errors.milestoneName = '无法验证里程碑名称，请检查网络连接'
          isValid = false
        }
      }
      
      return isValid
    },
    
    /**
     * 检查里程碑名称是否重复
     */
    async checkMilestoneNameDuplicate() {
      const milestoneName = this.formData.milestoneName.trim()
      if (!milestoneName) return false
      
      try {
        let response
        if (this.mode === 'edit' && this.formData.milestoneId) {
          // 编辑模式：排除当前里程碑ID
          response = await checkMilestoneNameExists(milestoneName, this.formData.milestoneId)
        } else {
          // 新增模式：检查所有里程碑
          response = await checkMilestoneNameExists(milestoneName)
        }
        
        return response.exists
      } catch (error) {
        console.error('检查里程碑名称重复失败:', error)
        throw error
      }
    },
    
    /**
     * 里程碑名称输入事件
     */
    onMilestoneNameInput() {
      // 清除当前错误
      this.errors.milestoneName = ''
      
      // 防抖检查重复
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      
      this.debounceTimer = setTimeout(async () => {
        if (this.formData.milestoneName && this.formData.milestoneName.trim()) {
          try {
            const isDuplicate = await this.checkMilestoneNameDuplicate()
            if (isDuplicate) {
              this.errors.milestoneName = '里程碑名称已存在，请使用其他名称'
            }
          } catch (error) {
            console.error('检查里程碑名称重复失败:', error)
          }
        }
      }, 500)
    },
    
    /**
     * 里程碑名称失焦事件
     */
    async onMilestoneNameBlur() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      
      if (this.formData.milestoneName && this.formData.milestoneName.trim()) {
        try {
          const isDuplicate = await this.checkMilestoneNameDuplicate()
          if (isDuplicate) {
            this.errors.milestoneName = '里程碑名称已存在，请使用其他名称'
          }
        } catch (error) {
          console.error('检查里程碑名称重复失败:', error)
        }
      }
    },
    
    /**
     * 提交表单
     */
    async submitForm() {
      if (this.isSubmitting) return
      
      // 验证表单
      const isValid = await this.validateForm()
      if (!isValid) return
      
      this.isSubmitting = true
      
      try {
        // 准备提交数据
        const submitData = {
          milestoneName: this.formData.milestoneName.trim()
        }
        
        // 编辑模式需要包含ID
        if (this.mode === 'edit' && this.formData.milestoneId) {
          submitData.milestoneId = this.formData.milestoneId
        }
        
        // 触发保存事件
        this.$emit('save', submitData)
        
      } catch (error) {
        console.error('提交表单失败:', error)
      } finally {
        this.isSubmitting = false
      }
    }
  },
  
  /**
   * 组件销毁时清理定时器
   */
  beforeDestroy() {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer)
    }
    this.endDrag()
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
}

/* 模态框内容 */
.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  cursor: move;
  user-select: none;
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

/* 模态框主体 */
.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 表单组 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.required {
  color: #ff4d4f;
}

/* 表单控件 */
.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-control.is-invalid {
  border-color: #ff4d4f;
}

.form-control.is-invalid:focus {
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
}

/* 错误信息 */
.invalid-feedback {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* 模态框底部 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  background: white;
  color: #262626;
}

.btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn:disabled:hover {
  border-color: #d9d9d9;
  color: #262626;
}

.btn-primary {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
  border-color: #40a9ff;
  color: white;
}

.btn-secondary {
  background: white;
  border-color: #d9d9d9;
  color: #262626;
}

.btn-secondary:hover:not(:disabled) {
  border-color: #40a9ff;
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 12px 16px;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>