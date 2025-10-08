package com.missoft.pms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 在建项目数据传输对象
 * 包含客户名称等关联信息
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class ConstructingProjectDTO {
    
    private Long projectId;
    private String projectNum;
    private String projectName;
    private String projectType;
    private String projectState;
    private Long projectLeader;
    private String projectLeaderName;
    private LocalDate startDate;
    private LocalDate planEndDate;
    private BigDecimal value;
    private Integer year;
    private Long customerId;
    private String customerName;  // 客户名称

    // 构造函数
    public ConstructingProjectDTO() {}

    public ConstructingProjectDTO(Long projectId, String projectNum, String projectName, 
                                String projectType, String projectState, Long projectLeader, 
                                String projectLeaderName, LocalDate startDate, LocalDate planEndDate, 
                                BigDecimal value, Integer year, Long customerId, String customerName) {
        this.projectId = projectId;
        this.projectNum = projectNum;
        this.projectName = projectName;
        this.projectType = projectType;
        this.projectState = projectState;
        this.projectLeader = projectLeader;
        this.projectLeaderName = projectLeaderName;
        this.startDate = startDate;
        this.planEndDate = planEndDate;
        this.value = value;
        this.year = year;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    // Getter和Setter方法
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectNum() {
        return projectNum;
    }

    public void setProjectNum(String projectNum) {
        this.projectNum = projectNum;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectState() {
        return projectState;
    }

    public void setProjectState(String projectState) {
        this.projectState = projectState;
    }

    public Long getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(Long projectLeader) {
        this.projectLeader = projectLeader;
    }

    public String getProjectLeaderName() {
        return projectLeaderName;
    }

    public void setProjectLeaderName(String projectLeaderName) {
        this.projectLeaderName = projectLeaderName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(LocalDate planEndDate) {
        this.planEndDate = planEndDate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
}