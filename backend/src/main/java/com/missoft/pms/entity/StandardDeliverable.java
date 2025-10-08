package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 标准交付物实体类
 * 对应数据库表：standard_deliverable
 */
@Entity
@Table(name = "standard_deliverable")
public class StandardDeliverable {

    /**
     * 交付物ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deliverableId")
    private Long deliverableId;

    /**
     * 交付物名称
     */
    @Column(name = "deliverableName", length = 200)
    private String deliverableName;

    /**
     * 系统名称（产品名称）
     */
    @Column(name = "systemName", length = 100)
    private String systemName;

    /**
     * 交付物类型
     * 值域：步骤交付物、里程碑交付物
     */
    @Column(name = "deliverableType", length = 50)
    private String deliverableType;

    /**
     * 是否必须上传，0-非必须，1-必须
     */
    @Column(name = "isMustLoad")
    private Boolean isMustLoad;

    /**
     * 标准步骤ID（外键）
     */
    @Column(name = "sstepId")
    private Long sstepId;

    /**
     * 里程碑ID（外键）
     */
    @Column(name = "milestoneId")
    private Long milestoneId;

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

    /**
     * 默认构造函数
     */
    public StandardDeliverable() {
    }

    /**
     * 带参数的构造函数
     *
     * @param deliverableName 交付物名称
     * @param systemName      系统名称
     * @param deliverableType 交付物类型
     */
    public StandardDeliverable(String deliverableName, String systemName, String deliverableType) {
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

    /**
     * 在持久化之前设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    /**
     * 在更新之前设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "StandardDeliverable{" +
                "deliverableId=" + deliverableId +
                ", deliverableName='" + deliverableName + '\'' +
                ", systemName='" + systemName + '\'' +
                ", deliverableType='" + deliverableType + '\'' +
                ", isMustLoad=" + isMustLoad +
                ", sstepId=" + sstepId +
                ", milestoneId=" + milestoneId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}