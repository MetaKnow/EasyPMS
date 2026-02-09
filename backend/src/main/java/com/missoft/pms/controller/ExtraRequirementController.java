package com.missoft.pms.controller;

import com.missoft.pms.entity.ExtraRequirement;
import com.missoft.pms.service.ExtraRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类级注释：
 * 合同外需求控制器，提供创建与按项目查询接口。
 */
@RestController
@RequestMapping("/api/extra-requirements")
public class ExtraRequirementController {

    @Autowired
    private ExtraRequirementService extraRequirementService;

    /**
     * 函数级注释：创建合同外需求条目
     * @param payload 请求体，包含 `projectId`, `requirementName`, 以及其它可选字段
     * @return 创建后的实体
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ExtraRequirement payload) {
        try {
            ExtraRequirement saved = extraRequirementService.create(payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("extraRequirement", saved);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建合同外需求失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：按项目ID查询该项目下的合同外需求列表
     * @param projectId 项目ID
     * @return 合同外需求列表（直接返回列表，便于前端处理）
     */
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<ExtraRequirement>> listByProject(@PathVariable Long projectId) {
        List<ExtraRequirement> list = extraRequirementService.listByProjectId(projectId);
        return ResponseEntity.ok(list);
    }

    /**
     * 函数级注释：更新合同外需求条目
     * @param id 需求ID
     * @param payload 更新数据
     * @return 更新后的实体
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ExtraRequirement payload) {
        try {
            ExtraRequirement updated = extraRequirementService.update(id, payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("extraRequirement", updated);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新合同外需求失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：删除合同外需求条目
     * @param id 需求ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            extraRequirementService.delete(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "删除成功");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除合同外需求失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
