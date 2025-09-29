<template>
  <div class="modal-overlay" v-if="visible" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ isEdit ? '编辑运维项目' : '创建运维项目' }}</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      
      <form @submit.prevent="submitForm" class="project-form">
        <div class="form-grid">
          <!-- 基本信息 -->
          <div class="form-group full-width">
            <label for="projectName">项目名称 <span class="required">*</span></label>
            <input 
              type="text" 
              id="projectName" 
              v-model="form.projectName" 
              required 
              placeholder="请输入项目名称"
            />
          </div>

          <div class="form-group full-width">
            <label for="arcSystem">档案系统 <span class="required">*</span></label>
            <input 
              type="text" 
              id="arcSystem" 
              v-model="form.arcSystem" 
              required 
              placeholder="请输入档案系统"
            />
          </div>

          <div class="form-group">
            <label for="director">负责人 <span class="required">*</span></label>
            <select id="director" v-model="form.director" required>
              <option value="">请选择负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.username }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceState">运维状态 <span class="required">*</span></label>
            <select id="serviceState" v-model="form.serviceState" required>
              <option value="">请选择运维状态</option>
              <option value="免费运维期">免费运维期</option>
              <option value="付费运维">付费运维</option>
              <option value="无付费运维">无付费运维</option>
              <option value="暂停运维">暂停运维</option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceYear">运维年数</label>
            <input 
              type="number" 
              id="serviceYear" 
              v-model="form.serviceYear" 
              min="1"
              max="10"
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
              placeholder="请输入总工时"
            />
          </div>

          <!-- 日期信息 -->
          <div class="form-group">
            <label for="startDate">开始日期</label>
            <input 
              type="date" 
              id="startDate" 
              v-model="form.startDate"
            />
          </div>

          <div class="form-group">
            <label for="endDate">结束日期</label>
            <input 
              type="date" 
              id="endDate" 
              v-model="form.endDate"
            />
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AfterserviceProjectForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    projectData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      isSubmitting: false,
      users: [],
      form: {
        projectName: '',
        arcSystem: '',
        director: '',
        serviceYear: '',
        startDate: '',
        endDate: '',
        serviceState: '',
        totalHours: ''
      }
    }
  },
  computed: {
    /**
     * 是否为编辑模式
     */
    isEdit() {
      return this.projectData && this.projectData.projectId
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.loadFormData()
        this.loadUsers()
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
    },

    /**
     * 重置表单
     */
    resetForm() {
      this.form = {
        projectName: '',
        arcSystem: '',
        director: '',
        serviceYear: '',
        startDate: '',
        endDate: '',
        serviceState: '',
        totalHours: ''
      }
    },

    /**
     * 加载用户列表
     */
    async loadUsers() {
      try {
        const response = await axios.get('http://localhost:8081/api/users')
        if (response.data.success) {
          this.users = response.data.data
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    },

    /**
     * 提交表单
     */
    async submitForm() {
      this.isSubmitting = true
      try {
        const url = this.isEdit 
          ? `http://localhost:8081/api/afterservice-projects/${this.projectData.projectId}`
          : 'http://localhost:8081/api/afterservice-projects'
        
        const method = this.isEdit ? 'put' : 'post'
        
        const response = await axios[method](url, this.form)
        
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 6px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  border-radius: 6px 6px 0 0;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #8c8c8c;
  padding: 4px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  color: #595959;
  background: #f5f5f5;
}

.project-form {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
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
</style>