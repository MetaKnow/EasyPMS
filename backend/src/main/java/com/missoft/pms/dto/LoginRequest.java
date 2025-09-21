package com.missoft.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 * 用于接收前端登录请求数据
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 50, message = "用户名长度必须在1-50个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 255, message = "密码长度不能超过255个字符")
    private String password;
    
    private boolean rememberMe = false;
    
    /**
     * 默认构造函数
     */
    public LoginRequest() {}
    
    /**
     * 构造函数
     * 
     * @param username 用户名
     * @param password 密码
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    /**
     * 构造函数
     * 
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我
     */
    public LoginRequest(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }
    
    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
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
     * 获取是否记住我
     * @return 是否记住我
     */
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    /**
     * 设置是否记住我
     * @param rememberMe 是否记住我
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}