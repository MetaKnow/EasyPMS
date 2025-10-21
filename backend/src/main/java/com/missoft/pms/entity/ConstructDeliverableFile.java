package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目交付物文件实体类
 * 对应数据库表：construct_deliverableFiles
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
     * 标准步骤ID（外键 standard_construct_step.sstepId）
     */
    @Column(name = "sstepId")
    private Long sstepId;

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

    public Long getSstepId() { return sstepId; }
    public void setSstepId(Long sstepId) { this.sstepId = sstepId; }

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