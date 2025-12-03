package com.missoft.pms.service;

import com.missoft.pms.dto.LoginRequest;
import com.missoft.pms.dto.LoginResponse;
import com.missoft.pms.dto.UserWithRoleDTO;
import com.missoft.pms.entity.Organ;
import com.missoft.pms.entity.Role;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.OrganRepository;
import com.missoft.pms.repository.RoleRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private OrganRepository organRepository;
    
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
            if (user.getRoleId() != null) {
                roleRepository.findById(user.getRoleId())
                        .ifPresent(r -> userInfo.setRoleName(r.getRoleName()));
            }
            
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
    
    /**
     * 分页查询用户列表（函数级注释：支持 organId 与 keyword 可选条件，按 userId 倒序）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param organId 机构ID（可选）
     * @param keyword 关键词（匹配用户名或姓名，可选）
     * @return Page<User> 分页数据
     */
    public Page<User> getUsers(int page, int size, Long organId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "userId"));
        if (organId == null && (keyword == null || keyword.trim().isEmpty())) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(organId, (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim(), pageable);
    }

    /**
     * 分页查询用户列表（包含角色信息）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param organId 机构ID（可选）
     * @param keyword 关键词（匹配用户名或姓名，可选）
     * @return Page<UserWithRoleDTO> 包含角色信息的分页数据
     */
    public Page<UserWithRoleDTO> getUsersWithRole(int page, int size, Long organId, String keyword) {
        // 先获取用户分页数据
        Page<User> userPage = getUsers(page, size, organId, keyword);
        
        // 转换为UserWithRoleDTO
        List<UserWithRoleDTO> userWithRoleDTOs = userPage.getContent().stream()
                .map(this::convertToUserWithRoleDTO)
                .collect(Collectors.toList());
        
        // 返回新的分页对象
        return new PageImpl<>(userWithRoleDTOs, userPage.getPageable(), userPage.getTotalElements());
    }

    /**
     * 将User实体转换为UserWithRoleDTO
     * @param user 用户实体
     * @return UserWithRoleDTO
     */
    public UserWithRoleDTO convertToUserWithRoleDTO(User user) {
        String roleName = null;
        if (user.getRoleId() != null) {
            Optional<Role> roleOptional = roleRepository.findById(user.getRoleId());
            if (roleOptional.isPresent()) {
                roleName = roleOptional.get().getRoleName();
            }
        }
        
        String organName = null;
        if (user.getOrganId() != null) {
            Optional<Organ> organOptional = organRepository.findById(user.getOrganId());
            if (organOptional.isPresent()) {
                organName = organOptional.get().getOrganName();
            }
        }
        
        return new UserWithRoleDTO(
                user.getUserId(),
                user.getUserName(),
                user.getName(),
                user.getOrganId(),
                organName,
                user.getRoleId(),
                roleName,
                user.getLocked(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    /**
     * 获取所有用户列表
     * @return 所有用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据ID查询用户（函数级注释：找不到则抛出异常）
     * @param userId 用户ID
     * @return User 实体
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
    }

    /**
     * 创建用户（函数级注释：加密密码并保存）
     * @param user 用户数据
     * @return 保存后的用户
     */
    public User createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(encodePassword(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * 更新用户（函数级注释：允许更新姓名、角色、锁定状态、机构）
     * @param userId 用户ID
     * @param updates 更新数据
     * @return 更新后的用户
     */
    public User updateUser(Long userId, User updates) {
        User existing = getUserById(userId);
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getRoleId() != null) existing.setRoleId(updates.getRoleId());
        if (updates.getLocked() != null) existing.setLocked(updates.getLocked());
        if (updates.getOrganId() != null) existing.setOrganId(updates.getOrganId());
        return userRepository.save(existing);
    }

    /**
     * 更新用户（函数级注释：允许更新姓名、角色、锁定状态、机构，支持roleId为null）
     * @param userId 用户ID
     * @param updates 更新数据
     * @param updateRoleId 是否更新roleId字段（包括设置为null）
     * @return 更新后的用户
     */
    public User updateUser(Long userId, User updates, boolean updateRoleId) {
        User existing = getUserById(userId);
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updateRoleId) existing.setRoleId(updates.getRoleId()); // 支持设置为null
        if (updates.getLocked() != null) existing.setLocked(updates.getLocked());
        if (updates.getOrganId() != null) existing.setOrganId(updates.getOrganId());
        return userRepository.save(existing);
    }

    /**
     * 删除用户（函数级注释：根据ID删除）
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * 批量删除用户（函数级注释：根据ID数组删除）
     * @param userIds 用户ID列表
     */
    public void batchDeleteUsers(java.util.List<Long> userIds) {
        java.util.List<User> toDelete = userRepository.findAllById(userIds);
        userRepository.deleteAll(toDelete);
    }

    /**
     * 切换用户状态（函数级注释：更新 locked 字段，0-正常，1-锁定）
     * @param userId 用户ID
     * @param locked 是否锁定（true->1, false->0）
     * @return 更新后的用户
     */
    public User toggleUserStatus(Long userId, boolean locked) {
        User existing = getUserById(userId);
        existing.setLocked(locked ? 1 : 0);
        return userRepository.save(existing);
    }



    /**
     * 修改用户密码（函数级注释：校验旧密码并更新为新密码）
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changeUserPassword(Long userId, String oldPassword, String newPassword) {
        User existing = getUserById(userId);
        if (!verifyPassword(oldPassword, existing.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        existing.setPassword(encodePassword(newPassword));
        userRepository.save(existing);
    }

    /**
     * 重置用户密码（函数级注释：无需旧密码，直接设置新密码）
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void resetUserPassword(Long userId, String newPassword) {
        User existing = getUserById(userId);
        existing.setPassword(encodePassword(newPassword));
        userRepository.save(existing);
    }
}
