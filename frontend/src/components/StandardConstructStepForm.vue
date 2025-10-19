<template>
  <div class="modal-overlay">
    <div class="modal-content" ref="dragModal" :style="dragStyle" @click.stop>
      <div class="modal-header" @mousedown="startDrag">
        <h3>{{ mode === 'add' ? '新增交付步骤' : '修改交付步骤' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label for="type">步骤类型 <span class="required">*</span></label>
            <select
              id="type"
              v-model="formData.type"
              class="form-control"
              :class="{ 'is-invalid': errors.type }"
            >
              <option value="">请选择步骤类型</option>
              <option value="标准产品">标准产品</option>
              <option value="接口开发">接口开发</option>
              <option value="数据迁移">数据迁移</option>
              <option value="个性化功能开发">个性化功能开发</option>
              <option value="用户培训">用户培训</option>
              <option value="系统上线试运行">系统上线试运行</option>
            </select>
            <div v-if="errors.type" class="invalid-feedback">
              {{ errors.type }}
            </div>
          </div>

          <div class="form-group">
            <label for="smilestoneId">所属里程碑 <span class="required">*</span></label>
            <select
              id="smilestoneId"
              v-model="formData.smilestoneId"
              class="form-control"
              :class="{ 'is-invalid': errors.smilestoneId }"
              required
            >
              <option value="">请选择所属里程碑</option>
              <option 
                v-for="milestone in milestones" 
                :key="milestone.milestoneId" 
                :value="milestone.milestoneId"
              >
                {{ milestone.milestoneName }}
              </option>
            </select>
            <div v-if="errors.smilestoneId" class="invalid-feedback">
              {{ errors.smilestoneId }}
            </div>
          </div>

          <div class="form-group">
            <label for="sstepName">步骤名称 <span class="required">*</span></label>
            <input
              id="sstepName"
              v-model="formData.sstepName"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errors.sstepName }"
              placeholder="请输入步骤名称"
              maxlength="100"
              @input="onStepNameInput"
              @blur="onStepNameBlur"
            />
            <div v-if="errors.sstepName" class="invalid-feedback">
              {{ errors.sstepName }}
            </div>
          </div>

          <div class="form-group">
            <label for="systemName">产品名称 <span class="required">*</span></label>
            <select
              id="systemName"
              v-model="formData.systemName"
              class="form-control"
              :class="{ 'is-invalid': errors.systemName }"
              :disabled="!selectedProductName"
            >
              <option value="">{{ selectedProductName ? '请选择产品名称' : '请先在左侧选择产品标签' }}</option>
              <option v-if="selectedProductName" :value="selectedProductName">{{ selectedProductName }}</option>
            </select>
            <div v-if="errors.systemName" class="invalid-feedback">
              {{ errors.systemName }}
            </div>
            <div class="form-help">
              产品名称从左侧产品标签中选择，当前选中：{{ selectedProductName || '未选择' }}
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
import { getAllStandardMilestones } from '../api/standardMilestone.js'

/**
 * 标准交付步骤表单组件
 * 用于新增和编辑标准交付步骤
 */
export default {
  name: 'StandardConstructStepForm',
  props: {
    /**
     * 表单显示状态
     */
    visible: {
      type: Boolean,
      default: false
    },
    
    /**
     * 交付步骤数据（编辑模式时传入）
     */
    step: {
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
    },

    /**
     * 选中的产品名称（从左侧标签传入）
     */
    selectedProductName: {
      type: String,
      default: ''
    },
    
    /**
     * 默认产品名称
     */
    defaultProductName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      /**
       * 表单数据
       */
      formData: {
        sstepId: null,
        sstepName: '',
        type: '',
        systemName: '',
        smilestoneId: ''
      },
      
      /**
       * 验证错误信息
       */
      errors: {
        sstepName: '',
        type: '',
        systemName: '',
        smilestoneId: ''
      },
      
      /**
       * 提交状态
       */
      isSubmitting: false,

      /**
       * 里程碑列表
       */
      milestones: [],
      
      // 拖动状态
      dragStyle: { position: 'fixed', top: '0px', left: '0px' },
      dragging: false,
      dragStart: { mouseX: 0, mouseY: 0, top: 0, left: 0 }
    }
  },
  watch: {
    /**
     * 监听交付步骤数据变化，更新表单
     */
    step: {
      handler(newStep) {
        this.initializeFormData(newStep)
      },
      immediate: true
    },
    
    /**
     * 监听表单显示状态变化
     */
    visible(newVisible) {
      if (newVisible) {
        // 表单显示时，重新初始化数据
        this.initializeFormData(this.step)
        // 清除之前的验证错误
        this.clearErrors()
        // 加载里程碑列表
        this.loadMilestones()
        // 居中弹窗
        this.$nextTick(() => this.centerModal())
      }
    },

    /**
     * 监听选中的产品名称变化
     */
    selectedProductName: {
      handler(newProductName) {
        if (newProductName && this.mode === 'add') {
          this.formData.systemName = newProductName
        }
      },
      immediate: true
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
      this.$emit('close')
    },
    
    // 居中弹窗
    centerModal() {
      const modal = this.$refs.dragModal
      if (!modal) return
      const w = modal.offsetWidth
      const h = modal.offsetHeight
      const left = Math.max((window.innerWidth - w) / 2, 8)
      const top = Math.max((window.innerHeight - h) / 2, 20)
      this.dragStyle = { position: 'fixed', top: `${top}px`, left: `${left}px` }
    },
    
    // 开始拖动（在头部按下）
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
      // 边界限制
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
    initializeFormData(step) {
      if (step && this.mode === 'edit') {
        // 编辑模式：回显交付步骤数据
        this.formData = {
          sstepId: step.sstepId || null,
          sstepName: step.sstepName || '',
          type: step.type || '',
          systemName: step.systemName || '',
          smilestoneId: step.smilestoneId || ''
        }
      } else {
        // 新增模式：重置表单
        this.resetForm()
        // 如果有选中的产品名称，设置为默认值
        if (this.selectedProductName) {
          this.formData.systemName = this.selectedProductName
        } else if (this.defaultProductName) {
          this.formData.systemName = this.defaultProductName
        }
      }
    },
    
    /**
     * 清除验证错误
     */
    clearErrors() {
      this.errors = {
        sstepName: '',
        type: '',
        systemName: '',
        smilestoneId: ''
      }
    },
    
    /**
     * 重置表单
     */
    resetForm() {
      this.formData = {
        sstepId: null,
        sstepName: '',
        type: '',
        systemName: '',
        smilestoneId: ''
      }
      this.clearErrors()
    },

    /**
     * 加载里程碑列表
     */
    async loadMilestones() {
      try {
        const data = await getAllStandardMilestones()
        // 后端返回的数据格式是 { milestones: [...] }
        this.milestones = data.milestones || []
      } catch (error) {
        console.error('加载里程碑列表失败:', error)
        this.milestones = []
      }
    },
    
    /**
     * 验证表单
     */
    async validateForm() {
      let isValid = true
      
      // 重置错误信息
      this.errors = {
        sstepName: '',
        type: '',
        systemName: '',
        smilestoneId: ''
      }
      
      // 验证步骤名称
      if (!this.formData.sstepName || !this.formData.sstepName.trim()) {
        this.errors.sstepName = '请输入步骤名称'
        isValid = false
      } else if (this.formData.sstepName.trim().length > 100) {
        this.errors.sstepName = '步骤名称不能超过100个字符'
        isValid = false
      }

      // 验证步骤类型
      if (!this.formData.type) {
        this.errors.type = '请选择步骤类型'
        isValid = false
      }

      // 验证产品名称
      if (!this.formData.systemName) {
        this.errors.systemName = '请选择产品名称'
        isValid = false
      }

      // 验证所属里程碑
      if (!this.formData.smilestoneId) {
        this.errors.smilestoneId = '请选择所属里程碑'
        isValid = false
      }
      
      return isValid
    },
    
    /**
     * 步骤名称输入事件
     */
    onStepNameInput() {
      // 清除当前错误
      this.errors.sstepName = ''
    },
    
    /**
     * 步骤名称失焦事件
     */
    onStepNameBlur() {
      // 清除当前错误
      this.errors.sstepName = ''
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
          sstepName: this.formData.sstepName.trim(),
          type: this.formData.type,
          systemName: this.formData.systemName,
          smilestoneId: this.formData.smilestoneId || null
        }
        
        // 编辑模式需要包含ID
        if (this.mode === 'edit' && this.formData.sstepId) {
          submitData.sstepId = this.formData.sstepId
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
   * 组件创建时加载里程碑数据
   */
  created() {
    this.loadMilestones()
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
  max-width: 600px;
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

.form-control:disabled {
  background-color: #f5f5f5;
  color: #8c8c8c;
  cursor: not-allowed;
}

/* 下拉选择框 */
select.form-control {
  cursor: pointer;
}

select.form-control:disabled {
  cursor: not-allowed;
}

/* 表单帮助文本 */
.form-help {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
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