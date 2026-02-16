package com.missoft.pms.controller;

import com.missoft.pms.entity.ConstructingProjectWeeklyReport;
import com.missoft.pms.service.ConstructingProjectWeeklyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类级注释：项目周报控制器
 */
@RestController
@RequestMapping("/api/constructing-project-weekly-reports")
public class ConstructingProjectWeeklyReportController {

    @Autowired
    private ConstructingProjectWeeklyReportService weeklyReportService;

    /**
     * 函数级注释：创建项目周报
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ConstructingProjectWeeklyReport payload) {
        try {
            ConstructingProjectWeeklyReport saved = weeklyReportService.create(payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("weeklyReport", saved);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建项目周报失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：按项目ID获取周报列表
     */
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<ConstructingProjectWeeklyReport>> listByProject(@PathVariable Long projectId) {
        List<ConstructingProjectWeeklyReport> list = weeklyReportService.listByProjectId(projectId);
        return ResponseEntity.ok(list);
    }

    /**
     * 函数级注释：更新指定项目周报
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ConstructingProjectWeeklyReport payload) {
        try {
            ConstructingProjectWeeklyReport updated = weeklyReportService.update(id, payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("weeklyReport", updated);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新项目周报失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：删除指定项目周报
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            weeklyReportService.delete(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除项目周报失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
