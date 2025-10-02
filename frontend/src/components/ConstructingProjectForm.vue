<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>{{ isEdit ? '编辑在建项目' : '创建在建项目' }}</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      
      <form @submit.prevent="submitForm" class="project-form">
        <div class="form-grid">
          <!-- 基本信息 -->
          <div class="form-group">
            <label for="projectNum">项目编号 <span class="required">*</span></label>
            <input 
              type="text" 
              id="projectNum" 
              v-model="form.projectNum" 
              required 
              placeholder="请输入项目编号"
            />
          </div>

          <div class="form-group">
            <label for="year">年度 <span class="required">*</span></label>
            <input 
              type="number" 
              id="year" 
              v-model="form.year" 
              required 
              :min="2020" 
              :max="2030"
              placeholder="请输入年度"
            />
          </div>

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

          <div class="form-group">
            <label for="projectType">项目类型 <span class="required">*</span></label>
            <select id="projectType" v-model="form.projectType" required>
              <option value="">请选择项目类型</option>
              <option value="软件开发">软件开发</option>
              <option value="系统集成">系统集成</option>
              <option value="技术咨询">技术咨询</option>
              <option value="运维服务">运维服务</option>
            </select>
          </div>

          <div class="form-group">
            <label for="projectState">项目状态</label>
            <select id="projectState" v-model="form.projectState">
              <option value="待开始">待开始</option>
              <option value="进行中">进行中</option>
              <option value="已完成">已完成</option>
              <option value="已暂停">已暂停</option>
            </select>
          </div>

          <div class="form-group">
            <label for="customerId">客户</label>
            <select id="customerId" v-model="form.customerId">
              <option value="">请选择客户</option>
              <option v-for="customer in customers" :key="customer.customerId" :value="customer.customerId">
                {{ customer.customerName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="projectLeader">项目负责人</label>
            <select id="projectLeader" v-model="form.projectLeader">
              <option value="">请选择项目负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.username }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="saleLeader">商务负责人</label>
            <select id="saleLeader" v-model="form.saleLeader">
              <option value="">请选择商务负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.username }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="arcSystem">档案系统</label>
            <input 
              type="text" 
              id="arcSystem" 
              v-model="form.arcSystem" 
              placeholder="请输入档案系统"
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
            <label for="planEndDate">计划结束日期</label>
            <input 
              type="date" 
              id="planEndDate" 
              v-model="form.planEndDate"
            />
          </div>

          <div class="form-group">
            <label for="actualEndDate">实际结束日期</label>
            <input 
              type="date" 
              id="actualEndDate" 
              v-model="form.actualEndDate"
            />
          </div>

          <div class="form-group">
            <label for="acceptanceDate">项目验收日期</label>
            <input 
              type="date" 
              id="acceptanceDate" 
              v-model="form.acceptanceDate"
            />
          </div>

          <!-- 工期信息 -->
          <div class="form-group">
            <label for="planPeriod">项目预计工期（天）</label>
            <input 
              type="number" 
              id="planPeriod" 
              v-model="form.planPeriod" 
              min="1"
              placeholder="请输入预计工期"
            />
          </div>

          <div class="form-group">
            <label for="actualPeriod">项目实际工期（天）</label>
            <input 
              type="number" 
              id="actualPeriod" 
              v-model="form.actualPeriod" 
              min="1"
              placeholder="请输入实际工期"
            />
          </div>

          <!-- 渠道信息 -->
          <div class="form-group">
            <label for="isAgent">是否渠道项目</label>
            <select id="isAgent" v-model="form.isAgent">
              <option :value="0">否</option>
              <option :value="1">是</option>
            </select>
          </div>

          <div class="form-group">
            <label for="agentName">渠道名称</label>
            <input 
              type="text" 
              id="agentName" 
              v-model="form.agentName" 
              placeholder="请输入渠道名称"
              :disabled="form.isAgent === 0"
            />
          </div>

          <!-- 金额信息 -->
          <div class="form-group">
            <label for="value">项目金额</label>
            <input 
              type="number" 
              id="value" 
              v-model="form.value" 
              step="0.01"
              min="0"
              placeholder="请输入项目金额"
            />
          </div>

          <div class="form-group">
            <label for="receivedMoney">已回款金额</label>
            <input 
              type="number" 
              id="receivedMoney" 
              v-model="form.receivedMoney" 
              step="0.01"
              min="0"
              placeholder="请输入已回款金额"
            />
          </div>

          <div class="form-group">
            <label for="unreceiveMoney">未回款金额</label>
            <input 
              type="number" 
              id="unreceiveMoney" 
              v-model="form.unreceiveMoney" 
              step="0.01"
              min="0"
              placeholder="请输入未回款金额"
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
  name: 'ConstructingProjectForm',
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
      customers: [],
      users: [],
      form: {
        projectNum: '',
        year: new Date().getFullYear(),
        projectName: '',
        projectType: '',
        projectLeader: '',
        saleLeader: '',
        projectState: '待开始',
        arcSystem: '',
        startDate: '',
        planEndDate: '',
        actualEndDate: '',
        planPeriod: '',
        actualPeriod: '',
        customerId: '',
        isAgent: 0,
        agentName: '',
        value: '',
        receivedMoney: 0,
        unreceiveMoney: 0,
        acceptanceDate: ''
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
        this.loadCustomers()
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
        projectNum: '',
        year: new Date().getFullYear(),
        projectName: '',
        projectType: '',
        projectLeader: '',
        saleLeader: '',
        projectState: '待开始',
        arcSystem: '',
        startDate: '',
        planEndDate: '',
        actualEndDate: '',
        planPeriod: '',
        actualPeriod: '',
        customerId: '',
        isAgent: 0,
        agentName: '',
        value: '',
        receivedMoney: 0,
        unreceiveMoney: 0,
        acceptanceDate: ''
      }
    },

    /**
     * 加载客户列表
     */
    async loadCustomers() {
      try {
        const response = await axios.get('http://localhost:8081/api/customers')
        if (response.data.success) {
          this.customers = response.data.data
        }
      } catch (error) {
        console.error('加载客户列表失败:', error)
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
          ? `http://localhost:8081/api/constructing-projects/${this.projectData.projectId}`
          : 'http://localhost:8081/api/constructing-projects'
        
        const method = this.isEdit ? 'put' : 'post'
        
        const response = await axios[method](url, this.form)
        
        if (response.data.success) {
          this.$emit('success', response.data.data)
          this.closeModal()
        } else {
          alert(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存项目失败:', error)
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
/* 模态框覆盖层 */
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
  border-radius: 6px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

/* 关闭按钮 */
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
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #262626;
}

/* 表单样式 */
.project-form {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.form-group.full-width {
  grid-column: 1 / -1;
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
.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-group input.error,
.form-group select.error {
  border-color: #ff4d4f;
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #8c8c8c;
  cursor: not-allowed;
}

/* 表单操作按钮 */
.form-actions {
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
  font-size: 14px;
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

.btn-primary:disabled {
  background: #d9d9d9;
  border-color: #d9d9d9;
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
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .project-form {
    padding: 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .form-group {
    margin-bottom: 16px;
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
  
  .form-group input,
  .form-group select {
    font-size: 16px; /* 防止iOS缩放 */
  }
}
</style>