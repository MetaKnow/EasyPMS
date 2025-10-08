package com.missoft.pms.dto;

import java.time.LocalDateTime;

/**
 * 标准交付物数据传输对象
 * 用于前后端数据传输
 */
public class StandardDeliverableDTO {

    /**
     * 交付物ID
     */
    private Long deliverableId;

    /**
     * 交付物名称
     */
    private String deliverableName;

    /**
     * 系统名称（产品名称）
     */
    private String systemName;

    /**
     * 交付物类型
     * 值域：步骤交付物、里程碑交付物
     */
    private String deliverableType;

    /**
     * 是否必须上传
     */
    private Boolean isMustLoad;

    /**
     * 标准步骤ID
     */
    private Long sstepId;

    /**
     * 标准步骤名称（关联查询字段）
     */
    private String sstepName;

    /**
     * 里程碑ID
     */
    private Long milestoneId;

    /**
     * 里程碑名称（关联查询字段）
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

    /**
     * 默认构造函数
     */
    public StandardDeliverableDTO() {
    }

    /**
     * 带参数的构造函数
     *
     * @param deliverableId   交付物ID
     * @param deliverableName 交付物名称
     * @param systemName      系统名称
     * @param deliverableType 交付物类型
     */
    public StandardDeliverableDTO(Long deliverableId, String deliverableName, String systemName, String deliverableType) {
        this.deliverableId = deliverableId;
        this.deliverableName = deliverableName;
        this.systemName = systemName;
        this.deliverableType = deliverableType;
    }

    /**
     * 获取交付物ID
     *
     * @return 交付物ID
     */
    public Long getDeliverableId() {
        return deliverableId;
    }

    /**
     * 设置交付物ID
     *
     * @param deliverableId 交付物ID
     */
    public void setDeliverableId(Long deliverableId) {
        this.deliverableId = deliverableId;
    }

    /**
     * 获取交付物名称
     *
     * @return 交付物名称
     */
    public String getDeliverableName() {
        return deliverableName;
    }

    /**
     * 设置交付物名称
     *
     * @param deliverableName 交付物名称
     */
    public void setDeliverableName(String deliverableName) {
        this.deliverableName = deliverableName;
    }

    /**
     * 获取系统名称
     *
     * @return 系统名称
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * 设置系统名称
     *
     * @param systemName 系统名称
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * 获取交付物类型
     *
     * @return 交付物类型
     */
    public String getDeliverableType() {
        return deliverableType;
    }

    /**
     * 设置交付物类型
     *
     * @param deliverableType 交付物类型
     */
    public void setDeliverableType(String deliverableType) {
        this.deliverableType = deliverableType;
    }

    /**
     * 获取是否必须上传
     *
     * @return 是否必须上传
     */
    public Boolean getIsMustLoad() {
        return isMustLoad;
    }

    /**
     * 设置是否必须上传
     *
     * @param isMustLoad 是否必须上传
     */
    public void setIsMustLoad(Boolean isMustLoad) {
        this.isMustLoad = isMustLoad;
    }

    /**
     * 获取标准步骤ID
     *
     * @return 标准步骤ID
     */
    public Long getSstepId() {
        return sstepId;
    }

    /**
     * 设置标准步骤ID
     *
     * @param sstepId 标准步骤ID
     */
    public void setSstepId(Long sstepId) {
        this.sstepId = sstepId;
    }

    /**
     * 获取标准步骤名称
     *
     * @return 标准步骤名称
     */
    public String getSstepName() {
        return sstepName;
    }

    /**
     * 设置标准步骤名称
     *
     * @param sstepName 标准步骤名称
     */
    public void setSstepName(String sstepName) {
        this.sstepName = sstepName;
    }

    /**
     * 获取里程碑ID
     *
     * @return 里程碑ID
     */
    public Long getMilestoneId() {
        return milestoneId;
    }

    /**
     * 设置里程碑ID
     *
     * @param milestoneId 里程碑ID
     */
    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    /**
     * 获取里程碑名称
     *
     * @return 里程碑名称
     */
    public String getMilestoneName() {
        return milestoneName;
    }

    /**
     * 设置里程碑名称
     *
     * @param milestoneName 里程碑名称
     */
    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "StandardDeliverableDTO{" +
                "deliverableId=" + deliverableId +
                ", deliverableName='" + deliverableName + '\'' +
                ", systemName='" + systemName + '\'' +
                ", deliverableType='" + deliverableType + '\'' +
                ", isMustLoad=" + isMustLoad +
                ", sstepId=" + sstepId +
                ", sstepName='" + sstepName + '\'' +
                ", milestoneId=" + milestoneId +
                ", milestoneName='" + milestoneName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}