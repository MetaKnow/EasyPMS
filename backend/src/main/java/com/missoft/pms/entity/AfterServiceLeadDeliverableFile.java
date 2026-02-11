package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "afterService_leads_deliverableFiles")
public class AfterServiceLeadDeliverableFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    @Column(name = "filePath", length = 500)
    private String filePath;

    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "uploadUser")
    private Long uploadUser;

    @Column(name = "afterServiceProjectId")
    private Long afterServiceProjectId;

    @Column(name = "leadsId")
    private Long leadsId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Long getUploadUser() { return uploadUser; }
    public void setUploadUser(Long uploadUser) { this.uploadUser = uploadUser; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }
    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public Long getLeadsId() { return leadsId; }
    public void setLeadsId(Long leadsId) { this.leadsId = leadsId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
