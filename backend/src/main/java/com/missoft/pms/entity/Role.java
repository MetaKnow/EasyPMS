package com.missoft.pms.entity;

import jakarta.persistence.*;

/**
 * 角色实体类
 * 对应数据库中的 role 表
 *
 * @author MissoftPMS
 * @version 1.0.0
 */
@Entity
@Table(name = "role")
public class Role {

    /**
     * 角色ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleId")
    private Long roleId;

    /**
     * 角色名称（唯一、非空）
     */
    @Column(name = "roleName", unique = true, nullable = false, length = 100)
    private String roleName;

    /**
     * 角色描述（可选，使用文本类型）
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 默认构造函数 */
    public Role() {}

    /**
     * 全参构造函数
     * @param roleId 角色ID
     * @param roleName 角色名称
     * @param description 描述
     */
    public Role(Long roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
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
     * 获取角色名称
     * @return 角色名称
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 设置角色名称
     * @param roleName 角色名称
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * 获取角色描述
     * @return 角色描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置角色描述
     * @param description 角色描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}