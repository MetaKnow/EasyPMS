package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 类级注释：
 * 合同外需求附件实体类
 * 对应数据库表：extra_requirement_deliverableFiles
 */
@Entity
@Table(name = "extra_requirement_deliverableFiles")
public class ExtraRequirementDeliverableFile {

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
    @Column(name = "filePath", length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "fileSize")
    private Long fileSize;

    /**
     * 上传人
     */
    @Column(name = "uploadUser", length = 100)
    private String uploadUser;

    /**
     * 项目ID（外键 constructing_project.projectId）
     */
    @Column(name = "projectId")
    private Long projectId;

    /**
     * 合同外需求ID（外键 extra_requirement.requirementId）
     */
    @Column(name = "requirementId")
    private Long requirementId;

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

    public ExtraRequirementDeliverableFile() {}

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getUploadUser() { return uploadUser; }
    public void setUploadUser(String uploadUser) { this.uploadUser = uploadUser; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }

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
