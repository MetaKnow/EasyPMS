package com.missoft.pms.controller;

import com.missoft.pms.dto.AfterServiceProjectDTO;
import com.missoft.pms.dto.HandoverRequest;
import com.missoft.pms.service.AfterServiceProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Long saleDirector) { // Updated from director to saleDirector
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AfterServiceProjectDTO> projectPage = afterServiceProjectService.getProjects(projectName, status, saleDirector, pageRequest);
        
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
        afterServiceProjectService.handoverProject(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "移交成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProject(@PathVariable Long id, @RequestBody AfterServiceProjectDTO dto) {
        AfterServiceProjectDTO updated = afterServiceProjectService.updateProject(id, dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "更新成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProject(@PathVariable Long id) {
        afterServiceProjectService.deleteProject(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量删除项目
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteProjects(@RequestBody List<Long> ids) {
        afterServiceProjectService.batchDeleteProjects(ids);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量删除成功");
        
        return ResponseEntity.ok(response);
    }
}
