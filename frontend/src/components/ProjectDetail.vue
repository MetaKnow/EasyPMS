<template>
  <div class="project-detail-page">
    <div class="topbar">
      <button class="back-btn" @click="goBack">返回</button>
      <div class="title">
        <span class="name">{{ project?.projectName || '项目详情' }}</span>
        <span class="num" v-if="project?.projectNum">编号：{{ project.projectNum }}</span>
      </div>
      <div class="stats" v-if="summaryLoaded">
        <div class="chip">步骤 {{ steps.length }}</div>
        <div class="chip">里程碑 {{ milestones.length }}</div>
        <div class="chip">交付物 {{ deliverables.length }}</div>
        <div class="chip">文件 {{ files.length }}</div>
      </div>
    </div>

    <div v-if="loading" class="state">正在加载...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>
    <div v-else class="content-grid">


      <section class="card wide">
        <h3>步骤</h3>
        <table class="table">
          <thead>
            <tr>
              <th width="60">序号</th>
              <th>步骤名称</th>
              <th width="120">类型</th>
              <th width="100">负责人</th>
              <th width="120">计划开始</th>
              <th width="120">计划结束</th>
              <th width="120">实际开始</th>
              <th width="120">实际结束</th>
              <th width="100">计划工期</th>
              <th width="100">实际工期</th>
              <th width="140">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(s, idx) in steps" :key="s.sstepId || s.nstepId">
              <td>{{ idx + 1 }}</td>
              <td>{{ s.sstepName || s.nstepName }}</td>
              <td>{{ s.type || '标准' }}</td>
              <td>{{ s.director ?? '-' }}</td>
              <td>{{ s.planStartDate ?? '-' }}</td>
              <td>{{ s.planEndDate ?? '-' }}</td>
              <td>{{ s.actualStartDate ?? '-' }}</td>
              <td>{{ s.actualEndDate ?? '-' }}</td>
              <td>{{ s.planPeriod ?? '-' }}</td>
              <td>{{ s.actualPeriod ?? '-' }}</td>
              <td>{{ s.isCompleted ? '完成' : (s.status || '未完成') }}</td>
            </tr>
          </tbody>
        </table>
      </section>

    </div>
  </div>
</template>

<script>
import { getProjectSummary } from '../api/constructingProject';
export default {
  name: 'ProjectDetail',
  data() {
    return {
      loading: true,
      error: '',
      project: null,
      steps: [],
      milestones: [],
      deliverables: [],
      files: [],
      filesByDeliverableId: {},
      summaryLoaded: false,
    };
  },
  created() {
    this.loadSummary();
  },
  methods: {
    async loadSummary() {
      try {
        this.loading = true;
        const projectId = this.$route.params.projectId;
        const resp = await getProjectSummary(projectId);
        const payload = (resp && resp.data && resp.data.data) ? resp.data.data : (resp && resp.data ? resp.data : {});
        this.project = payload.project || payload.constructingProject || {};
        // 原始步骤包含产品下所有标准步骤 + 关联关系覆盖；按需求仅展示已生成的步骤（存在项目-步骤关系）
        const rawSteps = payload.steps || [];
        this.steps = rawSteps.filter(s => ('relationId' in s));
        this.milestones = payload.milestones || [];
        this.deliverables = payload.deliverables || [];
        this.files = payload.files || [];
        const grouped = {};
        this.files.forEach(f => {
          const did = f.deliverableId;
          if (!grouped[did]) grouped[did] = [];
          grouped[did].push(f);
        });
        this.filesByDeliverableId = grouped;
        this.summaryLoaded = true;
      } catch (err) {
        this.error = (err && err.message) ? err.message : '加载失败';
      } finally {
        this.loading = false;
      }
    },
    downloadURL(fileId) {
      return `${import.meta.env.VITE_API_BASE || 'http://localhost:8081'}/api/construct-deliverable-files/download/${fileId}`;
    },
    fileBaseName(path) {
      if (!path) return '未知文件';
      const parts = path.split(/[\\/]/);
      return parts[parts.length - 1];
    },
    prettySize(bytes) {
      if (!bytes && bytes !== 0) return '';
      const units = ['B','KB','MB','GB','TB'];
      let size = bytes, idx = 0;
      while (size >= 1024 && idx < units.length - 1) { size /= 1024; idx++; }
      return `${size.toFixed(1)} ${units[idx]}`;
    },
    formatDate(ts) {
      if (!ts) return '';
      try {
        const d = new Date(ts);
        return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
      } catch (_) { return ''; }
    },
    goBack() {
      this.$router.push('/home/construction');
    }
  }
}
</script>

<style scoped>
.project-detail-page { display:flex; flex-direction:column; height:100vh; overflow-y:auto; padding:12px; box-sizing:border-box; }
.topbar { display:flex; align-items:center; gap:12px; padding:8px 0; border-bottom:1px solid #eee; }
.back-btn { padding:6px 12px; border:1px solid #ddd; border-radius:4px; background:#fff; cursor:pointer; }
.title { flex:1; display:flex; align-items:baseline; gap:12px; font-size:18px; font-weight:600; }
.title .num { color:#666; font-size:13px; font-weight:400; }
.stats { display:flex; gap:8px; }
.chip { padding:4px 8px; background:#f5f5f5; border-radius:12px; font-size:12px; }
.state { padding:24px; color:#333; }
.state.error { color:#c00; }
.content-grid { display:grid; grid-template-columns: repeat(2, 1fr); gap:12px; padding-top:12px; overflow-x:auto; }
.card { background:#fff; border:1px solid #eee; border-radius:8px; padding:12px; }
.card.wide { grid-column: 1 / -1; }
.info-grid { display:grid; grid-template-columns: repeat(2, 1fr); gap:8px; }
.info-grid label { color:#888; font-size:12px; }
.list { list-style:none; padding:0; margin:0; }
.list-item { display:flex; align-items:center; justify-content:space-between; padding:6px 0; border-bottom:1px dashed #eee; }
.tag { font-size:12px; color:#666; }
.tag.done { color:#2f8f2f; }
.deliverables { display:flex; flex-direction:column; gap:12px; }
.deliverable-header { display:flex; align-items:center; justify-content:space-between; }
.deliverable-title { display:flex; align-items:center; gap:8px; }
.file-list { list-style:none; padding:0; margin:6px 0 0; }
.file-item { display:flex; align-items:center; gap:8px; padding:4px 0; border-bottom:1px dashed #eee; }
.file-link { color:#1677ff; text-decoration:none; }
.file-link:hover { text-decoration:underline; }
.size, .time { color:#888; font-size:12px; }
.empty { color:#888; padding:8px 0; }

/* 表格样式 */
.table { width:100%; border-collapse:collapse; }
.table th, .table td { padding:8px; border-bottom:1px dashed #eee; font-size:14px; text-align:left; }
.table thead th { background:#fafafa; font-weight:600; color:#333; }
</style>