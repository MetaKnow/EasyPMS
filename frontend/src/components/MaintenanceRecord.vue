<template>
  <div class="project-detail-page">
    <div v-if="tooltip.visible" class="custom-tooltip" :style="tooltip.style">{{ tooltip.content }}</div>
    <div class="topbar">
      <button class="back-btn" @click="goBack">返回</button>
      <div class="title">
        <span class="name">{{ project?.projectName || '运维记录' }}</span>
        <span class="num" v-if="project?.projectNum">编号：{{ project.projectNum }}</span>
      </div>
      <div class="actions">
        <button class="add-btn" @click="onAddClick">添加记录</button>
      </div>
    </div>

    <div v-if="loading" class="state">正在加载...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <div v-else class="content-wrapper">
      <div class="top-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-item"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          {{ tab.name }}
        </div>
      </div>

      <div class="main-content">
        <div v-show="activeTab === 'events'" class="card wide">
          <div v-if="eventsLoading" class="state">运维事件加载中...</div>
          <div v-else-if="eventsError" class="state error">{{ eventsError }}</div>
          <div v-else class="table-scroll">
            <table class="table">
              <colgroup>
                <col style="width:5%">
                <col style="width:9%">
                <col style="width:26%">
                <col style="width:8%">
                <col style="width:8%">
                <col style="width:8%">
                <col style="width:6%">
                <col style="width:8%">
                <col style="width:12%">
                <col style="width:10%">
              </colgroup>
              <thead>
                <tr>
                  <th>序号</th>
                  <th>事件时间</th>
                  <th>事件名称</th>
                  <th>事件类型</th>
                  <th>服务方式</th>
                  <th>负责人</th>
                  <th>工时</th>
                  <th>是否完成</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(ev, idx) in events" :key="ev.eventId">
                  <td>{{ rowIndex(idx) }}</td>
                  <td @mouseenter="showTooltip($event, formatDate(ev.eventDate))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ formatDate(ev.eventDate) }}</div></td>
                  <td @mouseenter="showTooltip($event, ev.eventName)" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ ev.eventName }}</div></td>
                  <td @mouseenter="showTooltip($event, ev.eventType || '—')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ ev.eventType || '—' }}</div></td>
                  <td @mouseenter="showTooltip($event, ev.serviceMode || '—')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ ev.serviceMode || '—' }}</div></td>
                  <td @mouseenter="showTooltip($event, ev.directorName || ev.director || '—')" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ ev.directorName || ev.director || '—' }}</div></td>
                  <td @mouseenter="showTooltip($event, formatHours(ev.eventhours))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ formatHours(ev.eventhours) }}</div></td>
                  <td>
                    <span :class="ev.isComplete ? 'status-done' : 'status-pending'">
                      {{ ev.isComplete ? '是' : '否' }}
                    </span>
                  </td>
                  <td @mouseenter="showTooltip($event, formatDateTime(ev.createTime))" @mouseleave="hideTooltip" @mousemove="updateTooltipPosition"><div class="text-truncate">{{ formatDateTime(ev.createTime) }}</div></td>
                  <td>
                    <div class="action-group">
                      <button class="icon-btn" :class="{ 'has-files': ev.hasFiles }" title="查看" @click="viewEvent(ev)" :disabled="eventsLoading" aria-label="查看">
                        <svg viewBox="0 0 24 24"><path d="M12 5c-7 0-11 7-11 7s4 7 11 7 11-7 11-7-4-7-11-7zm0 12a5 5 0 110-10 5 5 0 010 10z"/></svg>
                      </button>
                      <button class="icon-btn warning" title="修改" @click="editEvent(ev)" :disabled="eventsLoading" aria-label="修改">
                        <svg viewBox="0 0 24 24"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zm2.92 2.83H5v-1.92l8.06-8.06 1.92 1.92-8.06 8.06zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.82 1.82 3.75 3.75 1.82-1.82z"/></svg>
                      </button>
                      <button class="icon-btn" title="删除" @click="deleteEvent(ev)" :disabled="eventsLoading" aria-label="删除">
                        <svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="!events.length">
                  <td class="empty" colspan="9">暂无事件</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination">
            <button class="btn" :disabled="eventsPage <= 1 || eventsLoading" @click="prevEventsPage">上一页</button>
            <span class="page-info">第 {{ eventsPage }} / {{ eventsPages }} 页，共 {{ eventsTotal }} 条</span>
            <button class="btn" :disabled="eventsPage >= eventsPages || eventsLoading" @click="nextEventsPage">下一页</button>
          </div>
        </div>

        <div v-show="activeTab === 'visits'" class="empty-tab">
          <div class="empty-state">
            <i class="empty-icon">📞</i>
            <h3>售后（销售）回访</h3>
            <p>该模块正在建设中...</p>
          </div>
        </div>

        <div v-show="activeTab === 'opportunities'" class="empty-tab">
          <div class="empty-state">
            <i class="empty-icon">💡</i>
            <h3>销售机会</h3>
            <p>该模块正在建设中...</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 查看事件弹窗（表单样式，不可编辑） -->
  <div v-if="showViewDialog" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>查看运维事件</h3>
        <button class="close-btn" @click="closeView">&times;</button>
      </div>
      <div class="modal-body">
        <div class="project-form">
          <div class="form-section">
            <div class="form-grid">
              <div class="form-group">
                <label>事件名称 <span class="required">*</span></label>
                <input type="text" v-model="viewForm.eventName" disabled />
              </div>
              <div class="form-group">
                <label>事件时间 <span class="required">*</span></label>
                <input type="date" v-model="viewForm.eventDate" disabled />
              </div>
              <div class="form-group">
                <label>事件类型 <span class="required">*</span></label>
                <select v-model="viewForm.eventType" disabled>
                  <option value="主动服务">主动服务</option>
                  <option value="响应服务">响应服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>服务方式 <span class="required">*</span></label>
                <select v-model="viewForm.serviceMode" disabled>
                  <option value="远程服务">远程服务</option>
                  <option value="现场服务">现场服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>负责人 <span class="required">*</span></label>
                <select v-model.number="viewForm.director" disabled>
                  <option value="" disabled>请选择负责人</option>
                  <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.name || u.userName || u.userId }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>工时 <span class="required">*</span></label>
                <input type="number" step="0.5" min="0.5" v-model="viewForm.eventhours" disabled />
              </div>
              <div class="form-group">
                <label>是否完成 <span class="required">*</span></label>
                <select v-model="viewForm.isComplete" disabled>
                  <option :value="true">已完成</option>
                  <option :value="false">未完成</option>
                </select>
              </div>
              <div class="form-group full-width">
                <label>事件描述</label>
                <textarea rows="4" v-model="viewForm.eventDetails" disabled></textarea>
              </div>
            </div>
          </div>

          <!-- 附件列表（仅浏览/下载） -->
          <div class="form-section attachments-section">
            <h4 class="section-title">附件</h4>
            <div class="upload-card">
              <div class="upload-body">
                <div class="uploaded-list" v-if="viewAttachments.length">
                  <div class="file-row" v-for="f in viewAttachments" :key="f.fileId">
                    <div class="file-meta">
                      <span class="file-link" @click="onPreviewFile(f)" :title="fileBaseName(f.filePath) || f.deliverableName">
                        {{ f.deliverableName || fileBaseName(f.filePath) }}
                      </span>
                      <span class="file-size">{{ prettySize(f.fileSize) }}</span>
                    </div>
                    <div class="file-actions">
                      <a class="mini-icon" :href="getDownloadUrl(f.fileId)" title="下载" aria-label="下载">
                        <svg viewBox="0 0 24 24"><path d="M5 20h14v-2H5v2zm7-18L5.33 8h3.84v4h6.66V8h3.84L12 2z"/></svg>
                      </a>
                    </div>
                  </div>
                </div>
                <div class="empty" v-else>暂无附件</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="closeView">关闭</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 新增事件弹窗（与新建项目样式一致） -->
  <div v-if="showAddDialog" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>新增运维事件</h3>
        <button class="close-btn" @click="closeAdd">&times;</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="saveAdd" class="project-form">
          <div class="form-section">
            <div class="form-grid">
              <div class="form-group">
                <label>事件名称 <span class="required">*</span></label>
                <input type="text" v-model.trim="addForm.eventName" placeholder="请输入事件名称" />
              </div>
              <div class="form-group">
                <label>事件时间 <span class="required">*</span></label>
                <input type="date" v-model="addForm.eventDate" />
              </div>
              <div class="form-group">
                <label>事件类型 <span class="required">*</span></label>
                <select v-model="addForm.eventType">
                  <option value="主动服务">主动服务</option>
                  <option value="响应服务">响应服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>服务方式 <span class="required">*</span></label>
                <select v-model="addForm.serviceMode">
                  <option value="远程服务">远程服务</option>
                  <option value="现场服务">现场服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>负责人 <span class="required">*</span></label>
                <select v-model.number="addForm.director">
                  <option value="" disabled>请选择负责人</option>
                  <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.name || u.userName || u.userId }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>工时 <span class="required">*</span></label>
                <input
                  type="number"
                  step="0.5"
                  min="0.5"
                  v-model="addForm.eventhours"
                  placeholder="请输入工时（以0.5小时为单位，保留一位小数）"
                  @input="onHoursInput"
                  @blur="onHoursBlur"
                />
              </div>
              <div class="form-group">
                <label>是否完成 <span class="required">*</span></label>
                <select v-model="addForm.isComplete">
                  <option :value="true">已完成</option>
                  <option :value="false">未完成</option>
                </select>
              </div>
              <div class="form-group full-width">
                <label>事件描述</label>
                <textarea rows="4" v-model="addForm.eventDetails" placeholder="可填写本次服务的详细说明"></textarea>
              </div>
              <div class="form-group full-width" v-if="addFormError">
                <div class="form-error">{{ addFormError }}</div>
              </div>
            </div>
          </div>

          <!-- 附件上传区域（统一样式与交互） -->
          <div class="form-section attachments-section">
            <h4 class="section-title">上传附件</h4>
            <div class="upload-card">
              <div class="upload-head">
                <!--
                <div class="head-left">
                  <span class="hint">支持多文件上传</span>
                </div>
                -->
                <div class="head-right">
                  <button type="button" class="btn primary select-btn" @click="triggerAttachmentInput">选择文件</button>
                  <input ref="attachmentInput" type="file" multiple class="hidden-file" @change="onSelectAttachmentFiles" />
                </div>
              </div>

              <div class="upload-body">
                <div class="selected-files" v-if="selectedAttachmentFiles.length">
                  <div class="selected-list">
                    <div class="selected-file-row" v-for="(f, idx) in selectedAttachmentFiles" :key="f.name + '-' + idx">
                      <span class="file-name" :title="f.name">{{ f.name }}</span>
                      <button class="mini-icon danger" @click="removeSelectedFile(idx)" title="删除" aria-label="删除">
                        <svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <!--
                <div class="hint" v-if="uploadPending && !createdEventId">附件将随保存事件一起上传</div>
                -->
                <div class="progress" v-if="uploading">
                  <div class="bar" :style="{ width: uploadProgress + '%' }"></div>
                  <div class="percent">{{ uploadProgress }}%</div>
                </div>

                <div class="uploaded-list" v-if="attachments.length">
                  <div class="file-row" v-for="f in attachments" :key="f.fileId">
                    <div class="file-meta">
                      <span class="file-link" @click="onPreviewFile(f)" :title="fileBaseName(f.filePath) || f.deliverableName">
                        {{ f.deliverableName || fileBaseName(f.filePath) }}
                      </span>
                      <span class="file-size">{{ prettySize(f.fileSize) }}</span>
                    </div>
                    <div class="file-actions">
                      <button class="mini-icon danger" @click="onDeleteAttachment(f)" title="删除" aria-label="删除">
                        <svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <!--<div class="empty" v-else>暂无附件</div>-->
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="closeAdd" :disabled="addSubmitting">取消</button>
          <button type="submit" class="btn btn-primary" @click="saveAdd" :disabled="addSubmitting">保存</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 编辑事件弹窗（可修改与重新上传） -->
  <div v-if="showEditDialog" class="modal-overlay">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>编辑运维事件</h3>
        <button class="close-btn" @click="closeEdit">&times;</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="saveEdit" class="project-form">
          <div class="form-section">
            <div class="form-grid">
              <div class="form-group">
                <label>事件名称 <span class="required">*</span></label>
                <input type="text" v-model.trim="editForm.eventName" placeholder="请输入事件名称" />
              </div>
              <div class="form-group">
                <label>事件时间 <span class="required">*</span></label>
                <input type="date" v-model="editForm.eventDate" />
              </div>
              <div class="form-group">
                <label>事件类型 <span class="required">*</span></label>
                <select v-model="editForm.eventType">
                  <option value="主动服务">主动服务</option>
                  <option value="响应服务">响应服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>服务方式 <span class="required">*</span></label>
                <select v-model="editForm.serviceMode">
                  <option value="远程服务">远程服务</option>
                  <option value="现场服务">现场服务</option>
                </select>
              </div>
              <div class="form-group">
                <label>负责人 <span class="required">*</span></label>
                <select v-model.number="editForm.director">
                  <option value="" disabled>请选择负责人</option>
                  <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.name || u.userName || u.userId }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>工时 <span class="required">*</span></label>
                <input
                  type="number"
                  step="0.5"
                  min="0.5"
                  v-model="editForm.eventhours"
                  placeholder="请输入工时（以0.5小时为单位，保留一位小数）"
                  @input="onEditHoursInput"
                  @blur="onEditHoursBlur"
                />
              </div>
              <div class="form-group">
                <label>是否完成 <span class="required">*</span></label>
                <select v-model="editForm.isComplete">
                  <option :value="true">已完成</option>
                  <option :value="false">未完成</option>
                </select>
              </div>
              <div class="form-group full-width">
                <label>事件描述</label>
                <textarea rows="4" v-model="editForm.eventDetails" placeholder="可填写本次服务的详细说明"></textarea>
              </div>
              <div class="form-group full-width" v-if="editFormError">
                <div class="form-error">{{ editFormError }}</div>
              </div>
            </div>
          </div>

          <!-- 附件上传区域（编辑可选择与删除） -->
          <div class="form-section attachments-section">
            <h4 class="section-title">附件</h4>
            <div class="upload-card">
              <div class="upload-head">
                <div class="head-right">
                  <button type="button" class="btn primary select-btn" @click="triggerEditAttachmentInput">选择文件</button>
                  <input ref="editAttachmentInput" type="file" multiple class="hidden-file" @change="onSelectEditAttachmentFiles" />
                </div>
              </div>

              <div class="upload-body">
                <div class="selected-files" v-if="editSelectedAttachmentFiles.length">
                  <div class="selected-list">
                    <div class="selected-file-row" v-for="(f, idx) in editSelectedAttachmentFiles" :key="f.name + '-' + idx">
                      <span class="file-name" :title="f.name">{{ f.name }}</span>
                      <button type="button" class="mini-icon danger" @click="removeEditSelectedFile(idx)" title="删除" aria-label="删除">
                        <svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                      </button>
                    </div>
                  </div>
                </div>

                <div class="progress" v-if="editUploading">
                  <div class="bar" :style="{ width: editUploadProgress + '%' }"></div>
                  <div class="percent">{{ editUploadProgress }}%</div>
                </div>

                <div class="uploaded-list" v-if="editAttachments.length">
                  <div class="file-row" v-for="f in editAttachments" :key="f.fileId">
                    <div class="file-meta">
                      <span class="file-link" @click="onPreviewFile(f)" :title="fileBaseName(f.filePath) || f.deliverableName">
                        {{ f.deliverableName || fileBaseName(f.filePath) }}
                      </span>
                      <span class="file-size">{{ prettySize(f.fileSize) }}</span>
                    </div>
                    <div class="file-actions">
                      <button type="button" class="mini-icon danger" @click="onDeleteEditAttachment(f)" title="删除" aria-label="删除">
                        <svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <div class="empty" v-else>暂无附件</div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" @click="closeEdit" :disabled="editSubmitting">取消</button>
              <button type="submit" class="btn btn-primary" @click="saveEdit" :disabled="editSubmitting">保存</button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
  <!-- 文件预览弹窗 -->
  <!-- 全屏文件预览弹窗 (与 ProjectDetail 保持一致) -->
  <div v-if="showPreviewDialog" class="preview-overlay">
    <div class="preview-header">
      <span class="title">{{ previewTitle }}</span>
      <div class="tools">
        <button v-if="previewType !== 'pdf' && previewType !== 'video'" class="icon-btn" title="缩小" @click="pdfZoomOut">
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
        <div v-else-if="previewType === 'html'" class="html-view" v-html="previewHTML"></div>
        <pre v-else-if="previewType === 'text'" class="text-view">{{ previewText }}</pre>
        <div v-else class="unsupported">文件格式不支持预览，请下载后查看</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAfterserviceProjectById } from '../api/afterserviceProject'
