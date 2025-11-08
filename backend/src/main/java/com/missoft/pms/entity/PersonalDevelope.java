package com.missoft.pms.entity;

import jakarta.persistence.*;

/**
 * 类级注释：
 * 个性化开发实体，映射表 `personal_develope`。
 * 字段包含：个人开发ID、名称、所属项目ID、所属项目里程碑ID。
 */
@Entity
@Table(name = "personal_develope")
public class PersonalDevelope {

    /**
     * 主键：个性化开发ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personalDevId")
    private Long personalDevId;

    /**
     * 个性化开发名称
     */
    @Column(name = "personalDevName", nullable = false, length = 200)
    private String personalDevName;

    /**
     * 关联项目ID（constructing_project 外键）
     */
    @Column(name = "projectId")
    private Long projectId;

    /**
     * 关联项目里程碑ID（construct_milestone 外键）
     */
    @Column(name = "milestoneId")
    private Long milestoneId;

    /**
     * 函数级注释：获取主键ID
     * @return 个性化开发ID
     */
    public Long getPersonalDevId() { return personalDevId; }

    /**
     * 函数级注释：设置主键ID
     * @param personalDevId 个性化开发ID
     */
    public void setPersonalDevId(Long personalDevId) { this.personalDevId = personalDevId; }

    /**
     * 函数级注释：获取个性化开发名称
     * @return 名称
     */
    public String getPersonalDevName() { return personalDevName; }

    /**
     * 函数级注释：设置个性化开发名称
     * @param personalDevName 名称
     */
    public void setPersonalDevName(String personalDevName) { this.personalDevName = personalDevName; }

    /**
     * 函数级注释：获取关联项目ID
     * @return 项目ID
     */
    public Long getProjectId() { return projectId; }

    /**
     * 函数级注释：设置关联项目ID
     * @param projectId 项目ID
     */
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    /**
     * 函数级注释：获取关联里程碑ID
     * @return 里程碑ID
     */
    public Long getMilestoneId() { return milestoneId; }

    /**
     * 函数级注释：设置关联里程碑ID
     * @param milestoneId 里程碑ID
     */
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }
}