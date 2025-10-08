package com.missoft.pms.dto;

import java.time.LocalDateTime;

/**
 * 标准交付物数据传输对象（包含关联名称）
 * 用于前端显示，包含里程碑名称和步骤名称
 */
public class StandardDeliverableWithNamesDTO {
    
    /**
     * 交付物ID
     */
    private Long deliverableId;
    
    /**
     * 交付物名称
     */
    private String deliverableName;
    
    /**
     * 系统名称
     */
    private String systemName;
    
    /**
     * 交付物类型
     */
    private String deliverableType;
    
    /**
     * 是否必须加载
     */
    private Boolean isMustLoad;
    
    /**
     * 标准步骤ID
     */
    private Long sstepId;
    
    /**
     * 里程碑ID
     */
    private Long milestoneId;
    
    /**
     * 步骤名称
     */
    private String sstepName;
    
    /**
     * 里程碑名称
     */
    private String milestoneName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // 构造函数
    public StandardDeliverableWithNamesDTO() {}

    public StandardDeliverableWithNamesDTO(Long deliverableId, String deliverableName, String systemName, 
                                         String deliverableType, Boolean isMustLoad, Long sstepId, 
                                         Long milestoneId, String sstepName, String milestoneName,
                                         LocalDateTime createTime, LocalDateTime updateTime) {
        this.deliverableId = deliverableId;
        this.deliverableName = deliverableName;
        this.systemName = systemName;
        this.deliverableType = deliverableType;
        this.isMustLoad = isMustLoad;
        this.sstepId = sstepId;
        this.milestoneId = milestoneId;
        this.sstepName = sstepName;
        this.milestoneName = milestoneName;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getter和Setter方法
    public Long getDeliverableId() {
        return deliverableId;
    }

    public void setDeliverableId(Long deliverableId) {
        this.deliverableId = deliverableId;
    }

    public String getDeliverableName() {
        return deliverableName;
    }

    public void setDeliverableName(String deliverableName) {
        this.deliverableName = deliverableName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDeliverableType() {
        return deliverableType;
    }

    public void setDeliverableType(String deliverableType) {
        this.deliverableType = deliverableType;
    }

    public Boolean getIsMustLoad() {
        return isMustLoad;
    }

    public void setIsMustLoad(Boolean isMustLoad) {
        this.isMustLoad = isMustLoad;
    }

    public Long getSstepId() {
        return sstepId;
    }

    public void setSstepId(Long sstepId) {
        this.sstepId = sstepId;
    }

    public Long getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getSstepName() {
        return sstepName;
    }

    public void setSstepName(String sstepName) {
        this.sstepName = sstepName;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
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