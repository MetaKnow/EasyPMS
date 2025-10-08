package com.missoft.pms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 标准里程碑实体类
 * 对应数据库中的standard_milestone表
 */
@Entity
@Table(name = "standard_milestone")
public class StandardMilestone {

    /**
     * 里程碑ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id")
    private Long milestoneId;

    /**
     * 里程碑名称
     */
    @NotBlank(message = "里程碑名称不能为空")
    @Size(min = 2, max = 100, message = "里程碑名称长度必须在2-100个字符之间")
    @Column(name = "milestone_name", nullable = false, length = 100)
    private String milestoneName;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 默认构造函数
     */
    public StandardMilestone() {
    }

    /**
     * 带参数的构造函数
     *
     * @param milestoneName 里程碑名称
     */
    public StandardMilestone(String milestoneName) {
        this.milestoneName = milestoneName;
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
        return "StandardMilestone{" +
                "milestoneId=" + milestoneId +
                ", milestoneName='" + milestoneName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandardMilestone that = (StandardMilestone) o;

        return milestoneId != null ? milestoneId.equals(that.milestoneId) : that.milestoneId == null;
    }

    @Override
    public int hashCode() {
        return milestoneId != null ? milestoneId.hashCode() : 0;
    }
}