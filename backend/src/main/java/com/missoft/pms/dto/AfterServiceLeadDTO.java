package com.missoft.pms.dto;

import java.time.LocalDateTime;

public class AfterServiceLeadDTO {
    private Long leadsId;
    private Long afterServiceProjectId;
    private Boolean isTransform;
    private Long leadsFinder;
    private String leadsFinderName;
    private String leadsSource;
    private String leadsDescript;
    private LocalDateTime createTime;
    private Boolean hasFiles;

    public Long getLeadsId() { return leadsId; }
    public void setLeadsId(Long leadsId) { this.leadsId = leadsId; }

    public Long getAfterServiceProjectId() { return afterServiceProjectId; }
    public void setAfterServiceProjectId(Long afterServiceProjectId) { this.afterServiceProjectId = afterServiceProjectId; }

    public Boolean getIsTransform() { return isTransform; }
    public void setIsTransform(Boolean isTransform) { this.isTransform = isTransform; }

    public Long getLeadsFinder() { return leadsFinder; }
    public void setLeadsFinder(Long leadsFinder) { this.leadsFinder = leadsFinder; }

    public String getLeadsFinderName() { return leadsFinderName; }
    public void setLeadsFinderName(String leadsFinderName) { this.leadsFinderName = leadsFinderName; }

    public String getLeadsSource() { return leadsSource; }
    public void setLeadsSource(String leadsSource) { this.leadsSource = leadsSource; }

    public String getLeadsDescript() { return leadsDescript; }
    public void setLeadsDescript(String leadsDescript) { this.leadsDescript = leadsDescript; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Boolean getHasFiles() { return hasFiles; }
    public void setHasFiles(Boolean hasFiles) { this.hasFiles = hasFiles; }
}
