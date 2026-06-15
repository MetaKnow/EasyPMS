package com.missoft.pms.controller;

import com.missoft.pms.dto.AfterServiceProjectDTO;
import com.missoft.pms.dto.HandoverRequest;
import com.missoft.pms.service.AfterServiceProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维项目控制器
 */
@RestController
@RequestMapping("/api/afterservice-projects")
public class AfterServiceProjectController {

    @Autowired
    private AfterServiceProjectService afterServiceProjectService;

    /**
     * 获取项目列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long saleDirector,
            @RequestParam(required = false) Long serviceDirector,
            @RequestParam(required = false) Long participantUserId) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AfterServiceProjectDTO> projectPage = afterServiceProjectService.getProjects(projectName, status, saleDirector, serviceDirector, participantUserId, pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", projectPage.getContent());
        data.put("total", projectPage.getTotalElements());
        data.put("pages", projectPage.getTotalPages());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取单个项目
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectById(@PathVariable Long id) {
        AfterServiceProjectDTO project = afterServiceProjectService.getProjectById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", project);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取一个新的唯一项目编号（用于新建表单展示）
     */
    @GetMapping("/new-project-num")
    public ResponseEntity<Map<String, Object>> getNewProjectNum() {
        String num = afterServiceProjectService.generateNewProjectNum();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("projectNum", num);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建项目
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody AfterServiceProjectDTO dto) {
        AfterServiceProjectDTO created = afterServiceProjectService.createProject(dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        response.put("message", "创建成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 从在建项目移交
     */
    @PostMapping("/handover")
    public ResponseEntity<Map<String, Object>> handoverProject(@RequestBody HandoverRequest request) {
        try {
            afterServiceProjectService.handoverProject(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "移交成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "移交失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "移交失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProject(@PathVariable Long id,
                                                             @RequestBody AfterServiceProjectDTO dto,
                                                             @RequestParam(required = false) Long operatorUserId,
                                                             HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            AfterServiceProjectDTO updated = afterServiceProjectService.updateProject(id, dto, resolvedOperatorUserId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "更新成功");
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
            errorResponse.put("message", "更新失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProject(@PathVariable Long id,
                                                             @RequestParam(required = false) Long operatorUserId,
                                                             HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            afterServiceProjectService.deleteProject(id, resolvedOperatorUserId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
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
            errorResponse.put("message", "删除失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 批量删除项目
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteProjects(@RequestBody List<Long> ids,
                                                                   @RequestParam(required = false) Long operatorUserId,
                                                                   HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            afterServiceProjectService.batchDeleteProjects(ids, resolvedOperatorUserId);
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
