package com.missoft.pms.controller;

import com.missoft.pms.entity.ConstructingProjectRisk;
import com.missoft.pms.service.ConstructingProjectRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类级注释：项目风险控制器
 */
@RestController
@RequestMapping("/api/constructing-project-risks")
public class ConstructingProjectRiskController {

    @Autowired
    private ConstructingProjectRiskService riskService;

    /**
     * 函数级注释：创建项目风险
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ConstructingProjectRisk payload) {
        try {
            ConstructingProjectRisk saved = riskService.create(payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("risk", saved);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建项目风险失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：按项目ID获取风险列表
     */
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<ConstructingProjectRisk>> listByProject(@PathVariable Long projectId) {
        List<ConstructingProjectRisk> list = riskService.listByProjectId(projectId);
        return ResponseEntity.ok(list);
    }

    /**
     * 函数级注释：更新指定项目风险
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ConstructingProjectRisk payload) {
        try {
            ConstructingProjectRisk updated = riskService.update(id, payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("risk", updated);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新项目风险失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：删除指定项目风险
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            riskService.delete(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除项目风险失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
