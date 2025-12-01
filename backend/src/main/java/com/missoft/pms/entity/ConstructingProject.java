package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 在建项目实体类
 * 对应数据库表：constructing_project
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "constructing_project")
public class ConstructingProject {

    /**
     * 项目ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Long projectId;

    /**
     * 项目编号（唯一、非空）
     */
    @Column(name = "projectNum", nullable = false, unique = true, length = 50)
    private String projectNum;

    /**
     * 年度（非空）
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * 项目名称（非空）
     */
    @Column(name = "projectName", nullable = false, length = 200)
    private String projectName;

    /**
     * 项目类型（非空）
     */
    @Column(name = "projectType", nullable = false, length = 50)
    private String projectType;

    /**
     * 项目负责人ID（外键）
     */
    @Column(name = "projectLeader")
    private Long projectLeader;

    /**
     * 商务负责人ID（外键）
     */
    @Column(name = "saleLeader")
    private Long saleLeader;

    /**
     * 客户ID（外键）
     */
    @Column(name = "customerId")
    private Long customerId;

    /**
     * 项目状态（默认：待开始）
     */
    @Column(name = "projectState", nullable = false, length = 50)
    private String projectState = "待开始";

    /**
     * 档案软件ID（外键）
     */
    @Column(name = "softId")
    private Long softId;

    /**
     * 开始日期
     */
    @Column(name = "startDate")
    private LocalDate startDate;

    /**
     * 计划结束日期
     */
    @Column(name = "planEndDate")
    private LocalDate planEndDate;

    /**
     * 实际结束日期
     */
    @Column(name = "actualEndDate")
    private LocalDate actualEndDate;

    /**
     * 项目预计工期（天）
     */
    @Column(name = "planPeriod")
    private Integer planPeriod;

    /**
     * 项目实际工期（天）
     */
    @Column(name = "actualPeriod")
    private Integer actualPeriod;

    /**
     * 是否渠道项目（0-否，1-是）
     */
    @Column(name = "isAgent", nullable = false)
    private Integer isAgent = 0;

    /**
     * 渠道ID（外键）
     */
    @Column(name = "channelId")
    private Long channelId;

    /**
     * 项目金额
     */
    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;

    /**
     * 已回款金额
     */
    @Column(name = "receivedMoney", nullable = false, precision = 15, scale = 2)
    private BigDecimal receivedMoney = BigDecimal.ZERO;

    /**
     * 未回款金额
     */
    @Column(name = "unreceiveMoney", nullable = false, precision = 15, scale = 2)
    private BigDecimal unreceiveMoney = BigDecimal.ZERO;

    /**
     * 项目验收日期
     */
    @Column(name = "acceptanceDate")
    private LocalDate acceptanceDate;

    /**
     * 项目建设内容（用/分隔多个选项）
     */
    @Column(name = "constructContent", length = 100)
    private String constructContent;

    /**
     * 是否移交运维（0-未移交，1-已移交）
     */
    @Column(name = "isCommitAfterSale", nullable = false)
    private Boolean isCommitAfterSale = false;

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
    public ConstructingProject() {
    }

    /**
     * 带参数的构造函数
     */
    public ConstructingProject(String projectNum, Integer year, String projectName, String projectType) {
        this.projectNum = projectNum;
        this.year = year;
        this.projectName = projectName;
        this.projectType = projectType;
    }

    /**
     * 在持久化之前设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 在更新之前设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getter 和 Setter 方法

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public Long getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(Long projectLeader) {
        this.projectLeader = projectLeader;
    }

    public Long getSaleLeader() {
        return saleLeader;
    }

    public void setSaleLeader(Long saleLeader) {
        this.saleLeader = saleLeader;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getProjectState() {
        return projectState;
    }

    public void setProjectState(String projectState) {
        this.projectState = projectState;
    }

    public Long getSoftId() {
        return softId;
    }

    public void setSoftId(Long softId) {
        this.softId = softId;
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

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Integer getPlanPeriod() {
        return planPeriod;
    }

    public void setPlanPeriod(Integer planPeriod) {
        this.planPeriod = planPeriod;
    }

    public Integer getActualPeriod() {
        return actualPeriod;
    }

    public void setActualPeriod(Integer actualPeriod) {
        this.actualPeriod = actualPeriod;
    }

    public Integer getIsAgent() {
        return isAgent;
    }

    public void setIsAgent(Integer isAgent) {
        this.isAgent = isAgent;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getReceivedMoney() {
        return receivedMoney;
    }

    public void setReceivedMoney(BigDecimal receivedMoney) {
        this.receivedMoney = receivedMoney;
    }

    public BigDecimal getUnreceiveMoney() {
        return unreceiveMoney;
    }

    public void setUnreceiveMoney(BigDecimal unreceiveMoney) {
        this.unreceiveMoney = unreceiveMoney;
    }

    public LocalDate getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(LocalDate acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public String getConstructContent() {
        return constructContent;
    }

    public void setConstructContent(String constructContent) {
        this.constructContent = constructContent;
    }

    public Boolean getIsCommitAfterSale() {
        return isCommitAfterSale;
    }

    public void setIsCommitAfterSale(Boolean isCommitAfterSale) {
        this.isCommitAfterSale = isCommitAfterSale;
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

    @Override
    public String toString() {
        return "ConstructingProject{" +
                "projectId=" + projectId +
                ", projectNum='" + projectNum + '\'' +
                ", year=" + year +
                ", projectName='" + projectName + '\'' +
                ", projectType='" + projectType + '\'' +
                ", projectState='" + projectState + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstructingProject that = (ConstructingProject) o;

        return projectId != null ? projectId.equals(that.projectId) : that.projectId == null;
    }

    @Override
    public int hashCode() {
        return projectId != null ? projectId.hashCode() : 0;
    }
}