package com.missoft.pms.dto;

import java.time.LocalDate;

/**
 * 项目移交请求 DTO
 */
public class HandoverRequest {
    /**
     * 在建项目ID
     */
    private Long constructingProjectId;

    /**
     * 运维状态
     */
    private String serviceState;

    /**
     * 运维类型
     */
    private String serviceType;

    /**
     * 运维负责人ID
     */
    private Long serviceDirector;

    /**
     * 运维年数
     */
    private Integer serviceYear;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    public Long getConstructingProjectId() {
        return constructingProjectId;
    }

    public void setConstructingProjectId(Long constructingProjectId) {
        this.constructingProjectId = constructingProjectId;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Long getServiceDirector() {
        return serviceDirector;
    }

    public void setServiceDirector(Long serviceDirector) {
        this.serviceDirector = serviceDirector;
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
}
