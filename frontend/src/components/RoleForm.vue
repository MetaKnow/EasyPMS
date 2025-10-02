<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ mode === 'add' ? '新增角色' : '修改角色' }}</h3>
        <button class="close-btn" @click="close">
          <span class="icon-close">✕</span>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="role-form">
        <div class="form-group">
          <label for="roleName" class="required">角色名称</label>
          <input
            id="roleName"
            v-model="formData.roleName"
            type="text"
            class="form-input"
            placeholder="请输入角色名称"
            :class="{ error: errors.roleName }"
            @input="onRoleNameInput"
            @blur="onRoleNameBlur"
            maxlength="50"
          />
          <span v-if="errors.roleName" class="error-message">
            {{ errors.roleName }}
          </span>
        </div>

        <div class="form-group">
          <label for="description">角色描述</label>
          <textarea
            id="description"
            v-model="formData.description"
            class="form-textarea"
            placeholder="请输入角色描述（可选）"
            :class="{ error: errors.description }"
            maxlength="200"
            rows="4"
          ></textarea>
          <span v-if="errors.description" class="error-message">
            {{ errors.description }}
          </span>
          <div class="char-count">
            {{ (formData.description || '').length }}/200
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="close">
            取消
          </button>
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { createRole, updateRole, checkRoleNameExists } from '../api/role.js'

/**
 * 角色表单组件（类级注释：负责角色的新增和修改表单）
 */
export default {
  name: 'RoleForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    role: {
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
      formData: {
        roleId: null,
        roleName: '',
        description: ''
      },
      errors: {},
      loading: false,
      isCheckingRoleName: false,
      _roleNameDebounceTimer: null
    }
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          this.initForm()
        }
      },
      immediate: true
    },
    role: {
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
     * 初始化表单（函数级注释：根据模式初始化表单数据）
     */
    initForm() {
      if (this.mode === 'edit' && this.role) {
        // 编辑模式，回显数据
        this.formData = {
          roleId: this.role.roleId,
          roleName: this.role.roleName || '',
          description: this.role.description || ''
        }
      } else {
        // 新增模式，重置表单
        this.formData = {
          roleId: null,
          roleName: '',
          description: ''
        }
      }
      
      // 清空错误信息
      this.errors = {}
    },

    /**
     * 表单验证（函数级注释：验证表单数据的有效性）
     */
    async validateForm() {
      const errors = {}

      // 角色名称验证
      if (!this.formData.roleName.trim()) {
        errors.roleName = '请输入角色名称'
      } else if (this.formData.roleName.trim().length < 2) {
        errors.roleName = '角色名称至少2个字符'
      } else if (this.formData.roleName.trim().length > 50) {
        errors.roleName = '角色名称不能超过50个字符'
      }

      // 角色描述验证（可选）
      if (this.formData.description && this.formData.description.length > 200) {
        errors.description = '角色描述不能超过200个字符'
      }

      // 若基础校验通过，进行异步判重（新增模式或编辑时名称发生变化）
      const trimmedName = this.formData.roleName.trim()
      const needCheckDuplicate = trimmedName && (this.mode === 'add' || (this.mode === 'edit' && trimmedName !== (this.role?.roleName || '')))
      if (needCheckDuplicate) {
        try {
          this.isCheckingRoleName = true
          const { exists } = await checkRoleNameExists(trimmedName)
          if (exists) {
            errors.roleName = '角色名称已存在，请更换'
          }
        } catch (e) {
          // 网络错误不阻塞，但不覆盖已有错误
          console.warn('角色名称判重检查失败:', e)
        } finally {
          this.isCheckingRoleName = false
        }
      }

      this.errors = errors
      return Object.keys(errors).length === 0
    },

    /**
     * 处理表单提交（函数级注释：提交表单数据到后端）
     */
    async handleSubmit() {
      if (!(await this.validateForm())) {
        return
      }

      this.loading = true
      try {
        const roleData = {
          roleName: this.formData.roleName.trim(),
          description: this.formData.description?.trim() || null
        }

        if (this.mode === 'add') {
          await createRole(roleData)
          this.$message?.success('角色新增成功')
        } else {
          await updateRole(this.formData.roleId, roleData)
          this.$message?.success('角色更新成功')
        }

        this.$emit('save')
      } catch (error) {
        console.error('保存角色失败:', error)
        this.$message?.error('保存角色失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    /**
     * 关闭表单（函数级注释：关闭表单对话框）
     */
    close() {
      this.$emit('close')
    },

    /**
     * 处理遮罩层点击（函数级注释：点击遮罩层关闭对话框）
     */
    handleOverlayClick() {
      this.close()
    },

    /**
     * 角色名称输入时（300ms 防抖 + 异步判重）
     */
    onRoleNameInput() {
      const name = (this.formData.roleName || '').trim()
      // 基础前端长度校验同步提示
      if (!name) {
        this.errors.roleName = '请输入角色名称'
        this.clearRoleNameDebounce()
        return
      }
      if (name.length < 2) {
        this.errors.roleName = '角色名称至少2个字符'
        this.clearRoleNameDebounce()
        return
      }
      if (name.length > 50) {
        this.errors.roleName = '角色名称不能超过50个字符'
        this.clearRoleNameDebounce()
        return
      }

      // 若与原名称一致（编辑场景），不再判重
      if (this.mode === 'edit' && name === (this.role?.roleName || '')) {
        this.errors.roleName = ''
        this.clearRoleNameDebounce()
        return
      }

      // 防抖后异步判重
      this.errors.roleName = ''
      this.isCheckingRoleName = true
      this.clearRoleNameDebounce()
      this._roleNameDebounceTimer = setTimeout(async () => {
        try {
          const { exists } = await checkRoleNameExists(name)
          if (exists) {
            this.errors.roleName = '角色名称已存在，请更换'
          } else {
            this.errors.roleName = ''
          }
        } catch (e) {
          console.warn('角色名称判重检查失败:', e)
        } finally {
          this.isCheckingRoleName = false
        }
      }, 300)
    },

    /**
     * 角色名称失焦时（立即判重）
     */
    async onRoleNameBlur() {
      const name = (this.formData.roleName || '').trim()
      this.clearRoleNameDebounce()
      if (!name) return
      if (this.mode === 'edit' && name === (this.role?.roleName || '')) return
      try {
        this.isCheckingRoleName = true
        const { exists } = await checkRoleNameExists(name)
        if (exists) {
          this.errors.roleName = '角色名称已存在，请更换'
        }
      } catch (e) {
        console.warn('角色名称判重检查失败:', e)
      } finally {
        this.isCheckingRoleName = false
      }
    },

    /**
     * 清理角色名称判重防抖定时器
     */
    clearRoleNameDebounce() {
      if (this._roleNameDebounceTimer) {
        clearTimeout(this._roleNameDebounceTimer)
        this._roleNameDebounceTimer = null
      }
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
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 6px;
  min-width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  color: #8c8c8c;
  border-radius: 2px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

.role-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.form-group label.required::after {
  content: " *";
  color: #ff4d4f;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-input.error,
.form-textarea.error {
  border-color: #ff4d4f;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.error-message {
  display: block;
  margin-top: 4px;
  color: #ff4d4f;
  font-size: 12px;
}

.char-count {
  text-align: right;
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.form-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.btn-secondary {
  background: #f5f5f5;
  color: #595959;
  border-color: #d9d9d9;
}

.btn-secondary:hover:not(:disabled) {
  background: #fafafa;
  border-color: #40a9ff;
  color: #1890ff;
}

.icon-close {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-content {
    min-width: auto;
    margin: 20px;
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .role-form {
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>