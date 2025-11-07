package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interface")
public class InterfaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interfaceId")
    private Long interfaceId;

    @Column(name = "projectId")
    private Long projectId;

    @Column(name = "interfaceType", nullable = false, length = 100)
    private String interfaceType;

    @Column(name = "integrationSysName", nullable = false, length = 200)
    private String integrationSysName;

    @Column(name = "integrationSysManufacturer", nullable = false, length = 200)
    private String integrationSysManufacturer;

    @Column(name = "archieveDataCategory", length = 100)
    private String archieveDataCategory;

    @Column(name = "archieveInterfaceImpl", length = 200)
    private String archieveInterfaceImpl;

    @Column(name = "milestoneId")
    private Long milestoneId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getInterfaceId() { return interfaceId; }
    public void setInterfaceId(Long interfaceId) { this.interfaceId = interfaceId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getInterfaceType() { return interfaceType; }
    public void setInterfaceType(String interfaceType) { this.interfaceType = interfaceType; }

    public String getIntegrationSysName() { return integrationSysName; }
    public void setIntegrationSysName(String integrationSysName) { this.integrationSysName = integrationSysName; }

    public String getIntegrationSysManufacturer() { return integrationSysManufacturer; }
    public void setIntegrationSysManufacturer(String integrationSysManufacturer) { this.integrationSysManufacturer = integrationSysManufacturer; }

    public String getArchieveDataCategory() { return archieveDataCategory; }
    public void setArchieveDataCategory(String archieveDataCategory) { this.archieveDataCategory = archieveDataCategory; }

    public String getArchieveInterfaceImpl() { return archieveInterfaceImpl; }
    public void setArchieveInterfaceImpl(String archieveInterfaceImpl) { this.archieveInterfaceImpl = archieveInterfaceImpl; }

    public Long getMilestoneId() { return milestoneId; }
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }


    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}