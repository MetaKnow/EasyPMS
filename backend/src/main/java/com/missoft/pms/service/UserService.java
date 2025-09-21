package com.missoft.pms.service;

import com.missoft.pms.dto.LoginRequest;
import com.missoft.pms.dto.LoginResponse;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户登录验证
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // 查找用户
            Optional<User> userOptional = userRepository.findByUserName(loginRequest.getUsername());
            
            if (userOptional.isEmpty()) {
                return LoginResponse.failure("用户名或密码错误");
            }
            
            User user = userOptional.get();
            
            // 检查用户是否被锁定
            if (user.isLocked()) {
                return LoginResponse.failure("用户账户已被锁定，请联系管理员");
            }
            
            // 验证密码
            if (!verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                return LoginResponse.failure("用户名或密码错误");
            }
            
            // 创建用户信息
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUserId(),
                user.getUserName(),
                user.getName(),
                user.getRoleId(),
                user.getOrganId()
            );
            
            // 生成JWT令牌（这里暂时返回一个简单的token，后续可以集成JWT）
            String token = generateSimpleToken(user);
            
            return LoginResponse.success(token, userInfo);
            
        } catch (Exception e) {
            return LoginResponse.failure("登录过程中发生错误：" + e.getMessage());
        }
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 验证结果
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        try {
            // 如果是BCrypt加密的密码
            if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            }
            
            // 兼容旧的MD5密码（仅用于过渡期）
            if (encodedPassword.length() == 32) {
                String md5Hash = org.springframework.util.DigestUtils.md5DigestAsHex(rawPassword.getBytes());
                return md5Hash.equals(encodedPassword);
            }
            
            // 直接比较（不推荐，仅用于开发测试）
            return rawPassword.equals(encodedPassword);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成简单的令牌
     * 
     * @param user 用户对象
     * @return 令牌
     */
    private String generateSimpleToken(User user) {
        // 这里是一个简单的token生成，实际项目中应该使用JWT
        return "simple_token_" + user.getUserId() + "_" + System.currentTimeMillis();
    }
    
    /**
     * 根据用户名查找用户
     * 
     * @param userName 用户名
     * @return 用户对象
     */
    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
    
    /**
     * 检查用户名是否存在
     * 
     * @param userName 用户名
     * @return 是否存在
     */
    public boolean existsByUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }
    
    /**
     * 保存用户
     * 
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    public User save(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}