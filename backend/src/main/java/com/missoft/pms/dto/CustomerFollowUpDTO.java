package com.missoft.pms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerFollowUpDTO {
    private Long recordId;
    private Long followUpPerson;
    private String followUpPersonName;
    private LocalDate followUpDate;
    private String followUpWay;
    private String description;
    private Long afterServiceProjectId;
    private LocalDateTime createTime;
    private Boolean hasFiles;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getFollowUpPerson() { return followUpPerson; }
    public void setFollowUpPerson(Long followUpPerson) { this.followUpPerson = followUpPerson; }

    public String getFollowUpPersonName() { return followUpPersonName; }
    public void setFollowUpPersonName(String followUpPersonName) { this.followUpPersonName = followUpPersonName; }

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

    public Boolean getHasFiles() { return hasFiles; }
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }
}
