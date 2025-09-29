package com.missoft.pms.dto;

/**
 * 用户信息DTO（包含角色名称）
 * 用于前端显示用户列表时包含角色信息
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class UserWithRoleDTO {
    
    private Long userId;
    private String userName;
    private String name;
    private Long organId;
    private String organName;  // 机构名称
    private Long roleId;
    private String roleName;  // 角色名称
    private Integer locked;
    
    /**
     * 默认构造函数
     */
    public UserWithRoleDTO() {}
    
    /**
     * 全参构造函数
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param name 真实姓名
     * @param organId 机构ID
     * @param organName 机构名称
     * @param roleId 角色ID
     * @param roleName 角色名称
     * @param locked 锁定状态
     */
    public UserWithRoleDTO(Long userId, String userName, String name, Long organId, String organName,
                          Long roleId, String roleName, Integer locked) {
        this.userId = userId;
        this.userName = userName;
        this.name = name;
        this.organId = organId;
        this.organName = organName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.locked = locked;
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
     * 获取机构ID
     * @return 机构ID
     */
    public Long getOrganId() {
        return organId;
    }
    
    /**
     * 设置机构ID
     * @param organId 机构ID
     */
    public void setOrganId(Long organId) {
        this.organId = organId;
    }
    
    /**
     * 获取机构名称
     * @return 机构名称
     */
    public String getOrganName() {
        return organName;
    }
    
    /**
     * 设置机构名称
     * @param organName 机构名称
     */
    public void setOrganName(String organName) {
        this.organName = organName;
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
     * 获取锁定状态
     * @return 锁定状态
     */
    public Integer getLocked() {
        return locked;
    }
    
    /**
     * 设置锁定状态
     * @param locked 锁定状态
     */
    public void setLocked(Integer locked) {
        this.locked = locked;
    }
    
    @Override
    public String toString() {
        return "UserWithRoleDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", organId=" + organId +
                ", organName='" + organName + '\'' +
                ", roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", locked=" + locked +
                '}';
    }
}