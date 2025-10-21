package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目-标准步骤关系实体类
 * 对应表：project_sstep_relations
 */
@Entity
@Table(name = "project_sstep_relations")
public class ProjectSstepRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationId")
    private Long relationId;

    @Column(name = "projectId")
    private Long projectId;

    @Column(name = "sstepId")
    private Long sstepId;

    @Column(name = "deliverableId")
    private Long deliverableId;

    @Column(name = "director")
    private Long director;

    @Column(name = "planStartDate")
    private LocalDate planStartDate;

    @Column(name = "planEndDate")
    private LocalDate planEndDate;

    @Column(name = "actualStartDate")
    private LocalDate actualStartDate;

    @Column(name = "actualEndDate")
    private LocalDate actualEndDate;

    @Column(name = "planPeriod")
    private Integer planPeriod;

    @Column(name = "actualPeriod")
    private Integer actualPeriod;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public ProjectSstepRelation() {}

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getRelationId() { return relationId; }
    public void setRelationId(Long relationId) { this.relationId = relationId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getSstepId() { return sstepId; }
    public void setSstepId(Long sstepId) { this.sstepId = sstepId; }

    public Long getDeliverableId() { return deliverableId; }
    public void setDeliverableId(Long deliverableId) { this.deliverableId = deliverableId; }

    public Long getDirector() { return director; }
    public void setDirector(Long director) { this.director = director; }

    public LocalDate getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(LocalDate planStartDate) { this.planStartDate = planStartDate; }

    public LocalDate getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(LocalDate planEndDate) { this.planEndDate = planEndDate; }

    public LocalDate getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDate actualStartDate) { this.actualStartDate = actualStartDate; }

    public LocalDate getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }

    public Integer getPlanPeriod() { return planPeriod; }
    public void setPlanPeriod(Integer planPeriod) { this.planPeriod = planPeriod; }

    public Integer getActualPeriod() { return actualPeriod; }
    public void setActualPeriod(Integer actualPeriod) { this.actualPeriod = actualPeriod; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}