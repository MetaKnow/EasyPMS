package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 运维项目参与人员实体类
 * 对应数据库表：afterservice_project_participants
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "afterservice_project_participants")
public class AfterServiceProjectParticipant {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 运维项目ID（外键）
     */
    @Column(name = "projectId", nullable = false)
    private Long projectId;

    /**
     * 用户ID（外键）
     */
    @Column(name = "userId", nullable = false)
    private Long userId;

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

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