import { getAfterserviceEvents, deleteAfterserviceEvent, createAfterserviceEvent } from '../api/afterserviceEvent'
import { getAllUsers } from '../api/user'
import {
  uploadAfterserviceDeliverableFiles,
  listAfterserviceDeliverableFiles,
  getAfterserviceDeliverablePreviewUrl,
  getAfterserviceDeliverableDownloadUrl,
  deleteAfterserviceDeliverableFile,
  getAfterserviceDeliverablePreviewPdfUrl,
  getAfterserviceDeliverablePreviewVideoUrl
} from '../api/afterserviceDeliverableFile'
import * as mammoth from 'mammoth/mammoth.browser'

export default {
  name: 'MaintenanceRecord',
  data() {
    return {
      activeTab: 'events',
      tabs: [
        { id: 'events', name: '运维事件' },
        { id: 'visits', name: '售后（销售）回访' },
        { id: 'opportunities', name: '销售机会' }
      ],
      loading: true,
      error: '',
      project: null,
      // 运维事件列表状态
      events: [],
      eventsLoading: true,
      eventsError: '',
      eventsPage: 1,
      eventsSize: 10,
      eventsPages: 1,
      eventsTotal: 0
      ,
      // 查看弹窗
      showViewDialog: false,
      viewEventData: null,
      viewEventId: null,
      viewForm: {
        eventName: '',
        eventDate: '',
        eventType: '',
        serviceMode: '',
        director: '',
        eventhours: '',
        eventDetails: ''
      },
      viewAttachments: [],
      // 新增弹窗与表单
      showAddDialog: false,
      addSubmitting: false,
      addFormError: '',
      addForm: {
        eventName: '',
        eventDate: '',
        eventType: '响应服务',
        serviceMode: '远程服务',
        director: '',
        eventhours: '',
        eventDetails: ''
      },
      // 工时输入规范化的防抖计时器
      hoursNormalizeTimer: null,
      users: [],
      // 附件上传状态
      createdEventId: null,
      selectedAttachmentFiles: [],
      attachments: [],
      uploading: false,
      uploadProgress: 0,
      uploadPending: false
      ,
      // 编辑弹窗与表单
      showEditDialog: false,
      editSubmitting: false,
      editFormError: '',
      editEventId: null,
      editForm: {
        eventName: '',
        eventDate: '',
        eventType: '响应服务',
        serviceMode: '远程服务',
        director: '',
        eventhours: '',
        eventDetails: ''
      },
      editSelectedAttachmentFiles: [],
      editAttachments: [],
      editUploading: false,
      editUploadProgress: 0,
      editHoursNormalizeTimer: null,
      // 预览弹窗状态
      showPreviewDialog: false,
      previewTitle: '',
      previewType: '', // 'image', 'pdf', 'video', 'docx', 'text', 'unsupported'
      previewUrl: '',
      previewHTML: '',
      previewText: '',
      previewLoading: false,
      previewError: '',
      previewScale: 1.0,
      tooltip: { visible: false, content: '', style: { top: '0px', left: '0px' } }
    }
  },
  async mounted() {
    await this.loadProject()
    await this.loadEvents()
    await this.loadUsers()
  },
  beforeUnmount() {
    // 清理工时规范化定时器，避免内存泄漏或异步回填已销毁组件
    if (this.hoursNormalizeTimer) {
      clearTimeout(this.hoursNormalizeTimer)
      this.hoursNormalizeTimer = null
    }
    if (this.editHoursNormalizeTimer) {
      clearTimeout(this.editHoursNormalizeTimer)
      this.editHoursNormalizeTimer = null
    }
  },
  methods: {
    showTooltip(e, content) {
      if (!content) return
      this.tooltip.content = content
      this.tooltip.visible = true
      this.updateTooltipPosition(e)
    },
    hideTooltip() {
      this.tooltip.visible = false
    },
    updateTooltipPosition(e) {
      const x = e.clientX + 15
      const y = e.clientY + 15
      // Ensure tooltip doesn't go off screen
      this.tooltip.style = {
        top: `${y}px`,
        left: `${x}px`
      }
    },
    goBack() {
      this.$router.push('/home/maintenance')
    },
    async loadProject() {
      const id = this.$route.params.projectId
      try {
        const res = await getAfterserviceProjectById(id)
        if (res?.data?.success) {
          this.project = res.data.data
        } else {
          this.error = res?.data?.message || '加载项目失败'
        }
      } catch (e) {
        this.error = e?.response?.data?.message || e?.message || '加载项目失败'
      } finally {
        this.loading = false
      }
    },
    async loadEvents() {
      const afterServiceProjectId = this.$route.params.projectId
      this.eventsLoading = true
      this.eventsError = ''
      try {
        const res = await getAfterserviceEvents({ afterServiceProjectId, page: this.eventsPage, size: this.eventsSize })
        const data = res?.data?.data || {}
        const list = data.list || []
        this.events = Array.isArray(list) ? list : []
        this.eventsTotal = Number(data.total || 0)
        this.eventsPages = Number(data.pages || 1)
      } catch (e) {
        this.eventsError = e?.response?.data?.message || e?.message || '加载运维事件失败'
      } finally {
        this.eventsLoading = false
      }
    },
    async loadUsers() {
      try {
        const list = await getAllUsers()
        this.users = Array.isArray(list) ? list : []
      } catch (e) {
        this.users = []
      }
    },
    prevEventsPage() {
      if (this.eventsPage > 1) {
        this.eventsPage -= 1
        this.loadEvents()
      }
    },
    nextEventsPage() {
      if (this.eventsPage < this.eventsPages) {
        this.eventsPage += 1
        this.loadEvents()
      }
    },
    formatDate(val) {
      if (!val) return '—'
      try {
        const d = new Date(val)
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      } catch (e) {
        return val
      }
    },
    formatHours(val) {
      if (val == null) return '—'
      const num = Number(val)
      if (Number.isNaN(num)) return String(val)
      return `${num}`
    },
    rowIndex(idx) {
      return (this.eventsPage - 1) * this.eventsSize + idx + 1
    },
    formatDateTime(val) {
      if (!val) return '—'
      try {
        const d = new Date(val)
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const hh = String(d.getHours()).padStart(2, '0')
        const mm = String(d.getMinutes()).padStart(2, '0')
        return `${y}-${m}-${day} ${hh}:${mm}`
      } catch (e) {
        return val
      }
    },
    viewEvent(ev) {
      if (!ev) return
      this.viewEventData = ev
      this.viewEventId = ev.eventId || null
      this.viewForm = {
        eventName: ev.eventName || '',
        eventDate: ev.eventDate || '',
        eventType: ev.eventType || '',
        serviceMode: ev.serviceMode || '',
        director: ev.director || '',
        eventhours: ev.eventhours || '',
        eventDetails: ev.eventDetails || '',
        isComplete: ev.isComplete != null ? ev.isComplete : false
      }
      this.showViewDialog = true
      this.loadViewAttachments()
    },
    closeView() {
      this.showViewDialog = false
      this.viewEventData = null
      this.viewEventId = null
      this.viewAttachments = []
    },
    async loadViewAttachments() {
      const pid = Number(this.$route.params.projectId)
      const eid = Number(this.viewEventId)
      if (!pid || !eid) { this.viewAttachments = []; return }
      try {
        const list = await listAfterserviceDeliverableFiles(pid, eid)
        this.viewAttachments = Array.isArray(list) ? list : []
      } catch (_) {
        this.viewAttachments = []
      }
    },
    onAddClick() {
      if (this.activeTab !== 'events') {
        alert('当前标签暂未实现“添加记录”功能，请切换到“运维事件”标签。')
        return
      }
      this.openAdd()
    },
    openAdd() {
      this.addFormError = ''
      this.addSubmitting = false

      // 计算今天日期
      const d = new Date()
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const today = `${y}-${m}-${day}`

      // 默认负责人为当前登录用户
      let defaultDirector = ''
      try {
        const raw = localStorage.getItem('userInfo')
        const userInfo = raw ? JSON.parse(raw) : null
        const uid = userInfo && userInfo.userId != null ? Number(userInfo.userId) : null
        if (uid != null) defaultDirector = uid
      } catch (_) {}

      // 重置表单为默认值，避免显示上次填写的数据
      this.addForm = {
        eventName: '',
        eventDate: today,
        eventType: '响应服务',
        serviceMode: '远程服务',
        director: defaultDirector,
        eventhours: '',
        eventDetails: '',
        isComplete: false
      }

      // 清理工时规范化定时器与上传状态
      if (this.hoursNormalizeTimer) {
        clearTimeout(this.hoursNormalizeTimer)
        this.hoursNormalizeTimer = null
      }
      this.createdEventId = null
      this.selectedAttachmentFiles = []
      this.attachments = []
      this.uploadPending = false
      this.uploading = false
      this.uploadProgress = 0

      // 最后再打开弹窗，确保展示的是重置后的默认值
      this.showAddDialog = true
    },
    closeAdd() {
      this.showAddDialog = false
    },
    // 工时输入：停止输入1秒后自动规范
    onHoursInput() {
      if (this.hoursNormalizeTimer) clearTimeout(this.hoursNormalizeTimer)
      this.hoursNormalizeTimer = setTimeout(() => {
        this.applyHoursNormalization()
      }, 1000)
    },
    // 工时失焦：1秒后自动规范（与输入停止一致）
    onHoursBlur() {
      if (this.hoursNormalizeTimer) clearTimeout(this.hoursNormalizeTimer)
      this.hoursNormalizeTimer = setTimeout(() => {
        this.applyHoursNormalization()
      }, 1000)
    },
    // 执行工时规范化：转换为0.5的正整数倍，不合规则上调到最近的0.5
    applyHoursNormalization() {
      this.hoursNormalizeTimer = null
      const raw = this.addForm.eventhours
      if (raw === '' || raw == null) return
      let h = Number(raw)
      if (!Number.isFinite(h)) return
      if (h <= 0) h = 0.5
      if (!Number.isInteger(h * 2)) {
        const remainder = h % 0.5
        h = h - remainder + 0.5
      }
      this.addForm.eventhours = Number(h.toFixed(1))
    },
    async saveAdd() {
      if (this.addSubmitting) return
      this.addFormError = ''

      // 简单校验
      const f = this.addForm
      if (!f.eventName || !f.eventDate || !f.eventType || !f.serviceMode || !f.director || f.eventhours === '') {
        this.addFormError = '请完整填写必填字段（事件名称、事件时间、事件类型、服务方式、负责人、工时）'
        return
      }
      const hours = Number(f.eventhours)
      if (Number.isNaN(hours) || hours < 0) {
        this.addFormError = '工时需为非负数字'
        return
      }

      const payload = {
        eventName: f.eventName,
        eventDate: f.eventDate, // LocalDate字符串
        eventType: f.eventType,
        serviceMode: f.serviceMode,
        director: Number(f.director),
        // 将工时规范为0.5的正整数倍：若不是倍数则取模后+0.5（上调到下一个0.5）
        eventhours: (() => {
          let h = Number(f.eventhours)
          if (!Number.isFinite(h)) {
            // 非数字：提示并中止
            throw new Error('请输入有效的工时数字')
          }
          if (h <= 0) h = 0.5
          // 判断是否是0.5的倍数
          if (!Number.isInteger(h * 2)) {
            // 取模后加0.5：上调到下一个0.5倍
            const remainder = h % 0.5
            h = h - remainder + 0.5
          }
          // 显示与保存一位小数
          h = Number(h.toFixed(1))
          // 回填到表单显示
          this.addForm.eventhours = h
          return h
        })(),
        eventDetails: f.eventDetails || '',
        isComplete: !!f.isComplete,
        afterServiceProjectId: Number(this.$route.params.projectId)
      }

      this.addSubmitting = true
      try {
        const res = await createAfterserviceEvent(payload)
        const ok = res?.data?.success
        if (ok) {
          const created = res?.data?.data || {}
          this.createdEventId = created?.eventId || null
          // 若选择了附件，则自动上传，实现与表单一起保存
          if (this.createdEventId && this.selectedAttachmentFiles.length) {
            await this.onUploadAttachments()
          }
          // 刷新列表并关闭弹窗
          await this.loadEvents()
          this.showAddDialog = false
          this.uploadPending = false
        } else {
          this.addFormError = res?.data?.message || '创建失败'
        }
      } catch (e) {
        this.addFormError = e?.response?.data?.message || e?.message || '创建失败'
      } finally {
        this.addSubmitting = false
      }
    },
    editEvent(ev) {
      if (!ev) return
      this.editFormError = ''
      this.editSubmitting = false

      // 设置编辑表单初始值
      this.editEventId = ev.eventId || null
      this.editForm = {
        eventName: ev.eventName || '',
        eventDate: ev.eventDate || '',
        eventType: ev.eventType || '响应服务',
        serviceMode: ev.serviceMode || '远程服务',
        director: ev.director != null ? Number(ev.director) : '',
        eventhours: ev.eventhours || '',
        eventDetails: ev.eventDetails || '',
        isComplete: ev.isComplete != null ? ev.isComplete : false
      }

      // 清理选择与上传状态
      this.editSelectedAttachmentFiles = []
      this.editAttachments = []
      this.editUploading = false
      this.editUploadProgress = 0
      if (this.editHoursNormalizeTimer) {
        clearTimeout(this.editHoursNormalizeTimer)
        this.editHoursNormalizeTimer = null
      }

      // 加载附件
      this.loadEditAttachments()

      // 打开编辑弹窗
      this.showEditDialog = true
    },
    closeEdit() {
      this.showEditDialog = false
      this.editEventId = null
      this.editSelectedAttachmentFiles = []
      this.editAttachments = []
    },
    async saveEdit() {
      if (this.editSubmitting) return
      this.editFormError = ''

      const f = this.editForm
      if (!f.eventName || !f.eventDate || !f.eventType || !f.serviceMode || !f.director || f.eventhours === '') {
        this.editFormError = '请完整填写必填字段（事件名称、事件时间、事件类型、服务方式、负责人、工时）'
        return
      }

      const payload = {
        eventName: f.eventName,
        eventDate: f.eventDate,
        eventType: f.eventType,
        serviceMode: f.serviceMode,
        director: Number(f.director),
        eventhours: (() => {
          let h = Number(f.eventhours)
          if (!Number.isFinite(h)) throw new Error('请输入有效的工时数字')
          if (h <= 0) h = 0.5
          if (!Number.isInteger(h * 2)) {
            const remainder = h % 0.5
            h = h - remainder + 0.5
          }
          h = Number(h.toFixed(1))
          this.editForm.eventhours = h
          return h
        })(),
        eventDetails: f.eventDetails || '',
        isComplete: !!f.isComplete,
        afterServiceProjectId: Number(this.$route.params.projectId)
      }

      this.editSubmitting = true
      try {
        const { updateAfterserviceEvent } = await import('../api/afterserviceEvent')
        const res = await updateAfterserviceEvent(Number(this.editEventId), payload)
        const ok = res?.data?.success
        if (ok) {
          // 如果有选中的附件，执行上传
          if (this.editSelectedAttachmentFiles.length > 0) {
            this.editUploading = true
            this.editUploadProgress = 0
            try {
              const pid = Number(this.$route.params.projectId)
              const eid = Number(this.editEventId)
              await uploadAfterserviceDeliverableFiles(pid, eid, this.editSelectedAttachmentFiles, {
                onProgress: (p) => { this.editUploadProgress = p }
              })
              this.editSelectedAttachmentFiles = []
            } catch (uploadErr) {
              alert('事件信息更新成功，但附件上传失败：' + (uploadErr?.message || '未知错误'))
            } finally {
              this.editUploading = false
            }
          }

          await this.loadEvents()
          this.showEditDialog = false
        } else {
          this.editFormError = res?.data?.message || '更新失败'
        }
      } catch (e) {
        this.editFormError = e?.response?.data?.message || e?.message || '更新失败'
      } finally {
        this.editSubmitting = false
      }
    },
    triggerEditAttachmentInput() {
      try {
        if (this.$refs.editAttachmentInput && this.$refs.editAttachmentInput.click) {
          this.$refs.editAttachmentInput.click()
        }
      } catch (_) {}
    },
    onSelectEditAttachmentFiles(e) {
      const incoming = (e && e.target && e.target.files) ? Array.from(e.target.files) : []
      if (incoming.length) {
        const existing = this.editSelectedAttachmentFiles ? Array.from(this.editSelectedAttachmentFiles) : []
        const byKey = new Map()
        for (const f of existing) {
          const k = `${f.name}:${f.size}:${f.lastModified || 0}`
          byKey.set(k, f)
        }
        for (const f of incoming) {
          const k = `${f.name}:${f.size}:${f.lastModified || 0}`
          if (!byKey.has(k)) byKey.set(k, f)
        }
        this.editSelectedAttachmentFiles = Array.from(byKey.values())
      }
      if (e && e.target) e.target.value = ''
    },
    removeEditSelectedFile(idx) {
      if (typeof idx !== 'number') return
      if (idx < 0 || idx >= this.editSelectedAttachmentFiles.length) return
      this.editSelectedAttachmentFiles.splice(idx, 1)
    },
    async onUploadEditAttachments() {
      if (!this.editEventId) return
      const pid = Number(this.$route.params.projectId)
      const eid = Number(this.editEventId)
      if (!this.editSelectedAttachmentFiles.length) return alert('请先选择文件')
      this.editUploading = true
      this.editUploadProgress = 0
      try {
        await uploadAfterserviceDeliverableFiles(pid, eid, this.editSelectedAttachmentFiles, {
          onProgress: (p) => { this.editUploadProgress = p }
        })
        this.editSelectedAttachmentFiles = []
        await this.loadEditAttachments()
        alert('上传成功')
      } catch (e) {
        alert(e?.response?.data?.message || e?.message || '上传失败')
      } finally {
        this.editUploading = false
      }
    },
    async loadEditAttachments() {
      const pid = Number(this.$route.params.projectId)
      const eid = Number(this.editEventId)
      if (!pid || !eid) { this.editAttachments = []; return }
      try {
        const list = await listAfterserviceDeliverableFiles(pid, eid)
        this.editAttachments = Array.isArray(list) ? list : []
      } catch (_) {
        this.editAttachments = []
      }
    },
    async onDeleteEditAttachment(f) {
      const ok = window.confirm('确认删除该附件吗？')
      if (!ok) return
      try {
        await deleteAfterserviceDeliverableFile(f.fileId)
        await this.loadEditAttachments()
      } catch (e) {
        alert(e?.response?.data?.message || e?.message || '删除失败')
      }
    },
    // 编辑工时输入：停止输入1秒后自动规范
    onEditHoursInput() {
      if (this.editHoursNormalizeTimer) clearTimeout(this.editHoursNormalizeTimer)
      this.editHoursNormalizeTimer = setTimeout(() => {
        this.applyEditHoursNormalization()
      }, 1000)
    },
    // 编辑工时失焦：1秒后自动规范
    onEditHoursBlur() {
      if (this.editHoursNormalizeTimer) clearTimeout(this.editHoursNormalizeTimer)
      this.editHoursNormalizeTimer = setTimeout(() => {
        this.applyEditHoursNormalization()
      }, 1000)
    },
    applyEditHoursNormalization() {
      this.editHoursNormalizeTimer = null
      const raw = this.editForm.eventhours
      if (raw === '' || raw == null) return
      let h = Number(raw)
      if (!Number.isFinite(h)) return
      if (h <= 0) h = 0.5
      if (!Number.isInteger(h * 2)) {
        const remainder = h % 0.5
        h = h - remainder + 0.5
      }
      this.editForm.eventhours = Number(h.toFixed(1))
    },
    async deleteEvent(ev) {
      if (!ev?.eventId) return
      const ok = window.confirm('确认删除该事件吗？此操作不可恢复。')
      if (!ok) return
      try {
        await deleteAfterserviceEvent(ev.eventId)
        await this.loadEvents()
      } catch (e) {
        alert(e?.response?.data?.message || e?.message || '删除失败')
      }
    },
    // 附件交互
    triggerAttachmentInput() {
      try {
        if (this.$refs.attachmentInput && this.$refs.attachmentInput.click) {
          this.$refs.attachmentInput.click()
        }
      } catch (_) {}
    },
    onSelectAttachmentFiles(e) {
      const incoming = (e && e.target && e.target.files) ? Array.from(e.target.files) : []
      if (incoming.length) {
        const existing = this.selectedAttachmentFiles ? Array.from(this.selectedAttachmentFiles) : []
        const byKey = new Map()
        for (const f of existing) {
          const k = `${f.name}:${f.size}:${f.lastModified || 0}`
          byKey.set(k, f)
        }
        for (const f of incoming) {
          const k = `${f.name}:${f.size}:${f.lastModified || 0}`
          if (!byKey.has(k)) byKey.set(k, f)
        }
        this.selectedAttachmentFiles = Array.from(byKey.values())
      }
      if (this.createdEventId && this.selectedAttachmentFiles && this.selectedAttachmentFiles.length) {
        this.onUploadAttachments()
      } else if (this.selectedAttachmentFiles && this.selectedAttachmentFiles.length) {
        this.uploadPending = true
      }
      // 重置 input 以便重复选择相同文件
      if (e && e.target) e.target.value = ''
    },
    removeSelectedFile(idx) {
      if (typeof idx !== 'number') return
      if (idx < 0 || idx >= this.selectedAttachmentFiles.length) return
      this.selectedAttachmentFiles.splice(idx, 1)
      if (!this.selectedAttachmentFiles.length) {
        this.uploadPending = false
      }
    },
    async onUploadAttachments() {
      if (!this.createdEventId) return
      const pid = Number(this.$route.params.projectId)
      const eid = Number(this.createdEventId)
      if (!this.selectedAttachmentFiles.length) return alert('请先选择文件')
      this.uploading = true
      this.uploadProgress = 0
      try {
        await uploadAfterserviceDeliverableFiles(pid, eid, this.selectedAttachmentFiles, {
          onProgress: (p) => { this.uploadProgress = p }
        })
        this.selectedAttachmentFiles = []
        await this.loadAttachments()
        alert('上传成功')
      } catch (e) {
        alert(e?.response?.data?.message || e?.message || '上传失败')
      } finally {
        this.uploading = false
      }
    },
    async loadAttachments() {
      const pid = Number(this.$route.params.projectId)
      const eid = Number(this.createdEventId)
      if (!pid || !eid) { this.attachments = []; return }
      try {
        const list = await listAfterserviceDeliverableFiles(pid, eid)
        this.attachments = Array.isArray(list) ? list : []
      } catch (_) {
        this.attachments = []
      }
    },

    /**
     * 关闭预览弹窗
     */
    closePreviewDialog() {
      try {
        if ((this.previewType === 'image' || this.previewType === 'pdf') && this.previewUrl) {
          URL.revokeObjectURL(this.previewUrl)
        }
      } catch (_) {}
      this.showPreviewDialog = false
      this.previewTitle = ''
      this.previewType = ''
      this.previewUrl = ''
      this.previewHTML = ''
      this.previewText = ''
      this.previewLoading = false
      this.previewError = ''
      this.previewScale = 1.0
    },

    async onPreviewFile(file) {
      const name = this.fileBaseName(file?.filePath || '')
      const ext = (name.split('.').pop() || '').toLowerCase()
      this.previewTitle = name || '文件预览'
      this.previewLoading = true
      this.previewError = ''
      this.previewScale = 1.0
      this.showPreviewDialog = true

      // 图片
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

      // PDF
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

      // MP4
      if (ext === 'mp4') {
        this.previewType = 'video'
        try {
          this.previewUrl = getAfterserviceDeliverablePreviewVideoUrl(file.fileId)
        } catch (e) {
          this.previewError = e?.message || '视频预览失败'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // Word (doc/docx)
      if (ext === 'doc' || ext === 'docx') {
        try {
          // 优先尝试转 PDF
          const pdfBlob = await this.fetchPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          // docx 回退 mammoth
          if (ext === 'docx') {
            try {
              const blob = await this.fetchBlob(file.fileId)
              const buf = await blob.arrayBuffer()
              const result = await mammoth.convertToHtml({ arrayBuffer: buf })
              this.previewType = 'html'
              this.previewHTML = result.value || '<div>文档转换为空</div>'
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

      // Excel/PPT -> PDF
      if (['xls','xlsx','ppt','pptx'].includes(ext)) {
        try {
          const pdfBlob = await this.fetchPreviewPdfBlob(file.fileId)
          const url = URL.createObjectURL(new Blob([await pdfBlob.arrayBuffer()], { type: 'application/pdf' }))
          this.previewType = 'pdf'
          this.previewUrl = url
        } catch (e) {
          this.previewType = 'unsupported'
          this.previewError = e?.message || '预览失败，请下载查看'
        } finally {
          this.previewLoading = false
        }
        return
      }

      // TXT
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

      // 其他
      this.previewType = 'unsupported'
      this.previewLoading = false
    },

    pdfZoomIn() {
      this.previewScale = Math.min(this.previewScale + 0.1, 3.0)
    },
    pdfZoomOut() {
      this.previewScale = Math.max(this.previewScale - 0.1, 0.3)
    },

    async fetchBlob(fileId) {
      const url = getAfterserviceDeliverableDownloadUrl(fileId)
      // 如果 API url 是完整路径，直接 fetch
      // 注意：这里假设 url 是 http 开头。如果是相对路径需要处理
      const resp = await fetch(url) 
      // 注意：ProjectDetail.vue 用了 credentials: 'include'，如果跨域需要。
      // 这里同源或已处理好 cookie，暂时保持简单，如有问题再加
      if (!resp.ok) throw new Error('文件获取失败：' + resp.status)
      return await resp.blob()
    },

    async fetchPreviewPdfBlob(fileId) {
      const url = getAfterserviceDeliverablePreviewPdfUrl(fileId)
      const resp = await fetch(url)
      if (!resp.ok) throw new Error('预览转换失败：' + resp.status)
      return await resp.blob()
    },

    getPreviewUrl(fileId) {
      return getAfterserviceDeliverablePreviewUrl(fileId)
    },
    getDownloadUrl(fileId) {
      return getAfterserviceDeliverableDownloadUrl(fileId)
    },
    fileBaseName(path) {
      if (!path) return ''
      const parts = String(path).split(/[\\\/]/)
      return parts[parts.length - 1]
    },
    prettySize(bytes) {
      if (bytes == null) return ''
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let size = Number(bytes), idx = 0
      while (size >= 1024 && idx < units.length - 1) { size /= 1024; idx++ }
      return `${size.toFixed(1)} ${units[idx]}`
    },
    async onDeleteAttachment(f) {
      const ok = window.confirm('确认删除该附件吗？')
      if (!ok) return
      try {
        await deleteAfterserviceDeliverableFile(f.fileId)
        await this.loadAttachments()
      } catch (e) {
        alert(e?.response?.data?.message || e?.message || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
/* 与 ProjectDetail.vue 保持一致的基础布局与样式 */
.project-detail-page { display:flex; flex-direction:column; height:100vh; overflow:hidden; padding:8px; box-sizing:border-box; }
.topbar { display:flex; align-items:center; gap:8px; padding:4px 0; border-bottom:1px solid #eee; }
.back-btn { padding:6px 12px; border:1px solid #ddd; border-radius:4px; background:#fff; cursor:pointer; }
.title { flex:1; display:flex; align-items:baseline; gap:8px; font-size:18px; font-weight:600; }
.title .num { color:#666; font-size:13px; font-weight:400; }
.actions { display:flex; gap:8px; }
.add-btn { padding:6px 12px; border:1px solid #2563eb; color:#2563eb; background:#fff; border-radius:4px; cursor:pointer; }
.add-btn:hover { background:#eff6ff; }
.state { padding:24px; color:#333; }
.state.error { color:#c00; }

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-height: 0;
}

.top-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow-x: auto;
  flex-shrink: 0;
  border-bottom: 1px solid #e5e7eb;
}

.tab-item {
  padding: 8px 16px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  border-left: none;
  transition: all 0.2s;
  color: #4b5563;
  font-weight: 500;
  font-size: 13px;
  white-space: nowrap;
}

.tab-item:hover { background-color: #f9fafb; color: #111827; }
.tab-item.active { background-color: #fff; color: #2563eb; border-bottom-color: #2563eb; border-left-color: transparent; }

.main-content { flex: 1; min-width: 0; display: flex; flex-direction: column; min-height: 0; }
.card { background:#fff; border:1px solid #eee; border-radius:8px; padding:12px; }
.card.wide { display:flex; flex-direction:column; flex:1; min-height:0; }
/* 表格样式保持与 ProjectDetail.vue 一致 */
.table { width:100%; border-collapse:separate; border-spacing:0; table-layout: fixed; }
.table th, .table td { padding:8px; border-bottom:1px dashed #eee; font-size:14px; text-align:left; }
.table td { position: relative; }
.table thead th { background:#fafafa; font-weight:600; color:#333; }
.table-scroll { flex: 1; min-height: 0; overflow: auto; }
.table-scroll thead { position: sticky; top: 0; z-index: 4; background: #fafafa; }
.table-scroll thead th { position: sticky; top: 0; z-index: 5; background: #fafafa; }
.empty { color:#888; padding:8px 0; }
.pagination { display:flex; align-items:center; justify-content:flex-end; gap:8px; padding-top:8px; }
.btn { padding:6px 12px; border:1px solid #ddd; background:#fff; border-radius:4px; cursor:pointer; }
.btn:disabled { opacity:.6; cursor:not-allowed; }
.page-info { color:#666; font-size:12px; }

/* 操作列样式 */
.action-group { display:flex; gap:6px; }
.actions-inner { display:flex; align-items:center; gap:8px; justify-content:flex-start; }
.icon-btn { width: 28px; height: 28px; border: 1px solid #e5e7eb; border-radius: 6px; background: #fff; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all .15s ease; color: #374151; }
.icon-btn:hover { background: #f9fafb; box-shadow: 0 1px 2px rgba(0,0,0,0.06); }
.icon-btn.danger { border-color: #ef4444; background: #fee2e2; }
.icon-btn.danger:hover { background: #fecaca; border-color: #ef4444; }
.icon-btn.has-files { border-color: #ef4444; background: #fee2e2; }
.icon-btn.has-files:hover { background: #fecaca; border-color: #ef4444; }
.icon-btn.has-files svg { fill: #ef4444; }
.icon-btn svg { width: 18px; height: 18px; fill: #000; }
.icon-btn:disabled { opacity:.6; cursor:not-allowed; }

/* 查看弹窗 */
.modal-mask { position:fixed; inset:0; background:rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; z-index:1000; }
.modal-card { background:#fff; border-radius:8px; border:1px solid #eee; width:640px; max-width:95vw; padding:16px; }
.modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.modal-title { font-weight:600; font-size:16px; }
.modal-close { padding:4px 8px; border:1px solid #ddd; background:#fff; border-radius:4px; cursor:pointer; font-size:12px; }
.modal-body { font-size:13px; color:#333; }

/* 新增事件弹窗样式（与 ProjectCreateForm.vue 保持一致） */
.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background-color: rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index: 1000; }
.modal-content { background: white; border-radius: 12px; width: 90%; max-width: 900px; max-height: 90vh; box-shadow: 0 4px 20px rgba(0,0,0,0.15); display:flex; flex-direction:column; overflow:hidden; }
.modal-content .modal-header { display:flex; justify-content:space-between; align-items:center; padding:20px 24px; border-bottom:1px solid #e8e8e8; background:#fafafa; border-radius:12px 12px 0 0; }
.modal-content .modal-header h3 { margin:0; font-size:18px; font-weight:600; color:#262626; }
.close-btn { background:none; border:none; font-size:24px; cursor:pointer; color:#999; padding:0; width:30px; height:30px; display:flex; align-items:center; justify-content:center; border-radius:4px; transition:all .2s; }
.close-btn:hover { background:#f0f0f0; color:#666; }
.modal-content .modal-body { flex:1; overflow-y:auto; padding:0; }
.modal-footer { flex-shrink:0; padding:16px 24px; border-top:1px solid #e8e8e8; background:#fafafa; border-radius:0 0 12px 12px; }
.project-form { padding:24px; }
.form-section { margin-bottom:32px; }
.section-title { font-size:16px; font-weight:600; color:#262626; margin:0 0 16px 0; padding-bottom:8px; border-bottom:2px solid #1890ff; display:inline-block; }
.form-grid { display:grid; grid-template-columns: 1fr 1fr; gap:16px; align-items:start; }
.form-group { display:flex; flex-direction:column; }
.form-group.full-width { grid-column: 1 / -1; }
.form-group label { font-weight:500; margin-bottom:6px; color:#262626; font-size:14px; }
.required { color:#ff4d4f; }
.form-group input,
.form-group select,
.form-group textarea { padding:8px 12px; border:1px solid #d9d9d9; border-radius:6px; font-size:14px; transition:border-color .2s; }
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus { outline:none; border-color:#1890ff; box-shadow:0 0 0 2px rgba(24,144,255,0.2); }
.form-group input:disabled,
.form-group select:disabled,
.form-group textarea:disabled {
  color: #262626;
  background-color: #f5f5f5;
  -webkit-text-fill-color: #262626;
  opacity: 1;
  cursor: default;
}
.form-error { color:#ff4d4f; font-size:13px; }
.form-actions { display:flex; justify-content:flex-end; gap:12px; margin:0; padding:0; }
.btn-primary { background:#1890ff; color:white; border:none; }
.btn-primary:hover:not(:disabled) { background:#40a9ff; }
.btn-primary:disabled { background:#d9d9d9; cursor:not-allowed; }
.btn-secondary { background:#f5f5f5; color:#666; border:1px solid #d9d9d9; }
.btn-secondary:hover { background:#e6f7ff; border-color:#1890ff; color:#1890ff; }

/* 附件上传统一样式 */
.attachments-section .selected-files { color:#666; font-size:12px; }
.attachments-section .hint { color:#8c8c8c; font-size:12px; }
.upload-card { border:1px solid #e5e7eb; border-radius:12px; background:#fff; padding:12px; }
.upload-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.selected-list { display:flex; flex-direction:column; gap:6px; }
.selected-file-row { display:flex; align-items:center; justify-content:space-between; padding:6px 8px; border:1px dashed #e5e7eb; border-radius:8px; background:#fafafa; }
.selected-file-row .file-name { max-width: calc(100% - 36px); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.mini-icon { width:24px; height:24px; border:1px solid #e5e7eb; border-radius:6px; background:#fff; display:flex; align-items:center; justify-content:center; cursor:pointer; }
.mini-icon.danger { border-color:#ef4444; background:#fee2e2; }
.mini-icon svg { width:16px; height:16px; fill:#000; }
.upload-head .hint { color:#666; font-size:12px; }
.select-btn { border-color:#1677ff; background:#1677ff; color:#fff; }
.select-btn:hover { background:#0f5fd6; border-color:#0f5fd6; }
.hidden-file { display:none; }
.upload-body { display:flex; flex-direction:column; gap:8px; }
.uploaded-list { display:flex; flex-direction:column; gap:8px; padding:6px 0; }
.file-row { display:flex; align-items:center; justify-content:space-between; padding:6px 8px; border:1px dashed #e5e7eb; border-radius:8px; }
.file-meta { display:flex; align-items:center; gap:12px; }
.file-link { color:#2563eb; text-decoration: none; font-weight:500; cursor: pointer; }
.file-link:hover { text-decoration: underline; }
.file-size { color:#6b7280; font-size:12px; }
.mini-icon { width:28px; height:28px; border:1px solid #e5e7eb; border-radius:6px; background:#fff; display:inline-flex; align-items:center; justify-content:center; cursor:pointer; transition:all .15s ease; }
.mini-icon svg { width:18px; height:18px; fill:#000; }
.mini-icon.danger { border-color:#ef4444; background:#fee2e2; }
.mini-icon.danger:hover { background:#fecaca; border-color:#ef4444; }
.progress { position: relative; height: 6px; background: #f0f0f0; border-radius: 4px; }
.progress .bar { height: 100%; background: #409eff; border-radius: 4px; width: 0%; transition: width .2s ease; }
.progress .percent { position: absolute; top: -18px; right: 0; font-size: 12px; color: #666; }

@media (max-width: 768px) {
  .modal-content { width:95%; margin:20px; }
  .form-grid { grid-template-columns: 1fr; }
}

/* 预览弹窗样式 */
.preview-modal { width: 60vw; max-width: 800px; height: 60vh; display: flex; flex-direction: column; }
.preview-modal.large-modal { width: 90vw; max-width: 1200px; height: 90vh; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.preview-body { flex: 1; overflow: hidden; position: relative; padding: 0; background: #f5f5f5; display: flex; flex-direction: column; }
.loading-container, .error-container, .unknown-preview { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #666; }
.loading-spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin-bottom: 16px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
/* 预览弹窗样式 (全屏覆盖) */
.preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 3000; display: flex; flex-direction: column; }
.preview-header { height: 48px; display: flex; align-items: center; justify-content: space-between; padding: 0 12px; background: #111827; color: #fff; }
.preview-header .title { font-size: 14px; font-weight: 600; color: #fff; }
.preview-header .tools { display: flex; align-items: center; gap: 8px; }
.preview-body { flex: 1; background: #0f172a; color: #fff; overflow: auto; padding: 12px; }
.preview-content { display: inline-block; transform-origin: center top; }
.preview-image { max-width: 100%; height: auto; display: block; }
.pdf-embed { width: 100%; height: 100%; border: none; background: #0f172a; }
.video-player { width: 100%; height: 100%; background: #000; }
.html-view { background: #fff; color: #111; padding: 12px; border-radius: 6px; max-width: 100%; }
.text-view { background: #111827; color: #e5e7eb; padding: 12px; border-radius: 6px; max-width: 100%; white-space: pre-wrap; }
.loading { color: #e5e7eb; }
.error { color: #fecaca; }
.unsupported { color: #e5e7eb; }
.status-done { color: #52c41a; font-weight: 500; background: #f6ffed; border: 1px solid #b7eb8f; padding: 2px 10px; border-radius: 4px; font-size: 12px; display: inline-block; }
.status-pending { color: #faad14; font-weight: 500; background: #fffbe6; border: 1px solid #ffe58f; padding: 2px 10px; border-radius: 4px; font-size: 12px; display: inline-block; }
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
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  pointer-events: none;
}
</style>
