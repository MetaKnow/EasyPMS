package com.missoft.pms.controller;

import com.missoft.pms.dto.ConstructingProjectDTO;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.service.ConstructingProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在建项目管理控制器
 * 提供在建项目相关的REST API接口
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/constructing-projects")
@CrossOrigin(origins = "*")
public class ConstructingProjectController {

    @Autowired
    private ConstructingProjectService constructingProjectService;

    /**
     * 分页查询在建项目列表
     *
     * @param page          页码（从0开始，默认0）
     * @param size          每页大小（默认10）
     * @param projectName   项目名称（可选）
     * @param year          年度（可选）
     * @param projectState  项目状态（可选）
     * @param projectLeader 项目负责人ID（可选）
     * @param customerId    客户ID（可选）
     * @param sortBy        排序字段（默认projectId）
     * @param sortDir       排序方向（默认desc）
     * @return 项目分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConstructingProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String projectState,
            @RequestParam(required = false) Long projectLeader,
            @RequestParam(required = false) Long saleLeader,
            @RequestParam(required = false) Long participantUserId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String softName,
            @RequestParam(defaultValue = "projectId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Page<ConstructingProjectDTO> projectPage = constructingProjectService.getConstructingProjectsWithCustomerName(
                    page, size, projectName, year, projectState, projectLeader, saleLeader, participantUserId, customerId, customerName, softName, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", projectPage.getContent());
            data.put("total", projectPage.getTotalElements());
            data.put("totalPages", projectPage.getTotalPages());
            data.put("currentPage", projectPage.getNumber());
            data.put("size", projectPage.getSize());
            data.put("hasNext", projectPage.hasNext());
            data.put("hasPrevious", projectPage.hasPrevious());
            
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询项目列表失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询在建项目详情
     *
     * @param projectId 项目ID
     * @return 项目详情
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> getConstructingProjectById(@PathVariable Long projectId) {
        try {
            ConstructingProject project = constructingProjectService.getConstructingProjectById(projectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", project);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目不存在");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询项目失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新项目
     *
     * @param constructingProject 项目信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createConstructingProject(
            @RequestParam(required = false) Long operatorUserId,
            @Valid @RequestBody ConstructingProject constructingProject,
            HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            if (resolvedOperatorUserId != null) {
                constructingProject.setModifyUser(resolvedOperatorUserId);
            }
            ConstructingProject savedProject = constructingProjectService.createConstructingProject(constructingProject);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "项目创建成功");
            response.put("data", savedProject);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目创建失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目创建失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新项目
     *
     * @param projectId           项目ID
     * @param constructingProject 项目信息
     * @return 更新结果
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> updateConstructingProject(
            @PathVariable Long projectId, 
            @RequestParam(required = false) Long operatorUserId,
            @Valid @RequestBody ConstructingProject constructingProject,
            HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            if (resolvedOperatorUserId != null) {
                constructingProject.setModifyUser(resolvedOperatorUserId);
            }
            ConstructingProject updatedProject = constructingProjectService.updateConstructingProject(projectId, constructingProject, resolvedOperatorUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "项目更新成功");
            response.put("data", updatedProject);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (e.getMessage() != null && (e.getMessage().contains("仅可查看项目") || e.getMessage().contains("无编辑或删除权限"))) {
                errorResponse.put("success", false);
                errorResponse.put("message", "您没有编辑项目的权限");
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            errorResponse.put("success", false);
            errorResponse.put("message", "项目更新失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目更新失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 删除结果
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> deleteConstructingProject(@PathVariable Long projectId,
                                                                         @RequestParam(required = false) Long operatorUserId,
                                                                         HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            constructingProjectService.deleteConstructingProject(projectId, resolvedOperatorUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "项目删除成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (e.getMessage() != null && (e.getMessage().contains("仅可查看项目") || e.getMessage().contains("无编辑或删除权限"))) {
                errorResponse.put("success", false);
                errorResponse.put("message", "您没有删除项目的权限");
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            errorResponse.put("success", false);
            errorResponse.put("message", "项目删除失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目删除失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除项目
     *
     * @param request 包含项目ID列表的请求体
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteConstructingProjects(@RequestBody Map<String, List<Long>> request,
                                                                                @RequestParam(required = false) Long operatorUserId) {
        try {
            List<Long> projectIds = request.get("ids");
            if (projectIds == null || projectIds.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "项目ID列表不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            constructingProjectService.batchDeleteConstructingProjects(projectIds, operatorUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量删除成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (e.getMessage() != null && (e.getMessage().contains("仅可查看项目") || e.getMessage().contains("无编辑或删除权限"))) {
                errorResponse.put("success", false);
                errorResponse.put("message", "您没有删除项目的权限");
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            errorResponse.put("success", false);
            errorResponse.put("message", "批量删除失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "批量删除失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有项目（不分页）
     *
     * @return 项目列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllConstructingProjects() {
        try {
            List<ConstructingProject> projects = constructingProjectService.getAllConstructingProjects();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", projects);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询项目列表失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据年度统计项目数量
     *
     * @param year 年度
     * @return 项目数量
     */
    @GetMapping("/count/year/{year}")
    public ResponseEntity<Map<String, Object>> countByYear(@PathVariable Integer year) {
        try {
            long count = constructingProjectService.countByYear(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "统计成功");
            response.put("data", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "统计失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据项目状态统计项目数量
     *
     * @param projectState 项目状态
     * @return 项目数量
     */
    @GetMapping("/count/state/{projectState}")
    public ResponseEntity<Map<String, Object>> countByProjectState(@PathVariable String projectState) {
        try {
            long count = constructingProjectService.countByProjectState(projectState);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "统计成功");
            response.put("data", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "统计失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Long resolveOperatorUserId(Long operatorUserId, HttpServletRequest request) {
        if (operatorUserId != null) {
            return operatorUserId;
        }
        Long fromHeader = parseLong(request.getHeader("X-User-Id"));
        if (fromHeader != null) {
            return fromHeader;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            String token = authorization.trim();
            if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }
            if (token.startsWith("simple_token_")) {
                String[] parts = token.split("_");
                if (parts.length >= 3) {
                    return parseLong(parts[2]);
                }
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
