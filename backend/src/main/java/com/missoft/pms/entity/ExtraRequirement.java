package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "extra_requirement")
public class ExtraRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requirementId")
    private Long requirementId;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "requirementName", nullable = false, length = 200)
    private String requirementName;

    @Column(name = "requirementType", length = 20)
    private String requirementType;

    @Column(name = "isPay")
    private Boolean isPay;

    @Column(name = "payAmount", precision = 12, scale = 2)
    private BigDecimal payAmount;

    @Column(name = "isDeliver")
    private Boolean isDeliver;

    @Column(name = "isComplete")
    private Boolean isComplete;

    @Column(name = "isProductization")
    private Boolean isProductization;

    @Column(name = "workload", precision = 10, scale = 2)
    private BigDecimal workload;

    @Column(name = "developer")
    private Long developer;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    @Transient
    private Long modifyUser;

    @Transient
    private Boolean hasFiles;

    public Long getRequirementId() { return requirementId; }

    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }

    public Long getProjectId() { return projectId; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getRequirementName() { return requirementName; }

    public void setRequirementName(String requirementName) { this.requirementName = requirementName; }

    public String getRequirementType() { return requirementType; }

    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }

    public Boolean getIsPay() { return isPay; }

    public void setIsPay(Boolean isPay) { this.isPay = isPay; }

    public BigDecimal getPayAmount() { return payAmount; }

    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public Boolean getIsDeliver() { return isDeliver; }

    public void setIsDeliver(Boolean isDeliver) { this.isDeliver = isDeliver; }

    public Boolean getIsComplete() { return isComplete; }

    public void setIsComplete(Boolean isComplete) { this.isComplete = isComplete; }

    public Boolean getIsProductization() { return isProductization; }

    public void setIsProductization(Boolean isProductization) { this.isProductization = isProductization; }

    public BigDecimal getWorkload() { return workload; }

    public void setWorkload(BigDecimal workload) { this.workload = workload; }

    public Long getDeveloper() { return developer; }

    public void setDeveloper(Long developer) { this.developer = developer; }

    public LocalDateTime getCreateTime() { return createTime; }

    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }

    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Long getModifyUser() { return modifyUser; }

    public void setModifyUser(Long modifyUser) { this.modifyUser = modifyUser; }

    public Boolean getHasFiles() { return hasFiles; }
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
