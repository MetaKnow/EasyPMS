package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 类级注释：项目风险实体
 */
@Entity
@Table(name = "constructing_project_risk")
public class ConstructingProjectRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "riskId")
    private Long riskId;

    @Column(name = "riskType", length = 50)
    private String riskType;

    @Column(name = "riskLevel", length = 20)
    private String riskLevel;

    @Column(name = "isRelieve")
    private Boolean isRelieve;

    @Column(name = "relieveWay", length = 2000)
    private String relieveWay;

    @Column(name = "riskDescription", length = 2000)
    private String riskDescription;

    @Column(name = "riskEvaluate", length = 2000)
    private String riskEvaluate;

    @Column(name = "creator")
    private Long creator;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    @Transient
    private Boolean hasFiles;

    /**
     * 函数级注释：无参构造
     */
    public ConstructingProjectRisk() {}

    /**
     * 函数级注释：获取风险ID
     */
    public Long getRiskId() { return riskId; }
    /**
     * 函数级注释：设置风险ID
     */
    public void setRiskId(Long riskId) { this.riskId = riskId; }

    /**
     * 函数级注释：获取风险类型
     */
    public String getRiskType() { return riskType; }
    /**
     * 函数级注释：设置风险类型
     */
    public void setRiskType(String riskType) { this.riskType = riskType; }

    /**
     * 函数级注释：获取风险级别
     */
    public String getRiskLevel() { return riskLevel; }
    /**
     * 函数级注释：设置风险级别
     */
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    /**
     * 函数级注释：获取是否解除
     */
    public Boolean getIsRelieve() { return isRelieve; }
    /**
     * 函数级注释：设置是否解除
     */
    public void setIsRelieve(Boolean isRelieve) { this.isRelieve = isRelieve; }

    /**
     * 函数级注释：获取解除方式
     */
    public String getRelieveWay() { return relieveWay; }
    /**
     * 函数级注释：设置解除方式
     */
    public void setRelieveWay(String relieveWay) { this.relieveWay = relieveWay; }

    /**
     * 函数级注释：获取风险描述
     */
    public String getRiskDescription() { return riskDescription; }
    /**
     * 函数级注释：设置风险描述
     */
    public void setRiskDescription(String riskDescription) { this.riskDescription = riskDescription; }

    /**
     * 函数级注释：获取风险评估
     */
    public String getRiskEvaluate() { return riskEvaluate; }
    /**
     * 函数级注释：设置风险评估
     */
    public void setRiskEvaluate(String riskEvaluate) { this.riskEvaluate = riskEvaluate; }

    /**
     * 函数级注释：获取创建人
     */
    public Long getCreator() { return creator; }
    /**
     * 函数级注释：设置创建人
     */
    public void setCreator(Long creator) { this.creator = creator; }

    /**
     * 函数级注释：获取项目ID
     */
    public Long getProjectId() { return projectId; }
    /**
     * 函数级注释：设置项目ID
     */
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    /**
     * 函数级注释：获取创建时间
     */
    public LocalDateTime getCreateTime() { return createTime; }
    /**
     * 函数级注释：设置创建时间
     */
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    /**
     * 函数级注释：获取更新时间
     */
    public LocalDateTime getUpdateTime() { return updateTime; }
    /**
     * 函数级注释：设置更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    /**
     * 函数级注释：获取是否存在附件
     */
    public Boolean getHasFiles() { return hasFiles; }
    /**
     * 函数级注释：设置是否存在附件
     */
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }

    /**
     * 函数级注释：持久化前设置时间字段
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    /**
     * 函数级注释：更新前刷新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
