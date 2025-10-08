package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 标准交付步骤实体类
 * 对应数据库表：standard_construct_step
 */
@Entity
@Table(name = "standard_construct_step")
public class StandardConstructStep {

    /**
     * 标准步骤ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sstepId")
    private Long sstepId;

    /**
     * 标准步骤名，必填
     */
    @Column(name = "sstepName", nullable = false, length = 200)
    private String sstepName;

    /**
     * 步骤类型
     * 值域：标准产品、接口开发、数据迁移、个性化功能开发、用户培训、系统上线试运行
     */
    @Column(name = "type", length = 50)
    private String type;

    /**
     * 档案系统名称（产品名称）
     */
    @Column(name = "systemName", length = 100)
    private String systemName;

    /**
     * 标准里程碑ID（外键）
     */
    @Column(name = "smilestoneId")
    private Long smilestoneId;

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
    public StandardConstructStep() {
    }

    /**
     * 带参数的构造函数
     *
     * @param sstepName  步骤名称
     * @param type       步骤类型
     * @param systemName 档案系统名称
     */
    public StandardConstructStep(String sstepName, String type, String systemName) {
        this.sstepName = sstepName;
        this.type = type;
        this.systemName = systemName;
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
     * 获取标准步骤名
     *
     * @return 标准步骤名
     */
    public String getSstepName() {
        return sstepName;
    }

    /**
     * 设置标准步骤名
     *
     * @param sstepName 标准步骤名
     */
    public void setSstepName(String sstepName) {
        this.sstepName = sstepName;
    }

    /**
     * 获取步骤类型
     *
     * @return 步骤类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置步骤类型
     *
     * @param type 步骤类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取档案系统名称
     *
     * @return 档案系统名称
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * 设置档案系统名称
     *
     * @param systemName 档案系统名称
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * 获取标准里程碑ID
     *
     * @return 标准里程碑ID
     */
    public Long getSmilestoneId() {
        return smilestoneId;
    }

    /**
     * 设置标准里程碑ID
     *
     * @param smilestoneId 标准里程碑ID
     */
    public void setSmilestoneId(Long smilestoneId) {
        this.smilestoneId = smilestoneId;
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

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "StandardConstructStep{" +
                "sstepId=" + sstepId +
                ", sstepName='" + sstepName + '\'' +
                ", type='" + type + '\'' +
                ", systemName='" + systemName + '\'' +
                ", smilestoneId=" + smilestoneId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandardConstructStep that = (StandardConstructStep) o;

        return sstepId != null ? sstepId.equals(that.sstepId) : that.sstepId == null;
    }

    @Override
    public int hashCode() {
        return sstepId != null ? sstepId.hashCode() : 0;
    }
}