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
      <section class="card">
        <h3>项目信息</h3>
        <div class="info-grid">
          <div><label>名称</label><div>{{ project?.projectName }}</div></div>
          <div><label>编号</label><div>{{ project?.projectNum }}</div></div>
          <div><label>产品</label><div>{{ project?.systemName }}</div></div>
          <div><label>状态</label><div>{{ project?.status }}</div></div>
          <div><label>创建时间</label><div>{{ formatDate(project?.createTime) }}</div></div>
        </div>
      </section>

      <section class="card">
        <h3>里程碑</h3>
        <ul class="list">
          <li v-for="m in milestones" :key="m.id" class="list-item">
            <span class="milestone-name">{{ m.milestoneName }}</span>
            <span class="tag" :class="{done: m.isCompleted}">{{ m.isCompleted ? '完成' : '未完成' }}</span>
          </li>
        </ul>
      </section>

      <section class="card">
        <h3>步骤</h3>
        <ul class="list">
          <li v-for="s in steps" :key="s.sstepId || s.nstepId" class="list-item">
            <span class="step-name">{{ s.sstepName || s.nstepName }}</span>
            <span class="tag">{{ s.type || '标准' }}</span>
          </li>
        </ul>
      </section>

      <section class="card wide">
        <h3>交付物与文件</h3>
        <div v-if="deliverables.length === 0" class="empty">暂无交付物</div>
        <div v-else class="deliverables">
          <div v-for="d in deliverables" :key="d.deliverableId" class="deliverable">
            <div class="deliverable-header">
              <div class="deliverable-title">
                <span class="name">{{ d.deliverableName || ('交付物 #' + d.deliverableId) }}</span>
                <span class="type" v-if="d.type">类型：{{ d.type }}</span>
              </div>
              <div class="count">文件：{{ (filesByDeliverableId[d.deliverableId] || []).length }}</div>
            </div>
            <ul class="file-list">
              <li v-for="f in (filesByDeliverableId[d.deliverableId] || [])" :key="f.fileId" class="file-item">
                <a :href="downloadURL(f.fileId)" target="_blank" class="file-link">{{ fileBaseName(f.filePath) }}</a>
                <span class="size" v-if="f.fileSize">{{ prettySize(f.fileSize) }}</span>
                <span class="time">{{ formatDate(f.createTime) }}</span>
              </li>
            </ul>
          </div>
        </div>
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
        const { data } = await getProjectSummary(projectId);
        this.project = data.project || data.constructingProject || {};
        this.steps = data.steps || [];
        this.milestones = data.milestones || [];
        this.deliverables = data.deliverables || [];
        this.files = data.files || [];
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
.project-detail-page { display:flex; flex-direction:column; height:100%; padding:12px; box-sizing:border-box; }
.topbar { display:flex; align-items:center; gap:12px; padding:8px 0; border-bottom:1px solid #eee; }
.back-btn { padding:6px 12px; border:1px solid #ddd; border-radius:4px; background:#fff; cursor:pointer; }
.title { flex:1; display:flex; align-items:baseline; gap:12px; font-size:18px; font-weight:600; }
.title .num { color:#666; font-size:13px; font-weight:400; }
.stats { display:flex; gap:8px; }
.chip { padding:4px 8px; background:#f5f5f5; border-radius:12px; font-size:12px; }
.state { padding:24px; color:#333; }
.state.error { color:#c00; }
.content-grid { display:grid; grid-template-columns: repeat(2, 1fr); gap:12px; padding-top:12px; }
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
</style>