package com.missoft.pms.service;

import com.missoft.pms.entity.Role;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.RoleRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 角色服务类
 * 提供角色管理的业务逻辑
 *
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * 根据ID获取角色
     * @param roleId 角色ID
     * @return 角色对象
     */
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    /**
     * 创建新角色（函数级注释：校验角色名称唯一性）
     * @param role 角色数据
     * @return 创建后的角色
     */
    public Role createRole(Role role) {
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new RuntimeException("角色名称不能为空");
        }
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new RuntimeException("角色名称已存在: " + role.getRoleName());
        }
        return roleRepository.save(role);
    }

    /**
     * 更新角色信息（函数级注释：若角色名称变更则校验唯一性）
     * @param roleId 角色ID
     * @param updates 更新数据
     * @return 更新后的角色
     */
    public Role updateRole(Long roleId, Role updates) {
        Role existing = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));

        if (updates.getRoleName() != null && !updates.getRoleName().equals(existing.getRoleName())) {
            if (roleRepository.existsByRoleName(updates.getRoleName())) {
                throw new RuntimeException("角色名称已存在: " + updates.getRoleName());
            }
            existing.setRoleName(updates.getRoleName());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        return roleRepository.save(existing);
    }

    /**
     * 删除角色（函数级注释：若该角色已被用户使用则不允许删除）
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    public boolean deleteRole(Long roleId) {
        long count = countUsersByRoleId(roleId);
        if (count > 0) {
            throw new RuntimeException("该角色已被用户使用，无法删除");
        }
        roleRepository.deleteById(roleId);
        return true;
    }

    /**
     * 统计该角色下的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    public long countUsersByRoleId(Long roleId) {
        // 优先使用计数查询提升性能，如未提供则回退至列表大小
        try {
            return userRepository.countByRoleId(roleId);
        } catch (Exception ignored) {
            List<User> users = userRepository.findByRoleId(roleId);
            return users == null ? 0 : users.size();
        }
    }

    /**
     * 获取该角色下的用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    public List<User> getUsersByRoleId(Long roleId) {
        return userRepository.findByRoleId(roleId);
    }

    /**
     * 获取该角色下的用户列表（包含角色和机构信息）
     * @param roleId 角色ID
     * @return 包含角色和机构信息的用户列表
     */
    public List<com.missoft.pms.dto.UserWithRoleDTO> getUsersWithRoleByRoleId(Long roleId) {
        List<User> users = userRepository.findByRoleId(roleId);
        return users.stream()
                .map(user -> userService.convertToUserWithRoleDTO(user))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 批量取消用户的角色授权（函数级注释：将指定用户的角色设置为null）
     * @param userIds 用户ID列表
     * @return 成功取消授权的用户数量
     */
    public int batchRemoveRoleFromUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Long userId : userIds) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRoleId(null);
                userRepository.save(user);
                count++;
            }
        }
        return count;
    }
}