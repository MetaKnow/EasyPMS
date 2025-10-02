<template>
  <div v-if="visible" class="modal-overlay">
    <div 
      class="modal-content" 
      :class="[sizeClass, typeClass]" 
      role="dialog" 
      aria-modal="true" 
      @click.stop
    >
      <div class="modal-header">
        <h3>{{ title || '确认操作' }}</h3>
        <button v-if="showCloseIcon" class="close-btn" @click="close" aria-label="关闭">×</button>
      </div>
      
      <div class="modal-body">
        <div class="confirm-icon">
          <i class="icon-warning">⚠️</i>
        </div>
        <div class="confirm-message">
          <p>{{ message }}</p>
          <div v-if="details" class="confirm-details">
            {{ details }}
          </div>
        </div>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="close">
          取消
        </button>
        <button 
          type="button" 
          :class="['btn', type === 'danger' ? 'btn-danger' : 'btn-primary']" 
          @click="handleConfirm"
          :disabled="loading"
        >
          <span v-if="loading" class="loading-spinner">⟳</span>
          {{ confirmText || '确认' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 确认对话框组件（支持尺寸/类型/键盘操作）
 * 用于确认删除等危险操作，提供更可配置的交互体验
 */
export default {
  name: 'ConfirmDialog',
  props: {
    /**
     * 是否可见
     */
    visible: {
      type: Boolean,
      default: false
    },
    /**
     * 标题文案
     */
    title: {
      type: String,
      default: '确认操作'
    },
    /**
     * 主消息内容
     */
    message: {
      type: String,
      required: true
    },
    /**
     * 详情说明
     */
    details: {
      type: String,
      default: ''
    },
    /**
     * 确认按钮文案
     */
    confirmText: {
      type: String,
      default: '确认'
    },
    /**
     * 对话框类型：danger（危险操作，红色主题）或 info（普通确认，蓝色主题）
     */
    type: {
      type: String,
      default: 'danger',
      validator: (v) => ['danger', 'info'].includes(v)
    },
    /**
     * 对话框尺寸：small / medium / large
     */
    size: {
      type: String,
      default: 'medium',
      validator: (v) => ['small', 'medium', 'large'].includes(v)
    },
    /**
     * 是否允许点击遮罩层关闭
     */
    overlayClosable: {
      type: Boolean,
      default: true
    },
    /**
     * 是否显示右上角关闭图标
     */
    showCloseIcon: {
      type: Boolean,
      default: true
    },
    /**
     * 是否启用键盘快捷操作（Enter 确认，Esc 取消）
     */
    keyboard: {
      type: Boolean,
      default: true
    }
  },
  emits: ['close', 'confirm'],
  data() {
    return {
      loading: false
    }
  },
  computed: {
    /**
     * 根据尺寸返回对应样式类
     */
    sizeClass() {
      return `size-${this.size}`
    },
    /**
     * 根据类型返回对应样式类
     */
    typeClass() {
      return `type-${this.type}`
    }
  },
  watch: {
    /**
     * 监听可见性，注册或移除键盘事件
     */
    visible(val) {
      if (this.keyboard) {
        if (val) {
          window.addEventListener('keydown', this.handleKeydown)
        } else {
          window.removeEventListener('keydown', this.handleKeydown)
        }
      }
    }
  },
  mounted() {
    // 初始挂载时根据当前状态绑定键盘事件
    if (this.keyboard && this.visible) {
      window.addEventListener('keydown', this.handleKeydown)
    }
  },
  beforeUnmount() {
    // 组件卸载时移除事件
    window.removeEventListener('keydown', this.handleKeydown)
  },
  methods: {
    /**
     * 处理确认操作
     * - 触发 confirm 事件
     * - 在 finally 中恢复 loading 状态
     */
    async handleConfirm() {
      this.loading = true
      try {
        this.$emit('confirm')
      } catch (error) {
        console.error('确认操作失败:', error)
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
     * 处理遮罩层点击（可配置是否允许关闭）
     */
    handleOverlayClick() {
      if (this.overlayClosable) {
        this.close()
      }
    },

    /**
     * 键盘事件处理：Enter 触发确认，Esc 触发关闭
     */
    handleKeydown(e) {
      if (e.key === 'Enter') {
        e.preventDefault()
        this.handleConfirm()
      } else if (e.key === 'Escape') {
        e.preventDefault()
        this.close()
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
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 尺寸样式 */
.size-small {
  min-width: 320px;
  max-width: 380px;
}

.size-medium {
  min-width: 400px;
  max-width: 500px;
}

.size-large {
  min-width: 560px;
  max-width: 720px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
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
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 确认内容样式 */
.confirm-icon {
  flex-shrink: 0;
}

.icon-warning {
  font-size: 24px;
  color: #faad14;
}

/* 类型主题 */
.type-danger .icon-warning {
  color: #ff4d4f;
}

.type-info .icon-warning {
  color: #1890ff;
}

.confirm-message {
  flex: 1;
}

.confirm-message p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #262626;
  line-height: 1.5;
}

.confirm-details {
  font-size: 13px;
  color: #8c8c8c;
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 4px;
  border-left: 3px solid #faad14;
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

.btn-danger {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #ff7875;
  border-color: #ff7875;
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

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>