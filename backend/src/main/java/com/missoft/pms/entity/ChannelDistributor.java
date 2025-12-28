package com.missoft.pms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 渠道商实体类
 * 对应数据库中的channel_distributor表
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "channel_distributor")
public class ChannelDistributor {

    /**
     * 渠道ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long channelId;

    /**
     * 渠道名称
     */
    @NotBlank(message = "渠道名称不能为空")
    @Size(min = 2, max = 100, message = "渠道名称长度必须在2-100个字符之间")
    @Column(name = "channel_name", nullable = false, length = 100)
    private String channelName;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    @Size(min = 2, max = 50, message = "联系人姓名长度必须在2-50个字符之间")
    @Column(name = "contactor", nullable = false, length = 50)
    private String contactor;

    /**
     * 联系方式
     */
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 20, message = "联系方式长度不能超过20个字符")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * 销售总监（关联user表）
     */
    @Column(name = "saleDirector")
    private Long saleDirector;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public ChannelDistributor() {
    }

    /**
     * 带参数的构造函数
     *
     * @param channelName 渠道名称
     * @param contactor   联系人
     * @param phoneNumber 联系方式
     */
    public ChannelDistributor(String channelName, String contactor, String phoneNumber) {
        this.channelName = channelName;
        this.contactor = contactor;
        this.phoneNumber = phoneNumber;
    }

    // Getter 和 Setter 方法

    /**
     * 获取渠道ID
     *
     * @return 渠道ID
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * 设置渠道ID
     *
     * @param channelId 渠道ID
     */
    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    /**
     * 获取渠道名称
     *
     * @return 渠道名称
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 设置渠道名称
     *
     * @param channelName 渠道名称
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * 获取联系人
     *
     * @return 联系人
     */
    public String getContactor() {
        return contactor;
    }

    /**
     * 设置联系人
     *
     * @param contactor 联系人
     */
    public void setContactor(String contactor) {
        this.contactor = contactor;
    }

    /**
     * 获取联系方式
     *
     * @return 联系方式
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置联系方式
     *
     * @param phoneNumber 联系方式
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 获取销售总监ID
     *
     * @return 销售总监ID
     */
    public Long getSaleDirector() {
        return saleDirector;
    }

    /**
     * 设置销售总监ID
     *
     * @param saleDirector 销售总监ID
     */
    public void setSaleDirector(Long saleDirector) {
        this.saleDirector = saleDirector;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ChannelDistributor{" +
                "channelId=" + channelId +
                ", channelName='" + channelName + '\'' +
                ", contactor='" + contactor + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", saleDirector=" + saleDirector +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
