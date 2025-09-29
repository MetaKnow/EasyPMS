<template>
  <div class="modal-overlay" v-if="visible" @click="handleOverlayClick">
    <div class="modal-dialog" @click.stop>
      <div class="modal-header">
        <h3>{{ isEdit ? '编辑用户' : '新增用户' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="submit">
          <div class="form-group" v-if="!isEdit">
            <label for="userName">用户名 <span class="required">*</span></label>
            <input
              id="userName"
              v-model="formData.userName"
              type="text"
              class="form-control"
              placeholder="请输入用户名"
              autocomplete="off"
              required
            />
            <div class="form-help">
              用户名创建后不可修改
            </div>
          </div>

          <div class="form-group">
            <label for="name">姓名</label>
            <input
              id="name"
              v-model="formData.name"
              type="text"
              class="form-control"
              placeholder="请输入姓名"
            />
          </div>

          <div class="form-group" v-if="!isEdit">
            <label for="password">密码 <span class="required">*</span></label>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              class="form-control"
              placeholder="请输入密码"
              autocomplete="new-password"
              required
            />
          </div>

          <div class="form-group" v-if="!isEdit">
            <label for="confirmPassword">确认密码 <span class="required">*</span></label>
            <input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              class="form-control"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              required
            />
            <div class="form-error" v-if="passwordMismatch">
              两次输入的密码不一致
            </div>
          </div>

          <div class="form-group" v-if="isEdit">
            <label for="organId">所属机构</label>
            <select
              id="organId"
              v-model="formData.organId"
              class="form-control"
            >
              <option value="">请选择机构</option>
              <option 
                v-for="organ in flattenedOrgans" 
                :key="organ.organId" 
                :value="Number(organ.organId)"
              >
                {{ organ.displayName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="roleId">角色</label>
            <select
              id="roleId"
              v-model="formData.roleId"
              class="form-control"
            >
              <option value="">无角色</option>
              <option 
                v-for="role in roles" 
                :key="role.roleId" 
                :value="Number(role.roleId)"
              >
                {{ role.roleName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="formData.locked"
              />
              锁定用户
            </label>
            <div class="form-help">
              锁定后用户将无法登录系统
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
          @click="submit"
          :disabled="!isFormValid"
        >
          {{ isEdit ? '保存' : '创建' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 组件级注释：用户新增/编辑对话框组件
 * 1) 负责展示与编辑用户的基础信息（用户名、姓名、角色、锁定状态）。
 * 2) 支持数据回显：在编辑模式下根据传入的user与roles自动填充表单。
 * 3) 保证前后端一致性：角色ID统一使用数字类型，提交时将锁定状态转换为后端所需的整型值。
 */
export default {
  name: 'UserDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    user: {
      type: Object,
      default: null
    },
    organId: {
      type: Number,
      required: false
    },
    roles: {
      type: Array,
      default: () => []
    },
    organs: {
      type: Array,
      default: () => []
    }
  },
  emits: ['close', 'submit'],
  data() {
    return {
      formData: {
        userName: '',
        name: '',
        password: '',
        confirmPassword: '',
        organId: '',
        roleId: '',
        locked: false
      }
    }
  },
  computed: {
    /**
     * 是否为编辑模式
     */
    isEdit() {
      return !!this.user
    },

    /**
     * 密码是否不匹配
     */
    passwordMismatch() {
      return !this.isEdit && 
             this.formData.password && 
             this.formData.confirmPassword && 
             this.formData.password !== this.formData.confirmPassword
    },

    /**
     * 表单是否有效
     */
    isFormValid() {
      if (this.isEdit) {
        // 编辑模式：不需要验证用户名和密码，只要有基本数据即可
        return true
      } else {
        // 新增模式：需要验证用户名、密码和确认密码
        return this.formData.userName.trim() !== '' &&
               this.formData.password.trim() !== '' &&
               this.formData.confirmPassword.trim() !== '' &&
               !this.passwordMismatch
      }
    },

    /**
     * 将机构树转换为带层级缩进的平铺列表
     */
    flattenedOrgans() {
      const result = []
      
      const flattenOrgan = (organ, level = 0) => {
        const indent = '　'.repeat(level) // 使用全角空格进行缩进
        const prefix = level > 0 ? '├─ ' : ''
        
        result.push({
          organId: organ.organId,
          organName: organ.organName,
          displayName: `${indent}${prefix}${organ.organName}`
        })
        
        if (organ.children && organ.children.length > 0) {
          organ.children.forEach(child => {
            flattenOrgan(child, level + 1)
          })
        }
      }
      
      this.organs.forEach(organ => {
        flattenOrgan(organ)
      })
      
      return result
    }
  },
  watch: {
    // 使对话框在初次显示（组件挂载时visible已经为true）也能触发初始化
    visible: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.initForm()
        }
      }
    },
    // 当传入的user在组件创建时就已经有值，immediate确保首次也会回调
    user: {
      immediate: true,
      handler() {
        if (this.visible) {
          this.initForm()
        }
      }
    }
  },
  // 在组件挂载时兜底初始化，避免任何竞态导致首次不回显
  mounted() {
    /**
     * 函数级注释：组件挂载后，如果当前对话框可见，则执行一次表单初始化，确保编辑模式的回显
     */
    if (this.visible) {
      this.initForm()
    }
  },
  methods: {
    /**
     * 初始化表单（函数级注释：根据传入的user和roles初始化表单字段，用于编辑/新增模式的回显）
     */
    initForm() {
      // 调试信息：查看roles数据
      console.log('UserDialog - roles数据:', this.roles)
      
      if (this.user) {
        // 调试信息：查看传入的user对象
        console.log('UserDialog - 传入的user对象:', this.user)
        console.log('UserDialog - user.name:', this.user.name)
        console.log('UserDialog - user.roleId:', this.user.roleId)
        console.log('UserDialog - user.roleId类型:', typeof this.user.roleId)
        
        // 编辑模式 - 直接设置数据，确保响应式更新
         this.formData.userName = this.user.userName || ''
         this.formData.name = this.user.name || ''
         this.formData.password = ''
         this.formData.confirmPassword = ''
         // 如果用户有机构ID，设置为数字；如果没有机构或organId为null，设置为空字符串
         this.formData.organId = this.user.organId != null ? Number(this.user.organId) : ''
         // 如果用户有角色ID，设置为数字；如果没有角色或roleId为null，设置为空字符串（对应"无角色"选项）
         this.formData.roleId = this.user.roleId != null ? Number(this.user.roleId) : ''
         this.formData.locked = !!this.user.locked // 转换Integer为boolean：0->false, 1->true
        
        // 调试信息：查看设置后的formData
        console.log('UserDialog - 设置后的formData:', this.formData)
        console.log('UserDialog - formData.roleId类型:', typeof this.formData.roleId)
        
        // 检查roleId是否在roles列表中
        const matchingRole = this.roles.find(role => Number(role.roleId) === Number(this.user.roleId))
        console.log('UserDialog - 匹配的角色:', matchingRole)
      } else {
        // 新增模式 - 清空所有字段，如果有传入的organId则使用，否则为空
        this.formData.userName = ''
        this.formData.name = ''
        this.formData.password = ''
        this.formData.confirmPassword = ''
        this.formData.organId = this.organId ? Number(this.organId) : ''
        this.formData.roleId = ''
        this.formData.locked = false
      }
    },

    /**
     * 提交表单
     */
    submit() {
      if (!this.isFormValid) {
        return
      }

      const submitData = {
         name: this.formData.name.trim(),
         // 新增用户时使用props中的organId，编辑用户时使用formData中的organId
         organId: this.isEdit ? 
           (this.formData.organId === '' ? null : this.formData.organId) : 
           (this.organId || null),
         // 如果选择了"无角色"（空字符串），则设为null；否则使用选择的roleId
         roleId: this.formData.roleId === '' ? null : this.formData.roleId,
         locked: this.formData.locked ? 1 : 0 // 转换boolean为Integer：true->1, false->0
       }

      if (!this.isEdit) {
        // 新增模式：包含用户名和密码
        submitData.userName = this.formData.userName.trim()
        submitData.password = this.formData.password
      }

      if (this.isEdit) {
        // 编辑模式：包含用户ID，但不包含用户名（用户名不可修改）
        submitData.userId = this.user.userId
      }

      this.$emit('submit', submitData)
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

/* 模态框主体 */
.modal-dialog {
  background: white;
  border-radius: 6px;
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

/* 模态框内容 */
.modal-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 表单样式 */
.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

label {
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

.form-control:disabled {
  background: #f5f5f5;
  color: #8c8c8c;
  cursor: not-allowed;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
  margin-bottom: 0;
}

.checkbox-label input[type="checkbox"] {
  margin: 0;
}

.form-help {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.form-error {
  margin-top: 4px;
  font-size: 12px;
  color: #ff4d4f;
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
  background: white;
  color: #262626;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
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
  border-color: #91d5ff;
  color: #1890ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-dialog {
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