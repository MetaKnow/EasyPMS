package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "constructing_project_modifyRecord")
public class ConstructingProjectModifyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recordId")
    private Long recordId;

    @Column(name = "modifyUser", nullable = false)
    private Long modifyUser;

    @Column(name = "modifyDate", nullable = false)
    private LocalDate modifyDate;

    @Column(name = "modifyAction", nullable = false, length = 50)
    private String modifyAction;

    @Column(name = "modifyDescription", length = 2000)
    private String modifyDescription;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Column(name = "createUser", updatable = false)
    private Long createUser;

    /**
     * 更新人ID
     */
    @Column(name = "updateUser")
    private Long updateUser;

    public Long getRecordId() { return recordId; }

    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getModifyUser() { return modifyUser; }

    public void setModifyUser(Long modifyUser) { this.modifyUser = modifyUser; }

    public LocalDate getModifyDate() { return modifyDate; }

    public void setModifyDate(LocalDate modifyDate) { this.modifyDate = modifyDate; }

    public String getModifyAction() { return modifyAction; }

    public void setModifyAction(String modifyAction) { this.modifyAction = modifyAction; }

    public String getModifyDescription() { return modifyDescription; }

    public void setModifyDescription(String modifyDescription) { this.modifyDescription = modifyDescription; }

    public Long getProjectId() { return projectId; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public LocalDateTime getCreateTime() { return createTime; }

    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }

    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createTime == null) this.createTime = now;
        this.updateTime = now;
        if (this.modifyDate == null) this.modifyDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
