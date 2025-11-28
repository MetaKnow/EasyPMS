package com.missoft.pms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维项目数据传输对象
 */
public class AfterServiceProjectDTO {

    private Long projectId;
    private String projectName;
    private Long customerId;
    private String customerName;
    private String arcSystem;
    private Long saleDirector;
    private String saleDirectorName;
    private Integer serviceYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String serviceState;
    private BigDecimal totalHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getSaleDirectorName() {
        return saleDirectorName;
    }

    public void setSaleDirectorName(String saleDirectorName) {
        this.saleDirectorName = saleDirectorName;
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
