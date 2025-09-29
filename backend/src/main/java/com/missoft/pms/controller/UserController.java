package com.missoft.pms.controller;

import com.missoft.pms.dto.UserWithRoleDTO;
import com.missoft.pms.entity.User;
import com.missoft.pms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户相关的REST API接口
 *
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表（函数级注释：支持 organId 与 keyword 参数，返回分页结构与前端对齐，包含角色信息）
     * GET /api/users?organId=&keyword=&page=&size=
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long organId,
            @RequestParam(required = false) String keyword) {
        try {
            Page<UserWithRoleDTO> userPage = userService.getUsersWithRole(page, size, organId, keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("users", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("pageSize", userPage.getSize());
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "查询用户列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 根据ID获取用户详情
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "用户不存在");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取用户详情失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 创建新用户
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User();
            user.setUserName((String) userData.get("userName"));
            user.setName((String) userData.get("name"));
            user.setPassword((String) userData.get("password"));
            
            // 处理roleId
            Object roleIdObj = userData.get("roleId");
            if (roleIdObj != null) {
                user.setRoleId(Long.valueOf(roleIdObj.toString()));
            }
            
            // 处理organId
            Object organIdObj = userData.get("organId");
            if (organIdObj != null) {
                user.setOrganId(Long.valueOf(organIdObj.toString()));
            }
            
            // 处理locked字段，支持boolean和integer类型
            Object lockedObj = userData.get("locked");
            if (lockedObj instanceof Boolean) {
                user.setLocked((Boolean) lockedObj ? 1 : 0);
            } else if (lockedObj instanceof Number) {
                user.setLocked(((Number) lockedObj).intValue());
            } else {
                user.setLocked(0); // 默认值
            }
            
            User created = userService.createUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "创建用户成功");
            response.put("user", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建用户失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 更新用户信息
     * PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userData) {
        try {
            User updates = new User();
            
            // 处理基本字段
            // 注意：用户名在编辑模式下不允许修改，所以不处理userName字段
            if (userData.containsKey("name")) {
                updates.setName((String) userData.get("name"));
            }
            if (userData.containsKey("password")) {
                updates.setPassword((String) userData.get("password"));
            }
            
            // 处理roleId（支持设置为null以清空角色）
            boolean updateRoleId = false;
            if (userData.containsKey("roleId")) {
                updateRoleId = true;
                Object roleIdObj = userData.get("roleId");
                if (roleIdObj != null) {
                    updates.setRoleId(Long.valueOf(roleIdObj.toString()));
                } else {
                    // 当roleId为null时，清空用户角色
                    updates.setRoleId(null);
                }
            }
            
            // 处理organId
            if (userData.containsKey("organId")) {
                Object organIdObj = userData.get("organId");
                if (organIdObj != null) {
                    updates.setOrganId(Long.valueOf(organIdObj.toString()));
                }
            }
            
            // 处理locked字段，支持boolean和integer类型
            if (userData.containsKey("locked")) {
                Object lockedObj = userData.get("locked");
                if (lockedObj instanceof Boolean) {
                    updates.setLocked((Boolean) lockedObj ? 1 : 0);
                } else if (lockedObj instanceof Number) {
                    updates.setLocked(((Number) lockedObj).intValue());
                }
            }
            
            User updated = userService.updateUser(userId, updates, updateRoleId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "更新用户成功");
            response.put("user", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新用户失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新用户失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除用户
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "删除用户成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除用户失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 批量删除用户
     * DELETE /api/users/batch
     * 请求体：{ userIds: number[] }
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteUsers(@RequestBody Map<String, List<Long>> body) {
        try {
            List<Long> userIds = body.get("userIds");
            userService.batchDeleteUsers(userIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除用户成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "批量删除用户失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 切换用户状态（锁定/解锁）
     * PUT /api/users/{userId}/status
     * 请求体：{ locked: boolean }
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        try {
            Object lockedObj = body.get("locked");
            boolean locked = false;
            if (lockedObj instanceof Boolean) {
                locked = (Boolean) lockedObj;
            } else if (lockedObj != null) {
                locked = Boolean.parseBoolean(lockedObj.toString());
            }
            User updated = userService.toggleUserStatus(userId, locked);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "更新用户状态成功");
            response.put("user", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新用户状态失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    /**
     * 修改用户密码
     * PUT /api/users/{userId}/password
     * 请求体：{ oldPassword: string, newPassword: string }
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, Object>> changeUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        try {
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            userService.changeUserPassword(userId, oldPassword, newPassword);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "修改密码成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "修改密码失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "修改密码失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 重置用户密码
     * PUT /api/users/{userId}/password/reset
     * 请求体：{ newPassword: string }
     */
    @PutMapping("/{userId}/password/reset")
    public ResponseEntity<Map<String, Object>> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("newPassword");
            userService.resetUserPassword(userId, newPassword);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "重置密码成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "重置密码失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 检查用户名是否存在（与前端 user.js 的 checkUserNameExists 对齐）
     * GET /api/users/check-username?userName=
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUserNameExists(@RequestParam String userName) {
        boolean exists = userService.existsByUserName(userName);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}