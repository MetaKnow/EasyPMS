<template>
  <div class="project-detail-page">
    <div v-if="tooltip.visible" class="custom-tooltip" :style="tooltip.style">{{ tooltip.content }}</div>
    <div class="topbar">
      <button class="back-btn" @click="goBack">返回</button>
      <div class="title">
        <span class="name">{{ project?.projectName || '项目详情' }}</span>
        <span class="num" v-if="project?.projectNum">编号：{{ project.projectNum }}</span>
      </div>
      <div class="actions">
        <button v-if="activeTab === 'out_contract'" class="add-btn" @click="openExtraDialog('create')">添加需求</button>
        <button v-if="activeTab === 'risk'" class="add-btn" @click="openRiskDialog('create')">添加风险</button>
      </div>
    </div>

    <div v-if="loading" class="state">正在加载...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>
    <div v-else class="content-wrapper">
      <!-- 顶部标签区域（横向切换） -->
      <div class="top-tabs">
        <div v-for="tab in tabs" :key="tab.id" class="tab-item" :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id">
          {{ tab.name }}
        </div>
      </div>

      <!-- 下方内容区域 -->
      <div class="main-content">
        <!-- 合同内建设内容（原页面内容） -->
        <div v-show="activeTab === 'contract'" class="content-grid">


          <section class="card wide">
            <!-- 类级注释：移除“步骤与里程碑”区域标题，保持页面其他部分不变；
             为步骤表格添加滚动容器以实现表头固定、仅内容滚动。 -->
            <div class="table-scroll">
              <table class="table table-fixed no-wrap-table">
                <colgroup>
                  <col style="width: 60px">
                  <col> <!-- 步骤名称 -->
                  <col style="width: 120px">
                  <col style="width: 100px">
                  <col style="width: 120px">
                  <col style="width: 120px">
                  <col style="width: 120px">
                  <col style="width: 120px">
                  <col style="width: 100px">
                  <col style="width: 100px">
                  <col style="width: 140px">
                  <col style="width: 160px">
                </colgroup>
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>步骤名称</th>
                    <th>类型</th>
                    <th>负责人</th>
                    <th>计划开始</th>
                    <th>计划结束</th>
                    <th>实际开始</th>
                    <th>实际结束</th>
                    <th>计划工期</th>
                    <th>实际工期</th>
                    <th>状态</th>
                    <th>交付物管理</th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="(row, idx) in combinedRows" :key="row.rowType === 'step'
                ? (row.sstepId || row.nstepId || row.relationId)
                : (row.rowType === 'milestone'
                  ? ('m-' + (row.milestoneId || row.milestoneName))
                  : (row.rowType === 'interface_step'
                    ? ('i-' + (row.relationId || (row.blockId + '-' + idx)))
                    : (row.rowType + '-' + (row.blockId || idx))))">
                    <tr v-if="row.rowType === 'step'">
                      <td>{{ idx + 1 }}</td>
                      <td @mouseenter="showTooltip($event, row.sstepName || row.nstepName)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.sstepName || row.nstepName }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.type || '标准')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.type || '标准' }}</div>
                      </td>
                      <td @dblclick="startEdit(row, 'director')" @mouseenter="showTooltip($event, row.directorName ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'director')">
                          <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit"
                            class="cell-input">
                            <option :value="null">-</option>
                            <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                              {{ u.name || u.userName }}
                            </option>
                          </select>
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.directorName ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planStartDate')" @mouseenter="showTooltip($event, row.planStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')"
                            @blur="commitEdit(row, 'planStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planEndDate')" @mouseenter="showTooltip($event, row.planEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')"
                            @blur="commitEdit(row, 'planEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualStartDate')" @mouseenter="showTooltip($event, row.actualStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')"
                            @blur="commitEdit(row, 'actualStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualEndDate')" @mouseenter="showTooltip($event, row.actualEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')"
                            @blur="commitEdit(row, 'actualEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @mouseenter="showTooltip($event, row.planPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.planPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.actualPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.actualPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</div>
                      </td>
                      <td class="deliverable-actions">
                        <template v-if="shouldShowDeliverableActions(row)">
                          <div class="actions-inner">
                            <button class="icon-btn" :class="viewButtonClass(row)" title="查看"
                              @click="onViewDeliverables(row)" aria-label="查看交付物">
                              <svg viewBox="0 0 24 24">
                                <path
                                  d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                              </svg>
                            </button>
                            <button class="icon-btn" :class="uploadButtonClass(row)" title="上传"
                              @click="onUploadDeliverable(row)" aria-label="上传交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z" />
                              </svg>
                            </button>
                            <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                              </svg>
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
                        <button class="btn ghost" :class="{ disabled: isProjectCompleted }"
                          :disabled="isProjectCompleted" @click="onDeleteInterface(row.blockId)">删除</button>
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
                        <button class="btn ghost" :class="{ disabled: isProjectCompleted }"
                          :disabled="isProjectCompleted" @click="onDeletePersonal(row.blockId)">删除</button>
                      </td>
                      <td class="deliverable-actions"></td>
                    </tr>
                    <!-- 接口开发步骤（支持双击编辑） -->
                    <tr v-else-if="row.rowType === 'interface_step'" class="interface-step-row">
                      <td>{{ idx + 1 }}</td>
                      <td @mouseenter="showTooltip($event, row.sstepName)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.sstepName }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.type)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.type }}</div>
                      </td>
                      <td @dblclick="startEdit(row, 'director')" @mouseenter="showTooltip($event, row.directorName ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'director')">
                          <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit"
                            class="cell-input">
                            <option :value="null">-</option>
                            <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                              {{ u.name || u.userName }}
                            </option>
                          </select>
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.directorName ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planStartDate')" @mouseenter="showTooltip($event, row.planStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')"
                            @blur="commitEdit(row, 'planStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planEndDate')" @mouseenter="showTooltip($event, row.planEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')"
                            @blur="commitEdit(row, 'planEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualStartDate')" @mouseenter="showTooltip($event, row.actualStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')"
                            @blur="commitEdit(row, 'actualStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualEndDate')" @mouseenter="showTooltip($event, row.actualEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')"
                            @blur="commitEdit(row, 'actualEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @mouseenter="showTooltip($event, row.planPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.planPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.actualPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.actualPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</div>
                      </td>
                      <td class="deliverable-actions">
                        <template v-if="shouldShowDeliverableActions(row)">
                          <div class="actions-inner">
                            <button class="icon-btn" :class="viewButtonClass(row)" title="查看"
                              @click="onViewDeliverables(row)" aria-label="查看交付物">
                              <svg viewBox="0 0 24 24">
                                <path
                                  d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                              </svg>
                            </button>
                            <button class="icon-btn" :class="uploadButtonClass(row)" title="上传"
                              @click="onUploadDeliverable(row)" aria-label="上传交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z" />
                              </svg>
                            </button>
                            <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                              </svg>
                            </button>
                          </div>
                        </template>
                      </td>
                    </tr>
                    <!-- 个性化开发步骤（支持双击编辑） -->
                    <tr v-else-if="row.rowType === 'personal_step'" class="personal-step-row">
                      <td>{{ idx + 1 }}</td>
                      <td @mouseenter="showTooltip($event, row.sstepName)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.sstepName }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.type)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.type }}</div>
                      </td>
                      <td @dblclick="startEdit(row, 'director')" @mouseenter="showTooltip($event, row.directorName ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'director')">
                          <select v-model="editValue" @change="commitEdit(row, 'director')" @blur="cancelEdit"
                            class="cell-input">
                            <option :value="null">-</option>
                            <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                              {{ u.name || u.userName }}
                            </option>
                          </select>
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.directorName ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planStartDate')" @mouseenter="showTooltip($event, row.planStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planStartDate')"
                            @blur="commitEdit(row, 'planStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'planEndDate')" @mouseenter="showTooltip($event, row.planEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'planEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'planEndDate')"
                            @blur="commitEdit(row, 'planEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.planEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualStartDate')" @mouseenter="showTooltip($event, row.actualStartDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualStartDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualStartDate')"
                            @blur="commitEdit(row, 'actualStartDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualStartDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @dblclick="startEdit(row, 'actualEndDate')" @mouseenter="showTooltip($event, row.actualEndDate ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <template v-if="isEditing(row, 'actualEndDate')">
                          <input type="date" v-model="editValue" @keyup.enter="commitEdit(row, 'actualEndDate')"
                            @blur="commitEdit(row, 'actualEndDate')" class="cell-input" />
                        </template>
                        <template v-else>
                          <div class="text-truncate">{{ row.actualEndDate ?? '-' }}</div>
                        </template>
                      </td>
                      <td @mouseenter="showTooltip($event, row.planPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.planPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.actualPeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.actualPeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.stepStatus || (row.isCompleted ? '已完成' : (row.status || '未开始')) }}</div>
                      </td>
                      <td class="deliverable-actions">
                        <template v-if="shouldShowDeliverableActions(row)">
                          <div class="actions-inner">
                            <button class="icon-btn" :class="viewButtonClass(row)" title="查看"
                              @click="onViewDeliverables(row)" aria-label="查看交付物">
                              <svg viewBox="0 0 24 24">
                                <path
                                  d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                              </svg>
                            </button>
                            <button class="icon-btn" :class="uploadButtonClass(row)" title="上传"
                              @click="onUploadDeliverable(row)" aria-label="上传交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z" />
                              </svg>
                            </button>
                            <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                              </svg>
                            </button>
                          </div>
                        </template>
                      </td>
                    </tr>
                    <!-- 添加接口按钮行（位于目标里程碑上一行） -->
                    <tr v-else-if="row.rowType === 'add_interface'" class="add-interface-row">
                      <td>{{ idx + 1 }}</td>
                      <td colspan="11">
                        <button class="add-interface-btn" :class="{ disabled: isProjectCompleted }"
                          :disabled="isProjectCompleted" @click="openInterfaceDialog">添加接口</button>
                      </td>
                    </tr>
                    <!-- 添加个性化需求按钮行（位于目标里程碑上一行） -->
                    <tr v-else-if="row.rowType === 'add_personal'" class="add-personal-row">
                      <td>{{ idx + 1 }}</td>
                      <td colspan="11">
                        <button class="add-personal-btn" :class="{ disabled: isProjectCompleted }"
                          :disabled="isProjectCompleted" @click="openPersonalDialog">添加个性化需求</button>
                      </td>
                    </tr>
                    <tr v-else class="milestone-row">
                      <td>{{ idx + 1 }}</td>
                      <td @mouseenter="showTooltip($event, '【里程碑】' + row.milestoneName)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">【里程碑】{{ row.milestoneName }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '里程碑')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">里程碑</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">-</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.milestonePeriod ?? '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.milestonePeriod ?? '-' }}</div>
                      </td>
                      <td @mouseenter="showTooltip($event, row.iscomplete ? '完成' : '未完成')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                        <div class="text-truncate">{{ row.iscomplete ? '完成' : '未完成' }}</div>
                      </td>
                      <td class="deliverable-actions">
                        <div class="actions-inner">
                          <button class="icon-btn" :class="viewButtonClass(row)" title="查看"
                            @click="onViewDeliverables(row)" aria-label="查看交付物">
                            <svg viewBox="0 0 24 24">
                              <path
                                d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                            </svg>
                          </button>
                          <button class="icon-btn" :class="uploadButtonClass(row)" title="上传"
                            @click="onUploadDeliverable(row)" aria-label="上传交付物">
                            <svg viewBox="0 0 24 24">
                              <path d="M5 20h14v-2H5v2zm7-18l-5 5h3v6h4V7h3l-5-5z" />
                            </svg>
                          </button>
                          <button class="icon-btn" title="下载" @click="onDownloadDeliverables(row)" aria-label="下载交付物">
                            <svg viewBox="0 0 24 24">
                              <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <!-- 合同外需求 -->
        <div v-show="activeTab === 'out_contract'" class="content-grid">
          <section class="card wide">
            <div class="table-scroll">
              <table class="table table-fixed no-wrap-table">
                <colgroup>
                  <col style="width: 60px">
                  <col> <!-- 需求名称 -->
                  <col style="width: 90px">
                  <col style="width: 120px">
                  <col style="width: 90px">
                  <col style="width: 90px">
                  <col style="width: 100px">
                  <col style="width: 120px">
                  <col style="width: 160px">
                  <col style="width: 120px">
                </colgroup>
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>需求名称</th>
                    <th>是否付费</th>
                    <th>付费金额（元）</th>
                    <th>是否交付</th>
                    <th>是否完成</th>
                    <th>是否产品化</th>
                    <th>工作量</th>
                    <th>开发负责人</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(r, idx) in extraRequirements" :key="r.requirementId || (r.id ?? idx)">
                    <td>{{ idx + 1 }}</td>
                    <td @mouseenter="showTooltip($event, r.requirementName || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.requirementName || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.isPay ? '是' : '否')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.isPay ? '是' : '否' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.payAmount != null ? String(r.payAmount) : '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.payAmount != null ? String(r.payAmount) : '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.isDeliver ? '是' : '否')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.isDeliver ? '是' : '否' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.isComplete ? '是' : '否')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.isComplete ? '是' : '否' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.isProductization ? '是' : '否')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.isProductization ? '是' : '否' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.workload != null ? String(r.workload) : '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.workload != null ? String(r.workload) : '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, userName(r.developer) || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ userName(r.developer) || '-' }}</div>
                    </td>
                    <td class="deliverable-actions">
                      <div class="actions-inner">
                        <button class="icon-btn" :class="{ 'has-files': r.hasFiles }" title="查看" @click="viewExtra(r)">
                          <svg viewBox="0 0 24 24">
                            <path
                              d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                          </svg>
                        </button>
                        <button class="icon-btn" title="编辑" @click="editExtra(r)" :disabled="isProjectCompleted"
                          :class="{ disabled: isProjectCompleted }">
                          <svg viewBox="0 0 24 24">
                            <path
                              d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                          </svg>
                        </button>
                        <button class="icon-btn" title="删除" @click="deleteExtra(r)" :disabled="isProjectCompleted"
                          :class="{ disabled: isProjectCompleted }">
                          <svg viewBox="0 0 24 24">
                            <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="!extraRequirements || extraRequirements.length === 0">
                    <td colspan="10" class="empty">当前暂无合同外需求</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="pagination">
              <button class="btn" disabled>上一页</button>
              <span class="page-info">共 {{ extraRequirements.length }} 条</span>
              <button class="btn" disabled>下一页</button>
            </div>
          </section>
        </div>

        <div v-show="activeTab === 'risk'" class="content-grid">
          <section class="card wide">
            <div class="table-scroll">
              <table class="table table-fixed no-wrap-table">
                <colgroup>
                  <col style="width: 60px">
                  <col style="width: 140px">
                  <col style="width: 90px">
                  <col style="width: 90px">
                  <col style="width: 160px">
                  <col> <!-- 风险描述 -->
                  <col> <!-- 风险评估 -->
                  <col style="width: 140px">
                  <col style="width: 120px">
                </colgroup>
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>风险类型</th>
                    <th>风险级别</th>
                    <th>是否解除</th>
                    <th>解除方式</th>
                    <th>风险描述</th>
                    <th>风险评估</th>
                    <th>创建人</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(r, idx) in projectRisks" :key="r.riskId || (r.id ?? idx)">
                    <td>{{ idx + 1 }}</td>
                    <td @mouseenter="showTooltip($event, r.riskType || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.riskType || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.riskLevel || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.riskLevel || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.isRelieve ? '是' : '否')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.isRelieve ? '是' : '否' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.relieveWay || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.relieveWay || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.riskDescription || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.riskDescription || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, r.riskEvaluate || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ r.riskEvaluate || '-' }}</div>
                    </td>
                    <td @mouseenter="showTooltip($event, userName(r.creator) || '-')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition">
                      <div class="text-truncate">{{ userName(r.creator) || '-' }}</div>
                    </td>
                    <td class="deliverable-actions">
                      <div class="actions-inner">
                        <button class="icon-btn" :class="{ 'has-files': r.hasFiles }" title="查看" @click="viewRisk(r)">
                          <svg viewBox="0 0 24 24">
                            <path
                              d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z" />
                          </svg>
                        </button>
                        <button class="icon-btn" title="编辑" @click="editRisk(r)" :disabled="isProjectCompleted"
                          :class="{ disabled: isProjectCompleted }">
                          <svg viewBox="0 0 24 24">
                            <path
                              d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                          </svg>
                        </button>
                        <button class="icon-btn" title="删除" @click="deleteRisk(r)" :disabled="isProjectCompleted"
                          :class="{ disabled: isProjectCompleted }">
                          <svg viewBox="0 0 24 24">
                            <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="!projectRisks || projectRisks.length === 0">
                    <td colspan="9" class="empty">当前暂无项目风险</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="pagination">
              <button class="btn" disabled>上一页</button>
              <span class="page-info">共 {{ projectRisks.length }} 条</span>
              <button class="btn" disabled>下一页</button>
            </div>
          </section>
        </div>

        <div v-show="activeTab === 'project_comment'" class="content-grid">
          <section class="card wide comment-section">
            <div class="comment-split">
              <div class="comment-pane-left">
                <div class="comment-compose-area">
                  <div class="compose-header">
                    <span class="compose-title">发表评论</span>
                  </div>
                  <div class="compose-body">
                    <textarea v-model="commentForm.content" rows="3" placeholder="写下你的想法...请控制评论内容在1000汉字和字符内"></textarea>
                    <div class="comment-upload">
                      <div class="comment-upload-head">
                        <span class="comment-upload-title">附件</span>
                        <label class="comment-upload-btn">
                          <input type="file" multiple @change="onCommentFileChange" />
                          选择文件
                        </label>
                        <span v-if="commentPendingFiles.length" class="comment-upload-count">已选 {{ commentPendingFiles.length }} 个</span>
                      </div>
                      <div v-if="commentPendingFiles.length" class="comment-upload-list">
                        <div v-for="(file, index) in commentPendingFiles" :key="file.name + file.size + index" class="comment-upload-item">
                          <span class="comment-upload-name">{{ file.name }}</span>
                          <span class="comment-upload-size">{{ prettySize(file.size) }}</span>
                          <button class="icon-btn" type="button" @click="removeCommentPendingFile(index)">×</button>
                        </div>
                      </div>
                      <div v-if="commentUploading" class="comment-upload-progress">
                        <div class="comment-upload-bar" :style="{ width: commentUploadProgress + '%' }"></div>
                      </div>
                    </div>
                    <div class="compose-footer">
                      <span class="comment-count" v-if="commentList.length > 0">共 {{ commentList.length }} 条评论</span>
                      <span class="comment-count" v-else>暂无评论，快来抢沙发吧~</span>
                      <button class="btn primary submit-btn" @click="submitProjectComment" :disabled="commentSubmitting">
                        {{ commentSubmitting ? '发送中...' : '发送' }}
                      </button>
                    </div>
                  </div>
                  <div v-if="commentError" class="comment-error">{{ commentError }}</div>
                </div>
              </div>

              <div class="comment-pane-right">
                <div class="comment-list-container">
                  <div v-if="commentLoading" class="state loading-state">
                    <div class="spinner"></div>
                    <span>加载评论中...</span>
                  </div>
                  <div v-else-if="!commentList || commentList.length === 0" class="empty-comment-state">
                    <div class="empty-icon">💬</div>
                    <p>还没有人评论，来说两句吧</p>
                  </div>
                  <div v-else class="comment-items">
                    <div v-for="item in commentList" :key="item.commentId || item.id" class="comment-card">
                      <div class="comment-avatar">
                        {{ (item.displayName || item.userName || item.userId || '?').charAt(0).toUpperCase() }}
                      </div>
                      <div class="comment-main">
                        <div class="comment-header">
                          <span class="comment-author">{{ item.displayName || item.userName || item.userId || '未知用户' }}</span>
                          <div class="comment-actions">
                            <span class="comment-date">{{ formatDate(item.createTime) }}</span>
                            <button v-if="Number(item.userId) === Number(currentUserId)" class="comment-delete-btn" type="button" @click="deleteProjectComment(item)">删除</button>
                          </div>
                        </div>
                        <div class="comment-text">{{ item.content }}</div>
                        <div v-if="(commentFilesByCommentId[item.commentId] || []).length" class="comment-file-list">
                          <div v-for="file in commentFilesByCommentId[item.commentId]" :key="file.fileId" class="comment-file-item">
                            <template v-if="isImageFile(file.filePath)">
                              <button class="comment-image-link" type="button" @click="openCommentImagePreview(file)">
                                <img :src="commentPreviewUrl(file.fileId)" :alt="fileBaseName(file.filePath)" class="comment-image-thumb" />
                              </button>
                              <div class="comment-file-meta">
                                <a class="comment-file-name" :href="commentDownloadUrl(file.fileId)" :download="fileBaseName(file.filePath)" target="_blank">
                                  {{ fileBaseName(file.filePath) }}
                                </a>
                                <span class="comment-file-size">{{ prettySize(file.fileSize) }}</span>
                              </div>
                            </template>
                            <template v-else>
                              <a class="comment-file-name" :href="commentDownloadUrl(file.fileId)" :download="fileBaseName(file.filePath)" target="_blank">
                                {{ fileBaseName(file.filePath) }}
                              </a>
                              <span class="comment-file-size">{{ prettySize(file.fileSize) }}</span>
                            </template>
                          </div>
                        </div>
                        <div class="comment-footer">
                          <button class="comment-reply-btn" type="button" @click="toggleReplyForm(item.commentId)">回复</button>
                        </div>
                        <div v-if="replyFormVisibleByCommentId[item.commentId]" class="comment-reply-editor">
                          <textarea v-model="replyFormContentByCommentId[item.commentId]" rows="2" placeholder="输入回复内容"></textarea>
                          <div class="comment-reply-upload">
                            <label class="comment-upload-btn">
                              <input type="file" multiple @change="onReplyFileChange(item.commentId, $event)" />
                              选择文件
                            </label>
                            <span v-if="(replyPendingFilesByCommentId[item.commentId] || []).length" class="comment-upload-count">已选 {{ (replyPendingFilesByCommentId[item.commentId] || []).length }} 个</span>
                          </div>
                          <div v-if="(replyPendingFilesByCommentId[item.commentId] || []).length" class="comment-upload-list">
                            <div v-for="(file, index) in (replyPendingFilesByCommentId[item.commentId] || [])" :key="file.name + file.size + index" class="comment-upload-item">
                              <span class="comment-upload-name">{{ file.name }}</span>
                              <span class="comment-upload-size">{{ prettySize(file.size) }}</span>
                              <button class="icon-btn" type="button" @click="removeReplyPendingFile(item.commentId, index)">×</button>
                            </div>
                          </div>
                          <div v-if="replyUploadingByCommentId[item.commentId]" class="comment-upload-progress">
                            <div class="comment-upload-bar" :style="{ width: (replyUploadProgressByCommentId[item.commentId] || 0) + '%' }"></div>
                          </div>
                          <div class="comment-reply-actions">
                            <button class="btn primary submit-btn" type="button" @click="submitReply(item)" :disabled="replySubmittingByCommentId[item.commentId]">
                              {{ replySubmittingByCommentId[item.commentId] ? '发送中...' : '发送回复' }}
                            </button>
                          </div>
                        </div>
                        <div v-if="(replyListByCommentId[item.commentId] || []).length" class="comment-reply-list">
                          <div v-for="reply in replyListByCommentId[item.commentId]" :key="reply.replyId" class="comment-reply-item">
                            <div class="comment-reply-header">
                              <span class="comment-reply-author">{{ reply.displayName || reply.userName || reply.userId || '未知用户' }}</span>
                              <div class="comment-reply-actions">
                                <span class="comment-reply-date">{{ formatDate(reply.createTime) }}</span>
                                <button v-if="Number(reply.userId) === Number(currentUserId)" class="comment-reply-delete-btn" type="button" @click="deleteReply(reply, item.commentId)">删除</button>
                              </div>
                            </div>
                            <div class="comment-reply-content">{{ reply.content }}</div>
                            <div v-if="(replyFilesByReplyId[reply.replyId] || []).length" class="comment-file-list">
                              <div v-for="file in replyFilesByReplyId[reply.replyId]" :key="file.fileId" class="comment-file-item">
                                <template v-if="isImageFile(file.filePath)">
                                  <button class="comment-image-link" type="button" @click="openReplyImagePreview(file)">
                                    <img :src="replyPreviewUrl(file.fileId)" :alt="fileBaseName(file.filePath)" class="comment-image-thumb" />
                                  </button>
                                  <div class="comment-file-meta">
                                    <a class="comment-file-name" :href="replyDownloadUrl(file.fileId)" :download="fileBaseName(file.filePath)" target="_blank">
                                      {{ fileBaseName(file.filePath) }}
                                    </a>
                                    <span class="comment-file-size">{{ prettySize(file.fileSize) }}</span>
                                  </div>
                                </template>
                                <template v-else>
                                  <a class="comment-file-name" :href="replyDownloadUrl(file.fileId)" :download="fileBaseName(file.filePath)" target="_blank">
                                    {{ fileBaseName(file.filePath) }}
                                  </a>
                                  <span class="comment-file-size">{{ prettySize(file.fileSize) }}</span>
                                </template>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>


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

        <!-- 交付物查看弹窗（结构化版本，去除上下文与模板功能） -->
        <div v-if="showDeliverableDialog" class="dialog-mask" @click.self="closeDeliverableDialog">
          <div class="dialog view-dialog">
            <h4>交付物列表</h4>
            <!-- 去除上下文标签与模式切换 -->

            <div class="form-row">
              <div v-if="deliverableLoading">正在加载交付物...</div>
              <div v-else-if="deliverableError" class="state error">{{ deliverableError }}</div>
              <div v-else>
                <div v-if="!deliverableList || deliverableList.length === 0" class="empty">当前未配置可查看的交付物</div>
                <div v-else class="deliverable-cards">
                  <div class="deliverable-card" v-for="d in deliverableList" :key="d.deliverableId || d.id">
                    <div class="deliverable-head">
                      <div class="deliverable-meta">
                        <span class="deliverable-name">{{ d.deliverableName || d.name }}</span>
                        <span class="deliverable-type">{{ d.deliverableType || '-' }}</span>
                      </div>
                    </div>
                    <!-- 文件列表区域：查看模式下显示已上传文件 -->
                    <div class="uploaded-list" v-if="uploadedFilesByDeliverableId[(d.deliverableId || d.id)]?.length">
                      <div class="template-title">已上传文件：</div>
                      <ul class="file-list compact">
                        <li v-for="f in uploadedFilesByDeliverableId[(d.deliverableId || d.id)]" :key="f.fileId"
                          class="file-item">
                          <button type="button" class="file-link preview-link" @click="onPreviewFile(f)">{{
                            fileBaseName(f.filePath) }}</button>
                          <span class="size">{{ prettySize(f.fileSize) }}</span>
                          <a class="icon-btn" :href="downloadURL(f.fileId)" :download="fileBaseName(f.filePath)"
                            title="下载" target="_blank">
                            <svg viewBox="0 0 24 24">
                              <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                            </svg>
                          </a>
                        </li>
                      </ul>
                    </div>
                    <div class="uploaded-list" v-else>
                      <div class="template-title" style="color:#999">该交付物暂无已上传文件</div>
                    </div>
                  </div>
                </div>
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
                <div v-if="!uploadDeliverables || uploadDeliverables.length === 0" style="color:#999">当前步骤/里程碑下没有交付物
                </div>
                <div v-else class="upload-list">
                  <div v-for="d in uploadDeliverables" :key="d.deliverableId" class="upload-item">
                    <div class="upload-head">
                      <span class="name">{{ d.deliverableName }}</span>
                      <span class="hint">支持多文件上传</span>
                    </div>
                    <div class="template-chips" v-if="uploadTemplatesByDeliverableId[d.deliverableId]?.length">
                      <div class="template-title">模板：</div>
                      <div class="chip-group">
                        <button type="button" class="chip clickable"
                          v-for="t in uploadTemplatesByDeliverableId[d.deliverableId]" :key="t.name"
                          @click="downloadTemplateForDialog(d.deliverableId, t.name)">
                          <span class="chip-name">{{ t.name }}</span>
                        </button>
                      </div>
                    </div>
                    <div class="upload-actions">
                      <label class="btn primary" :class="{ disabled: uploadingByDeliverableId[d.deliverableId] }"
                        :for="`file-input-${d.deliverableId}`">选择文件</label>
                      <input :id="`file-input-${d.deliverableId}`" type="file" multiple
                        :disabled="uploadingByDeliverableId[d.deliverableId]"
                        @change="handleUploadFileSelectedForDeliverable(d, $event)" hidden />
                      <div class="progress" v-if="uploadingByDeliverableId[d.deliverableId]">
                        <div class="bar"
                          :style="{ width: (uploadProgressByDeliverableId[d.deliverableId] || 0) + '%' }"></div>
                        <span class="percent">{{ uploadProgressByDeliverableId[d.deliverableId] || 0 }}%</span>
                      </div>
                    </div>
                    <div class="uploaded-list" v-if="uploadedFilesByDeliverableId[d.deliverableId]?.length">
                      <div class="template-title">已上传文件：</div>
                      <ul class="file-list compact">
                        <li v-for="f in uploadedFilesByDeliverableId[d.deliverableId]" :key="f.fileId"
                          class="file-item">
                          <button type="button" class="file-link preview-link" @click="onPreviewFile(f)">{{
                            fileBaseName(f.filePath) }}</button>
                          <span class="size">{{ prettySize(f.fileSize) }}</span>
                          <button class="icon-btn danger" :class="{ disabled: isProjectCompleted }"
                            :disabled="isProjectCompleted" title="删除"
                            @click="deleteUploadedFile(f.fileId, d.deliverableId)">
                            <svg viewBox="0 0 24 24">
                              <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                            </svg>
                          </button>
                          <!-- 已移除下载按钮：上传交付物界面不提供下载入口 -->
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

        <!-- 文件名点击进行预览；右侧下载图标已移除（按需保留预览与删除） -->

        <!-- 隐藏上传输入 -->
        <input ref="deliverableUploader" type="file" multiple style="display:none"
          @change="handleDeliverableFilesSelected" />

        <!-- 全屏文件预览弹窗 -->
        <div v-if="showPreviewDialog" class="preview-overlay">
          <div class="preview-header">
            <span class="title">{{ previewTitle }}</span>
            <div class="tools">
              <button v-if="previewType !== 'pdf' && previewType !== 'sheet'" class="icon-btn" title="缩小"
                @click="pdfZoomOut">
                <svg viewBox="0 0 24 24">
                  <path d="M19 13H5v-2h14v2z" />
                </svg>
              </button>
              <button v-if="previewType !== 'pdf' && previewType !== 'video'" class="icon-btn" title="放大"
                @click="pdfZoomIn">
                <svg viewBox="0 0 24 24">
                  <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                </svg>
              </button>
              <button class="icon-btn" title="关闭" @click="closePreviewDialog">
                <svg viewBox="0 0 24 24">
                  <path
                    d="M18.3 5.71L12 12.01 5.7 5.71 4.29 7.12 10.59 13.41 4.29 19.7 5.7 21.11 12 14.82 18.3 21.11 19.71 19.7 13.41 13.41 19.71 7.12z" />
                </svg>
              </button>
            </div>
          </div>
          <div class="preview-body">
            <div v-if="previewLoading" class="loading">正在加载…</div>
            <div v-else-if="previewError" class="error">{{ previewError }}</div>
            <div v-else class="preview-content"
              :style="(previewType === 'pdf' || previewType === 'video') ? { width: '100%', height: 'calc(100vh - 48px - 24px)', display: 'block' } : { transform: 'scale(' + previewScale + ')', transformOrigin: 'center top' }">
              <img v-if="previewType === 'image'" :src="previewUrl" class="preview-image" />
              <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="pdf-embed"></iframe>
              <video v-else-if="previewType === 'video'" :src="previewUrl" class="video-player" controls autoplay
                playsinline></video>
              <div v-else-if="previewType === 'docx'" class="html-view" v-html="previewHTML"></div>

              <pre v-else-if="previewType === 'text'" class="text-view">{{ previewText }}</pre>
              <div v-else class="unsupported">文件格式不支持预览，请下载后查看</div>
            </div>
          </div>
        </div>



      </div>

      <!-- 其他标签页空白占位 -->
      <div v-show="['warning', 'statistics', 'daily_report', 'modification_record'].includes(activeTab)"
        class="empty-tab">
        <div class="empty-state">
          <div class="empty-icon">📂</div>
          <h3>{{ getTabName(activeTab) }}</h3>
          <p>该模块正在建设中...</p>
        </div>
      </div>
    </div>
    <!-- 新增合同外需求弹窗 -->
    <div v-if="showExtraDialog" class="dialog-mask extra-modal-overlay">
      <div class="extra-modal">
        <div class="extra-modal-header">
          <h3>{{ extraDialogMode === 'create' ? '新增合同外需求' : (extraDialogMode === 'edit' ? '编辑合同外需求' : '查看合同外需求') }}</h3>
          <button class="extra-close" @click="closeExtraDialog">&times;</button>
        </div>
        <div class="extra-modal-body">
          <form class="extra-form" @submit.prevent>
            <div class="extra-section">
              <div class="extra-grid">
                <div class="extra-group">
                  <label>需求名称 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <input type="text" v-model.trim="extraForm.requirementName" placeholder="请输入需求名称"
                    :disabled="extraDialogMode === 'view'" />
                </div>
                <div class="extra-group">
                  <label>是否付费 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <select v-model="extraForm.isPay" :disabled="extraDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option :value="false">否</option>
                    <option :value="true">是</option>
                  </select>
                </div>
                <div class="extra-group" v-if="extraForm.isPay">
                  <label>付费金额 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <input type="number" v-model.number="extraForm.payAmount" step="0.01" placeholder="请输入金额（元）"
                    :disabled="extraDialogMode === 'view'" />
                </div>
                <div class="extra-group">
                  <label>是否交付 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <select v-model="extraForm.isDeliver" :disabled="extraDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option :value="false">否</option>
                    <option :value="true">是</option>
                  </select>
                </div>
                <div class="extra-group">
                  <label>是否完成 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <select v-model="extraForm.isComplete" :disabled="extraDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option :value="false">否</option>
                    <option :value="true">是</option>
                  </select>
                </div>
                <div class="extra-group">
                  <label>是否产品化 <span class="required" v-if="extraDialogMode !== 'view'">*</span></label>
                  <select v-model="extraForm.isProductization" :disabled="extraDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option :value="false">否</option>
                    <option :value="true">是</option>
                  </select>
                </div>
                <div class="extra-group">
                  <label>工作量</label>
                  <input type="number" v-model.number="extraForm.workload" step="0.01" placeholder="请输入工作量（人天）"
                    :disabled="extraDialogMode === 'view'" />
                </div>
                <div class="extra-group">
                  <label>开发负责人</label>
                  <select v-model="extraForm.developer" :disabled="extraDialogMode === 'view'">
                    <option :value="null">请选择负责人</option>
                    <option v-for="u in allUsers" :key="u.userId" :value="u.userId">
                      {{ u.name || u.userName }}
                    </option>
                  </select>
                </div>
                <div class="extra-group full-width">
                  <div class="extra-section-title">上传附件</div>
                  <div class="extra-upload-card">
                    <div class="extra-upload-head" v-if="extraDialogMode === 'edit' || extraDialogMode === 'create'">
                      <button type="button" class="btn primary select-btn"
                        @click="triggerExtraAttachmentInput">选择文件</button>
                      <input ref="extraAttachmentInput" type="file" multiple class="hidden-file"
                        @change="onExtraFilesSelected($event)" />
                    </div>
                    <div class="extra-upload-body">
                      <div class="progress" v-if="extraUploading">
                        <div class="bar" :style="{ width: extraUploadProgress + '%' }"></div>
                        <span class="percent">{{ extraUploadProgress }}%</span>
                      </div>
                      <div class="uploaded-list" v-if="extraDialogMode === 'create' && extraPendingFiles.length">
                        <div class="template-title">待上传附件：</div>
                        <ul class="file-list compact">
                          <li v-for="(f, idx) in extraPendingFiles" :key="f.name + '-' + idx" class="file-item">
                            <span class="file-link">{{ f.name }}</span>
                            <span class="size">{{ prettySize(f.size) }}</span>
                            <button class="icon-btn danger" title="移除" @click="removeExtraPendingFile(idx)">
                              <svg viewBox="0 0 24 24">
                                <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                              </svg>
                            </button>
                          </li>
                        </ul>
                      </div>
                      <div class="uploaded-list" v-if="extraAttachments.length">
                        <ul class="file-list compact">
                          <li v-for="f in extraAttachments" :key="f.fileId" class="file-item">
                            <button type="button" class="file-link preview-link" @click="onPreviewExtraFile(f)">{{
                              fileBaseName(f.filePath) }}</button>
                            <span class="size">{{ prettySize(f.fileSize) }}</span>
                            <a class="icon-btn" :href="convertExtraDownloadURL(f.fileId)"
                              :download="fileBaseName(f.filePath)" title="下载" target="_blank">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                              </svg>
                            </a>
                            <button class="icon-btn danger" v-if="extraDialogMode === 'edit'" title="删除"
                              @click="onDeleteExtraFile(f)">
                              <svg viewBox="0 0 24 24">
                                <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                              </svg>
                            </button>
                          </li>
                        </ul>
                      </div>
                      <div class="uploaded-list" v-else-if="!extraPendingFiles.length">
                        <div class="template-title" style="color:#999">暂无附件</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>
        <div class="extra-modal-footer">
          <div class="extra-actions">
            <button class="btn primary" @click="confirmExtra">{{ extraDialogMode === 'view' ? '关闭' : '确定' }}</button>
            <button class="btn ghost" @click="closeExtraDialog" v-if="extraDialogMode !== 'view'">取消</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showRiskDialog" class="dialog-mask extra-modal-overlay">
      <div class="extra-modal">
        <div class="extra-modal-header">
          <h3>{{ riskDialogMode === 'create' ? '新增项目风险' : (riskDialogMode === 'edit' ? '编辑项目风险' : '查看项目风险') }}</h3>
          <button class="extra-close" @click="closeRiskDialog">&times;</button>
        </div>
        <div class="extra-modal-body">
          <form class="extra-form" @submit.prevent>
            <div class="extra-section">
              <div class="extra-grid">
                <div class="extra-group">
                  <label>风险类型 <span class="required" v-if="riskDialogMode !== 'view'">*</span></label>
                  <select v-model="riskForm.riskType" :disabled="riskDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option v-for="t in riskTypeOptions" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div class="extra-group">
                  <label>风险级别 <span class="required" v-if="riskDialogMode !== 'view'">*</span></label>
                  <select v-model="riskForm.riskLevel" :disabled="riskDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option v-for="t in riskLevelOptions" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div class="extra-group">
                  <label>是否解除 <span class="required" v-if="riskDialogMode !== 'view'">*</span></label>
                  <select v-model="riskForm.isRelieve" :disabled="riskDialogMode === 'view'">
                    <option :value="null">请选择</option>
                    <option :value="false">否</option>
                    <option :value="true">是</option>
                  </select>
                </div>
                <div class="extra-group full-width" v-if="riskForm.isRelieve">
                  <label>解除方式 <span class="required" v-if="riskDialogMode !== 'view'">*</span></label>
                  <textarea rows="2" v-model.trim="riskForm.relieveWay" placeholder="请输入解除方式"
                    :disabled="riskDialogMode === 'view'"></textarea>
                </div>
                <div class="extra-group full-width">
                  <label>风险描述 <span class="required" v-if="riskDialogMode !== 'view'">*</span></label>
                  <textarea rows="3" v-model.trim="riskForm.riskDescription" placeholder="请输入风险描述"
                    :disabled="riskDialogMode === 'view'"></textarea>
                </div>
                <div class="extra-group full-width">
                  <label>风险评估</label>
                  <textarea rows="3" v-model.trim="riskForm.riskEvaluate" placeholder="请输入风险评估"
                    :disabled="riskDialogMode === 'view'"></textarea>
                </div>
                <div class="extra-group full-width">
                  <div class="extra-section-title">上传附件</div>
                  <div class="extra-upload-card">
                    <div class="extra-upload-head" v-if="riskDialogMode === 'edit' || riskDialogMode === 'create'">
                      <button type="button" class="btn primary select-btn"
                        @click="triggerRiskAttachmentInput">选择文件</button>
                      <input ref="riskAttachmentInput" type="file" multiple class="hidden-file"
                        @change="onRiskFilesSelected($event)" />
                    </div>
                    <div class="extra-upload-body">
                      <div class="progress" v-if="riskUploading">
                        <div class="bar" :style="{ width: riskUploadProgress + '%' }"></div>
                        <span class="percent">{{ riskUploadProgress }}%</span>
                      </div>
                      <div class="uploaded-list" v-if="riskDialogMode === 'create' && riskPendingFiles.length">
                        <div class="template-title">待上传附件：</div>
                        <ul class="file-list compact">
                          <li v-for="(f, idx) in riskPendingFiles" :key="f.name + '-' + idx" class="file-item">
                            <span class="file-link">{{ f.name }}</span>
                            <span class="size">{{ prettySize(f.size) }}</span>
                            <button class="icon-btn danger" title="移除" @click="removeRiskPendingFile(idx)">
                              <svg viewBox="0 0 24 24">
                                <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                              </svg>
                            </button>
                          </li>
                        </ul>
                      </div>
                      <div class="uploaded-list" v-if="riskAttachments.length">
                        <ul class="file-list compact">
                          <li v-for="f in riskAttachments" :key="f.fileId" class="file-item">
                            <button type="button" class="file-link preview-link" @click="onPreviewRiskFile(f)">{{
                              fileBaseName(f.filePath) }}</button>
                            <span class="size">{{ prettySize(f.fileSize) }}</span>
                            <a class="icon-btn" :href="convertRiskDownloadURL(f.fileId)"
                              :download="fileBaseName(f.filePath)" title="下载" target="_blank">
                              <svg viewBox="0 0 24 24">
                                <path d="M5 20h14v-2H5v2zM12 4v8l4-4h-3l-1 1-1-1H8l4 4V4z" />
                              </svg>
                            </a>
                            <button class="icon-btn danger" v-if="riskDialogMode === 'edit'" title="删除"
                              @click="onDeleteRiskFile(f)">
                              <svg viewBox="0 0 24 24">
                                <path d="M6 7h12v2H6V7zm2 4h8v8H8v-8zM9 4h6v2H9V4z" />
                              </svg>
                            </button>
                          </li>
                        </ul>
                      </div>
                      <div class="uploaded-list" v-else-if="!riskPendingFiles.length">
                        <div class="template-title" style="color:#999">暂无附件</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>
        <div class="extra-modal-footer">
          <div class="extra-actions">
            <button class="btn primary" @click="confirmRisk">{{ riskDialogMode === 'view' ? '关闭' : '确定' }}</button>
            <button class="btn ghost" @click="closeRiskDialog" v-if="riskDialogMode !== 'view'">取消</button>
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
import { createExtraRequirement, listExtraRequirementsByProject, updateExtraRequirement, deleteExtraRequirement, uploadExtraRequirementFiles, listExtraRequirementFiles, deleteExtraRequirementFile } from '../api/extraRequirement';
import { createConstructingProjectRisk, listConstructingProjectRisksByProject, updateConstructingProjectRisk, deleteConstructingProjectRisk, uploadConstructingProjectRiskFiles, listConstructingProjectRiskFiles, deleteConstructingProjectRiskFile } from '../api/constructingProjectRisk';
import { listConstructingProjectComments, createConstructingProjectComment, deleteConstructingProjectComment } from '../api/constructingProjectComment';
import { createConstructingProjectCommentReply, listConstructingProjectCommentReplies, deleteConstructingProjectCommentReply } from '../api/constructingProjectCommentReply';
import { uploadConstructingProjectCommentReplyFiles, listConstructingProjectCommentReplyFilesByComment, getConstructingProjectCommentReplyFilePreviewUrl, getConstructingProjectCommentReplyFileDownloadUrl } from '../api/constructingProjectCommentReplyFile';
import { uploadConstructingProjectCommentFiles, listConstructingProjectCommentFilesByProject, getConstructingProjectCommentFilePreviewUrl, getConstructingProjectCommentFileDownloadUrl } from '../api/constructingProjectCommentFile';
import request from '../api/request'
// 引入预览依赖：Mammoth（docx→HTML）、XLSX
import mammoth from 'mammoth/mammoth.browser'
// 已移除 Luckysheet/XLSX 前端预览，统一走后端转 PDF
import { uploadConstructDeliverableFiles, listConstructDeliverableFiles, deleteConstructDeliverableFile } from '../api/constructDeliverableFile';
import { ElMessageBox } from 'element-plus'
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
      // 标签页状态
      activeTab: 'contract',
      tabs: [
        { id: 'contract', name: '合同内建设内容' },
        { id: 'out_contract', name: '合同外需求' },
        { id: 'risk', name: '项目风险' },
        { id: 'warning', name: '项目预警' },
        { id: 'statistics', name: '项目统计' },
        { id: 'daily_report', name: '项目日（周）报' },
        { id: 'project_comment', name: '项目评论' },
        { id: 'modification_record', name: '修改记录' }
      ],
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
      // 模板存在性缓存：{ [deliverableId]: boolean }
      hasTemplatesByDeliverableId: {}
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
      previewScale: 1.0,
      tooltip: { visible: false, content: '', style: { top: '0px', left: '0px' } }
      ,
      // 合同外需求数据与表单
      extraRequirements: [],
      showExtraDialog: false,
      extraDialogMode: 'create', // create | edit | view
      extraAttachments: [],
      extraPendingFiles: [],
      extraUploading: false,
      extraUploadProgress: 0,
      extraForm: {
        requirementName: '',
        isPay: null,
        payAmount: null,
        isDeliver: null,
        isComplete: null,
        isProductization: null,
        workload: null,
        developer: null
      },
      projectRisks: [],
      showRiskDialog: false,
      riskDialogMode: 'create',
      riskAttachments: [],
      riskPendingFiles: [],
      riskUploading: false,
      riskUploadProgress: 0,
      riskTypeOptions: ['需求控制', '需求敲定', '协调配合', '数据迁移', '商务关系', '其他'],
      riskLevelOptions: ['高', '中', '低'],
      riskForm: {
        riskType: null,
        riskLevel: null,
        isRelieve: null,
        relieveWay: '',
        riskDescription: '',
        riskEvaluate: ''
      },
      commentList: [],
      commentLoading: false,
      commentSubmitting: false,
      commentError: '',
      commentUploading: false,
      commentUploadProgress: 0,
      commentPendingFiles: [],
      commentFilesByCommentId: {},
      currentUserId: null,
      replyListByCommentId: {},
      replyFilesByReplyId: {},
      replyFormVisibleByCommentId: {},
      replyFormContentByCommentId: {},
      replyPendingFilesByCommentId: {},
      replyUploadingByCommentId: {},
      replyUploadProgressByCommentId: {},
      replySubmittingByCommentId: {},
      commentForm: {
        content: ''
      }
    };
  },
  computed: {
    isProjectCompleted() {
      const s = this.project && this.project.projectState ? String(this.project.projectState).trim() : ''
      const lower = s.toLowerCase()
      return s === '已完成' || s === '完成' || lower === 'completed'
    },
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

        // 计算里程碑完成状态：存在步骤且全部为“已完成”
        const allCompleted = (list && list.length > 0) ? list.every(s => (s.isCompleted === true) || ((s.stepStatus || s.status) === '已完成')) : false

        // 里程碑行（若未生成则插入占位）
        const pm = (this.milestones || []).find(m => m.milestoneName === name)
        if (pm) {
          rows.push({ ...pm, milestoneName: name, milestonePeriod: sumActual, iscomplete: allCompleted, rowType: 'milestone' })
        } else {
          rows.push({ milestoneName: name, milestoneId: `placeholder-${name}`, milestonePeriod: sumActual, iscomplete: allCompleted, rowType: 'milestone' })
        }
      }

      return rows
    }
  },
  created() {
    try {
      const userInfoRaw = sessionStorage.getItem('userInfo')
      const userInfo = userInfoRaw ? JSON.parse(userInfoRaw) : null
      this.currentUserId = userInfo && userInfo.userId ? Number(userInfo.userId) : null
    } catch (_) {
      this.currentUserId = null
    }
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
    getTabName(id) {
      const tab = this.tabs.find(t => t.id === id);
      return tab ? tab.name : '';
    },
    /**
     * 函数级注释：显示表格单元格的悬浮提示
     * @param {MouseEvent} e 鼠标事件
     * @param {string} content 提示内容
     */
    showTooltip(e, content) {
      if (!content) return
      this.tooltip.content = content
      this.tooltip.visible = true
      this.updateTooltipPosition(e)
    },
    /**
     * 函数级注释：隐藏表格单元格的悬浮提示
     */
    hideTooltip() {
      this.tooltip.visible = false
    },
    /**
     * 函数级注释：更新悬浮提示的位置
     * @param {MouseEvent} e 鼠标事件
     */
    updateTooltipPosition(e) {
      const x = e.clientX + 15
      const y = e.clientY + 15
      this.tooltip.style = {
        top: `${y}px`,
        left: `${x}px`
      }
    },
    /**
     * 函数级注释：获取与当前行关联的标准交付物列表
     * 关联规则：
     * - 步骤/接口步骤/个性化步骤：使用 `row.sstepId` 匹配 `deliverables[].sstepId`
     * - 里程碑：优先使用 `row.milestoneId` 映射标准里程碑ID；若缺失则按 `row.milestoneName` 在已加载的标准里程碑中查找ID，匹配 `deliverables[].milestoneId`
     * @param {Object} row 当前表格行对象
     * @returns {Array<Object>} 与行关联的交付物列表
     */
    getDeliverablesForRow(row) {
      if (!row || !Array.isArray(this.deliverables)) return []
      if (row.rowType === 'step' || row.rowType === 'interface_step' || row.rowType === 'personal_step') {
        const sid = row.sstepId
        if (sid == null) return []
        return this.deliverables.filter(d => d && d.sstepId === sid)
      }
      if (row.rowType === 'milestone') {
        // 构造标准里程碑ID：项目里程碑ID与标准里程碑ID不同域，必须通过名称映射
        // 为提高鲁棒性，名称匹配忽略大小写并去除首尾空格
        let stdMid = null
        if (this.standardMilestones && row.milestoneName) {
          const targetName = String(row.milestoneName).trim().toLowerCase()
          const m = this.standardMilestones.find(x => String(x?.milestoneName || '').trim().toLowerCase() === targetName)
          stdMid = m ? m.milestoneId : null
        }
        if (stdMid == null) return []
        // 仅返回关联到该标准里程碑的交付物
        return (this.deliverables || []).filter(d => d && d.milestoneId === stdMid)
      }
      return []
    },
    /**
     * 函数级注释：判断当前行是否存在必须上传的交付物
     * 依据 `standard_deliverable.isMustLoad` 字段，若任一关联交付物为 true，则返回 true。
     * @param {Object} row 当前表格行对象
     * @returns {boolean} 是否存在必须上传的交付物
     */
    isMustUploadForRow(row) {
      let list = this.getDeliverablesForRow(row)
      // 里程碑行：仅考虑“里程碑交付物”，避免步骤交付物误标记
      if (row?.rowType === 'milestone') {
        list = list.filter(d => String(d?.deliverableType || '') === '里程碑交付物')
      }
      return list.some(d => d && (d.isMustLoad === true || d.isMustLoad === 1))
    },
    /**
     * 函数级注释：判断当前行是否存在“交付物模板”
     * 规则：若与该行关联的任一交付物存在模板文件（长度>0），则返回 true。
     * - 步骤/接口/个性化：按 sstepId 关联交付物；
     * - 里程碑：仅考虑 deliverableType === '里程碑交付物' 的交付物。
     * 为避免频繁请求，使用 hasTemplatesByDeliverableId 进行缓存；
     * 若缓存缺失，会在后台异步补充，完成后自动触发视图更新。
     * @param {Object} row 当前表格行对象
     * @returns {boolean} 是否存在模板
     */
    hasTemplatesForRow(row) {
      let list = this.getDeliverablesForRow(row)
      if (row?.rowType === 'milestone') {
        list = list.filter(d => String(d?.deliverableType || '') === '里程碑交付物')
      }
      if (!Array.isArray(list) || list.length === 0) return false
      let hit = false
      for (const d of list) {
        const did = d && (d.deliverableId || d.id)
        if (did == null) continue
        const cached = this.hasTemplatesByDeliverableId[did]
        if (cached === true) {
          hit = true
        } else if (cached === undefined) {
          // 异步补充缓存，不阻塞当前渲染
          this.ensureTemplateFlagForDeliverable(did)
        }
      }
      return hit
    },
    /**
     * 函数级注释：确保某交付物的模板存在性已写入缓存
     * 若尚未查询，则调用后端获取模板列表并写入 hasTemplatesByDeliverableId。
     * @param {number} deliverableId 交付物ID
     */
    async ensureTemplateFlagForDeliverable(deliverableId) {
      try {
        if (this.hasTemplatesByDeliverableId[deliverableId] !== undefined) return
        const files = await listDeliverableTemplates(deliverableId)
        this.hasTemplatesByDeliverableId[deliverableId] = Array.isArray(files) ? files.length > 0 : false
      } catch (_) {
        this.hasTemplatesByDeliverableId[deliverableId] = false
      }
    },
    /**
     * 函数级注释：判断当前行是否已有上传的交付物文件
     * 逻辑：遍历关联交付物，若其 `deliverableId` 在 `filesByDeliverableId` 中存在非空文件列表，则视为已上传。
     * @param {Object} row 当前表格行对象
     * @returns {boolean} 是否存在已上传文件
     */
    hasUploadedForRow(row) {
      // 修复：按当前行上下文过滤，避免不同接口/个性化的同名步骤互相“染色”
      // 步骤/接口/个性化：按 relationId 过滤；里程碑：按项目里程碑ID过滤
      const list = this.getDeliverablesForRow(row)
      if (!Array.isArray(list) || list.length === 0) return false
      const allFiles = Array.isArray(this.files) ? this.files : []
      // 里程碑仅考虑“里程碑交付物”，避免步骤交付物影响按钮状态
      const isMilestone = row?.rowType === 'milestone'
      const projectStepId = row?.relationId || null
      const milestoneId = row?.milestoneId || null
      for (const d of list) {
        const did = d && (d.deliverableId || d.id)
        if (did == null) continue
        if (isMilestone) {
          if (String(d?.deliverableType || '') !== '里程碑交付物') continue
          const hit = allFiles.some(f => f && f.deliverableId === did && f.milestoneId === milestoneId)
          if (hit) return true
        } else {
          const hit = allFiles.some(f => f && f.deliverableId === did && f.projectStepId === projectStepId)
          if (hit) return true
        }
      }
      return false
    },
    /**
     * 函数级注释：为“上传”按钮生成样式类
     * 需求：
     * - 若当前行存在必须上传的交付物（isMustLoad=true），添加红色（danger）。
     * - 若存在交付物模板，添加模板标记（has-template）。
     * @param {Object} row 当前表格行对象
     * @returns {Object} 样式类对象
     */
    uploadButtonClass(row) {
      return {
        danger: this.isMustUploadForRow(row),
        'has-template': this.hasTemplatesForRow(row)
      }
    },
    /**
     * 函数级注释：为“查看”按钮生成样式类
     * 需求：若当前行已经上传了交付物文件，则查看按钮设置为绿色（success）。
     * @param {Object} row 当前表格行对象
     * @returns {string|Object|Array} 样式类：'success' 或 空
     */
    viewButtonClass(row) {
      return this.hasUploadedForRow(row) ? 'success' : ''
    },
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
        } else if ((row.rowType === 'interface_step' || row.rowType === 'personal_step') && row.sstepId) {
          // 新增：接口/个性化步骤也支持查看交付物（与标准步骤同源）
          const resp = await getStandardDeliverablesByStepId(row.sstepId)
          list = resp?.deliverables || resp?.items || resp || []
        } else if (row.rowType === 'milestone') {
          // 优先使用里程碑ID，其次根据名称在已加载的标准里程碑中映射ID
          let mid = row.milestoneId || null
          if (!mid && this.standardMilestones && row.milestoneName) {
            const m = this.standardMilestones.find(x => x.milestoneName === row.milestoneName)
            mid = m ? m.milestoneId : null
          }
          const projectIdForMilestone = this.project?.projectId
          if (mid) {
            const resp = await getStandardDeliverables({ milestoneId: mid, page: 0, size: 100 })
            list = resp?.deliverables || resp?.items || resp || []
          }
          // 如果找不到标准里程碑ID，或按ID查询为空，则按【项目ID + 里程碑名称】回退查询
          if ((!mid || (Array.isArray(list) && list.length === 0)) && projectIdForMilestone && row.milestoneName) {
            const byName = await getStandardDeliverablesByProjectAndMilestoneName(projectIdForMilestone, row.milestoneName)
            list = Array.isArray(byName) ? byName : []
          }
        } else {
          // 接口/个性化信息及其步骤暂不直接关联标准交付物，保留占位
          list = []
        }
        this.deliverableList = Array.isArray(list) ? list : []

        // 拉取并填充每个交付物的已上传文件，供查看弹窗展示
        const projectId = this.project?.projectId
        if (projectId && this.deliverableList.length > 0) {
          for (const d of this.deliverableList) {
            const did = d?.deliverableId || d?.id
            if (!did) continue
            try {
              // 拉取该交付物在项目下的文件；步骤/接口/个性化行携带 relationId 进行后端精确过滤
              const rid = (row && row.rowType !== 'milestone') ? (row?.relationId || null) : null
              const files = await listConstructDeliverableFiles(projectId, did, rid != null ? { projectStepId: rid } : {})
              const raw = Array.isArray(files) ? files : []
              if (row?.rowType === 'milestone') {
                // 仅保留属于当前项目里程碑的文件
                const mid = row?.milestoneId || null
                this.uploadedFilesByDeliverableId[did] = raw.filter(f => f && f.milestoneId === mid)
              } else {
                // 步骤/接口/个性化：按 relationId 过滤
                const rid = row?.relationId || null
                this.uploadedFilesByDeliverableId[did] = raw.filter(f => f && f.projectStepId === rid)
              }
            } catch (_) {
              this.uploadedFilesByDeliverableId[did] = []
            }
          }
        }
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
    /**
     * 函数级注释：下载交付物文件
     * 规则实现：
     * - 步骤交付物：若仅一个文件则直接下载；多个文件则后端打包ZIP，压缩包以步骤名称命名；接口/个性化步骤为“开发项名称-步骤名称”。
     * - 里程碑交付物：点击后弹窗询问是否同时下载该里程碑所属步骤的交付物；
     *   - 选择否：仅打包该里程碑交付物（单文件直接下，多文件ZIP，压缩包以里程碑名称命名；若存在接口/个性化上下文则在ZIP中按“开发项名称/”分目录）。
     *   - 选择是：将里程碑交付物放到“里程碑名称/”目录，其所属步骤的交付物按“[开发项名称/]步骤名称/”分目录，统一ZIP下载（压缩包以里程碑名称命名）。
     * 后端接口：
     * - GET `/api/construct-deliverable-files/download/step/{projectId}/{relationId}`
     * - GET `/api/construct-deliverable-files/download/milestone/{projectId}/{milestoneId}?includeSteps={true|false}`
     */
    async onDownloadDeliverables(row) {
      try {
        const API_BASE = __BACKEND_API_URL__
        const projectId = this.project?.projectId
        if (!projectId) return this.showError('项目ID缺失，无法下载')

        if (row.rowType === 'step' || row.rowType === 'interface_step' || row.rowType === 'personal_step') {
          const relationId = row?.relationId
          if (!relationId) return this.showError('步骤关系ID缺失，无法下载')
          const allFiles = Array.isArray(this.files) ? this.files : []
          const hasFiles = allFiles.some(f => f && f.projectStepId === relationId && (f.exists === true || f.fileId != null))
          if (!hasFiles) {
            if (this.$message && this.$message.warning) this.$message.warning('当前步骤未上传交付物')
            else alert('当前步骤未上传交付物')
            return
          }
          const url = `${API_BASE}/api/construct-deliverable-files/download/step/${projectId}/${relationId}`
          this.downloadBinary(url)
          return
        }

        if (row.rowType === 'milestone') {
          // 解析项目里程碑ID（construct_milestone.milestoneId）。占位行需按名称映射。
          let mid = typeof row.milestoneId === 'number' ? row.milestoneId : null
          if (!mid && row.milestoneName && Array.isArray(this.milestones)) {
            const m = this.milestones.find(x => x.milestoneName === row.milestoneName)
            mid = m ? m.milestoneId : null
          }
          if (!mid) return this.showError('未找到该里程碑，无法下载')
          const allFiles = Array.isArray(this.files) ? this.files : []
          const hasAny = allFiles.some(f => f && f.milestoneId === mid && (f.exists === true || f.fileId != null))
          if (!hasAny) {
            if (this.$message && this.$message.warning) this.$message.warning('当前里程碑未上传交付物')
            else alert('当前里程碑未上传交付物')
            return
          }
          let ok = false
          try {
            await ElMessageBox.confirm(
              '是否同时下载该里程碑所属步骤的交付物？',
              '提示',
              { confirmButtonText: '是', cancelButtonText: '否' }
            )
            ok = true
          } catch (_) {
            ok = false
          }
          const url = `${API_BASE}/api/construct-deliverable-files/download/milestone/${projectId}/${mid}?includeSteps=${ok ? 'true' : 'false'}`
          this.downloadBinary(url)
          return
        }

        this.showError('当前行不支持下载交付物')
      } catch (e) {
        this.showError('下载发起失败：' + (e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：根据项目步骤关系ID批量删除交付物文件
     * - 通过 `this.files`（项目汇总中的交付物文件）定位属于指定 relationIds 的文件记录；
     * - 并发调用后端删除接口，删除物理文件及 construct_deliverablefiles 表记录；
     * - 返回删除的文件数量，便于上层提示。
     * @param {number[]} relationIds 项目-步骤关系ID数组
     * @returns {Promise<number>} 实际删除的文件数量
     */
    async deleteFilesByRelationIds(relationIds) {
      try {
        const relSet = new Set((relationIds || []).filter(id => id != null))
        if (relSet.size === 0) return 0
        const all = Array.isArray(this.files) ? this.files : []
        const fileIds = all
          .filter(f => f && relSet.has(f.projectStepId))
          .map(f => f.fileId)
          .filter(id => id != null)
        if (fileIds.length === 0) return 0
        const tasks = fileIds.map(id => (async () => {
          try { await deleteConstructDeliverableFile(id) } catch (_) { /* 单个失败不阻断 */ }
        })())
        await Promise.allSettled(tasks)
        return fileIds.length
      } catch (_) {
        return 0
      }
    },
    /**
     * 函数级注释：触发浏览器下载（后端流式返回）
     * @param {string} url 下载链接
     */
    downloadBinary(url) {
      try {
        const a = document.createElement('a')
        a.href = url
        a.style.display = 'none'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
      } catch (_) {
        window.open(url, '_blank')
      }
    },
    /**
     * 函数级注释：切换交付物查看弹窗模式
     * 用于在“查看文件”与“下载模板”之间切换，保持界面结构稳定。
     * @param {'view'|'download'} mode 模式
     */
    setDeliverableDialogMode(mode) {
      this.deliverableDialogMode = mode === 'download' ? 'download' : 'view'
    },
    /**
     * 函数级注释：生成查看弹窗的上下文名称
     * 步骤优先使用 `sstepName` 或 `nstepName`，里程碑使用 `milestoneName`。
     * @param {Object|null} row 当前弹窗绑定的行对象
     * @returns {string} 用于展示的名称
     */
    viewDialogContextName(row) {
      if (!row) return '-'
      if (row.rowType === 'step' || row.rowType === 'interface_step' || row.rowType === 'personal_step') {
        return row.sstepName || row.nstepName || '-'
      }
      if (row.rowType === 'milestone') {
        return row.milestoneName || '-'
      }
      return row.sstepName || row.nstepName || row.milestoneName || '-'
    },
    /**
     * 函数级注释：将行类型映射为中文标签
     * @param {string} rowType 行类型：step | milestone | interface_step | personal_step
     * @returns {string} 中文标签
     */
    labelForRowType(rowType) {
      switch (rowType) {
        case 'step': return '步骤'
        case 'milestone': return '里程碑'
        case 'interface_step': return '接口步骤'
        case 'personal_step': return '个性化步骤'
        default: return '未知类型'
      }
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
          // 已上传文件列表（按当前行上下文过滤）
          try {
            if (projectId) {
              const files = await listConstructDeliverableFiles(projectId, did, { projectStepId: row?.relationId || null })
              const raw = Array.isArray(files) ? files : []
              if (row?.rowType === 'milestone') {
                // 里程碑行：仅保留属于当前项目里程碑的文件
                const mid = row?.milestoneId || null
                this.uploadedFilesByDeliverableId[did] = raw.filter(f => f && f.milestoneId === mid)
              } else {
                // 步骤/接口/个性化：按 relationId 过滤
                const rid = row?.relationId || null
                this.uploadedFilesByDeliverableId[did] = raw.filter(f => f && f.projectStepId === rid)
              }
            } else {
              this.uploadedFilesByDeliverableId[did] = []
            }
          } catch (_) { /* 忽略单个失败 */ }
          // 无论是否有模板，都允许上传
          // 注入当前行的项目步骤关系ID或里程碑ID以供上传/刷新时使用
          const isMilestoneRow = row?.rowType === 'milestone'
          result.push({
            deliverableId: did,
            deliverableName: d.deliverableName,
            sstepId: d.sstepId,
            // 里程碑上下文应保存项目里程碑ID（row.milestoneId），而非标准里程碑ID
            milestoneId: isMilestoneRow ? (row?.milestoneId || null) : (d?.milestoneId || null),
            projectStepId: isMilestoneRow ? null : (row?.relationId || null)
          })
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
        // 先解析当前上传弹窗中的该交付物上下文（步骤或里程碑）
        const ctx = (this.uploadDeliverables || []).find(x => x && x.deliverableId === deliverableId)
        // 步骤上传界面：按当前步骤 relationId 过滤（后端直查，避免跨步骤混入）
        const files = await listConstructDeliverableFiles(projectId, deliverableId, {
          projectStepId: (ctx && ctx.projectStepId != null) ? ctx.projectStepId : null
        })
        const raw = Array.isArray(files) ? files : []
        // 根据当前上传弹窗中该交付物的上下文进行二次保障过滤
        if (ctx && ctx.projectStepId != null) {
          this.uploadedFilesByDeliverableId[deliverableId] = raw.filter(f => f && f.projectStepId === ctx.projectStepId)
        } else if (ctx && ctx.milestoneId != null) {
          this.uploadedFilesByDeliverableId[deliverableId] = raw.filter(f => f && f.milestoneId === ctx.milestoneId)
        } else {
          this.uploadedFilesByDeliverableId[deliverableId] = raw
        }
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
        // 当前上下文（步骤或里程碑），用于仅替换当前上下文下的文件，避免同名步骤互相影响
        const ctx = (this.uploadDeliverables || []).find(x => x && x.deliverableId === deliverableId)
        const prev = Array.isArray(this.files) ? this.files : []
        let filtered
        if (ctx && ctx.projectStepId != null) {
          // 仅移除当前步骤下该交付物的旧记录，保留其他步骤的记录
          filtered = prev.filter(f => !(f && f.deliverableId === deliverableId && f.projectStepId === ctx.projectStepId))
        } else if (ctx && ctx.milestoneId != null) {
          // 仅移除当前里程碑下该交付物的旧记录
          filtered = prev.filter(f => !(f && f.deliverableId === deliverableId && f.milestoneId === ctx.milestoneId))
        } else {
          // 无上下文信息时，保持原样（仅追加，避免误删其他上下文的数据）
          filtered = prev
        }
        // 重新拼接：追加当前上下文下的新记录
        this.files = filtered.concat(list)
        // 映射按交付物ID更新供局部展示使用（上传弹窗）；全局颜色判断依赖 this.files
        this.$set ? this.$set(this.filesByDeliverableId, deliverableId, list) : (this.filesByDeliverableId[deliverableId] = list)
      } catch (_) { /* 忽略局部同步错误 */ }
    },
    /**
     * 函数级注释：删除已上传文件并刷新列表
     * @param {number} fileId 文件记录ID
     * @param {number} deliverableId 交付物ID（用于刷新列表）
     */
    async deleteUploadedFile(fileId, deliverableId) {
      if (this.isProjectCompleted) {
        this.showError('已完成项目不能删除交付物文件');
        return;
      }
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该文件？') : window.confirm('确认删除该文件？')
        if (!ok) return
        await deleteConstructDeliverableFile(fileId)
        this.$message && this.$message.success('删除成功')
        // 刷新当前交付物的列表并同步到摘要，确保“查看”按钮颜色即时更新
        await this.refreshUploadedFiles(deliverableId)
        this.applyUploadedFilesToSummary(deliverableId)
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
        // 预拉取所有交付物的“模板存在性”，用于在主表中标记上传按钮
        try {
          await this.prefetchTemplatesForDeliverables(this.deliverables)
        } catch (_) { /* 忽略模板预取失败，不影响主流程 */ }
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
        // 加载合同外需求列表
        try {
          await this.loadExtraRequirements();
        } catch (_) { }
        try {
          await this.loadProjectRisks();
        } catch (_) { }
        try {
          await this.loadProjectComments();
        } catch (_) { }
      } catch (err) {
        const backendMsg = err?.response?.data?.message || err?.response?.data?.error;
        this.error = backendMsg ? `加载失败：${backendMsg}` : (err?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },
    /**
     * 函数级注释：预拉取交付物模板存在性
     * 为当前项目涉及的所有标准交付物并发查询模板列表，填充缓存映射。
     * @param {Array<Object>} list 交付物列表
     */
    async prefetchTemplatesForDeliverables(list) {
      const ids = Array.from(new Set((Array.isArray(list) ? list : []).map(d => d && (d.deliverableId || d.id)).filter(id => id != null)))
      const tasks = ids.map(id => (async () => {
        try {
          const files = await listDeliverableTemplates(id)
          this.hasTemplatesByDeliverableId[id] = Array.isArray(files) ? files.length > 0 : false
        } catch (_) {
          this.hasTemplatesByDeliverableId[id] = false
        }
      })())
      await Promise.allSettled(tasks)
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
      if (this.isProjectCompleted) {
        this.showError('已完成项目不能修改步骤字段');
        return;
      }
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
      if (this.isProjectCompleted) {
        this.showError('已完成项目不能修改步骤字段');
        return this.cancelEdit();
      }
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
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/construct-deliverable-files/download/${fileId}`;
    },
    /**
     * 函数级注释：获取后端 Office→PDF 预览端点URL
     * - 统一将 doc/docx/ppt/pptx 转换为 PDF，供 PDFJS 渲染。
     */
    convertPreviewPdfURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/construct-deliverable-files/preview/pdf/${fileId}`;
    },
    /**
     * 函数级注释：获取后端 MP4 视频预览端点 URL
     * - 返回 inline 的 mp4 资源，支持 Range 分段；用于 <video> src。
     */
    convertPreviewVideoURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/construct-deliverable-files/preview/video/${fileId}`;
    },
    fileBaseName(path) {
      if (!path) return '未知文件';
      const parts = path.split(/[\\/]/);
      return parts[parts.length - 1];
    },
    prettySize(bytes) {
      if (!bytes && bytes !== 0) return '';
      const units = ['B', 'KB', 'MB', 'GB', 'TB'];
      let size = bytes, idx = 0;
      while (size >= 1024 && idx < units.length - 1) { size /= 1024; idx++; }
      return `${size.toFixed(1)} ${units[idx]}`;
    },
    formatDate(ts) {
      if (!ts) return '';
      try {
        const d = new Date(ts);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
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
      } catch (_) { }
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
      const imageExts = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp']
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
    async fetchCommentBlob(fileId) {
      const url = this.commentDownloadUrl(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('文件获取失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：获取回复附件 Blob
     */
    async fetchReplyBlob(fileId) {
      const url = this.replyDownloadUrl(fileId)
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
      if (this.isProjectCompleted) {
        this.showError('已完成项目不能新增接口需求')
        return
      }
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
      if (this.isProjectCompleted) {
        this.showError('已完成项目不能新增个性化功能需求')
        return
      }
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
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能新增接口需求')
      }
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
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能新增个性化功能需求')
      }
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
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能删除接口需求')
      }
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该接口及其生成的步骤关系？') : window.confirm('确认删除该接口及其生成的步骤关系？')
        if (!ok) return
        // 级联删除：先删除该接口生成的步骤下已上传的交付物文件
        const relIds = (this.steps || [])
          .filter(s => s && s.type === '接口开发' && s.interfaceId === interfaceId)
          .map(s => s.relationId)
          .filter(id => id != null)
        const removedCount = await this.deleteFilesByRelationIds(relIds)
        await deleteInterface(interfaceId)
        // 本地移除并刷新摘要
        this.interfaceBlocks = (this.interfaceBlocks || []).filter(b => b.id !== interfaceId)
        if (this.$message && removedCount > 0) {
          this.$message.success(`接口已删除，同时清理交付物文件 ${removedCount} 个`)
        } else {
          this.$message && this.$message.success('接口已删除')
        }
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
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能删除个性化功能需求')
      }
      try {
        const ok = this.$confirm ? await this.$confirm('确认删除该个性化需求及其生成的步骤关系？') : window.confirm('确认删除该个性化需求及其生成的步骤关系？')
        if (!ok) return
        // 级联删除：先删除该个性化需求生成的步骤下已上传的交付物文件
        const relIds = (this.steps || [])
          .filter(s => s && s.type === '个性化功能开发' && s.personalDevId === personalDevId)
          .map(s => s.relationId)
          .filter(id => id != null)
        const removedCount = await this.deleteFilesByRelationIds(relIds)
        await deletePersonalDevelope(personalDevId)
        this.personalBlocks = (this.personalBlocks || []).filter(b => b.id !== personalDevId)
        if (this.$message && removedCount > 0) {
          this.$message.success(`个性化需求已删除，同时清理交付物文件 ${removedCount} 个`)
        } else {
          this.$message && this.$message.success('个性化需求已删除')
        }
        await this.loadSummary()
      } catch (e) {
        this.showError('删除个性化需求失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：加载合同外需求列表（按项目）
     */
    async loadExtraRequirements() {
      if (!this.project || !this.project.projectId) return
      try {
        const resp = await listExtraRequirementsByProject(this.project.projectId)
        const list = Array.isArray(resp?.data) ? resp.data : (resp?.data?.extraRequirements || resp || [])
        this.extraRequirements = Array.isArray(list) ? list.map(r => ({ ...r })) : []
        await this.refreshExtraHasFiles()
      } catch (e) {
        this.extraRequirements = []
      }
    },
    /**
     * 函数级注释：加载项目风险列表
     */
    async loadProjectRisks() {
      if (!this.project || !this.project.projectId) return
      try {
        const resp = await listConstructingProjectRisksByProject(this.project.projectId)
        const list = Array.isArray(resp?.data) ? resp.data : (resp?.data?.risks || resp || [])
        this.projectRisks = Array.isArray(list) ? list.map(r => ({ ...r })) : []
        await this.refreshRiskHasFiles()
      } catch (e) {
        this.projectRisks = []
      }
    },
    /**
     * 函数级注释：加载风险附件列表
     */
    async loadRiskFiles(riskId) {
      if (!riskId) {
        this.riskAttachments = []
        return
      }
      try {
        const resp = await listConstructingProjectRiskFiles(riskId)
        const files = resp?.data?.files || resp?.data || []
        this.riskAttachments = Array.isArray(files) ? files : []
        this.updateRiskHasFiles(riskId, this.riskAttachments.length > 0)
      } catch (_) {
        this.riskAttachments = []
        this.updateRiskHasFiles(riskId, false)
      }
    },
    /**
     * 函数级注释：同步风险附件状态
     */
    updateRiskHasFiles(riskId, hasFiles) {
      const rid = Number(riskId)
      if (!rid || !Array.isArray(this.projectRisks) || !this.projectRisks.length) return
      const idx = this.projectRisks.findIndex(r => Number(r?.riskId) === rid)
      if (idx >= 0) {
        this.projectRisks[idx].hasFiles = !!hasFiles
      }
    },
    /**
     * 函数级注释：刷新风险附件标记
     */
    async refreshRiskHasFiles() {
      if (!Array.isArray(this.projectRisks) || !this.projectRisks.length) return
      const tasks = this.projectRisks.map(async (r) => {
        const rid = Number(r?.riskId)
        if (!rid || r?.hasFiles != null) return
        try {
          const resp = await listConstructingProjectRiskFiles(rid)
          const files = resp?.data?.files || resp?.data || []
          this.updateRiskHasFiles(rid, Array.isArray(files) && files.length > 0)
        } catch (_) {
          this.updateRiskHasFiles(rid, false)
        }
      })
      await Promise.all(tasks)
    },
    /**
     * 函数级注释：打开风险弹窗并初始化表单
     */
    openRiskDialog(mode = 'create', row = null) {
      this.riskDialogMode = mode
      this.showRiskDialog = true
      this.riskAttachments = []
      this.riskPendingFiles = []
      this.riskUploading = false
      this.riskUploadProgress = 0
      if (mode === 'create') {
        this.riskForm = {
          riskType: null,
          riskLevel: null,
          isRelieve: null,
          relieveWay: '',
          riskDescription: '',
          riskEvaluate: '',
          creator: this.currentUserId || null
        }
      } else if (row) {
        this.riskForm = {
          riskId: row.riskId,
          riskType: row.riskType,
          riskLevel: row.riskLevel,
          isRelieve: row.isRelieve,
          relieveWay: row.relieveWay || '',
          riskDescription: row.riskDescription || '',
          riskEvaluate: row.riskEvaluate || '',
          creator: row.creator || null
        }
        this.loadRiskFiles(row.riskId)
      }
    },
    /**
     * 函数级注释：查看项目风险
     */
    viewRisk(row) {
      this.openRiskDialog('view', row)
    },
    /**
     * 函数级注释：编辑项目风险
     */
    editRisk(row) {
      this.openRiskDialog('edit', row)
    },
    /**
     * 函数级注释：删除项目风险
     */
    async deleteRisk(row) {
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能删除项目风险')
      }
      const ok = this.$confirm ? await this.$confirm('确认删除该项目风险及其附件？') : window.confirm('确认删除该项目风险及其附件？')
      if (!ok) return
      try {
        await deleteConstructingProjectRisk(row.riskId)
        this.$message && this.$message.success('项目风险已删除')
        await this.loadProjectRisks()
      } catch (e) {
        this.showError('删除项目风险失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：关闭风险弹窗
     */
    closeRiskDialog() {
      this.showRiskDialog = false
      this.riskAttachments = []
      this.riskPendingFiles = []
      this.riskUploading = false
      this.riskUploadProgress = 0
    },
    /**
     * 函数级注释：提交风险表单
     */
    async confirmRisk() {
      if (this.riskDialogMode === 'view') {
        this.closeRiskDialog()
        return
      }

      if (!this.riskForm.riskType) {
        this.showError('请选择风险类型')
        return
      }
      if (!this.riskForm.riskLevel) {
        this.showError('请选择风险级别')
        return
      }
      if (this.riskForm.isRelieve === null) {
        this.showError('请选择是否解除')
        return
      }
      if (!this.riskForm.riskDescription || !this.riskForm.riskDescription.trim()) {
        this.showError('请输入风险描述')
        return
      }
      if (this.riskForm.isRelieve && (!this.riskForm.relieveWay || !this.riskForm.relieveWay.trim())) {
        this.showError('请输入解除方式')
        return
      }
      const creatorId = this.riskForm.creator ?? this.currentUserId
      if (!creatorId) {
        this.showError('未获取到用户信息，无法保存风险')
        return
      }
      const isRelieve = !!this.riskForm.isRelieve
      const payload = {
        projectId: this.project?.projectId,
        riskType: this.riskForm.riskType,
        riskLevel: this.riskForm.riskLevel,
        isRelieve,
        relieveWay: isRelieve ? ((this.riskForm.relieveWay || '').trim() || null) : null,
        riskDescription: (this.riskForm.riskDescription || '').trim(),
        riskEvaluate: (this.riskForm.riskEvaluate || '').trim() || null,
        creator: creatorId
      }
      try {
        let resp
        if (this.riskDialogMode === 'edit') {
          resp = await updateConstructingProjectRisk(this.riskForm.riskId, payload)
        } else {
          resp = await createConstructingProjectRisk(payload)
        }

        this.$message && this.$message.success(this.riskDialogMode === 'edit' ? '项目风险已更新' : '项目风险已添加')

        if (this.riskDialogMode === 'create' && this.riskPendingFiles.length) {
          const createdId = resp?.data?.risk?.riskId || resp?.data?.riskId
          if (createdId) {
            await this.uploadRiskFiles(createdId, this.riskPendingFiles)
            this.riskPendingFiles = []
          }
        }

        this.showRiskDialog = false
        await this.loadProjectRisks()
      } catch (e) {
        const msg = e?.response?.data?.error || e?.message || (this.riskDialogMode === 'edit' ? '更新失败' : '添加失败')
        this.showError(msg)
      }
    },
    /**
     * 函数级注释：选择风险附件文件
     */
    onRiskFilesSelected(evt) {
      const files = Array.from(evt?.target?.files || [])
      if (evt?.target) evt.target.value = ''
      if (!files.length) return

      if (this.riskDialogMode === 'create') {
        this.riskPendingFiles = this.riskPendingFiles.concat(files)
        return
      }

      const riskId = this.riskForm.riskId
      if (!riskId) {
        this.showError('请先保存风险后再上传附件')
        return
      }
      this.uploadRiskFiles(riskId, files)
    },
    /**
     * 函数级注释：触发风险附件文件选择
     */
    triggerRiskAttachmentInput() {
      try {
        this.$refs.riskAttachmentInput && this.$refs.riskAttachmentInput.click()
      } catch (_) { }
    },
    /**
     * 函数级注释：上传风险附件
     */
    async uploadRiskFiles(riskId, files) {
      if (!this.project?.projectId) return
      this.riskUploading = true
      this.riskUploadProgress = 0
      try {
        await uploadConstructingProjectRiskFiles(this.project.projectId, riskId, files, {
          uploaderId: this.currentUserId,
          onProgress: (percent) => {
            this.riskUploadProgress = percent
          }
        })
        await this.loadRiskFiles(riskId)
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '附件上传失败')
      } finally {
        this.riskUploading = false
      }
    },
    /**
     * 函数级注释：删除风险附件
     */
    async onDeleteRiskFile(file) {
      try {
        await deleteConstructingProjectRiskFile(file.fileId)
        this.$message && this.$message.success('附件已删除')
        await this.loadRiskFiles(this.riskForm.riskId)
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '删除附件失败')
      }
    },
    /**
     * 函数级注释：移除待上传的风险附件
     */
    removeRiskPendingFile(idx) {
      this.riskPendingFiles.splice(idx, 1)
    },
    /**
     * 函数级注释：生成风险附件下载链接
     */
    convertRiskDownloadURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/constructing-project-risk-files/download/${fileId}`
    },
    /**
     * 函数级注释：生成风险附件PDF预览链接
     */
    convertRiskPreviewPdfURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/constructing-project-risk-files/preview/pdf/${fileId}`
    },
    /**
     * 函数级注释：生成风险附件视频预览链接
     */
    convertRiskPreviewVideoURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/constructing-project-risk-files/preview/video/${fileId}`
    },
    /**
     * 函数级注释：获取风险附件二进制数据
     */
    async fetchRiskBlob(fileId) {
      const url = this.convertRiskDownloadURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('文件获取失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：获取风险附件预览PDF数据
     */
    async fetchRiskPreviewPdfBlob(fileId) {
      const url = this.convertRiskPreviewPdfURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('PDF 预览失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：预览风险附件
     */
    async onPreviewRiskFile(file) {
      const name = this.fileBaseName(file?.filePath || '')
      const ext = (name.split('.').pop() || '').toLowerCase()
      this.previewTitle = name || '文件预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true

      const imageExts = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp']
      if (imageExts.includes(ext)) {
        this.previewType = 'image'
        try {
          const blob = await this.fetchRiskBlob(file.fileId)
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
          const blob = await this.fetchRiskBlob(file.fileId)
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

      if (ext === 'mp4') {
        this.previewType = 'video'
        try {
          this.previewUrl = this.convertRiskPreviewVideoURL(file.fileId)
        } catch (e) {
          this.previewError = e?.message || '视频预览失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'doc' || ext === 'docx') {
        try {
          const pdfBlob = await this.fetchRiskPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          if (ext === 'docx') {
            try {
              const blob = await this.fetchRiskBlob(file.fileId)
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
          const pdfBlob = await this.fetchRiskPreviewPdfBlob(file.fileId)
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
          const blob = await this.fetchRiskBlob(file.fileId)
          const text = await blob.text()
          this.previewText = text
        } catch (e) {
          this.previewError = e?.message || '文本加载失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'ppt' || ext === 'pptx') {
        try {
          const pdfBlob = await this.fetchRiskPreviewPdfBlob(file.fileId)
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

      this.previewType = 'unsupported'
      this.previewLoading = false
    },
    async loadProjectComments() {
      if (!this.project || !this.project.projectId) return
      this.commentLoading = true
      this.commentError = ''
      try {
        const resp = await listConstructingProjectComments(this.project.projectId)
        const list = Array.isArray(resp?.data) ? resp.data : (resp?.data?.data || [])
        this.commentList = Array.isArray(list) ? list : []
        await this.loadProjectCommentFiles()
        await this.loadProjectCommentReplies()
      } catch (e) {
        this.commentList = []
        this.commentFilesByCommentId = {}
        this.replyListByCommentId = {}
        this.replyFilesByReplyId = {}
        this.commentError = e?.response?.data?.error || e?.message || '加载评论失败'
      } finally {
        this.commentLoading = false
      }
    },
    async loadProjectCommentFiles() {
      if (!this.project || !this.project.projectId) return
      try {
        const files = await listConstructingProjectCommentFilesByProject(this.project.projectId)
        const map = {}
        for (const file of files || []) {
          const key = file?.commentId
          if (!key) continue
          if (!map[key]) map[key] = []
          map[key].push(file)
        }
        this.commentFilesByCommentId = map
      } catch (e) {
        this.commentFilesByCommentId = {}
      }
    },
    /**
     * 函数级注释：加载评论回复与回复附件
     */
    async loadProjectCommentReplies() {
      const list = Array.isArray(this.commentList) ? this.commentList : []
      if (list.length === 0) {
        this.replyListByCommentId = {}
        this.replyFilesByReplyId = {}
        return
      }
      const replyMap = {}
      const replyFilesMap = {}
      await Promise.all(list.map(async (item) => {
        const commentId = item?.commentId
        if (!commentId) return
        try {
          const resp = await listConstructingProjectCommentReplies(commentId)
          const replies = Array.isArray(resp?.data) ? resp.data : (resp?.data?.data || [])
          replyMap[commentId] = Array.isArray(replies) ? replies : []
        } catch (_) {
          replyMap[commentId] = []
        }
        try {
          const files = await listConstructingProjectCommentReplyFilesByComment(commentId)
          for (const file of files || []) {
            const key = file?.replyId
            if (!key) continue
            if (!replyFilesMap[key]) replyFilesMap[key] = []
            replyFilesMap[key].push(file)
          }
        } catch (_) {}
      }))
      this.replyListByCommentId = replyMap
      this.replyFilesByReplyId = replyFilesMap
    },
    async submitProjectComment() {
      if (this.commentSubmitting) return
      if (!this.project || !this.project.projectId) return this.showError('项目ID缺失，无法发表评论')
      const content = (this.commentForm.content || '').trim()
      const hasPendingFiles = Array.isArray(this.commentPendingFiles) && this.commentPendingFiles.length > 0
      if (!content && !hasPendingFiles) return this.showError('请输入评论内容或上传附件')
      if (content && content.length > 2000) return this.showError('评论内容超长，请控制在1000汉字和字符内')
      const userInfoRaw = sessionStorage.getItem('userInfo')
      const userInfo = userInfoRaw ? JSON.parse(userInfoRaw) : null
      const userId = userInfo && userInfo.userId ? Number(userInfo.userId) : null
      if (!userId) return this.showError('未获取到用户信息，无法发表评论')
      this.commentSubmitting = true
      this.commentUploadProgress = 0
      try {
        const resp = await createConstructingProjectComment({
          projectId: this.project.projectId,
          userId,
          content
        })
        const commentId = resp?.data?.data?.commentId || resp?.data?.commentId
        if (commentId && this.commentPendingFiles.length > 0) {
          this.commentUploading = true
          await uploadConstructingProjectCommentFiles(this.project.projectId, commentId, this.commentPendingFiles, {
            uploaderId: userId,
            onProgress: (percent) => {
              this.commentUploadProgress = percent
            }
          })
        }
        this.commentForm.content = ''
        this.commentPendingFiles = []
        this.commentUploadProgress = 0
        await this.loadProjectComments()
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '发表评论失败')
      } finally {
        this.commentUploading = false
        this.commentSubmitting = false
      }
    },
    async deleteProjectComment(item) {
      const commentId = item?.commentId
      if (!commentId) return
      if (!this.currentUserId || Number(item?.userId) !== Number(this.currentUserId)) {
        return this.showError('只能删除自己发表的评论')
      }
      const ok = this.$confirm ? await this.$confirm('确认删除该评论及附件？') : window.confirm('确认删除该评论及附件？')
      if (!ok) return
      try {
        await deleteConstructingProjectComment(commentId, this.currentUserId)
        this.$message && this.$message.success('评论已删除')
        await this.loadProjectComments()
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '删除评论失败')
      }
    },
    /**
     * 函数级注释：切换评论回复编辑区
     */
    toggleReplyForm(commentId) {
      const key = String(commentId)
      const current = !!this.replyFormVisibleByCommentId[key]
      this.replyFormVisibleByCommentId = {
        ...this.replyFormVisibleByCommentId,
        [key]: !current
      }
    },
    /**
     * 函数级注释：选择回复附件
     */
    onReplyFileChange(commentId, e) {
      const key = String(commentId)
      const files = Array.from(e?.target?.files || [])
      if (files.length === 0) return
      const list = Array.isArray(this.replyPendingFilesByCommentId[key]) ? this.replyPendingFilesByCommentId[key] : []
      this.replyPendingFilesByCommentId = {
        ...this.replyPendingFilesByCommentId,
        [key]: [...list, ...files]
      }
      if (e?.target) e.target.value = ''
    },
    /**
     * 函数级注释：移除待上传回复附件
     */
    removeReplyPendingFile(commentId, index) {
      const key = String(commentId)
      const list = Array.isArray(this.replyPendingFilesByCommentId[key]) ? [...this.replyPendingFilesByCommentId[key]] : []
      list.splice(index, 1)
      this.replyPendingFilesByCommentId = {
        ...this.replyPendingFilesByCommentId,
        [key]: list
      }
    },
    /**
     * 函数级注释：提交回复
     */
    async submitReply(commentItem) {
      const commentId = commentItem?.commentId
      if (!commentId) return
      const key = String(commentId)
      const content = (this.replyFormContentByCommentId[key] || '').trim()
      const pending = Array.isArray(this.replyPendingFilesByCommentId[key]) ? this.replyPendingFilesByCommentId[key] : []
      if (!content && pending.length === 0) return this.showError('请输入回复内容或上传附件')
      if (content && content.length > 2000) return this.showError('回复内容超长，请控制在1000汉字和字符内')
      if (!this.currentUserId) return this.showError('未获取到用户信息，无法回复')
      this.replySubmittingByCommentId = { ...this.replySubmittingByCommentId, [key]: true }
      this.replyUploadProgressByCommentId = { ...this.replyUploadProgressByCommentId, [key]: 0 }
      try {
        const resp = await createConstructingProjectCommentReply({
          projectId: this.project.projectId,
          commentId,
          userId: this.currentUserId,
          content
        })
        const replyId = resp?.data?.data?.replyId || resp?.data?.replyId
        if (replyId && pending.length > 0) {
          this.replyUploadingByCommentId = { ...this.replyUploadingByCommentId, [key]: true }
          await uploadConstructingProjectCommentReplyFiles(this.project.projectId, commentId, replyId, pending, {
            uploaderId: this.currentUserId,
            onProgress: (percent) => {
              this.replyUploadProgressByCommentId = { ...this.replyUploadProgressByCommentId, [key]: percent }
            }
          })
        }
        this.replyFormContentByCommentId = { ...this.replyFormContentByCommentId, [key]: '' }
        this.replyPendingFilesByCommentId = { ...this.replyPendingFilesByCommentId, [key]: [] }
        this.replyUploadProgressByCommentId = { ...this.replyUploadProgressByCommentId, [key]: 0 }
        await this.loadProjectCommentReplies()
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '回复失败')
      } finally {
        this.replyUploadingByCommentId = { ...this.replyUploadingByCommentId, [key]: false }
        this.replySubmittingByCommentId = { ...this.replySubmittingByCommentId, [key]: false }
      }
    },
    /**
     * 函数级注释：删除回复（仅本人）
     */
    async deleteReply(reply, commentId) {
      const replyId = reply?.replyId
      if (!replyId) return
      if (!this.currentUserId || Number(reply?.userId) !== Number(this.currentUserId)) {
        return this.showError('只能删除自己发表的回复')
      }
      const ok = this.$confirm ? await this.$confirm('确认删除该回复及附件？') : window.confirm('确认删除该回复及附件？')
      if (!ok) return
      try {
        await deleteConstructingProjectCommentReply(replyId, this.currentUserId)
        this.$message && this.$message.success('回复已删除')
        await this.loadProjectCommentReplies()
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '删除回复失败')
      }
    },
    /**
     * 函数级注释：获取回复预览URL
     */
    replyPreviewUrl(fileId) {
      return getConstructingProjectCommentReplyFilePreviewUrl(fileId)
    },
    /**
     * 函数级注释：获取回复下载URL
     */
    replyDownloadUrl(fileId) {
      return getConstructingProjectCommentReplyFileDownloadUrl(fileId)
    },
    /**
     * 函数级注释：打开回复图片预览
     */
    async openReplyImagePreview(file) {
      const name = this.fileBaseName(file?.filePath || '')
      this.previewTitle = name || '图片预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true
      this.previewType = 'image'
      try {
        const blob = await this.fetchReplyBlob(file.fileId)
        const url = URL.createObjectURL(blob)
        this.previewUrl = url
      } catch (e) {
        this.previewError = e?.message || '图片加载失败'
      } finally {
        this.previewLoading = false
      }
    },
    onCommentFileChange(e) {
      const files = Array.from(e?.target?.files || [])
      if (files.length === 0) return
      this.commentPendingFiles = [...(this.commentPendingFiles || []), ...files]
      if (e?.target) e.target.value = ''
    },
    removeCommentPendingFile(index) {
      const list = Array.isArray(this.commentPendingFiles) ? [...this.commentPendingFiles] : []
      list.splice(index, 1)
      this.commentPendingFiles = list
    },
    commentPreviewUrl(fileId) {
      return getConstructingProjectCommentFilePreviewUrl(fileId)
    },
    commentDownloadUrl(fileId) {
      return getConstructingProjectCommentFileDownloadUrl(fileId)
    },
    async openCommentImagePreview(file) {
      const name = this.fileBaseName(file?.filePath || '')
      this.previewTitle = name || '图片预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true
      this.previewType = 'image'
      try {
        const blob = await this.fetchCommentBlob(file.fileId)
        const url = URL.createObjectURL(blob)
        this.previewUrl = url
      } catch (e) {
        this.previewError = e?.message || '图片加载失败'
      } finally {
        this.previewLoading = false
      }
    },
    isImageFile(path) {
      const name = this.fileBaseName(path || '')
      const ext = (name.split('.').pop() || '').toLowerCase()
      return ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'].includes(ext)
    },
    /**
     * 函数级注释：加载合同外需求附件列表
     * @param {number} requirementId 需求ID
     */
    async loadExtraRequirementFiles(requirementId) {
      if (!requirementId) {
        this.extraAttachments = []
        return
      }
      try {
        const resp = await listExtraRequirementFiles(requirementId)
        const files = resp?.data?.files || resp?.data || []
        this.extraAttachments = Array.isArray(files) ? files : []
        this.updateExtraHasFiles(requirementId, this.extraAttachments.length > 0)
      } catch (_) {
        this.extraAttachments = []
        this.updateExtraHasFiles(requirementId, false)
      }
    },
    updateExtraHasFiles(requirementId, hasFiles) {
      const rid = Number(requirementId)
      if (!rid || !Array.isArray(this.extraRequirements) || !this.extraRequirements.length) return
      const idx = this.extraRequirements.findIndex(r => Number(r?.requirementId) === rid)
      if (idx >= 0) {
        this.extraRequirements[idx].hasFiles = !!hasFiles
      }
    },
    async refreshExtraHasFiles() {
      if (!Array.isArray(this.extraRequirements) || !this.extraRequirements.length) return
      const tasks = this.extraRequirements.map(async (r) => {
        const rid = Number(r?.requirementId)
        if (!rid || r?.hasFiles != null) return
        try {
          const resp = await listExtraRequirementFiles(rid)
          const files = resp?.data?.files || resp?.data || []
          this.updateExtraHasFiles(rid, Array.isArray(files) && files.length > 0)
        } catch (_) {
          this.updateExtraHasFiles(rid, false)
        }
      })
      await Promise.all(tasks)
    },
    /**
     * 函数级注释：打开添加/编辑/查看合同外需求弹窗
     * @param {string} mode 模式：create|edit|view
     * @param {Object} row 行数据（仅edit/view时需要）
     */
    openExtraDialog(mode = 'create', row = null) {
      this.extraDialogMode = mode
      this.showExtraDialog = true
      this.extraAttachments = []
      this.extraPendingFiles = []
      this.extraUploading = false
      this.extraUploadProgress = 0
      if (mode === 'create') {
        this.extraForm = {
          requirementName: '',
          isPay: null,
          payAmount: null,
          isDeliver: null,
          isComplete: null,
          isProductization: null,
          workload: null,
          developer: null
        }
      } else if (row) {
        // 复制数据
        this.extraForm = {
          requirementId: row.requirementId,
          requirementName: row.requirementName,
          isPay: row.isPay,
          payAmount: row.payAmount,
          isDeliver: row.isDeliver,
          isComplete: row.isComplete,
          isProductization: row.isProductization,
          workload: row.workload,
          developer: row.developer
        }
        this.loadExtraRequirementFiles(row.requirementId)
      }
    },
    /**
     * 函数级注释：查看合同外需求
     * @param {Object} row 
     */
    viewExtra(row) {
      this.openExtraDialog('view', row)
    },
    /**
     * 函数级注释：编辑合同外需求
     * @param {Object} row 
     */
    editExtra(row) {
      this.openExtraDialog('edit', row)
    },
    /**
     * 函数级注释：删除合同外需求
     * @param {Object} row
     */
    async deleteExtra(row) {
      if (this.isProjectCompleted) {
        return this.showError('已完成项目不能删除合同外需求')
      }
      const ok = this.$confirm ? await this.$confirm('确认删除该合同外需求及其附件？') : window.confirm('确认删除该合同外需求及其附件？')
      if (!ok) return
      try {
        await deleteExtraRequirement(row.requirementId)
        this.$message && this.$message.success('合同外需求已删除')
        await this.loadExtraRequirements()
      } catch (e) {
        this.showError('删除合同外需求失败：' + (e?.response?.data?.error || e?.message || '未知错误'))
      }
    },
    /**
     * 函数级注释：关闭添加合同外需求弹窗
     */
    closeExtraDialog() {
      this.showExtraDialog = false
      this.extraAttachments = []
      this.extraPendingFiles = []
      this.extraUploading = false
      this.extraUploadProgress = 0
    },
    /**
     * 函数级注释：提交合同外需求
     */
    async confirmExtra() {
      if (this.extraDialogMode === 'view') {
        this.closeExtraDialog()
        return
      }

      const name = (this.extraForm.requirementName || '').trim()
      if (!name) {
        this.showError('请填写需求名称')
        return
      }
      if (this.extraForm.isPay === null) {
        this.showError('请选择是否付费')
        return
      }
      if (this.extraForm.isPay && (this.extraForm.payAmount == null || this.extraForm.payAmount === '')) {
        this.showError('请填写付费金额')
        return
      }
      if (this.extraForm.isDeliver === null) {
        this.showError('请选择是否交付')
        return
      }
      if (this.extraForm.isComplete === null) {
        this.showError('请选择是否完成')
        return
      }
      if (this.extraForm.isProductization === null) {
        this.showError('请选择是否产品化')
        return
      }
      const payload = {
        projectId: this.project?.projectId,
        requirementName: name,
        isPay: !!this.extraForm.isPay,
        payAmount: this.extraForm.isPay ? (this.extraForm.payAmount ?? null) : null,
        isDeliver: !!this.extraForm.isDeliver,
        isComplete: !!this.extraForm.isComplete,
        isProductization: !!this.extraForm.isProductization,
        workload: this.extraForm.workload ?? null,
        developer: this.extraForm.developer ?? null
      }
      try {
        let resp
        if (this.extraDialogMode === 'edit') {
          resp = await updateExtraRequirement(this.extraForm.requirementId, payload)
        } else {
          resp = await createExtraRequirement(payload)
        }

        if (resp?.data?.success) {
          this.$message && this.$message.success(this.extraDialogMode === 'edit' ? '合同外需求已更新' : '合同外需求已添加')
        } else {
          this.$message && this.$message.success(this.extraDialogMode === 'edit' ? '合同外需求已更新' : '合同外需求已添加')
        }

        if (this.extraDialogMode === 'create' && this.extraPendingFiles.length) {
          const createdId = resp?.data?.extraRequirement?.requirementId
          if (createdId) {
            await this.uploadExtraRequirementFiles(createdId, this.extraPendingFiles)
            this.extraPendingFiles = []
          }
        }

        this.showExtraDialog = false
        await this.loadExtraRequirements()
      } catch (e) {
        const msg = e?.response?.data?.error || e?.message || (this.extraDialogMode === 'edit' ? '更新失败' : '添加失败')
        this.showError(msg)
      }
    },
    /**
     * 函数级注释：选择合同外需求附件
     * @param {Event} evt 选择事件
     */
    onExtraFilesSelected(evt) {
      const files = Array.from(evt?.target?.files || [])
      if (evt?.target) evt.target.value = ''
      if (!files.length) return

      if (this.extraDialogMode === 'create') {
        this.extraPendingFiles = this.extraPendingFiles.concat(files)
        return
      }

      const requirementId = this.extraForm.requirementId
      if (!requirementId) {
        this.showError('请先保存需求后再上传附件')
        return
      }
      this.uploadExtraRequirementFiles(requirementId, files)
    },
    triggerExtraAttachmentInput() {
      try {
        this.$refs.extraAttachmentInput && this.$refs.extraAttachmentInput.click()
      } catch (_) { }
    },
    /**
     * 函数级注释：上传合同外需求附件并刷新列表
     * @param {number} requirementId 需求ID
     * @param {File[]} files 文件列表
     */
    async uploadExtraRequirementFiles(requirementId, files) {
      if (!this.project?.projectId) return
      this.extraUploading = true
      this.extraUploadProgress = 0
      try {
        await uploadExtraRequirementFiles(this.project.projectId, requirementId, files, {
          onProgress: (percent) => {
            this.extraUploadProgress = percent
          }
        })
        await this.loadExtraRequirementFiles(requirementId)
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '附件上传失败')
      } finally {
        this.extraUploading = false
      }
    },
    /**
     * 函数级注释：删除合同外需求附件
     * @param {Object} file 文件记录
     */
    async onDeleteExtraFile(file) {
      try {
        await deleteExtraRequirementFile(file.fileId)
        this.$message && this.$message.success('附件已删除')
        await this.loadExtraRequirementFiles(this.extraForm.requirementId)
      } catch (e) {
        this.showError(e?.response?.data?.error || e?.message || '删除附件失败')
      }
    },
    /**
     * 函数级注释：移除待上传附件
     * @param {number} idx 索引
     */
    removeExtraPendingFile(idx) {
      this.extraPendingFiles.splice(idx, 1)
    },
    /**
     * 函数级注释：合同外需求附件下载URL
     * @param {number} fileId 文件ID
     * @returns {string} URL
     */
    convertExtraDownloadURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/extra-requirement-files/download/${fileId}`
    },
    /**
     * 函数级注释：合同外需求附件 PDF 预览URL
     */
    convertExtraPreviewPdfURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/extra-requirement-files/preview/pdf/${fileId}`
    },
    /**
     * 函数级注释：合同外需求附件视频预览URL
     */
    convertExtraPreviewVideoURL(fileId) {
      const API_BASE = __BACKEND_API_URL__
      return `${API_BASE}/api/extra-requirement-files/preview/video/${fileId}`
    },
    /**
     * 函数级注释：获取合同外需求附件的二进制
     * @param {number} fileId 文件ID
     * @returns {Promise<Blob>}
     */
    async fetchExtraBlob(fileId) {
      const url = this.convertExtraDownloadURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('文件获取失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：获取合同外需求附件 PDF 预览二进制
     * @param {number} fileId 文件ID
     * @returns {Promise<Blob>}
     */
    async fetchExtraPreviewPdfBlob(fileId) {
      const url = this.convertExtraPreviewPdfURL(fileId)
      const resp = await fetch(url, { credentials: 'include' })
      if (!resp.ok) throw new Error('PDF 预览失败：' + resp.status)
      return await resp.blob()
    },
    /**
     * 函数级注释：预览合同外需求附件
     * @param {{fileId:number,filePath:string}} file 文件记录
     */
    async onPreviewExtraFile(file) {
      const name = this.fileBaseName(file?.filePath || '')
      const ext = (name.split('.').pop() || '').toLowerCase()
      this.previewTitle = name || '文件预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true

      const imageExts = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp']
      if (imageExts.includes(ext)) {
        this.previewType = 'image'
        try {
          const blob = await this.fetchExtraBlob(file.fileId)
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
          const blob = await this.fetchExtraBlob(file.fileId)
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

      if (ext === 'mp4') {
        this.previewType = 'video'
        try {
          this.previewUrl = this.convertExtraPreviewVideoURL(file.fileId)
        } catch (e) {
          this.previewError = e?.message || '视频预览失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'doc' || ext === 'docx') {
        try {
          const pdfBlob = await this.fetchExtraPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          if (ext === 'docx') {
            try {
              const blob = await this.fetchExtraBlob(file.fileId)
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
          const pdfBlob = await this.fetchExtraPreviewPdfBlob(file.fileId)
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
          const blob = await this.fetchExtraBlob(file.fileId)
          const text = await blob.text()
          this.previewText = text
        } catch (e) {
          this.previewError = e?.message || '文本加载失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      if (ext === 'ppt' || ext === 'pptx') {
        try {
          const pdfBlob = await this.fetchExtraPreviewPdfBlob(file.fileId)
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

      this.previewType = 'unsupported'
      this.previewLoading = false
    },
    /**
     * 函数级注释：根据用户ID显示姓名
     * @param {number} uid 用户ID
     * @returns {string} 用户姓名或用户名
     */
    userName(uid) {
      const u = (this.allUsers || []).find(x => x && x.userId === uid)
      return u ? (u.name || u.userName || '') : ''
    }
  }
}
</script>

<style scoped>
/* 类级注释：页面容器调整为内部滚动，仅内容区滚动，避免底部空白 */
.project-detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  padding: 8px;
  box-sizing: border-box;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid #eee;
}

.back-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.title {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.title .num {
  color: #666;
  font-size: 13px;
  font-weight: 400;
}

.stats {
  display: flex;
  gap: 6px;
}

.chip {
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 12px;
  font-size: 12px;
}

.state {
  padding: 24px;
  color: #333;
}

.state.error {
  color: #c00;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  overflow-x: auto;
  flex: 1;
  min-height: 0;
}

.card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
}

.card.wide {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.info-grid label {
  color: #888;
  font-size: 12px;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
}

.tag {
  font-size: 12px;
  color: #666;
}

.tag.done {
  color: #2f8f2f;
}

.deliverables {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.deliverable-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.deliverable-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-list {
  list-style: none;
  padding: 0;
  margin: 6px 0 0;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px dashed #eee;
}

.file-link {
  color: #1677ff;
  text-decoration: none;
}

.file-link:hover {
  text-decoration: underline;
}

.size,
.time {
  color: #888;
  font-size: 12px;
}

.empty {
  color: #888;
  padding: 8px 0;
}

/* 评论区样式优化 */
.comment-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 0 !important; /* 重置内边距，由内部容器控制 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.comment-split {
  display: flex;
  height: calc(100vh - 120px);
}

.comment-pane-left {
  width: 30%;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.comment-pane-right {
  width: 70%;
  display: flex;
  flex-direction: column;
}

.comment-compose-area {
  padding: 20px;
  border-bottom: none;
  height: 100%;
}

.compose-header {
  margin-bottom: 12px;
}

.compose-title {
  font-weight: 600;
  font-size: 16px;
  color: #111827;
}

.compose-body {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.compose-body:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.compose-body textarea {
  width: 100%;
  border: none;
  padding: 12px;
  font-size: 14px;
  resize: vertical;
  min-height: 120px;
  max-height: 240px;
  overflow: auto;
  display: block;
  outline: none;
}

.comment-upload {
  border-top: 1px solid #f3f4f6;
  padding: 10px 12px;
  background: #fff;
}

.comment-upload-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.comment-upload-title {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.comment-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  color: #374151;
  cursor: pointer;
  background: #fff;
}

.comment-upload-btn input {
  display: none;
}

.comment-upload-count {
  font-size: 12px;
  color: #6b7280;
}

.comment-upload-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comment-upload-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: #f9fafb;
  border-radius: 6px;
}

.comment-upload-name {
  flex: 1;
  font-size: 12px;
  color: #1f2937;
  word-break: break-all;
}

.comment-upload-size {
  font-size: 12px;
  color: #9ca3af;
}

.comment-upload-progress {
  height: 6px;
  border-radius: 999px;
  background: #eef2ff;
  overflow: hidden;
  margin-top: 8px;
}

.comment-upload-bar {
  height: 100%;
  background: #2563eb;
  transition: width 0.2s ease;
}

.compose-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: #fff;
  border-top: 1px solid #f3f4f6;
}

.comment-count {
  font-size: 13px;
  color: #6b7280;
}

.submit-btn {
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.comment-list-container {
  flex: 1;
  padding: 0 20px;
  overflow-y: auto;
  min-height: 300px;
}

.comment-items {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-card {
  display: flex;
  gap: 16px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.comment-main {
  flex: 1;
  background: #f3f4f6;
  padding: 12px 16px;
  border-radius: 0 12px 12px 12px;
  position: relative;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.comment-date {
  font-size: 12px;
  color: #9ca3af;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-delete-btn {
  border: none;
  background: transparent;
  color: #ef4444;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.comment-delete-btn:hover {
  text-decoration: underline;
}

.comment-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-reply-btn {
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.comment-reply-btn:hover {
  text-decoration: underline;
}

.comment-reply-editor {
  margin-top: 10px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.comment-reply-editor textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px;
  font-size: 13px;
  outline: none;
  resize: vertical;
  min-height: 60px;
  max-height: 180px;
}

.comment-reply-upload {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.comment-reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-reply-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-reply-item {
  padding: 10px 12px;
  background: #eef2ff;
  border-radius: 8px;
}

.comment-reply-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.comment-reply-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-reply-delete-btn {
  border: none;
  background: transparent;
  color: #ef4444;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.comment-reply-delete-btn:hover {
  text-decoration: underline;
}

.comment-reply-author {
  font-weight: 600;
  color: #1f2937;
  font-size: 13px;
}

.comment-reply-date {
  font-size: 12px;
  color: #9ca3af;
}

.comment-reply-content {
  font-size: 13px;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-file-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.comment-image-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  padding: 0;
  cursor: pointer;
}

.comment-image-thumb {
  width: 96px;
  height: 96px;
  object-fit: cover;
  display: block;
}

.comment-file-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-file-name {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}

.comment-file-name:hover {
  text-decoration: underline;
}

.comment-file-size {
  font-size: 12px;
  color: #9ca3af;
}

.empty-comment-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #9ca3af;
}

.empty-comment-state .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #6b7280;
  gap: 12px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.comment-error {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  border-radius: 6px;
  font-size: 13px;
}

/* 旧样式清理 (如有冲突请删除) */
.comment-compose, .comment-list, .comment-item, .comment-meta, .comment-user, .comment-content {
  /* 保留或移除，新样式已使用新类名 */
}

/* 表格样式 */
/* 兼容粘性表头：使用分离边框以提高浏览器兼容性 */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}
.table.table-fixed {
  table-layout: fixed;
}
.table.no-wrap-table th,
.table.no-wrap-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table th,
.table td {
  padding: 8px;
  border-bottom: 1px dashed #eee;
  font-size: 14px;
  text-align: left;
}

.table td {
  position: relative;
}

.table thead th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

/* 固定表头与滚动容器（仅本组件生效） */
.table-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

/* 同时对 thead 设为粘性，避免某些浏览器对 th 粘性支持不一致 */
.table-scroll thead {
  position: sticky;
  top: 0;
  z-index: 4;
  background: #fafafa;
}

.table-scroll thead th {
  position: sticky;
  top: 0;
  z-index: 5;
  background: #fafafa;
}

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

.milestone-row {
  color: #c00;
  background: #fff5f5;
}

/* 接口相关样式与弹窗 */
.interface-info-row {
  background: #f7fbff;
  color: #0a65c2;
}

.interface-step-row {
  background: #fafdff;
}

.add-interface-row {
  background: #f7fbff;
}

.add-interface-btn {
  padding: 6px 12px;
  border: 1px solid #1677ff;
  background: #1677ff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.add-interface-btn:hover {
  background: #0f5fd6;
}

.add-interface-btn.disabled {
  opacity: .6;
  cursor: not-allowed;
  filter: grayscale(40%);
}

.personal-info-row {
  background: #f7fbff;
  color: #0a65c2;
}

.personal-step-row {
  background: #fafdff;
}

.add-personal-row {
  background: #f7fbff;
}

.add-personal-btn {
  padding: 6px 12px;
  border: 1px solid #1677ff;
  background: #1677ff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.add-personal-btn:hover {
  background: #0f5fd6;
}

.add-personal-btn.disabled {
  opacity: .6;
  cursor: not-allowed;
  filter: grayscale(40%);
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  width: 420px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  padding: 16px;
}

.dialog.upload-dialog {
  width: 560px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.dialog.view-dialog {
  width: 560px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.dialog h4 {
  margin: 0 0 12px;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.extra-modal {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.extra-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.extra-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.extra-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all .2s;
}

.extra-close:hover {
  background: #f0f0f0;
  color: #666;
}

.extra-modal-body {
  flex: 1;
  overflow-y: auto;
}

.extra-modal-footer {
  flex-shrink: 0;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.extra-form {
  padding: 24px;
}

.extra-section {
  margin-bottom: 24px;
}

.extra-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #1890ff;
  display: inline-block;
}

.extra-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.extra-group {
  display: flex;
  flex-direction: column;
}

.extra-group.full-width {
  grid-column: 1 / -1;
}

.extra-group label {
  font-weight: 500;
  margin-bottom: 6px;
  color: #262626;
  font-size: 14px;
}

.extra-group input,
.extra-group select,
.extra-group textarea {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color .2s;
}

.extra-group textarea {
  resize: vertical;
}

.extra-group input:focus,
.extra-group select:focus,
.extra-group textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.extra-group input:disabled,
.extra-group select:disabled,
.extra-group textarea:disabled {
  color: #262626;
  background-color: #f5f5f5;
  -webkit-text-fill-color: #262626;
  opacity: 1;
  cursor: default;
}

.extra-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.extra-upload-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  padding: 12px;
}

.extra-upload-head {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 8px;
}

.extra-upload-body .uploaded-list {
  margin-top: 8px;
}

.hidden-file {
  display: none;
}

.select-btn {
  padding: 6px 12px;
}

.context-chips {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.chip.primary {
  background: #eef2ff;
  border-color: #c7d2fe;
  color: #1d4ed8;
}

.segmented {
  display: inline-flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.segmented .seg-btn {
  padding: 6px 12px;
  background: #fff;
  border: none;
  border-right: 1px solid #e5e7eb;
  cursor: pointer;
  font-size: 12px;
  color: #374151;
}

.segmented .seg-btn:last-child {
  border-right: none;
}

.segmented .seg-btn.active {
  background: #1677ff;
  color: #fff;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.form-row input,
.form-row select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.required {
  color: #c00;
}

.dialog-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 12px;
}

.btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.btn.ghost {
  background: #f5f7fa;
}

.btn.primary {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
}

.btn.primary:hover {
  background: #0f5fd6;
  border-color: #0f5fd6;
}

.btn.disabled {
  opacity: .6;
  cursor: not-allowed;
}

/* 删除按钮颜色与名称文字颜色保持一致（接口与个性化信息行） */
.interface-info-row .btn.ghost,
.personal-info-row .btn.ghost {
  color: inherit;
}

/* 交付物管理列与图标按钮样式 */
.deliverable-actions {
  /* 保持表格单元格默认布局，避免高度不一致 */
}

.deliverable-actions .actions-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;
}

.table td.deliverable-actions:empty::before {
  content: "";
  display: block;
  height: 28px;
  /* 与图标按钮高度一致，保证行高与下边线对齐 */
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
  position: relative;
  /* 为模板标记定位 */
}

.icon-btn:hover {
  background: #f9fafb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.icon-btn.danger {
  /* 类级注释：危险状态（必须上传）——红色背景更醒目，图标保持黑色 */
  border-color: #ef4444;
  /* red-500 */
  background: #fee2e2;
  /* red-200 */
}

.icon-btn.danger:hover {
  background: #fecaca;
  border-color: #ef4444;
}

.icon-btn.success {
  /* 类级注释：成功状态（已有上传）——绿色背景更醒目，图标保持黑色 */
  border-color: #22c55e;
  /* green-500 */
  background: #dcfce7;
  /* green-200 */
}

.icon-btn.success:hover {
  background: #bbf7d0;
  border-color: #22c55e;
}

.icon-btn.has-files {
  border-color: #ef4444;
  background: #fee2e2;
}

.icon-btn.has-files:hover {
  background: #fecaca;
  border-color: #ef4444;
}

.icon-btn.has-files svg {
  fill: #ef4444;
}

.icon-btn svg {
  width: 18px;
  height: 18px;
  /* 保持图标黑色，不受按钮颜色影响 */
  fill: #000;
}

.icon-btn.disabled {
  border-color: #d1d5db;
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.7;
}

.icon-btn.disabled:hover {
  background: #f3f4f6;
  box-shadow: none;
}

.icon-btn.disabled svg {
  fill: #9ca3af;
}

/* 类级注释：上传按钮“模板”标记样式 —— 在右上角显示一个醒目的星标 */
.icon-btn.has-template::after {
  content: '\2605';
  /* ★ 星号，表示存在模板 */
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  font-size: 12px;
  line-height: 16px;
  color: #f59e0b;
  /* amber-500 */
  background: #fff;
  border-radius: 50%;
  border: 1px solid #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.upload-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.upload-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.upload-head .name {
  font-weight: 600;
  color: #333;
}

.upload-head .hint {
  color: #888;
  font-size: 12px;
}

.template-title {
  color: #666;
  font-size: 12px;
  margin-bottom: 6px;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  font-size: 12px;
  color: #374151;
}

.chip.clickable {
  cursor: pointer;
}

.chip.clickable:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.chip .chip-name {
  font-weight: 500;
}

.chip .chip-meta {
  color: #6b7280;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.deliverable-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.deliverable-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.deliverable-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}

.deliverable-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.deliverable-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.deliverable-name {
  font-weight: 600;
  color: #333;
}

.deliverable-type {
  color: #666;
  font-size: 12px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 2px 8px;
}

.deliverable-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  border-bottom: 1px dashed #e5e7eb;
}

.deliverable-list .name {
  font-weight: 500;
}

.deliverable-list .type {
  color: #6b7280;
  font-size: 12px;
}

/* 上传进度条样式 */
.progress {
  position: relative;
  height: 6px;
  background: #f0f0f0;
  border-radius: 4px;
  margin-top: 8px;
}

.progress .bar {
  height: 100%;
  background: #409eff;
  border-radius: 4px;
  width: 0%;
  transition: width .2s ease;
}

.progress .percent {
  position: absolute;
  top: -18px;
  right: 0;
  font-size: 12px;
  color: #666;
}

.file-list.compact .file-item {
  padding: 2px 0;
}

/* 预览弹窗样式 */
.preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 2000;
  display: flex;
  flex-direction: column;
}

.preview-header {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: #111827;
  color: #fff;
}

.preview-header .title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.preview-header .tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-body {
  flex: 1;
  background: #0f172a;
  color: #fff;
  overflow: auto;
  padding: 12px;
}

.preview-content {
  display: inline-block;
  transform-origin: center top;
}

.preview-image {
  max-width: 100%;
  height: auto;
  display: block;
}

.pdf-embed {
  width: 100%;
  height: 100%;
  border: none;
  background: #0f172a;
}

.video-player {
  width: 100%;
  height: 100%;
  background: #000;
}

.pdf-viewer {
  display: inline-block;
  background: #fff;
  padding: 8px;
  border-radius: 6px;
}

.pdf-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.html-view {
  background: #fff;
  color: #111;
  padding: 12px;
  border-radius: 6px;
  max-width: 100%;
}

.text-view {
  background: #111827;
  color: #e5e7eb;
  padding: 12px;
  border-radius: 6px;
  max-width: 100%;
  white-space: pre-wrap;
}

.loading {
  color: #e5e7eb;
}

.error {
  color: #fecaca;
}

.unsupported {
  color: #e5e7eb;
}

/* 已移除 Luckysheet 容器样式：统一使用 iframe 全屏预览 PDF */
/* 类级注释：上传按钮模板标记改为字母 T（覆盖旧星标） */
.icon-btn.has-template::after {
  content: 'T';
  font-weight: 700;
}

/* 布局容器调整 */
.content-wrapper {
  display: flex;
  flex-direction: column;
  /* 改为垂直排列 */
  gap: 2px;
  /* 进一步减小标签栏与内容区的间距 */
  flex: 1;
  min-height: 0;
}

/* 顶部横向标签栏样式 */
.top-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  flex-shrink: 0;
  border-bottom: 1px solid #e5e7eb;
}

.tab-item {
  padding: 8px 16px;
  /* 减小内边距以降低高度 */
  cursor: pointer;
  border-bottom: 2px solid transparent;
  /* 底部边框指示选中状态 */
  border-left: none;
  /* 移除左侧边框 */
  transition: all 0.2s;
  color: #4b5563;
  font-weight: 500;
  font-size: 13px;
  /* 稍微减小字体 */
  white-space: nowrap;
}

.tab-item:hover {
  background-color: #f9fafb;
  color: #111827;
}

.tab-item.active {
  background-color: #fff;
  color: #2563eb;
  border-bottom-color: #2563eb;
  /* 底部高亮 */
  border-left-color: transparent;
}

.main-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  /* 关键：允许flex子项收缩 */
}

.empty-tab {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 60px;
  text-align: center;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #6b7280;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.empty-state p {
  margin: 0;
}

/* 合同外需求样式补充 */
.actions {
  display: flex;
  gap: 8px;
}

.add-btn {
  padding: 6px 12px;
  border: 1px solid #2563eb;
  color: #2563eb;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background: #eff6ff;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
}

.page-info {
  color: #666;
  font-size: 12px;
}
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  display: block;
}
.custom-tooltip {
  position: fixed;
  z-index: 9999;
  background: #303133;
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  max-width: 300px;
  word-wrap: break-word;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  pointer-events: none;
}
</style>
