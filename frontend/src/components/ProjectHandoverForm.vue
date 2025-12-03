<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>移交售后运维</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="submitHandover" class="handover-form">
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
            <label for="serviceType">运维类型 <span class="required">*</span></label>
            <select id="serviceType" v-model="form.serviceType" required>
              <option value="">请选择运维类型</option>
              <option value="我公司全权运维">我公司全权运维</option>
              <option value="我公司配合远程运维">我公司配合远程运维</option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceDirector">运维负责人 <span class="required">*</span></label>
            <select id="serviceDirector" v-model="form.serviceDirector" required>
              <option value="">请选择运维负责人</option>
              <option v-for="user in users" :key="user.userId" :value="user.userId">
                {{ user.name || user.userName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceYear">运维年数 <span class="required">*</span></label>
            <input 
              type="number" 
              id="serviceYear" 
              v-model="form.serviceYear" 
              min="1" 
              required
            />
          </div>

          <div class="form-group">
            <label for="startDate">开始日期 <span class="required">*</span></label>
            <input 
              type="date" 
              id="startDate" 
              v-model="form.startDate" 
              required
            />
          </div>

          <div class="form-group">
            <label for="endDate">结束日期 <span class="required">*</span></label>
            <input 
              type="date" 
              id="endDate" 
              v-model="form.endDate" 
              required
            />
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              {{ isSubmitting ? '移交中...' : '移交' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { handoverProject } from '../api/afterserviceProject'

export default {
  name: 'ProjectHandoverForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    projectData: {
      type: Object,
      default: () => ({})
    },
    users: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      form: {
        serviceState: '免费运维期',
        serviceType: '我公司全权运维',
        serviceDirector: '',
        serviceYear: 1,
        startDate: '',
        endDate: ''
      },
      isSubmitting: false
    }
  },
  watch: {
    visible(val) {
      if (val) {
        // Initialize dates if needed
        const today = new Date().toISOString().substr(0, 10);
        this.form.startDate = today;
        // Default end date to 1 year later
        const nextYear = new Date();
        nextYear.setFullYear(nextYear.getFullYear() + 1);
        this.form.endDate = nextYear.toISOString().substr(0, 10);
        
        console.log('ProjectHandoverForm visible, users count:', this.users ? this.users.length : 0);
      }
    },
    users: {
      immediate: true,
      handler(val) {
        console.log('ProjectHandoverForm received users:', val ? val.length : 0);
      }
    }
  },
  methods: {
    closeModal() {
      this.$emit('close')
    },
    async submitHandover() {
      this.isSubmitting = true;
      try {
        if (!this.form.serviceDirector) {
          alert('请选择运维负责人');
          this.isSubmitting = false;
          return;
        }
        const payload = {
          constructingProjectId: this.projectData.projectId,
          ...this.form
        };

        const response = await handoverProject(payload);
        
        if (response.data.success) {
          alert('移交成功！');
          this.$emit('success');
          this.closeModal();
        } else {
          alert(response.data.message || '移交失败');
        }
      } catch (error) {
        console.error('Handover failed:', error);
        alert('移交失败，请稍后重试');
      } finally {
        this.isSubmitting = false;
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
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.required {
  color: #ff4d4f;
}
</style>
