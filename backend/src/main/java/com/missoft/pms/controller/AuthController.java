package com.missoft.pms.controller;

import com.missoft.pms.dto.LoginRequest;
import com.missoft.pms.dto.LoginResponse;
import com.missoft.pms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、登出等认证相关的HTTP请求
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录接口
     * 
     * @param loginRequest 登录请求数据
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.login(loginRequest);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            LoginResponse errorResponse = LoginResponse.failure("服务器内部错误：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 用户登出接口
     * 
     * @return 登出响应
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // 这里可以实现token失效逻辑
        return ResponseEntity.ok("登出成功");
    }
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUserName(username);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("认证服务运行正常");
    }
}