package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_follow_up_deliverableFiles")
public class CustomerFollowUpDeliverableFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    @Column(name = "filePath", length = 500)
    private String filePath;

    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "uploadUser", length = 100)
    private String uploadUser;

    @Column(name = "afterServiceProjectId")
    private Long afterServiceProjectId;

    @Column(name = "followUpRecordId")
    private Long followUpRecordId;

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

    public String getUploadUser() { return uploadUser; }
    public void setUploadUser(String uploadUser) { this.uploadUser = uploadUser; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }
    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public Long getFollowUpRecordId() { return followUpRecordId; }
    public void setFollowUpRecordId(Long followUpRecordId) { this.followUpRecordId = followUpRecordId; }

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
