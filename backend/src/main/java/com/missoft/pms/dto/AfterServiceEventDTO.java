package com.missoft.pms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维事件DTO
 */
public class AfterServiceEventDTO {
    private Long eventId;
    private String eventName;
    private LocalDate eventDate;
    private String eventType;
    private String serviceMode;
    private Long director;
    private String directorName;
    private BigDecimal eventhours;
    private String eventDetails;
    private Long afterServiceProjectId;
    private LocalDateTime createTime;
    private Boolean hasFiles;
    private Boolean isComplete;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }

    public Long getDirector() { return director; }
    public void setDirector(Long director) { this.director = director; }

    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }

    public BigDecimal getEventhours() { return eventhours; }
    public void setEventhours(BigDecimal eventhours) { this.eventhours = eventhours; }

    public String getEventDetails() { return eventDetails; }
    public void setEventDetails(String eventDetails) { this.eventDetails = eventDetails; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }
    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Boolean getHasFiles() { return hasFiles; }
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }

    public Boolean getIsComplete() { return isComplete; }
    public void setIsComplete(Boolean isComplete) { this.isComplete = isComplete; }
}
