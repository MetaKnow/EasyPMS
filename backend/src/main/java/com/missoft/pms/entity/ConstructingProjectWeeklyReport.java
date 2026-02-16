package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 类级注释：项目周报实体
 */
@Entity
@Table(name = "constructing_project_weeklyReport")
public class ConstructingProjectWeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weeklyReportId")
    private Long weeklyReportId;

    @Column(name = "period", length = 100)
    private String period;

    @Column(name = "submitUser")
    private Long submitUser;

    @Column(name = "submitDate")
    private LocalDate submitDate;

    @Column(name = "weeklyWorkload")
    private BigDecimal weeklyWorkload;

    @Column(name = "workDifficulties", length = 2000)
    private String workDifficulties;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    @Transient
    private Boolean hasFiles;

    /**
     * 函数级注释：无参构造
     */
    public ConstructingProjectWeeklyReport() {}

    /**
     * 函数级注释：获取周报ID
     */
    public Long getWeeklyReportId() { return weeklyReportId; }
    /**
     * 函数级注释：设置周报ID
     */
    public void setWeeklyReportId(Long weeklyReportId) { this.weeklyReportId = weeklyReportId; }

    /**
     * 函数级注释：获取周期
     */
    public String getPeriod() { return period; }
    /**
     * 函数级注释：设置周期
     */
    public void setPeriod(String period) { this.period = period; }

    /**
     * 函数级注释：获取提交用户
     */
    public Long getSubmitUser() { return submitUser; }
    /**
     * 函数级注释：设置提交用户
     */
    public void setSubmitUser(Long submitUser) { this.submitUser = submitUser; }

    /**
     * 函数级注释：获取提交日期
     */
    public LocalDate getSubmitDate() { return submitDate; }
    /**
     * 函数级注释：设置提交日期
     */
    public void setSubmitDate(LocalDate submitDate) { this.submitDate = submitDate; }

    /**
     * 函数级注释：获取本周工作量
     */
    public BigDecimal getWeeklyWorkload() { return weeklyWorkload; }
    /**
     * 函数级注释：设置本周工作量
     */
    public void setWeeklyWorkload(BigDecimal weeklyWorkload) { this.weeklyWorkload = weeklyWorkload; }

    /**
     * 函数级注释：获取工作难点
     */
    public String getWorkDifficulties() { return workDifficulties; }
    /**
     * 函数级注释：设置工作难点
     */
    public void setWorkDifficulties(String workDifficulties) { this.workDifficulties = workDifficulties; }

    /**
     * 函数级注释：获取项目ID
     */
    public Long getProjectId() { return projectId; }
    /**
     * 函数级注释：设置项目ID
     */
    public void setProjectId(Long projectId) { this.projectId = projectId; }

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
     * 函数级注释：获取是否存在附件
     */
    public Boolean getHasFiles() { return hasFiles; }
    /**
     * 函数级注释：设置是否存在附件
     */
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }

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
