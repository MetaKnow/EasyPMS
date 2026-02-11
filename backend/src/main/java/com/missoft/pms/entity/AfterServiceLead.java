package com.missoft.pms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "afterService_leads")
public class AfterServiceLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leadsId")
    private Long leadsId;

    @Column(name = "afterServiceProjectId", nullable = false)
    private Long afterServiceProjectId;

    @Column(name = "isTransform", nullable = false)
    private Boolean isTransform;

    @Column(name = "leadsFinder", nullable = false)
    private Long leadsFinder;

    @Column(name = "leadsSource", nullable = false, length = 50)
    private String leadsSource;

    @Column(name = "leadsDescript")
    private String leadsDescript;

    @CreationTimestamp
    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getLeadsId() { return leadsId; }
    public void setLeadsId(Long leadsId) { this.leadsId = leadsId; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }
    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public Boolean getIsTransform() { return isTransform; }
    public void setIsTransform(Boolean isTransform) { this.isTransform = isTransform; }

    public Long getLeadsFinder() { return leadsFinder; }
    public void setLeadsFinder(Long leadsFinder) { this.leadsFinder = leadsFinder; }

    public String getLeadsSource() { return leadsSource; }
    public void setLeadsSource(String leadsSource) { this.leadsSource = leadsSource; }

    public String getLeadsDescript() { return leadsDescript; }
    public void setLeadsDescript(String leadsDescript) { this.leadsDescript = leadsDescript; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
