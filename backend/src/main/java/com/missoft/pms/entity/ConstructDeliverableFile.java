package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目交付物文件实体类
 * 对应数据库表：construct_deliverableFiles
 *
 * 字段说明：
 * - fileId: 主键ID。
 * - filePath: 文件相对路径，存储于项目根目录下的 deliverableFiles 子目录。
 * - fileSize: 文件大小（字节）。
 * - uploaderId: 上传人用户ID（外键 user.userId）。
 * - deliverableId: 标准交付物ID（外键 standard_deliverable.deliverableId）。
 * - projectId: 项目ID（外键 constructing_project.projectId）。
 * - milestoneId: 标准里程碑ID（外键 construct_milestone.milestoneId）。
 * - projectStepId: 项目-标准步骤关系ID（外键 project_sstep_relations.relationId）。
 * - nstepId: 非标准步骤ID（外键 nonstandard_construct_step.nstepId）。
 * - createTime/updateTime: 记录创建与更新时间，由 JPA 生命周期回调维护。
 */
@Entity
@Table(name = "construct_deliverableFiles")
public class ConstructDeliverableFile {

    /**
     * 文件ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    /**
     * 文件相对路径
     */
    @Column(name = "filePath", length = 255)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "fileSize")
    private Long fileSize;

    /**
     * 上传人ID（外键 user.userId）
     */
    @Column(name = "uploadUser")
    private Long uploaderId;

    /**
     * 标准交付物ID（外键 standard_deliverable.deliverableId）
     */
    @Column(name = "deliverableId")
    private Long deliverableId;

    /**
     * 项目ID（外键 constructing_project.projectId）
     */
    @Column(name = "projectId")
    private Long projectId;

    /**
     * 标准里程碑ID（外键 construct_milestone.milestoneId）
     */
    @Column(name = "milestoneId")
    private Long milestoneId;

    /**
     * 项目步骤关系ID（外键 project_sstep_relations.relationId）
     */
    @Column(name = "projectStepId")
    private Long projectStepId;

    /**
     * 非标准步骤ID（外键 nonstandard_construct_step.nstepId）
     */
    @Column(name = "nstepId")
    private Long nstepId;

    /**
     * 创建时间
     */
    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public ConstructDeliverableFile() {}

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public Long getDeliverableId() { return deliverableId; }
    public void setDeliverableId(Long deliverableId) { this.deliverableId = deliverableId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    /**
     * 获取标准里程碑ID
     * @return 里程碑ID（construct_milestone.milestoneId）
     */
    public Long getMilestoneId() { return milestoneId; }

    /**
     * 设置标准里程碑ID
     * @param milestoneId 里程碑ID
     */
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }

    /**
     * 获取项目步骤关系ID
     * @return 项目-标准步骤关系主键ID
     */
    public Long getProjectStepId() { return projectStepId; }
    /**
     * 设置项目步骤关系ID
     * @param projectStepId 项目-标准步骤关系主键ID
     */
    public void setProjectStepId(Long projectStepId) { this.projectStepId = projectStepId; }

    public Long getNstepId() { return nstepId; }
    public void setNstepId(Long nstepId) { this.nstepId = nstepId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}