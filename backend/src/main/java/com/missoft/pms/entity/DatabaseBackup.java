package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据库备份记录实体
 * 对应数据库中的databaseBackup表
 */
@Entity
@Table(name = "databaseBackup")
public class DatabaseBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fileName", nullable = false)
    private String fileName;

    @Column(name = "backupDate", nullable = false)
    private LocalDateTime backupDate;

    @Column(name = "backupState", nullable = false)
    private String backupState; // "成功" 或 "失败"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getBackupDate() {
        return backupDate;
    }

    public void setBackupDate(LocalDateTime backupDate) {
        this.backupDate = backupDate;
    }

    public String getBackupState() {
        return backupState;
    }

    public void setBackupState(String backupState) {
        this.backupState = backupState;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
