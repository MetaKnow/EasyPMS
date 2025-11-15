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
              <th width="160">交付物管理</th>
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
                <td class="deliverable-actions">
                  <template v-if="shouldShowDeliverableActions(row)">
                    <div class="actions-inner">
                      <button class="icon-btn" title="查看" @click="onViewDeliverables(row)" aria-label="查看交付物">
                        <svg viewBox="0 0 24 24"><path d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z"/></svg>
                      </button>
                      <button class="icon-btn" title="上传" @click="onUploadDeliverable(row)" aria-label="上传交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z"/></svg>
                      </button>
                      <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                      </button>
                    </div>
                  </template>
                </td>
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
                <td>
                  <button class="btn ghost" @click="onDeleteInterface(row.blockId)">删除</button>
                </td>
                <td class="deliverable-actions"></td>
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
                <td>
                  <button class="btn ghost" @click="onDeletePersonal(row.blockId)">删除</button>
                </td>
                <td class="deliverable-actions"></td>
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
                <td class="deliverable-actions">
                  <template v-if="shouldShowDeliverableActions(row)">
                    <div class="actions-inner">
                      <button class="icon-btn" title="查看" @click="onViewDeliverables(row)" aria-label="查看交付物">
                        <svg viewBox="0 0 24 24"><path d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z"/></svg>
                      </button>
                      <button class="icon-btn" title="上传" @click="onUploadDeliverable(row)" aria-label="上传交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z"/></svg>
                      </button>
                      <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                      </button>
                    </div>
                  </template>
                </td>
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
                <td class="deliverable-actions">
                  <template v-if="shouldShowDeliverableActions(row)">
                    <div class="actions-inner">
                      <button class="icon-btn" title="查看" @click="onViewDeliverables(row)" aria-label="查看交付物">
                        <svg viewBox="0 0 24 24"><path d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z"/></svg>
                      </button>
                      <button class="icon-btn" title="上传" @click="onUploadDeliverable(row)" aria-label="上传交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z"/></svg>
                      </button>
                      <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                      </button>
                    </div>
                  </template>
                </td>
              </tr>
              <!-- 添加接口按钮行（位于目标里程碑上一行） -->
              <tr v-else-if="row.rowType === 'add_interface'" class="add-interface-row">
                <td>{{ idx + 1 }}</td>
                <td colspan="11">
                  <button class="add-interface-btn" @click="openInterfaceDialog">添加接口</button>
                </td>
              </tr>
              <!-- 添加个性化需求按钮行（位于目标里程碑上一行） -->
              <tr v-else-if="row.rowType === 'add_personal'" class="add-personal-row">
                <td>{{ idx + 1 }}</td>
                <td colspan="11">
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
                <td class="deliverable-actions">
                  <div class="actions-inner">
                    <button class="icon-btn" title="查看" @click="onViewDeliverables(row)" aria-label="查看交付物">
                      <svg viewBox="0 0 24 24"><path d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z"/></svg>
                    </button>
                    <button class="icon-btn" title="上传" @click="onUploadDeliverable(row)" aria-label="上传交付物">
                      <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z"/></svg>
                    </button>
                    <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                      <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                    </button>
                  </div>
                </td>
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

      <!-- 交付物查看弹窗（轻量版） -->
      <div v-if="showDeliverableDialog" class="dialog-mask" @click.self="closeDeliverableDialog">
        <div class="dialog">
          <h4>交付物列表</h4>
          <div class="form-row">
            <div v-if="deliverableLoading">正在加载交付物...</div>
            <div v-else-if="deliverableError" class="state error">{{ deliverableError }}</div>
            <div v-else>
              <div v-if="!deliverableList || deliverableList.length === 0" style="color:#999">当前未配置可查看的交付物</div>
              <ul v-else class="deliverable-list">
                <li v-for="d in deliverableList" :key="d.deliverableId || d.id">
                  <span class="name">{{ d.deliverableName || d.name }}</span>
                  <span class="type">{{ d.deliverableType || '-' }}</span>
                  <template v-if="deliverableDialogMode === 'download'">
                    <button class="icon-btn" title="下载模板" @click="downloadFirstTemplate(d)" aria-label="下载模板">
                      <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                    </button>
                  </template>
                </li>
              </ul>
            </div>
          </div>
          <div class="dialog-actions">
            <button class="btn" @click="closeDeliverableDialog">关闭</button>
          </div>
        </div>
      </div>

      <!-- 交付物上传弹窗 -->
      <div v-if="showUploadDialog" class="dialog-mask" @click.self="closeUploadDialog">
        <div class="dialog upload-dialog">
          <h4>上传交付物</h4>
          <div class="form-row">
            <div v-if="uploadLoading">正在准备上传上下文...</div>
            <div v-else-if="uploadError" class="state error">{{ uploadError }}</div>
            <div v-else>
              <div v-if="!uploadDeliverables || uploadDeliverables.length === 0" style="color:#999">当前步骤/里程碑下没有交付物</div>
              <div v-else class="upload-list">
                <div v-for="d in uploadDeliverables" :key="d.deliverableId" class="upload-item">
                  <div class="upload-head">
                    <span class="name">{{ d.deliverableName }}</span>
                    <span class="hint">支持多文件上传</span>
                  </div>
                  <div class="template-chips" v-if="uploadTemplatesByDeliverableId[d.deliverableId]?.length">
                    <div class="template-title">模板：</div>
                    <div class="chip-group">
                      <button type="button" class="chip clickable" v-for="t in uploadTemplatesByDeliverableId[d.deliverableId]" :key="t.name" @click="downloadTemplateForDialog(d.deliverableId, t.name)">
                        <span class="chip-name">{{ t.name }}</span>
                      </button>
                    </div>
                  </div>
                  <div class="upload-actions">
                    <label class="btn primary" :class="{ disabled: uploadingByDeliverableId[d.deliverableId] }" :for="`file-input-${d.deliverableId}`">选择文件</label>
                    <input :id="`file-input-${d.deliverableId}`" type="file" multiple :disabled="uploadingByDeliverableId[d.deliverableId]" @change="handleUploadFileSelectedForDeliverable(d, $event)" hidden />
                    <div class="progress" v-if="uploadingByDeliverableId[d.deliverableId]">
                      <div class="bar" :style="{ width: (uploadProgressByDeliverableId[d.deliverableId] || 0) + '%' }"></div>
                      <span class="percent">{{ uploadProgressByDeliverableId[d.deliverableId] || 0 }}%</span>
                    </div>
                  </div>
                  <div class="uploaded-list" v-if="uploadedFilesByDeliverableId[d.deliverableId]?.length">
                    <div class="template-title">已上传文件：</div>
                    <ul class="file-list compact">
                      <li v-for="f in uploadedFilesByDeliverableId[d.deliverableId]" :key="f.fileId" class="file-item">
                        <button type="button" class="file-link preview-link" @click="onPreviewFile(f)">{{ fileBaseName(f.filePath) }}</button>
                        <span class="size">{{ prettySize(f.fileSize) }}</span>
                        <button class="icon-btn danger" title="删除" @click="deleteUploadedFile(f.fileId, d.deliverableId)">
                          <svg viewBox="0 0 24 24"><path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z"/></svg>
                        </button>
                        <a class="icon-btn" :href="downloadURL(f.fileId)" :download="fileBaseName(f.filePath)" title="下载" target="_blank">
                          <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z"/></svg>
                        </a>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="dialog-actions">
            <button class="btn" @click="closeUploadDialog">关闭</button>
          </div>
        </div>
      </div>

      <!-- 文件名点击进行预览，保留右侧下载图标 -->

      <!-- 隐藏上传输入 -->
      <input ref="deliverableUploader" type="file" multiple style="display:none" @change="handleDeliverableFilesSelected" />

      <!-- 全屏文件预览弹窗 -->
      <div v-if="showPreviewDialog" class="preview-overlay">
      <div class="preview-header">
        <span class="title">{{ previewTitle }}</span>
        <div class="tools">
          <button v-if="previewType !== 'pdf' && previewType !== 'sheet'" class="icon-btn" title="缩小" @click="pdfZoomOut">
            <svg viewBox="0 0 24 24"><path d="M19 13H5v-2h14v2z"/></svg>
          </button>
          <button v-if="previewType !== 'pdf' && previewType !== 'video'" class="icon-btn" title="放大" @click="pdfZoomIn">
            <svg viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
          </button>
          <button class="icon-btn" title="关闭" @click="closePreviewDialog">
            <svg viewBox="0 0 24 24"><path d="M18.3 5.71L12 12.01 5.7 5.71 4.29 7.12 10.59 13.41 4.29 19.7 5.7 21.11 12 14.82 18.3 21.11 19.71 19.7 13.41 13.41 19.71 7.12z"/></svg>
          </button>
        </div>
      </div>
      <div class="preview-body">
        <div v-if="previewLoading" class="loading">正在加载…</div>
        <div v-else-if="previewError" class="error">{{ previewError }}</div>
        <div v-else class="preview-content" :style="(previewType === 'pdf' || previewType === 'video') ? { width: '100%', height: 'calc(100vh - 48px - 24px)', display: 'block' } : { transform: 'scale(' + previewScale + ')', transformOrigin: 'center top' }">
          <img v-if="previewType === 'image'" :src="previewUrl" class="preview-image" />
          <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="pdf-embed"></iframe>
          <video v-else-if="previewType === 'video'" :src="previewUrl" class="video-player" controls autoplay playsinline></video>
          <div v-else-if="previewType === 'docx'" class="html-view" v-html="previewHTML"></div>
          
          <pre v-else-if="previewType === 'text'" class="text-view">{{ previewText }}</pre>
          <div v-else class="unsupported">文件格式不支持预览，请下载后查看</div>
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
import { getStandardDeliverablesByStepId, getStandardDeliverables, listDeliverableTemplates, downloadDeliverableTemplate, getStandardDeliverablesByProjectAndMilestoneName } from '../api/standardDeliverable';
import { createInterface, listInterfacesByProject, deleteInterface } from '../api/interface';
import { createPersonalDevelope, listPersonalDevelopesByProject, deletePersonalDevelope } from '../api/personalDevelope';
import request from '../api/request'
// 引入预览依赖：Mammoth（docx→HTML）、XLSX
import mammoth from 'mammoth/mammoth.browser'
// 已移除 Luckysheet/XLSX 前端预览，统一走后端转 PDF
import { uploadConstructDeliverableFiles, listConstructDeliverableFiles, deleteConstructDeliverableFile } from '../api/constructDeliverableFile';
export default {
  name: 'ProjectDetail',
  /**
   * 类级注释：ProjectDetail 组件
   * 展示在建项目的步骤、里程碑、接口与个性化信息，支持基础编辑、
   * 交付物管理（查看、上传、下载）、接口与个性化块的增删。
   */
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
      ,
      // 交付物管理弹窗与数据
      showDeliverableDialog: false,
      deliverableDialogRow: null,
      deliverableList: [],
      deliverableLoading: false,
      deliverableError: '',
      selectedUploadFiles: [],
      deliverableDialogMode: 'view', // view | download
      deliverableTemplates: {} // { [deliverableId]: Array<{filename: string, name?:string}> }
      ,
      // 上传交付物弹窗与数据
      showUploadDialog: false,
      uploadDeliverables: [], // [{ deliverableId, deliverableName, sstepId?, milestoneId?, projectStepId? }]
      uploadTemplatesByDeliverableId: {}, // { [deliverableId]: Array<{name:string,size:number,lastModified?:number}> }
      selectedUploadFilesByDeliverableId: {}, // { [deliverableId]: File[] }
      uploadedFilesByDeliverableId: {}, // { [deliverableId]: Array<{fileId:number,filePath:string,fileSize:number,lastModified?:number,exists?:boolean}> }
      uploadingByDeliverableId: {}, // { [deliverableId]: boolean }
      uploadProgressByDeliverableId: {}, // { [deliverableId]: number(0-100) }
      uploadLoading: false,
      uploadError: '',
      hasDeliverablesBySstepId: {}
      ,
      // 文件预览弹窗状态
      showPreviewDialog: false,
      previewTitle: '',
      previewType: '', // image | pdf | video | docx | text | unsupported
      previewUrl: '', // 用于 image / pdf（Blob URL）
      previewHTML: '', // docx
      previewText: '', // txt
      previewLoading: false,
      previewError: '',
      // 预览缩放（非 PDF 内置工具）
      previewScale: 1.0
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
        // 非“接口开发/个性化功能开发”的步骤（保留接口需求调研与个性化需求调研）
        const nonInterfaceSteps = list.filter(s => {
          if (s.type === '接口开发') {
            const stepName = s.sstepName || s.nstepName || ''
            const keepDemandResearch = includeInterfaceDev && stepName.includes('业务系统接口需求调研')
            return keepDemandResearch
          }
          if (s.type === '个性化功能开发') {
            const stepName = s.sstepName || s.nstepName || ''
            // 仅在“02需求确定”里程碑下保留“个性化功能需求调研”步骤
            const keepPersonalDemandResearch = includePersonalDev && name === '02需求确定' && stepName.includes('个性化功能需求调研')
            return keepPersonalDemandResearch
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
    /**
     * 函数级注释：
     * 打开交付物查看弹窗并加载与当前行关联的交付物。
     * 行关联规则：
     * - 标准步骤行：使用 row.sstepId 查询标准交付物；
     * - 里程碑行：使用 row.milestoneId 或名称映射查询标准交付物；
     * - 其他类型（接口/个性化信息行）：当前展示占位，后续接入项目级交付物。
     */
    async onViewDeliverables(row, mode = 'view') {
      try {
        this.deliverableDialogMode = mode
        this.showDeliverableDialog = true
        this.deliverableDialogRow = row
        this.deliverableError = ''
        this.deliverableLoading = true
        let list = []
        if (row.rowType === 'step' && row.sstepId) {
          const resp = await getStandardDeliverablesByStepId(row.sstepId)
          list = resp?.deliverables || resp?.items || resp || []
        } else if (row.rowType === 'milestone') {
          // 优先使用里程碑ID，其次根据名称在已加载的标准里程碑中映射ID
          let mid = row.milestoneId || null
          if (!mid && this.standardMilestones && row.milestoneName) {
            const m = this.standardMilestones.find(x => x.milestoneName === row.milestoneName)
            mid = m ? m.milestoneId : null
          }
          if (mid) {
            const resp = await getStandardDeliverables({ milestoneId: mid, page: 0, size: 100 })
            list = resp?.deliverables || resp?.items || resp || []
          }
        } else {
          // 接口/个性化信息及其步骤暂不直接关联标准交付物，保留占位
          list = []
        }
        this.deliverableList = Array.isArray(list) ? list : []
      } catch (e) {
        this.deliverableError = e?.message || '交付物加载失败'
      } finally {
        this.deliverableLoading = false
      }
    },
    /**
     * 函数级注释：下载指定交付物的模板文件（上传弹窗中使用）
     * @param {number} deliverableId 交付物ID
     * @param {string} filename 文件名
     */
    async downloadTemplateForDialog(deliverableId, filename) {
      try {
        const blob = await downloadDeliverableTemplate(deliverableId, filename)
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
      } catch (e) {
        this.showError('下载模板失败：' + (e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：是否展示“交付物管理”三按钮
     * 仅当当前行是步骤（标准/接口/个性化），且该 sstepId 有交付物配置时显示。
     */
    shouldShowDeliverableActions(row) {
      const type = row?.rowType
      if (type === 'step' || type === 'interface_step' || type === 'personal_step') {
        const sid = row?.sstepId
        return !!sid && !!this.hasDeliverablesBySstepId[sid]
      }
      // 里程碑行保持原样显示
      if (type === 'milestone') return true
      return false
    },
    /**
     * 函数级注释：
     * 触发上传文件选择。
     * 后端对接计划：将文件与项目步骤/里程碑实体关联，上传到项目交付文件表。
     */
    onUploadDeliverable(row) {
      // 打开“上传交付物”弹窗，并根据当前行（步骤或里程碑）准备上下文
      this.uploadError = ''
      this.uploadLoading = true
      this.showUploadDialog = true
      this.prepareUploadContext(row)
    },
    /**
     * 函数级注释：
     * 触发下载交付物模板/文件。
     * 当前实现为打开“交付物列表”弹窗并切换为下载模式，
     * 在列表中可为每个交付物拉取模板并下载首个文件。
     */
    async onDownloadDeliverables(row) {
      await this.onViewDeliverables(row, 'download')
    },
    /**
     * 函数级注释：
     * 为指定交付物拉取模板列表，并下载第一个模板文件。
     * 若无模板，提示用户。
     */
    async downloadFirstTemplate(d) {
      try {
        const did = d?.deliverableId || d?.id
        if (!did) return this.showError('交付物ID缺失，无法下载模板')
        const files = await listDeliverableTemplates(did)
        if (!files || files.length === 0) {
          const msg = '该交付物暂无模板文件'
          this.$message?.warning ? this.$message.warning(msg) : alert(msg)
          return
        }
        const filename = files[0]?.name || files[0]?.filename
        if (!filename) {
          return this.showError('模板文件名缺失，无法下载')
        }
        const blob = await downloadDeliverableTemplate(did, filename)
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
      } catch (e) {
        this.showError('下载模板失败：' + (e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：
     * 处理文件选择完成事件，暂存文件并提示。
     * 后续将通过后端API上传并与当前行绑定。
     */
    handleDeliverableFilesSelected(evt) {
      const files = Array.from(evt?.target?.files || [])
      this.selectedUploadFiles = files
      if (files.length > 0) {
        const msg = `已选择 ${files.length} 个文件，稍后将接入上传接口`
        this.$message?.info ? this.$message.info(msg) : alert(msg)
      }
      // 重置 input 以便重复选择相同文件
      if (evt?.target) evt.target.value = ''
    },
    /**
     * 函数级注释：
     * 删除交付物入口（占位）。
     * 当前暂未接入后端删除接口，统一在“查看”弹窗中执行具体删除操作。
     */
    onDeleteDeliverables(row) {
      const msg = '请在“查看”列表中选择具体交付物进行删除（后端待对接）'
      this.$message?.info ? this.$message.info(msg) : alert(msg)
      // 也可直接打开查看弹窗
      this.onViewDeliverables(row)
    },
    /**
     * 函数级注释：
     * 关闭交付物查看弹窗并清理状态。
     */
    closeDeliverableDialog() {
      this.showDeliverableDialog = false
      this.deliverableDialogRow = null
      this.deliverableList = []
      this.deliverableError = ''
    },
    /**
     * 函数级注释：准备“上传交付物”上下文
     * 根据行类型拉取标准交付物列表，并为每个交付物拉取模板（仅保留有模板的交付物）。
     * 步骤：使用 sstepId 获取交付物；里程碑：根据名称映射 milestoneId 获取交付物。
     */
    async prepareUploadContext(row) {
      try {
        this.uploadDeliverables = []
        this.uploadTemplatesByDeliverableId = {}
        this.uploadedFilesByDeliverableId = {}
        // 获取当前行对应的交付物列表
        let list = []
        if (row.rowType === 'step' && row.sstepId) {
          const resp = await getStandardDeliverablesByStepId(row.sstepId)
          list = Array.isArray(resp) ? resp : (resp?.deliverables || resp?.items || [])
          // 为模板过滤保留 sstepId
          list = (list || []).map(d => ({ ...d, sstepId: row.sstepId }))
        } else if (row.rowType === 'interface_step' && row.sstepId) {
          // 接口开发步骤：使用标准步骤ID载入交付物与模板
          const resp = await getStandardDeliverablesByStepId(row.sstepId)
          list = Array.isArray(resp) ? resp : (resp?.deliverables || resp?.items || [])
          list = (list || []).map(d => ({ ...d, sstepId: row.sstepId }))
        } else if (row.rowType === 'personal_step' && row.sstepId) {
          // 个性化开发步骤：使用标准步骤ID载入交付物与模板
          const resp = await getStandardDeliverablesByStepId(row.sstepId)
          list = Array.isArray(resp) ? resp : (resp?.deliverables || resp?.items || [])
          list = (list || []).map(d => ({ ...d, sstepId: row.sstepId }))
        } else if (row.rowType === 'milestone') {
          // 按项目ID与里程碑名称查询标准交付物（后台按名称映射标准里程碑ID）
          const projectId = this.project?.projectId
          const milestoneName = row.milestoneName
          if (projectId && milestoneName) {
            const listByName = await getStandardDeliverablesByProjectAndMilestoneName(projectId, milestoneName)
            list = Array.isArray(listByName) ? listByName : []
          }
          // 兜底：仍尝试使用已加载的标准里程碑映射ID
          if ((!list || list.length === 0) && this.standardMilestones && row.milestoneName) {
            let mid = row.milestoneId || null
            if (!mid) {
              const m = this.standardMilestones.find(x => x.milestoneName === row.milestoneName)
              mid = m ? m.milestoneId : null
            }
            if (mid) {
              const resp = await getStandardDeliverables({ milestoneId: mid, page: 0, size: 200 })
              list = Array.isArray(resp) ? resp : (resp?.deliverables || resp?.items || [])
            }
          }
        }
        // 拉取模板（若无模板也允许上传）与已上传文件列表
        const result = []
        const projectId = this.project?.projectId
        for (const d of list || []) {
          const did = d?.deliverableId || d?.id
          if (!did) continue
          try {
            const templates = await listDeliverableTemplates(did)
            if (templates && templates.length > 0) {
              this.uploadTemplatesByDeliverableId[did] = templates
            } else {
              this.uploadTemplatesByDeliverableId[did] = []
            }
          } catch (_) {
            this.uploadTemplatesByDeliverableId[did] = []
          }
          // 已上传文件列表
          try {
            if (projectId) {
              const files = await listConstructDeliverableFiles(projectId, did)
              this.uploadedFilesByDeliverableId[did] = Array.isArray(files) ? files : []
            } else {
              this.uploadedFilesByDeliverableId[did] = []
            }
          } catch (_) { /* 忽略单个失败 */ }
          // 无论是否有模板，都允许上传
          // 注入当前行的项目步骤关系ID以供上传接口使用（projectStepId）
          result.push({ deliverableId: did, deliverableName: d.deliverableName, sstepId: d.sstepId, milestoneId: d.milestoneId, projectStepId: row.relationId || null })
        }
        this.uploadDeliverables = result
      } catch (e) {
        this.uploadError = e?.message || '上传上下文准备失败'
      } finally {
        this.uploadLoading = false
      }
    },
    /**
     * 函数级注释：关闭上传弹窗并清理状态
     */
    closeUploadDialog() {
      this.showUploadDialog = false
      this.uploadDeliverables = []
      this.uploadTemplatesByDeliverableId = {}
      this.selectedUploadFilesByDeliverableId = {}
      this.uploadError = ''
      this.uploadLoading = false
    },
    /**
     * 函数级注释：为某个交付物选择待上传文件
     * @param {Object} d 交付物
     * @param {Event} evt 文件选择事件
     */
    /**
     * 函数级注释：文件选择后自动发起上传并显示进度
     * - 存储选中文件并立即调用上传方法；
     * - 使用 axios 的 onUploadProgress 回调更新进度条；
     * - 上传完成后刷新已上传文件列表。
     */
    handleUploadFileSelectedForDeliverable(d, evt) {
      const files = Array.from(evt?.target?.files || [])
      const did = d?.deliverableId
      if (did) this.selectedUploadFilesByDeliverableId[did] = files
      // 重置 input 便于重复选择
      if (evt?.target) evt.target.value = ''
      if (did && files.length > 0) {
        this.uploadingByDeliverableId[did] = true
        this.uploadProgressByDeliverableId[did] = 0
        this.uploadFilesForDeliverable(d, (percent) => {
          this.uploadProgressByDeliverableId[did] = percent
        }).finally(() => {
          this.uploadingByDeliverableId[did] = false
        })
      }
    },
    /**
     * 函数级注释：执行上传交付物文件
     * 使用后端 `/construct-deliverable-files/{projectId}/{deliverableId}/upload` 接口，
     * 后端将按规则重命名并保存到 `deliverableFiles/<项目编号-项目名称>/<里程碑名称>/`。
     * @param {Object} d 交付物对象
     */
    /**
     * 函数级注释：执行上传交付物文件（支持进度回调）
     * 使用后端 `/construct-deliverable-files/{projectId}/{deliverableId}/upload` 接口，
     * 后端将按规则重命名并保存到 `deliverableFiles/<项目编号-项目名称>/<里程碑名称>/`。
     * @param {Object} d 交付物对象
     * @param {Function} [onProgress] 进度回调，参数为百分比整数 0-100
     */
    /**
     * 函数级注释：执行上传交付物文件（支持进度回调，避免整页刷新闪烁）
     * - 上传成功后仅刷新当前交付物的文件列表并本地同步摘要文件映射，
     *   不再调用整页的 loadSummary()，从而避免页面闪烁。
     */
    async uploadFilesForDeliverable(d, onProgress) {
      try {
        const projectId = this.project?.projectId
        const deliverableId = d?.deliverableId
        if (!projectId || !deliverableId) {
          return this.showError('项目或交付物信息缺失，无法上传')
        }
        const files = this.selectedUploadFilesByDeliverableId[deliverableId] || []
        if (!files || files.length === 0) {
          return this.showError('请先选择文件')
        }
        const options = {}
        // 步骤交付物传递 projectStepId，以便后端解析步骤名称与接口/个性化目录段
        if (d.projectStepId) options.projectStepId = d.projectStepId
        if (typeof onProgress === 'function') options.onProgress = onProgress
        await uploadConstructDeliverableFiles(projectId, deliverableId, files, options)
        this.$message && this.$message.success('上传成功')
        // 仅刷新当前交付物的已上传文件列表并本地同步摘要文件映射，避免整页闪烁
        await this.refreshUploadedFiles(deliverableId)
        this.applyUploadedFilesToSummary(deliverableId)
      } catch (e) {
        this.showError('上传失败：' + (e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：刷新指定交付物的已上传文件列表
     * @param {number} deliverableId 交付物ID
     */
    async refreshUploadedFiles(deliverableId) {
      try {
        const projectId = this.project?.projectId
        if (!projectId || !deliverableId) return
        const files = await listConstructDeliverableFiles(projectId, deliverableId)
        this.uploadedFilesByDeliverableId[deliverableId] = Array.isArray(files) ? files : []
      } catch (_) {
        this.uploadedFilesByDeliverableId[deliverableId] = []
      }
    },
    /**
     * 函数级注释：本地同步摘要文件映射，避免整页刷新造成的闪烁
     * - 将指定交付物的已上传文件映射到 `filesByDeliverableId` 与 `files`，
     *   仅替换该交付物对应的片段，不影响其他内容。
     * @param {number} deliverableId 交付物ID
     */
    applyUploadedFilesToSummary(deliverableId) {
      try {
        const list = this.uploadedFilesByDeliverableId[deliverableId] || []
        // 更新映射
        this.$set ? this.$set(this.filesByDeliverableId, deliverableId, list) : (this.filesByDeliverableId[deliverableId] = list)
        // 更新平铺数组：先移除该交付物旧记录，再追加新记录
        const prev = Array.isArray(this.files) ? this.files : []
        const filtered = prev.filter(f => f && f.deliverableId !== deliverableId)
        this.files = filtered.concat(list)
      } catch (_) { /* 忽略局部同步错误 */ }
    },
    /**
     * 函数级注释：删除已上传文件并刷新列表
     * @param {number} fileId 文件记录ID
     * @param {number} deliverableId 交付物ID（用于刷新列表）
     */
    async deleteUploadedFile(fileId, deliverableId) {
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该文件？') : window.confirm('确认删除该文件？')
        if (!ok) return
        await deleteConstructDeliverableFile(fileId)
        this.$message && this.$message.success('删除成功')
        await this.refreshUploadedFiles(deliverableId)
      } catch (e) {
        this.showError('删除失败：' + (e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：加载项目汇总数据
     * - 根据路由参数 `projectId` 调用后端 `/api/projects/{id}/summary` 接口。
     * - 成功时填充项目、步骤、里程碑、交付物与已上传文件。
     * - 失败时优先显示后端返回的错误信息（如“项目不存在”），并停止加载状态。
     */
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
        // 计算每个步骤是否在标准交付物模块中配置了交付物
        const deliverableStepMap = {};
        (this.deliverables || []).forEach(d => {
          const sid = d && d.sstepId;
          if (sid != null) deliverableStepMap[sid] = true;
        });
        this.hasDeliverablesBySstepId = deliverableStepMap;
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
        const backendMsg = err?.response?.data?.message || err?.response?.data?.error;
        this.error = backendMsg ? `加载失败：${backendMsg}` : (err?.message || '加载失败');
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
    /**
     * 函数级注释：获取后端 Office→PDF 预览端点URL
     * - 统一将 doc/docx/ppt/pptx 转换为 PDF，供 PDFJS 渲染。
     */
    convertPreviewPdfURL(fileId) {
      return `${import.meta.env.VITE_API_BASE || 'http://localhost:8081'}/api/construct-deliverable-files/preview/pdf/${fileId}`;
    },
    /**
     * 函数级注释：获取后端 MP4 视频预览端点 URL
     * - 返回 inline 的 mp4 资源，支持 Range 分段；用于 <video> src。
     */
    convertPreviewVideoURL(fileId) {
      return `${import.meta.env.VITE_API_BASE || 'http://localhost:8081'}/api/construct-deliverable-files/preview/video/${fileId}`;
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
    /**
     * 函数级注释：关闭文件预览弹窗并清理状态
     */
    closePreviewDialog() {
      try {
        // 释放图片/PDF Blob URL
        if ((this.previewType === 'image' || this.previewType === 'pdf') && this.previewUrl) {
          URL.revokeObjectURL(this.previewUrl)
        }
      } catch (_) {}
      // 重置状态
      this.showPreviewDialog = false
      this.previewTitle = ''
      this.previewType = ''
      this.previewUrl = ''
      this.previewHTML = ''
      this.previewText = ''
      this.previewLoading = false
      this.previewError = ''
      // 重置缩放
      this.previewScale = 1.0
    },
    /**
     * 函数级注释：点击文件进行预览
     * 支持类型：图片、pdf、doc/docx、xls/xlsx、ppt/pptx、txt；
     * - 非 PDF 的 Office 文档（Word/Excel/PPT）统一调用后端 Office→PDF 端点，iframe 全屏预览。
     * 其余类型提示不支持预览。
     * @param {{fileId:number,filePath:string}} file 文件记录
     */
    async onPreviewFile(file) {
      const name = this.fileBaseName(file?.filePath || '')
      const ext = (name.split('.').pop() || '').toLowerCase()
      this.previewTitle = name || '文件预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true

      // 类型判定
      const imageExts = ['png','jpg','jpeg','gif','bmp','webp']
      if (imageExts.includes(ext)) {
        this.previewType = 'image'
        try {
          const blob = await this.fetchBlob(file.fileId)
          const url = URL.createObjectURL(blob)
          this.previewUrl = url
        } catch (e) {
          this.previewError = e?.message || '图片加载失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'pdf') {
        this.previewType = 'pdf'
        try {
          const blob = await this.fetchBlob(file.fileId)
          const buf = await blob.arrayBuffer()
          const pdfBlob = new Blob([buf], { type: 'application/pdf' })
          const url = URL.createObjectURL(pdfBlob)
          this.previewUrl = url
        } catch (e) {
          this.previewError = e?.message || 'PDF 加载失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // MP4 视频：直接指向后端视频预览端点，实现在线播放与拖动
      if (ext === 'mp4') {
        this.previewType = 'video'
        try {
          this.previewUrl = this.convertPreviewVideoURL(file.fileId)
        } catch (e) {
          this.previewError = e?.message || '视频预览失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // Word 文档：优先后端转 PDF 统一预览，失败时 DOCX 回退为 HTML
      if (ext === 'doc' || ext === 'docx') {
        try {
          // 优先调用后端 Office→PDF 预览端点
          const pdfBlob = await this.fetchPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          // 回退方案：仅对 docx 使用 Mammoth 转为 HTML
          if (ext === 'docx') {
            try {
              const blob = await this.fetchBlob(file.fileId)
              const buf = await blob.arrayBuffer()
              const result = await mammoth.convertToHtml({ arrayBuffer: buf })
              this.previewType = 'docx'
              this.previewHTML = result.value || '<div>该文档无法转换为HTML</div>'
            } catch (err) {
              this.previewError = err?.message || 'DOCX 预览失败'
            }
          } else {
            this.previewType = 'unsupported'
            this.previewError = e?.message || 'Word 预览失败，请下载查看'
          }
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'xls' || ext === 'xlsx') {
        try {
          const pdfBlob = await this.fetchPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          this.previewType = 'unsupported'
          this.previewError = e?.message || 'Excel 预览失败，请下载查看'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'txt') {
        this.previewType = 'text'
        try {
          const blob = await this.fetchBlob(file.fileId)
          const text = await blob.text()
          this.previewText = text
        } catch (e) {
          this.previewError = e?.message || '文本加载失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // ppt、pptx：统一走后端 Office→PDF 预览端点，iframe 内嵌展示
      if (ext === 'ppt' || ext === 'pptx') {
        try {
          const pdfBlob = await this.fetchPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          this.previewType = 'unsupported'
          this.previewError = e?.message || '演示文稿预览失败，请下载查看'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // 其他未知格式
      this.previewType = 'unsupported'
      this.previewLoading = false
    },
    /**
     * 函数级注释：PDF 放大
     */
    pdfZoomIn() {
      this.previewScale = Math.min(this.previewScale + 0.1, 3.0)
    },
    /**
     * 函数级注释：PDF 缩小
     */
    pdfZoomOut() {
      this.previewScale = Math.max(this.previewScale - 0.1, 0.3)
    },
    /**
     * 函数级注释：PDF 上一页
     */
    // 移除分页控制，使用浏览器内置 PDF 查看器的分页功能
    /**
     * 函数级注释：从后端下载接口获取文件 Blob
     * 使用现有下载端点，不更改后端逻辑与响应头。
     * @param {number} fileId 文件ID
     * @returns {Promise<Blob>} 文件二进制
     */
    async fetchBlob(fileId) {
      const url = this.downloadURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('文件获取失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：获取后端 Office→PDF 预览的 PDF Blob
     * - 统一将 Word 文档（doc/docx）转换为 PDF，供 iframe 内置查看器预览。
     * - 保持与后端认证一致，携带凭证。
     * @param {number} fileId 文件ID
     * @returns {Promise<Blob>} PDF 二进制
     */
    async fetchPreviewPdfBlob(fileId) {
      const url = this.convertPreviewPdfURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('预览转换失败：' + resp.status)
      return await resp.blob()
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
    },
    /**
     * 函数级注释：删除接口块并刷新摘要
     * @param {number} interfaceId 接口ID
     */
    async onDeleteInterface(interfaceId) {
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该接口及其生成的步骤关系？') : window.confirm('确认删除该接口及其生成的步骤关系？')
        if (!ok) return
        await deleteInterface(interfaceId)
        // 本地移除并刷新摘要
        this.interfaceBlocks = (this.interfaceBlocks || []).filter(b => b.id !== interfaceId)
        this.$message && this.$message.success('接口已删除')
        await this.loadSummary()
      } catch (e) {
        this.showError('删除接口失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：删除个性化需求块并刷新摘要
     * @param {number} personalDevId 个性化开发ID
     */
    async onDeletePersonal(personalDevId) {
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该个性化需求及其生成的步骤关系？') : window.confirm('确认删除该个性化需求及其生成的步骤关系？')
        if (!ok) return
        await deletePersonalDevelope(personalDevId)
        this.personalBlocks = (this.personalBlocks || []).filter(b => b.id !== personalDevId)
        this.$message && this.$message.success('个性化需求已删除')
        await this.loadSummary()
      } catch (e) {
        this.showError('删除个性化需求失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
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
.dialog.upload-dialog { width: 560px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.dialog h4 { margin:0 0 12px; }
.form-row { display:flex; flex-direction:column; gap:6px; margin-bottom:10px; }
.form-row input, .form-row select { height:32px; padding:0 8px; border:1px solid #ddd; border-radius:4px; }
.required { color:#c00; }
.dialog-actions { display:flex; gap:8px; justify-content:flex-end; margin-top:12px; }
.btn { padding:6px 12px; border:1px solid #ddd; background:#fff; border-radius:4px; cursor:pointer; }
.btn.ghost { background:#f5f7fa; }
.btn.primary { background:#1677ff; border-color:#1677ff; color:#fff; }
.btn.primary:hover { background:#0f5fd6; border-color:#0f5fd6; }
.btn.disabled { opacity:.6; cursor:not-allowed; }
/* 删除按钮颜色与名称文字颜色保持一致（接口与个性化信息行） */
.interface-info-row .btn.ghost,
.personal-info-row .btn.ghost {
  color: inherit;
}
/* 交付物管理列与图标按钮样式 */
.deliverable-actions { /* 保持表格单元格默认布局，避免高度不一致 */ }
.deliverable-actions .actions-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;
}
.table td.deliverable-actions:empty::before {
  content: "";
  display: block;
  height: 28px; /* 与图标按钮高度一致，保证行高与下边线对齐 */
}
.icon-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s ease;
  color: #374151;
}
.icon-btn:hover {
  background: #f9fafb;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}
.icon-btn.danger {
  border-color: #fca5a5;
  color: #b91c1c;
}
.icon-btn svg {
  width: 18px;
  height: 18px;
  fill: currentColor;
}
.upload-list { display:flex; flex-direction:column; gap:12px; }
.upload-item { border:1px solid #eee; border-radius:8px; padding:12px; background:#fafafa; }
.upload-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.upload-head .name { font-weight:600; color:#333; }
.upload-head .hint { color:#888; font-size:12px; }
.template-title { color:#666; font-size:12px; margin-bottom:6px; }
.chip-group { display:flex; flex-wrap:wrap; gap:6px; }
.chip { display:inline-flex; align-items:center; gap:6px; padding:4px 10px; border:1px solid #e5e7eb; border-radius:16px; background:#fff; font-size:12px; color:#374151; }
.chip.clickable { cursor:pointer; }
.chip.clickable:hover { background:#f9fafb; border-color:#d1d5db; }
.chip .chip-name { font-weight:500; }
.chip .chip-meta { color:#6b7280; }
.upload-actions { display:flex; align-items:center; gap:12px; margin-top:8px; }
.deliverable-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.deliverable-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  border-bottom: 1px dashed #e5e7eb;
}
.deliverable-list .name { font-weight: 500; }
.deliverable-list .type { color: #6b7280; font-size: 12px; }
/* 上传进度条样式 */
.progress { position: relative; height: 6px; background: #f0f0f0; border-radius: 4px; margin-top: 8px; }
.progress .bar { height: 100%; background: #409eff; border-radius: 4px; width: 0%; transition: width .2s ease; }
.progress .percent { position: absolute; top: -18px; right: 0; font-size: 12px; color: #666; }
.file-list.compact .file-item { padding:2px 0; }
/* 预览弹窗样式 */
.preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 2000; display: flex; flex-direction: column; }
.preview-header { height: 48px; display: flex; align-items: center; justify-content: space-between; padding: 0 12px; background: #111827; color: #fff; }
.preview-header .title { font-size: 14px; font-weight: 600; color: #fff; }
.preview-header .tools { display: flex; align-items: center; gap: 8px; }
.preview-body { flex: 1; background: #0f172a; color: #fff; overflow: auto; padding: 12px; }
.preview-content { display: inline-block; transform-origin: center top; }
.preview-image { max-width: 100%; height: auto; display: block; }
.pdf-embed { width: 100%; height: 100%; border: none; background: #0f172a; }
.video-player { width: 100%; height: 100%; background: #000; }
.pdf-viewer { display: inline-block; background: #fff; padding: 8px; border-radius: 6px; }
.pdf-nav { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.html-view { background: #fff; color: #111; padding: 12px; border-radius: 6px; max-width: 100%; }
.text-view { background: #111827; color: #e5e7eb; padding: 12px; border-radius: 6px; max-width: 100%; white-space: pre-wrap; }
.loading { color: #e5e7eb; }
.error { color: #fecaca; }
.unsupported { color: #e5e7eb; }
/* 已移除 Luckysheet 容器样式：统一使用 iframe 全屏预览 PDF */
</style>