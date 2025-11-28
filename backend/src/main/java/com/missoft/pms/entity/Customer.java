package com.missoft.pms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 客户实体类
 * 对应数据库中的customer表
 */
@Entity
@Table(name = "customer")
public class Customer {

    /**
     * 客户ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private Long customerId;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空")
    @Size(min = 2, max = 100, message = "客户名称长度必须在2-100个字符之间")
    @Column(name = "customerName", nullable = false, length = 100)
    private String customerName;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    @Size(min = 2, max = 50, message = "联系人姓名长度必须在2-50个字符之间")
    @Column(name = "contact", nullable = false, length = 50)
    private String contact;

    /**
     * 联系方式
     */
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 20, message = "联系方式长度不能超过20个字符")
    @Column(name = "phoneNumber", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份名称长度不能超过20个字符")
    @Column(name = "province", nullable = false, length = 20)
    private String province;

    /**
     * 客户等级
     * 可选值：战略客户、重要客户、一般客户
     */
    @NotBlank(message = "客户等级不能为空")
    @Column(name = "customerRank", nullable = false, length = 20)
    private String customerRank;

    /**
     * 销售负责人ID
     */
    @Column(name = "saleLeader")
    private Long saleLeader;

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
    public Customer() {
    }

    /**
     * 带参数的构造函数
     *
     * @param customerName 客户名称
     * @param contact      联系人
     * @param phoneNumber  联系方式
     * @param province     省份
     * @param customerRank 客户等级
     */
    public Customer(String customerName, String contact, String phoneNumber, String province, String customerRank) {
        this.customerName = customerName;
        this.contact = contact;
        this.phoneNumber = phoneNumber;
        this.province = province;
        this.customerRank = customerRank;
    }

    // Getter和Setter方法

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCustomerRank() {
        return customerRank;
    }

    public void setCustomerRank(String customerRank) {
        this.customerRank = customerRank;
    }

    public Long getSaleLeader() {
        return saleLeader;
    }

    public void setSaleLeader(Long saleLeader) {
        this.saleLeader = saleLeader;
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
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", contact='" + contact + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", province='" + province + '\'' +
                ", customerRank='" + customerRank + '\'' +
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
        Customer customer = (Customer) o;
        return customerId != null && customerId.equals(customer.customerId);
    }

    /**
     * hashCode方法
     */
    @Override
    public int hashCode() {
        return customerId != null ? customerId.hashCode() : 0;
    }
}