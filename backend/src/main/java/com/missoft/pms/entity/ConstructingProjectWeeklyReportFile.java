package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 类级注释：项目周报附件实体
 */
@Entity
@Table(name = "constructing_project_weeklyReportFiles")
public class ConstructingProjectWeeklyReportFile {

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

    @Column(name = "projectId")
    private Long projectId;

    @Column(name = "weeklyReportId")
    private Long weeklyReportId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 函数级注释：无参构造
     */
    public ConstructingProjectWeeklyReportFile() {}

    /**
     * 函数级注释：获取文件ID
     */
    public Long getFileId() { return fileId; }
    /**
     * 函数级注释：设置文件ID
     */
    public void setFileId(Long fileId) { this.fileId = fileId; }

    /**
     * 函数级注释：获取文件路径
     */
    public String getFilePath() { return filePath; }
    /**
     * 函数级注释：设置文件路径
     */
    public void setFilePath(String filePath) { this.filePath = filePath; }

    /**
     * 函数级注释：获取文件大小
     */
    public Long getFileSize() { return fileSize; }
    /**
     * 函数级注释：设置文件大小
     */
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    /**
     * 函数级注释：获取上传人ID
     */
    public Long getUploadUser() { return uploadUser; }
    /**
     * 函数级注释：设置上传人ID
     */
    public void setUploadUser(Long uploadUser) { this.uploadUser = uploadUser; }

    /**
     * 函数级注释：获取项目ID
     */
    public Long getProjectId() { return projectId; }
    /**
     * 函数级注释：设置项目ID
     */
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    /**
     * 函数级注释：获取周报ID
     */
    public Long getWeeklyReportId() { return weeklyReportId; }
    /**
     * 函数级注释：设置周报ID
     */
    public void setWeeklyReportId(Long weeklyReportId) { this.weeklyReportId = weeklyReportId; }

    /**
     * 函数级注释：获取创建时间
     */
    public LocalDateTime getCreateTime() { return createTime; }
    /**
     * 函数级注释：设置创建时间
     */
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    /**
     * 函数级注释：获取更新时间
     */
    public LocalDateTime getUpdateTime() { return updateTime; }
    /**
     * 函数级注释：设置更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    /**
     * 函数级注释：持久化前设置时间字段
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    /**
     * 函数级注释：更新前刷新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
