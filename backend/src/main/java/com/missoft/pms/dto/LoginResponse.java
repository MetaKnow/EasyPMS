package com.missoft.pms.dto;

/**
 * 登录响应DTO
 * 用于返回登录结果数据
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class LoginResponse {
    
    private boolean success;
    private String message;
    private String token;
    private UserInfo userInfo;
    
    /**
     * 默认构造函数
     */
    public LoginResponse() {}
    
    /**
     * 构造函数
     * 
     * @param success 是否成功
     * @param message 消息
     */
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    /**
     * 构造函数
     * 
     * @param success 是否成功
     * @param message 消息
     * @param token JWT令牌
     * @param userInfo 用户信息
     */
    public LoginResponse(boolean success, String message, String token, UserInfo userInfo) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.userInfo = userInfo;
    }
    
    /**
     * 创建成功响应
     * 
     * @param token JWT令牌
     * @param userInfo 用户信息
     * @return 登录响应
     */
    public static LoginResponse success(String token, UserInfo userInfo) {
        return new LoginResponse(true, "登录成功", token, userInfo);
    }
    
    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @return 登录响应
     */
    public static LoginResponse failure(String message) {
        return new LoginResponse(false, message);
    }
    
    /**
     * 获取是否成功
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 设置是否成功
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * 获取消息
     * @return 消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置消息
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取JWT令牌
     * @return JWT令牌
     */
    public String getToken() {
        return token;
    }
    
    /**
     * 设置JWT令牌
     * @param token JWT令牌
     */
    public void setToken(String token) {
        this.token = token;
    }
    
    /**
     * 获取用户信息
     * @return 用户信息
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    /**
     * 设置用户信息
     * @param userInfo 用户信息
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        private Long userId;
        private String userName;
        private String name;
        private Long roleId;
        private Long organId;
        
        /**
         * 默认构造函数
         */
        public UserInfo() {}
        
        /**
         * 构造函数
         * 
         * @param userId 用户ID
         * @param userName 用户名
         * @param name 真实姓名
         * @param roleId 角色ID
         * @param organId 组织ID
         */
        public UserInfo(Long userId, String userName, String name, Long roleId, Long organId) {
            this.userId = userId;
            this.userName = userName;
            this.name = name;
            this.roleId = roleId;
            this.organId = organId;
        }
        
        // Getter和Setter方法
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
        
        public Long getOrganId() { return organId; }
        public void setOrganId(Long organId) { this.organId = organId; }
    }
}