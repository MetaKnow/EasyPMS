package com.missoft.pms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 软件产品实体类
 * 对应数据库中的archieveSoft表
 */
@Entity
@Table(name = "archieveSoft")
public class ArchieveSoft {

    /**
     * 软件ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "softId")
    private Long softId;

    /**
     * 软件名称
     */
    @NotBlank(message = "软件名称不能为空")
    @Size(min = 2, max = 200, message = "软件名称长度必须在2-200个字符之间")
    @Column(name = "softName", nullable = false, length = 200)
    private String softName;

    /**
     * 软件版本
     */
    @NotBlank(message = "软件版本不能为空")
    @Size(max = 50, message = "软件版本长度不能超过50个字符")
    @Column(name = "softVersion", nullable = false, length = 50)
    private String softVersion;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "createTime", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updateTime", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 默认构造函数
     */
    public ArchieveSoft() {
    }

    /**
     * 带参数的构造函数
     *
     * @param softName    软件名称
     * @param softVersion 软件版本
     */
    public ArchieveSoft(String softName, String softVersion) {
        this.softName = softName;
        this.softVersion = softVersion;
    }

    // Getter和Setter方法

    public Long getSoftId() {
        return softId;
    }

    public void setSoftId(Long softId) {
        this.softId = softId;
    }

    public String getSoftName() {
        return softName;
    }

    public void setSoftName(String softName) {
        this.softName = softName;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
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

    /**
     * toString方法
     */
    @Override
    public String toString() {
        return "ArchieveSoft{" +
                "softId=" + softId +
                ", softName='" + softName + '\'' +
                ", softVersion='" + softVersion + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    /**
     * equals方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArchieveSoft archieveSoft = (ArchieveSoft) o;
        return softId != null && softId.equals(archieveSoft.softId);
    }

    /**
     * hashCode方法
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}