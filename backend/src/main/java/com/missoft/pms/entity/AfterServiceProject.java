package com.missoft.pms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维项目实体类
 */
@Entity
@Table(name = "afterService_project")
public class AfterServiceProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Long projectId;

    @Column(name = "projectName", nullable = false, length = 200)
    private String projectName;

    @Column(name = "customerId")
    private Long customerId;

    @Column(name = "arcSystem", nullable = false, length = 200)
    private String arcSystem;

    @Column(name = "saleDirector", nullable = false)
    private Long saleDirector;

    @Column(name = "serviceYear")
    private Integer serviceYear = 1;

    @Column(name = "startDate")
    private LocalDate startDate;

    @Column(name = "endDate")
    private LocalDate endDate;

    @Column(name = "serviceState", nullable = false, length = 50)
    private String serviceState;

    @Column(name = "totalHours", precision = 10, scale = 1)
    private BigDecimal totalHours;

    @CreationTimestamp
    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    // Getters and Setters

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getArcSystem() {
        return arcSystem;
    }

    public void setArcSystem(String arcSystem) {
        this.arcSystem = arcSystem;
    }

    public Long getSaleDirector() {
        return saleDirector;
    }

    public void setSaleDirector(Long saleDirector) {
        this.saleDirector = saleDirector;
    }

    public Integer getServiceYear() {
        return serviceYear;
    }

    public void setServiceYear(Integer serviceYear) {
        this.serviceYear = serviceYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
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
