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
        <h3>步骤与里程碑</h3>
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
            <template
              v-for="(row, idx) in combinedRows"
              :key="row.rowType === 'step'
                ? (row.sstepId || row.nstepId || row.relationId)
                : (row.rowType === 'milestone'
                  ? ('m-' + (row.milestoneId || row.milestoneName))
                  : (row.rowType === 'interface_step'
                    ? ('i-' + (row.relationId || (row.blockId + '-' + idx)))
                    : (row.rowType + '-' + (row.blockId || idx))))"
            >
              <tr v-if="row.rowType === 'step'">
                <td>{{ idx + 1 }}</td>
                <td>{{ row.sstepName || row.nstepName }}</td>
                <td>{{ row.type || '标准' }}</td>
                <td @dblclick="startEdit(row, 'director')">
                  <template v-if="isEditing(row, 'director')">
                    <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit" class="cell-input">
                      <option :value="null">-</option>
                      <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                        {{ u.name || u.userName }}
                      </option>
                    </select>
                  </template>
                  <template v-else>
                    {{ row.directorName ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planStartDate')">
                  <template v-if="isEditing(row, 'planStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')" @blur="commitEdit(row, 'planStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planEndDate')">
                  <template v-if="isEditing(row, 'planEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')" @blur="commitEdit(row, 'planEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planEndDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualStartDate')">
                  <template v-if="isEditing(row, 'actualStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')" @blur="commitEdit(row, 'actualStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualEndDate')">
                  <template v-if="isEditing(row, 'actualEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')" @blur="commitEdit(row, 'actualEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualEndDate ?? '-' }}
                  </template>
                </td>
                <td>
                  {{ row.planPeriod ?? '-' }}
                </td>
                <td>
                  {{ row.actualPeriod ?? '-' }}
                </td>
                <td>{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</td>
              </tr>
              <!-- 接口基本信息展示行 -->
              <tr v-else-if="row.rowType === 'interface_info'" class="interface-info-row">
                <td>{{ idx + 1 }}</td>
                <td>接口：{{ row.integrationSysName }}（{{ row.interfaceType }}）</td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
              </tr>
              <!-- 个性化需求基本信息展示行 -->
              <tr v-else-if="row.rowType === 'personal_info'" class="personal-info-row">
                <td>{{ idx + 1 }}</td>
                <td>个性化需求：{{ row.personalDevName }}</td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
              </tr>
              <!-- 接口开发步骤（支持双击编辑） -->
              <tr v-else-if="row.rowType === 'interface_step'" class="interface-step-row">
                <td>{{ idx + 1 }}</td>
                <td>{{ row.sstepName }}</td>
                <td>{{ row.type }}</td>
                <td @dblclick="startEdit(row, 'director')">
                  <template v-if="isEditing(row, 'director')">
                    <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit" class="cell-input">
                      <option :value="null">-</option>
                      <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                        {{ u.name || u.userName }}
                      </option>
                    </select>
                  </template>
                  <template v-else>
                    {{ row.directorName ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planStartDate')">
                  <template v-if="isEditing(row, 'planStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')" @blur="commitEdit(row, 'planStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planEndDate')">
                  <template v-if="isEditing(row, 'planEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')" @blur="commitEdit(row, 'planEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planEndDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualStartDate')">
                  <template v-if="isEditing(row, 'actualStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')" @blur="commitEdit(row, 'actualStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualEndDate')">
                  <template v-if="isEditing(row, 'actualEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')" @blur="commitEdit(row, 'actualEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualEndDate ?? '-' }}
                  </template>
                </td>
                <td>
                  {{ row.planPeriod ?? '-' }}
                </td>
                <td>
                  {{ row.actualPeriod ?? '-' }}
                </td>
                <td>{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</td>
              </tr>
              <!-- 个性化开发步骤（支持双击编辑） -->
              <tr v-else-if="row.rowType === 'personal_step'" class="personal-step-row">
                <td>{{ idx + 1 }}</td>
                <td>{{ row.sstepName }}</td>
                <td>{{ row.type }}</td>
                <td @dblclick="startEdit(row, 'director')">
                  <template v-if="isEditing(row, 'director')">
                    <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit" class="cell-input">
                      <option :value="null">-</option>
                      <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                        {{ u.name || u.userName }}
                      </option>
                    </select>
                  </template>
                  <template v-else>
                    {{ row.directorName ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planStartDate')">
                  <template v-if="isEditing(row, 'planStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')" @blur="commitEdit(row, 'planStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'planEndDate')">
                  <template v-if="isEditing(row, 'planEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')" @blur="commitEdit(row, 'planEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.planEndDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualStartDate')">
                  <template v-if="isEditing(row, 'actualStartDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')" @blur="commitEdit(row, 'actualStartDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualStartDate ?? '-' }}
                  </template>
                </td>
                <td @dblclick="startEdit(row, 'actualEndDate')">
                  <template v-if="isEditing(row, 'actualEndDate')">
                    <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')" @blur="commitEdit(row, 'actualEndDate')" class="cell-input"/>
                  </template>
                  <template v-else>
                    {{ row.actualEndDate ?? '-' }}
                  </template>
                </td>
                <td>
                  {{ row.planPeriod ?? '-' }}
                </td>
                <td>
                  {{ row.actualPeriod ?? '-' }}
                </td>
                <td>{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</td>
              </tr>
              <!-- 添加接口按钮行（位于目标里程碑上一行） -->
              <tr v-else-if="row.rowType === 'add_interface'" class="add-interface-row">
                <td>{{ idx + 1 }}</td>
                <td colspan="10">
                  <button class="add-interface-btn" @click="openInterfaceDialog">添加接口</button>
                </td>
              </tr>
              <!-- 添加个性化需求按钮行（位于目标里程碑上一行） -->
              <tr v-else-if="row.rowType === 'add_personal'" class="add-personal-row">
                <td>{{ idx + 1 }}</td>
                <td colspan="10">
                  <button class="add-personal-btn" @click="openPersonalDialog">添加个性化需求</button>
                </td>
              </tr>
              <tr v-else class="milestone-row">
                <td>{{ idx + 1 }}</td>
                <td>【里程碑】{{ row.milestoneName }}</td>
                <td>里程碑</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>{{ row.milestonePeriod ?? '-' }}</td>
                <td>{{ row.iscomplete ? '完成' : '未完成' }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </section>

      <!-- 新增接口弹窗 -->
      <div v-if="showInterfaceDialog" class="dialog-mask" @click.self="closeInterfaceDialog">
        <div class="dialog">
          <h4>新增接口</h4>
          <div class="form-row">
            <label>对方系统名称 <span class="required">*</span></label>
            <input type="text" v-model.trim="interfaceForm.integrationSysName" placeholder="请输入对方系统名称" />
          </div>
          <div class="form-row">
            <label>对方系统厂商 <span class="required">*</span></label>
            <input type="text" v-model.trim="interfaceForm.integrationSysManufacturer" placeholder="请输入对方系统厂商" />
          </div>
          <div class="form-row">
            <label>接口类型</label>
            <select v-model="interfaceForm.interfaceType">
              <option value="">请选择接口类型</option>
              <option v-for="opt in interfaceTypeOptions" :key="opt" :value="opt">{{ opt }}</option>
              <option value="__custom__">自定义</option>
            </select>
          </div>
          <div class="form-row" v-if="interfaceForm.interfaceType === '__custom__'">
            <label>自定义类型 <span class="required">*</span></label>
            <input type="text" v-model.trim="interfaceForm.customType" placeholder="请输入接口类型" />
          </div>
          <div class="dialog-actions">
            <button class="btn" @click="confirmInterface">确定</button>
            <button class="btn ghost" @click="closeInterfaceDialog">取消</button>
          </div>
        </div>
      </div>

      <!-- 新增个性化需求弹窗 -->
      <div v-if="showPersonalDialog" class="dialog-mask" @click.self="closePersonalDialog">
        <div class="dialog">
          <h4>新增个性化需求</h4>
          <div class="form-row">
            <label>需求名称 <span class="required">*</span></label>
            <input type="text" v-model.trim="personalForm.personalDevName" placeholder="请输入个性化需求名称" />
          </div>
          <div class="dialog-actions">
            <button class="btn" @click="confirmPersonal">确定</button>
            <button class="btn ghost" @click="closePersonalDialog">取消</button>
          </div>
        </div>
      </div>

      

    </div>
  </div>
</template>

<script>
import { getProjectSummary } from '../api/constructingProject';
import { updateProjectRelation } from '../api/projectRelation';
import { getAllUsers } from '../api/user';
import { getAllStandardMilestones } from '../api/standardMilestone';
import { createInterface, listInterfacesByProject } from '../api/interface';
import { createPersonalDevelope, listPersonalDevelopesByProject } from '../api/personalDevelope';
export default {
  name: 'ProjectDetail',
  /**
   * 类级注释：
   * 项目详情组件，展示项目步骤、里程碑、交付物与文件。
   * 渲染规则：
   * - 步骤与里程碑按标准里程碑顺序排序展示（使用标准里程碑名称正序）。
   * - 当项目建设内容包含“接口开发”时，在“05完成接口开发集成”里程碑之前插入接口信息块与“添加接口”按钮；
   *   若该里程碑尚未由后端生成，也仍按排序位置插入入口与占位里程碑行，避免接口入口落在页面最下方。
   */
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
      // 编辑状态
      editing: { relationId: null, field: null },
      editValue: null,
      allUsers: [],
      // 标准里程碑映射
      standardMilestones: [],
      milestoneNameById: {},
      // 接口新增弹窗与块数据
      showInterfaceDialog: false,
      interfaceForm: {
        integrationSysName: '',
        integrationSysManufacturer: '',
        interfaceType: '',
        customType: ''
      },
      interfaceTypeOptions: ['数据归档接口', '单点登录', '组织机构和用户同步', '待办消息集成'],
      interfaceBlocks: [], // { id, integrationSysName, interfaceType }
      // 个性化新增弹窗与块数据
      showPersonalDialog: false,
      personalForm: {
        personalDevName: ''
      },
      personalBlocks: [] // { id, personalDevName }
    };
  },
  computed: {
    // 将步骤与里程碑合并后用于渲染的行
    /*
     * 函数级注释：
     * 组合并排序步骤与里程碑：
     * - 先按标准里程碑名称正序确定渲染顺序；
     * - 每个里程碑分组内先展示非“接口开发”的步骤（保留“接口需求调研”），再展示对应的里程碑行；
     * - 若勾选“接口开发”，在“05完成接口开发集成”里程碑之前插入接口信息块与“添加接口”按钮；
     * - 若该里程碑未生成但存在于项目里程碑列表，仍按排序位置插入接口入口与占位里程碑行；
     * - 保证接口入口不落在页面最下方。
     */
    combinedRows() {
      const rows = []
      if (!Array.isArray(this.steps) || this.steps.length === 0) return rows

      // 是否勾选了“接口开发”建设内容
      const includeInterfaceDev = !!(this.project && typeof this.project.constructContent === 'string' && this.project.constructContent.includes('接口开发'))
      // 是否勾选了“个性化功能开发”建设内容
      const includePersonalDev = !!(this.project && typeof this.project.constructContent === 'string' && this.project.constructContent.includes('个性化功能开发'))

      // 标准里程碑名称顺序（正序）与ID->名称映射
      const standardOrder = (this.standardMilestones || []).map(m => m.milestoneName).filter(n => !!n)
      const orderIdx = new Map(standardOrder.map((n, i) => [n, i]))
      const nameById = this.milestoneNameById || {}

      // 分组步骤：按标准里程碑名称
      const groupsByName = new Map()
      for (const s of this.steps) {
        const mid = s.smilestoneId ?? null
        const name = mid != null ? nameById[mid] : null
        if (!name) continue
        if (!groupsByName.has(name)) groupsByName.set(name, [])
        groupsByName.get(name).push(s)
      }

      // 待渲染的里程碑名称集合（存在步骤的 + 必要的接口里程碑）
      const existingNames = Array.from(groupsByName.keys())
      const targetName = '05完成接口开发集成'
      const pmExists = (this.milestones || []).some(m => m.milestoneName === targetName)
      if (includeInterfaceDev && pmExists && !existingNames.includes(targetName)) {
        existingNames.push(targetName)
      }
      // 个性化开发目标里程碑
      const personalTargetName = '06完成个性化功能开发'
      const pmPersonalExists = (this.milestones || []).some(m => m.milestoneName === personalTargetName)
      if (includePersonalDev && pmPersonalExists && !existingNames.includes(personalTargetName)) {
        existingNames.push(personalTargetName)
      }

      // 按标准里程碑顺序排序
      existingNames.sort((a, b) => (orderIdx.get(a) ?? Number.MAX_SAFE_INTEGER) - (orderIdx.get(b) ?? Number.MAX_SAFE_INTEGER))

      // 渲染各里程碑分组
      for (const name of existingNames) {
        const list = groupsByName.get(name) || []
        // 非“接口开发/个性化功能开发”的步骤（保留接口需求调研）
        const nonInterfaceSteps = list.filter(s => {
          if (s.type === '接口开发') {
            const stepName = s.sstepName || s.nstepName || ''
            const keepDemandResearch = includeInterfaceDev && stepName.includes('业务系统接口需求调研')
            return keepDemandResearch
          }
          if (s.type === '个性化功能开发') {
            return false
          }
          return true
        })
        for (const s of nonInterfaceSteps) {
          rows.push({ ...s, rowType: 'step' })
        }

        // 接口开发步骤与入口（仅在目标里程碑前插入）
        const interfaceSteps = list.filter(s => s.type === '接口开发')
        if (includeInterfaceDev && name === targetName) {
          for (const blk of this.interfaceBlocks) {
            // 接口块信息
            rows.push({ rowType: 'interface_info', blockId: blk.id, integrationSysName: blk.integrationSysName, interfaceType: blk.interfaceType })
            // 仅渲染属于该接口的步骤，避免重复渲染导致编辑串联
            const stepsForBlock = interfaceSteps.filter(s => s.interfaceId === blk.id)
            for (const s of stepsForBlock) {
              rows.push({ ...s, rowType: 'interface_step', blockId: blk.id })
            }
          }
          rows.push({ rowType: 'add_interface' })
        }

        // 个性化功能开发步骤与入口（仅在目标里程碑前插入）
        const personalSteps = list.filter(s => s.type === '个性化功能开发')
        if (includePersonalDev && name === personalTargetName) {
          for (const blk of this.personalBlocks) {
            // 个性化需求块信息
            rows.push({ rowType: 'personal_info', blockId: blk.id, personalDevName: blk.personalDevName })
            // 仅渲染属于该个性化需求的步骤
            const stepsForBlock = personalSteps.filter(s => s.personalDevId === blk.id)
            for (const s of stepsForBlock) {
              rows.push({ ...s, rowType: 'personal_step', blockId: blk.id })
            }
          }
          rows.push({ rowType: 'add_personal' })
        }

        // 计算实际工期汇总
        const actualVals = list
          .map(s => {
            const v = Number(s.actualPeriod)
            return Number.isFinite(v) ? v : null
          })
          .filter(v => v !== null)
        const sumActual = actualVals.length > 0 ? actualVals.reduce((a, b) => a + b, 0) : null

        // 里程碑行（若未生成则插入占位）
        const pm = (this.milestones || []).find(m => m.milestoneName === name)
        if (pm) {
          rows.push({ ...pm, milestoneName: name, milestonePeriod: sumActual, rowType: 'milestone' })
        } else {
          rows.push({ milestoneName: name, milestoneId: `placeholder-${name}`, milestonePeriod: sumActual, rowType: 'milestone' })
        }
      }

      return rows
    }
  },
  created() {
    this.loadSummary();
    this.loadUsers();
    this.loadStandardMilestones();
  },
  mounted() {
    // 点击页面其他区域时，取消当前单元格的编辑显示（不更改提交方式）
    document.addEventListener('click', this.onDocumentClick);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.onDocumentClick);
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
        // 加载该项目下已保存的接口信息，用于展示接口块与对应步骤
        if (this.project && this.project.projectId) {
          try {
            const resp2 = await listInterfacesByProject(this.project.projectId);
            const list = Array.isArray(resp2?.data) ? resp2.data : (resp2?.data?.interfaces || []);
            this.interfaceBlocks = (list || []).map(it => ({
              id: it.interfaceId,
              integrationSysName: it.integrationSysName,
              interfaceType: it.interfaceType || '未指定类型'
            }));
          } catch (e) {
            // 保持为空，不影响其他加载流程
            this.interfaceBlocks = [];
          }
          // 加载该项目下的个性化需求信息，用于展示个性化块与对应步骤
          try {
            const resp3 = await listPersonalDevelopesByProject(this.project.projectId);
            const list2 = Array.isArray(resp3?.data) ? resp3.data : (resp3?.data?.personalDevelopes || []);
            this.personalBlocks = (list2 || []).map(it => ({
              id: it.personalDevId,
              personalDevName: it.personalDevName || '未命名需求'
            }));
          } catch (e) {
            this.personalBlocks = [];
          }
        }
      } catch (err) {
        this.error = (err && err.message) ? err.message : '加载失败';
      } finally {
        this.loading = false;
      }
    },
    async loadStandardMilestones() {
      try {
        const resp = await getAllStandardMilestones();
        const list = (resp && resp.milestones) ? resp.milestones : [];
        this.standardMilestones = list;
        const map = {};
        list.forEach(m => { if (m && m.milestoneId != null) map[m.milestoneId] = m.milestoneName; });
        this.milestoneNameById = map;
      } catch (e) {
        this.standardMilestones = [];
        this.milestoneNameById = {};
      }
    },
    async loadUsers() {
      try {
        this.allUsers = await getAllUsers();
      } catch (e) {
        this.allUsers = [];
      }
    },
    isEditing(step, field) {
      return this.editing.relationId === step.relationId && this.editing.field === field;
    },
    startEdit(step, field) {
      if (!step.relationId) return;
      // 规则3：当计划开始日期为空时，不允许填写计划结束日期
      if (field === 'planEndDate' && (!step.planStartDate || step.planStartDate === '')) {
        this.showError('请先填写计划开始日期，再填写计划结束日期');
        return;
      }
      // 规则4：当实际开始日期为空时，不允许填写实际结束日期
      if (field === 'actualEndDate' && (!step.actualStartDate || step.actualStartDate === '')) {
        this.showError('请先填写实际开始日期，再填写实际结束日期');
        return;
      }
      this.editing = { relationId: step.relationId, field };
      if (field === 'director') {
        this.editValue = step.director ?? null;
      } else {
        this.editValue = step[field] ?? '';
      }
    },
    cancelEdit() {
      this.editing = { relationId: null, field: null };
      this.editValue = null;
    },
    onDocumentClick(e) {
      // 仅在处于编辑状态且点击在编辑输入框之外时，取消编辑显示
      if (!this.editing || this.editing.relationId == null) return;
      const editor = this.$el.querySelector('.cell-input');
      if (!editor) {
        // 没有找到编辑输入框，直接取消编辑显示
        this.cancelEdit();
        return;
      }
      if (editor === e.target || editor.contains(e.target)) {
        // 点击在当前编辑框内，保持编辑状态
        return;
      }
      // 点击在外部区域，取消编辑显示（提交逻辑保持原样：日期在 blur 时提交；负责人在 change 时提交）
      this.cancelEdit();
    },
    async commitEdit(step, field) {
      if (!this.isEditing(step, field)) return;
      try {
        const relationId = step.relationId;
        const payload = {};
        let val = this.editValue;
        if (val === '') val = null;
        payload[field] = val;

        // 基础校验函数
        const pairValid = (start, end) => {
          if (!start || !end) return true; // 只有都存在时才校验先后
          try {
            const s = new Date(`${start}T00:00:00`).getTime();
            const e = new Date(`${end}T00:00:00`).getTime();
            if (isNaN(s) || isNaN(e)) return false;
            return e >= s;
          } catch (_) { return false; }
        };

        // 规则3&4：当开始日期为空时，禁止填写结束日期
        if (field === 'planEndDate' && val && (!step.planStartDate || step.planStartDate === '')) {
          this.showError('计划开始日期为空，不能填写计划结束日期');
          return this.cancelEdit();
        }
        if (field === 'actualEndDate' && val && (!step.actualStartDate || step.actualStartDate === '')) {
          this.showError('实际开始日期为空，不能填写实际结束日期');
          return this.cancelEdit();
        }

        // 规则1：实际结束不得早于实际开始；规则2：计划结束不得早于计划开始
        if (field === 'planEndDate') {
          const start = step.planStartDate;
          if (!pairValid(start, val)) {
            this.showError('计划结束日期不能早于计划开始日期');
            return this.cancelEdit();
          }
        }
        if (field === 'planStartDate') {
          const end = step.planEndDate;
          if (val && end && !pairValid(val, end)) {
            this.showError('计划开始日期不能晚于当前计划结束日期');
            return this.cancelEdit();
          }
        }
        if (field === 'actualEndDate') {
          const start = step.actualStartDate;
          if (!pairValid(start, val)) {
            this.showError('实际结束日期不能早于实际开始日期');
            return this.cancelEdit();
          }
        }
        if (field === 'actualStartDate') {
          const end = step.actualEndDate;
          if (val && end && !pairValid(val, end)) {
            this.showError('实际开始日期不能晚于当前实际结束日期');
            return this.cancelEdit();
          }
        }

        // 如果是日期字段，自动计算并同时提交工期
        if (field === 'planStartDate' || field === 'planEndDate') {
          const start = field === 'planStartDate' ? val : step.planStartDate;
          const end = field === 'planEndDate' ? val : step.planEndDate;
          const days = this.calcDays(start, end);
          payload.planPeriod = days;
        }
        if (field === 'actualStartDate' || field === 'actualEndDate') {
          const start = field === 'actualStartDate' ? val : step.actualStartDate;
          const end = field === 'actualEndDate' ? val : step.actualEndDate;
          const days = this.calcDays(start, end);
          payload.actualPeriod = days;
          // 同步提交步骤状态到后端
          if (!start) {
            payload.stepStatus = '未开始';
          } else if (!end) {
            payload.stepStatus = '进行中';
          } else {
            payload.stepStatus = '已完成';
          }
        }

        await updateProjectRelation(relationId, payload);

        // 前端状态更新
        if (field === 'director') {
          step.director = val || null;
          const u = this.allUsers.find(x => x.userId === val);
          step.directorName = u ? (u.name || u.userName) : null;
        } else {
          step[field] = val || null;
          if (field === 'planStartDate' || field === 'planEndDate') {
            step.planPeriod = payload.planPeriod ?? null;
          }
            if (field === 'actualStartDate' || field === 'actualEndDate') {
              step.actualPeriod = payload.actualPeriod ?? null;
              // 规则5：根据实际开始/结束日期更新步骤状态
              this.updateStepStatus(step);
            }
          }

        // 同步更新原始 steps 列表中的对应项，触发 combinedRows 重新计算
        const src = this.steps.find(s => s.relationId === relationId);
        if (src) {
          if (field === 'director') {
            src.director = step.director;
            src.directorName = step.directorName;
          } else {
            src[field] = step[field];
            if (field === 'planStartDate' || field === 'planEndDate') {
              src.planPeriod = step.planPeriod;
            }
            if (field === 'actualStartDate' || field === 'actualEndDate') {
              src.actualPeriod = step.actualPeriod;
              src.status = step.status;
              src.stepStatus = step.stepStatus;
              src.isCompleted = step.isCompleted;
            }
          }
        }
      } catch (e) {
        this.$message && this.$message.error('更新失败: ' + (e?.message || '未知错误'));
      } finally {
        this.cancelEdit();
      }
    },
    // 按规则5更新步骤状态
    updateStepStatus(step) {
      const hasStart = !!step.actualStartDate;
      const hasEnd = !!step.actualEndDate;
      if (!hasStart) {
        step.status = '未开始';
        step.stepStatus = '未开始';
        step.isCompleted = false;
      } else if (!hasEnd) {
        step.status = '进行中';
        step.stepStatus = '进行中';
        step.isCompleted = false;
      } else {
        step.status = '已完成';
        step.stepStatus = '已完成';
        step.isCompleted = true;
      }
    },
    showError(msg) {
      if (this.$message && this.$message.error) this.$message.error(msg);
      else alert(msg);
    },
    // 计算两个日期之间的天数（含首尾，若无效返回null）
    calcDays(start, end) {
      if (!start || !end) return null;
      try {
        const s = new Date(`${start}T00:00:00`);
        const e = new Date(`${end}T00:00:00`);
        const diffMs = e.getTime() - s.getTime();
        if (isNaN(diffMs)) return null;
        const d = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        return d >= 0 ? d + 1 : null; // 含首尾天数
      } catch (_) {
        return null;
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
    },
    // 接口新增弹窗控制
    openInterfaceDialog() {
      this.showInterfaceDialog = true
      this.interfaceForm.integrationSysName = ''
      this.interfaceForm.integrationSysManufacturer = ''
      this.interfaceForm.interfaceType = ''
      this.interfaceForm.customType = ''
    },
    closeInterfaceDialog() {
      this.showInterfaceDialog = false
    },
    // 个性化新增弹窗控制
    openPersonalDialog() {
      this.showPersonalDialog = true
      this.personalForm.personalDevName = ''
    },
    closePersonalDialog() {
      this.showPersonalDialog = false
    },
    /**
     * 函数级注释：
     * 确认新增接口并刷新展示：
     * - 校验表单并调用后端创建接口记录；
     * - 成功后关闭弹窗，并调用 loadSummary 重新拉取项目步骤、里程碑与接口块；
     * - 保证新增接口生成的标准开发步骤在“步骤与里程碑”表格中即时展示。
     */
    async confirmInterface() {
      const name = (this.interfaceForm.integrationSysName || '').trim()
      const manufacturer = (this.interfaceForm.integrationSysManufacturer || '').trim()
      const typeSel = this.interfaceForm.interfaceType
      const custom = (this.interfaceForm.customType || '').trim()
      if (!name) {
        return this.showError('请输入对方系统名称')
      }
      if (!manufacturer) {
        return this.showError('请输入对方系统厂商')
      }
      let type = ''
      if (typeSel === '__custom__') {
        if (!custom) return this.showError('请输入自定义接口类型')
        type = custom
      } else {
        type = typeSel || '未指定类型'
      }
      try {
        const projectId = this.project?.projectId
        if (!projectId) {
          return this.showError('项目ID缺失，无法保存接口')
        }
        // 里程碑：选择“05完成接口开发集成”
        const milestone = (this.milestones || []).find(m => m.milestoneName === '05完成接口开发集成')
        const milestoneId = milestone?.milestoneId ?? null
        if (!milestoneId) {
          return this.showError('未找到接口开发对应的里程碑，请检查标准配置')
        }

        const payload = {
          projectId,
          interfaceType: type,
          integrationSysName: name,
          integrationSysManufacturer: manufacturer,
          milestoneId
        }
        const resp = await createInterface(payload)
        const data = resp?.data || {}
        const created = data.interface || data
        const id = created?.interfaceId || Date.now()
        this.interfaceBlocks.push({ id, integrationSysName: name, interfaceType: type })
        this.$message && this.$message.success('接口已保存')
        // 关闭弹窗后刷新项目摘要，确保新生成的接口步骤与块即时呈现
        this.closeInterfaceDialog()
        await this.loadSummary()
      } catch (e) {
        this.showError('保存接口失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
    }
    ,
    /**
     * 函数级注释：
     * 确认新增个性化需求并刷新展示：
     * - 校验名称并调用后端创建个性化开发记录；
     * - 成功后关闭弹窗，并调用 loadSummary 重新拉取项目步骤、里程碑与个性化块；
     * - 保证新增个性化需求生成的标准开发步骤在“步骤与里程碑”表格中即时展示。
     */
    async confirmPersonal() {
      const name = (this.personalForm.personalDevName || '').trim()
      if (!name) {
        return this.showError('请输入个性化需求名称')
      }
      try {
        const projectId = this.project?.projectId
        if (!projectId) {
          return this.showError('项目ID缺失，无法保存个性化需求')
        }
        // 里程碑：选择“06完成个性化功能开发”
        const milestone = (this.milestones || []).find(m => m.milestoneName === '06完成个性化功能开发')
        const milestoneId = milestone?.milestoneId ?? null
        if (!milestoneId) {
          return this.showError('未找到个性化功能开发对应的里程碑，请检查标准配置')
        }

        const payload = {
          projectId,
          milestoneId,
          personalDevName: name
        }
        const resp = await createPersonalDevelope(payload)
        const data = resp?.data || {}
        const created = data.personalDevelope || data
        const id = created?.personalDevId || Date.now()
        this.personalBlocks.push({ id, personalDevName: name })
        this.$message && this.$message.success('个性化需求已保存')
        // 关闭弹窗后刷新项目摘要，确保新生成的个性化步骤与块即时呈现
        this.closePersonalDialog()
        await this.loadSummary()
      } catch (e) {
        this.showError('保存个性化需求失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
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
.table td { position: relative; }
.table thead th { background:#fafafa; font-weight:600; color:#333; }
.cell-input {
  position: absolute;
  left: 8px;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: auto;
  box-sizing: border-box;
  padding: 0 8px;
  font-size: 14px;
  line-height: 24px;
  height: 24px;
  border: 1px solid #ddd;
  background: #fff;
  z-index: 2;
}
.milestone-row { color:#c00; background:#fff5f5; }
/* 接口相关样式与弹窗 */
.interface-info-row { background:#f7fbff; color:#0a65c2; }
.interface-step-row { background:#fafdff; }
.add-interface-row { background:#f7fbff; }
.add-interface-btn { padding:6px 12px; border:1px solid #1677ff; background:#1677ff; color:#fff; border-radius:4px; cursor:pointer; }
.add-interface-btn:hover { background:#0f5fd6; }
.personal-info-row { background:#f7fbff; color:#0a65c2; }
.personal-step-row { background:#fafdff; }
.add-personal-row { background:#f7fbff; }
.add-personal-btn { padding:6px 12px; border:1px solid #1677ff; background:#1677ff; color:#fff; border-radius:4px; cursor:pointer; }
.add-personal-btn:hover { background:#0f5fd6; }
.dialog-mask { position:fixed; inset:0; background:rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; z-index:1000; }
.dialog { width:420px; background:#fff; border-radius:8px; border:1px solid #eee; padding:16px; }
.dialog h4 { margin:0 0 12px; }
.form-row { display:flex; flex-direction:column; gap:6px; margin-bottom:10px; }
.form-row input, .form-row select { height:32px; padding:0 8px; border:1px solid #ddd; border-radius:4px; }
.required { color:#c00; }
.dialog-actions { display:flex; gap:8px; justify-content:flex-end; margin-top:12px; }
.btn { padding:6px 12px; border:1px solid #ddd; background:#fff; border-radius:4px; cursor:pointer; }
.btn.ghost { background:#f5f7fa; }
</style>