package com.missoft.pms.controller;

import com.missoft.pms.entity.Organ;
import com.missoft.pms.entity.User;
import com.missoft.pms.service.OrganService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 机构管理控制器
 * 提供机构相关的REST API接口
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/organs")
@CrossOrigin(origins = "*")
public class OrganController {
    
    @Autowired
    private OrganService organService;
    
    /**
     * 获取机构树结构
     * 
     * @return 机构树数据
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getOrganTree() {
        try {
            List<Map<String, Object>> organTree = organService.getOrganTree();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", organTree);
            response.put("message", "获取机构树成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取机构树失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取所有机构列表
     * 
     * @return 机构列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrgans() {
        try {
            List<Organ> organs = organService.getAllOrgans();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", organs);
            response.put("message", "获取机构列表成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取机构列表失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 根据ID获取机构
     * 
     * @param organId 机构ID
     * @return 机构数据
     */
    @GetMapping("/{organId}")
    public ResponseEntity<Map<String, Object>> getOrganById(@PathVariable Long organId) {
        try {
            Optional<Organ> organ = organService.getOrganById(organId);
            
            Map<String, Object> response = new HashMap<>();
            if (organ.isPresent()) {
                response.put("success", true);
                response.put("data", organ.get());
                response.put("message", "获取机构信息成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "机构不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取机构信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 创建机构
     * 
     * @param organ 机构数据
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrgan(@Valid @RequestBody Organ organ) {
        try {
            Organ createdOrgan = organService.createOrgan(organ);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdOrgan);
            response.put("message", "创建机构成功");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "创建机构失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 更新机构
     * 
     * @param organId 机构ID
     * @param organ 机构数据
     * @return 更新结果
     */
    @PutMapping("/{organId}")
    public ResponseEntity<Map<String, Object>> updateOrgan(@PathVariable Long organId, @Valid @RequestBody Organ organ) {
        try {
            Organ updatedOrgan = organService.updateOrgan(organId, organ);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedOrgan);
            response.put("message", "更新机构成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "更新机构失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 删除机构
     * 
     * @param organId 机构ID
     * @return 删除结果
     */
    @DeleteMapping("/{organId}")
    public ResponseEntity<Map<String, Object>> deleteOrgan(@PathVariable Long organId) {
        try {
            organService.deleteOrgan(organId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除机构成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "删除机构失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 检查机构是否有用户
     * 
     * @param organId 机构ID
     * @return 检查结果
     */
    @GetMapping("/{organId}/has-users")
    public ResponseEntity<Map<String, Object>> checkOrganHasUsers(@PathVariable Long organId) {
        try {
            boolean hasUsers = organService.hasUsers(organId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hasUsers);
            response.put("message", "检查机构用户成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查机构用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查机构及其所有子机构的用户情况（函数级注释：获取机构及其子机构的详细用户信息，用于删除前的确认提示）
     * 
     * @param organId 机构ID
     * @return 检查结果，包含详细的用户信息
     */
    @GetMapping("/{organId}/check-users-detail")
    public ResponseEntity<Map<String, Object>> checkOrganAndChildrenUsersDetail(@PathVariable Long organId) {
        try {
            Map<String, Object> checkResult = organService.checkOrganAndChildrenHasUsers(organId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", checkResult);
            response.put("message", "检查机构用户详情成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查机构用户详情失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 检查机构是否有子机构
     * 
     * @param organId 机构ID
     * @return 检查结果
     */
    @GetMapping("/{organId}/has-children")
    public ResponseEntity<Map<String, Object>> checkOrganHasChildren(@PathVariable Long organId) {
        try {
            boolean hasChildren = organService.hasChildren(organId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hasChildren);
            response.put("message", "检查子机构成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查子机构失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 初始化默认机构
     * 
     * @return 初始化结果
     */
    @PostMapping("/init-default")
    public ResponseEntity<Map<String, Object>> initDefaultOrgan() {
        try {
            Organ defaultOrgan = organService.initDefaultOrgan();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", defaultOrgan);
            response.put("message", "初始化默认机构成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "初始化默认机构失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 按机构获取用户列表（函数级注释：返回指定 organId 下的所有用户，包含角色信息）
     * @param organId 机构ID
     * @return 响应结果
     */
    @GetMapping("/{organId}/users")
    public ResponseEntity<Map<String, Object>> getUsersByOrgan(@PathVariable Long organId) {
        try {
            java.util.List<com.missoft.pms.dto.UserWithRoleDTO> users = organService.getUsersWithRoleByOrgan(organId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            response.put("message", "获取机构用户成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取机构用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 统计机构下的用户数量（函数级注释：返回 count 字段与前端 organ.js 对齐）
     * @param organId 机构ID
     * @return { count: number }
     */
    @GetMapping("/{organId}/users/count")
    public ResponseEntity<Map<String, Object>> countOrganUsers(@PathVariable Long organId) {
        long count = organService.countUsersByOrganId(organId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }

    /**
     * 统计机构下的子机构数量（函数级注释：返回 count 字段与前端 organ.js 对齐）
     * @param organId 机构ID
     * @return { count: number }
     */
    @GetMapping("/{organId}/children/count")
    public ResponseEntity<Map<String, Object>> countOrganChildren(@PathVariable Long organId) {
        long count = organService.countChildrenByOrganId(organId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}