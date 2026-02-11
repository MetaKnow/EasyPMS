package com.missoft.pms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_follow_up")
public class CustomerFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recordId")
    private Long recordId;

    @Column(name = "followUpPerson", nullable = false)
    private Long followUpPerson;

    @Column(name = "followUpDate", nullable = false)
    private LocalDate followUpDate;

    @Column(name = "followUpWay", nullable = false, length = 20)
    private String followUpWay;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "afterServiceProjectId", nullable = false)
    private Long afterServiceProjectId;

    @CreationTimestamp
    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getRecordId() { return recordId; }

    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getFollowUpPerson() { return followUpPerson; }

    public void setFollowUpPerson(Long followUpPerson) { this.followUpPerson = followUpPerson; }

    public LocalDate getFollowUpDate() { return followUpDate; }

    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public String getFollowUpWay() { return followUpWay; }

    public void setFollowUpWay(String followUpWay) { this.followUpWay = followUpWay; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }

    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public LocalDateTime getCreateTime() { return createTime; }

    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }

    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
