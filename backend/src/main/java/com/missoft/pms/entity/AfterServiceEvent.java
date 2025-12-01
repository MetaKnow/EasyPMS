package com.missoft.pms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维事件实体类
 */
@Entity
@Table(name = "afterService_event")
public class AfterServiceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId")
    private Long eventId;

    @Column(name = "eventName", nullable = false, length = 200)
    private String eventName;

    @Column(name = "eventDate", nullable = false)
    private LocalDate eventDate;

    @Column(name = "eventType", nullable = false, length = 20)
    private String eventType; // 主动服务 / 响应服务

    @Column(name = "serviceMode", nullable = false, length = 20)
    private String serviceMode; // 远程服务 / 现场服务

    @Column(name = "director", nullable = false)
    private Long director; // user.userId 外键

    @Column(name = "eventhours", nullable = false, precision = 8, scale = 2)
    private BigDecimal eventhours;

    @Column(name = "eventDetails", length = 1000)
    private String eventDetails; // 事件描述

    @Column(name = "afterServiceProjectId", nullable = false)
    private Long afterServiceProjectId; // afterService_project.projectId 外键

    @Column(name = "isComplete")
    private Boolean isComplete;

    @CreationTimestamp
    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public Long getDirector() {
        return director;
    }

    public void setDirector(Long director) {
        this.director = director;
    }

    public BigDecimal getEventhours() {
        return eventhours;
    }

    public void setEventhours(BigDecimal eventhours) {
        this.eventhours = eventhours;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public Long getAfterServiceProjectId() {
        return afterServiceProjectId;
    }

    public void setAfterServiceProjectId(Long afterServiceProjectId) {
        this.afterServiceProjectId = afterServiceProjectId;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
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
}

