package com.missoft.pms.controller;

import com.missoft.pms.entity.Role;
import com.missoft.pms.entity.User;
import com.missoft.pms.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 角色管理控制器
 * 提供角色相关的REST API接口
 *
 * 与前端 src/api/role.js 定义的接口保持一致：
 * - GET /api/roles 获取所有角色（返回 { data: Role[] }）
 * - GET /api/roles/{id} 获取角色详情
 * - POST /api/roles 创建角色
 * - PUT /api/roles/{id} 更新角色
 * - DELETE /api/roles/{id} 删除角色
 * - GET /api/roles/check-name?roleName= 检查角色名称是否存在（返回 { exists: boolean }）
 * - GET /api/roles/{id}/users/count 获取角色下用户数量（返回 { count: number }）
 * - GET /api/roles/{id}/users 获取角色下用户列表（返回 User[]）
 *
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色列表
     * GET /api/roles
     * 返回结构：{ data: Role[] }
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            Map<String, Object> response = new HashMap<>();
            response.put("data", roles);
            response.put("message", "获取角色列表成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取角色列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 根据ID获取角色详情
     * GET /api/roles/{id}
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<?> getRoleById(@PathVariable Long roleId) {
        Optional<Role> roleOpt = roleService.getRoleById(roleId);
        if (roleOpt.isPresent()) {
            return ResponseEntity.ok(roleOpt.get());
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "角色不存在");
            error.put("message", "角色不存在: " + roleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 创建新角色
     * POST /api/roles
     * 请求体：{ roleName: string, description?: string }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        try {
            Role created = roleService.createRole(role);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "创建角色成功");
            response.put("role", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建角色失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 更新角色信息
     * PUT /api/roles/{id}
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long roleId, @RequestBody Role updates) {
        try {
            Role updated = roleService.updateRole(roleId, updates);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "更新角色成功");
            response.put("role", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新角色失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 删除角色
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long roleId) {
        try {
            boolean success = roleService.deleteRole(roleId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", success ? "删除角色成功" : "删除角色失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除角色失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 检查角色名称是否存在（与前端 role.js 的 checkRoleNameExists 对齐）
     * GET /api/roles/check-name?roleName=
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkRoleNameExists(@RequestParam String roleName) {
        boolean exists = roleService.existsByRoleName(roleName);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取角色下的用户数量
     * GET /api/roles/{id}/users/count
     * 返回结构：{ count: number }
     */
    @GetMapping("/{roleId}/users/count")
    public ResponseEntity<Map<String, Object>> getUsersCountByRole(@PathVariable Long roleId) {
        try {
            long count = roleService.countUsersByRoleId(roleId);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取角色用户数量失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取角色下的用户列表
     * GET /api/roles/{id}/users
     * 返回结构：UserWithRoleDTO[]
     */
    @GetMapping("/{roleId}/users")
    public ResponseEntity<?> getUsersByRole(@PathVariable Long roleId) {
        try {
            List<com.missoft.pms.dto.UserWithRoleDTO> users = roleService.getUsersWithRoleByRoleId(roleId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取角色用户列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 批量取消用户的角色授权
     * POST /api/roles/batch-remove-users
     * 请求体：{ userIds: Long[] }
     */
    @PostMapping("/batch-remove-users")
    public ResponseEntity<Map<String, Object>> batchRemoveRoleFromUsers(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> userIds = request.get("userIds");
            int count = roleService.batchRemoveRoleFromUsers(userIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "成功取消 " + count + " 个用户的角色授权");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "批量取消角色授权失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 批量删除角色
     * DELETE /api/roles/batch
     * 请求体：Long[]
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteRoles(@RequestBody List<Long> roleIds) {
        try {
            int deletedCount = roleService.batchDeleteRoles(roleIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除成功");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "批量删除失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}