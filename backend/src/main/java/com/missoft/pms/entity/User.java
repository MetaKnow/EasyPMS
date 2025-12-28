package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的user表
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;
    
    @Column(name = "userName", unique = true, nullable = false, length = 50)
    private String userName;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "organId")
    private Long organId;
    
    @Column(name = "roleId")
    private Long roleId;
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "locked", columnDefinition = "TINYINT DEFAULT 0")
    private Integer locked = 0;
    
    @Column(name = "createTime", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "updateTime")
    private LocalDateTime updateTime;
    
    /**
     * 默认构造函数
     */
    public User() {}
    
    /**
     * 构造函数
     * 
     * @param userName 用户名
     * @param password 密码
     * @param name 真实姓名
     */
    public User(String userName, String password, String name) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.locked = 0;
    }
    
    // Getter和Setter方法
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * 设置用户名
     * @param userName 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * 获取密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * 设置密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 获取组织ID
     * @return 组织ID
     */
    public Long getOrganId() {
        return organId;
    }
    
    /**
     * 设置组织ID
     * @param organId 组织ID
     */
    public void setOrganId(Long organId) {
        this.organId = organId;
    }
    
    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Long getRoleId() {
        return roleId;
    }
    
    /**
     * 设置角色ID
     * @param roleId 角色ID
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    /**
     * 获取真实姓名
     * @return 真实姓名
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置真实姓名
     * @param name 真实姓名
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取锁定状态
     * @return 锁定状态 (0-正常, 1-锁定)
     */
    public Integer getLocked() {
        return locked;
    }
    
    /**
     * 设置锁定状态
     * @param locked 锁定状态 (0-正常, 1-锁定)
     */
    public void setLocked(Integer locked) {
        this.locked = locked;
    }
    
    /**
     * 检查用户是否被锁定
     * @return true表示被锁定，false表示正常
     */
    public boolean isLocked() {
        return locked != null && locked == 1;
    }
    
    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    /**
     * 获取更新时间
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    /**
     * 设置更新时间
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", organId=" + organId +
                ", roleId=" + roleId +
                ", name='" + name + '\'' +
                ", locked=" + locked +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}