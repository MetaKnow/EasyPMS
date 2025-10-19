package com.missoft.pms.controller;

import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.service.StandardConstructStepService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准交付步骤管理控制器
 * 提供标准交付步骤相关的REST API接口
 */
@RestController
@RequestMapping("/api/standard-construct-steps")
@CrossOrigin(origins = "*")
public class StandardConstructStepController {

    @Autowired
    private StandardConstructStepService standardConstructStepService;

    /**
     * 分页查询步骤列表
     *
     * @param page         页码（从0开始，默认0）
     * @param size         每页大小（默认10）
     * @param sstepName    步骤名称（可选）
     * @param systemName   档案系统名称（可选）
     * @param type         步骤类型（可选）
     * @param smilestoneId 标准里程碑ID（可选）
     * @param sortBy       排序字段（默认sstepId）
     * @param sortDir      排序方向（默认desc）
     * @return 步骤分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStandardConstructSteps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sstepName,
            @RequestParam(required = false) String systemName,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long smilestoneId,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Page<StandardConstructStep> stepPage = standardConstructStepService.getStandardConstructSteps(
                    page, size, sstepName, systemName, type, smilestoneId, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("steps", stepPage.getContent());
            response.put("currentPage", stepPage.getNumber());
            response.put("totalItems", stepPage.getTotalElements());
            response.put("totalPages", stepPage.getTotalPages());
            response.put("pageSize", stepPage.getSize());
            response.put("hasNext", stepPage.hasNext());
            response.put("hasPrevious", stepPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询步骤列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询步骤详情
     *
     * @param sstepId 步骤ID
     * @return 步骤详情
     */
    @GetMapping("/{sstepId}")
    public ResponseEntity<Map<String, Object>> getStandardConstructStepById(@PathVariable Long sstepId) {
        try {
            StandardConstructStep step = standardConstructStepService.getStandardConstructStepById(sstepId);
            Map<String, Object> response = new HashMap<>();
            response.put("step", step);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤不存在");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询步骤详情失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新步骤
     *
     * @param standardConstructStep 步骤信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStandardConstructStep(@Valid @RequestBody StandardConstructStep standardConstructStep) {
        try {
            StandardConstructStep savedStep = standardConstructStepService.createStandardConstructStep(standardConstructStep);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "步骤创建成功");
            response.put("step", savedStep);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新步骤信息
     *
     * @param sstepId               步骤ID
     * @param standardConstructStep 更新的步骤信息
     * @return 更新结果
     */
    @PutMapping("/{sstepId}")
    public ResponseEntity<Map<String, Object>> updateStandardConstructStep(
            @PathVariable Long sstepId,
            @Valid @RequestBody StandardConstructStep standardConstructStep) {
        try {
            StandardConstructStep updatedStep = standardConstructStepService.updateStandardConstructStep(sstepId, standardConstructStep);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "步骤更新成功");
            response.put("step", updatedStep);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除步骤
     *
     * @param sstepId 步骤ID
     * @return 删除结果
     */
    @DeleteMapping("/{sstepId}")
    public ResponseEntity<Map<String, Object>> deleteStandardConstructStep(@PathVariable Long sstepId) {
        try {
            standardConstructStepService.deleteStandardConstructStep(sstepId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "步骤删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "步骤删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除步骤
     *
     * @param sstepIds 步骤ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteStandardConstructSteps(@RequestBody List<Long> sstepIds) {
        try {
            standardConstructStepService.batchDeleteStandardConstructSteps(sstepIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除步骤成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除步骤失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除步骤失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据档案系统名称获取步骤列表
     *
     * @param systemName 档案系统名称
     * @return 步骤列表
     */
    @GetMapping("/by-system")
    public ResponseEntity<Map<String, Object>> getStandardConstructStepsBySystemName(@RequestParam String systemName) {
        try {
            List<StandardConstructStep> steps = standardConstructStepService.getStandardConstructStepsBySystemName(systemName);
            Map<String, Object> response = new HashMap<>();
            response.put("steps", steps);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取步骤列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有不重复的档案系统名称列表
     *
     * @return 档案系统名称列表
     */
    @GetMapping("/system-names")
    public ResponseEntity<Map<String, Object>> getDistinctSystemNames() {
        try {
            List<String> systemNames = standardConstructStepService.getDistinctSystemNames();
            Map<String, Object> response = new HashMap<>();
            response.put("systemNames", systemNames);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取档案系统名称列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    /**
     * 获取所有步骤列表（不分页）
     *
     * @return 步骤列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStandardConstructSteps() {
        try {
            List<StandardConstructStep> steps = standardConstructStepService.getAllStandardConstructSteps();
            Map<String, Object> response = new HashMap<>();
            response.put("steps", steps);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取步骤列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据标准里程碑ID获取步骤列表
     *
     * @param smilestoneId 标准里程碑ID
     * @return 步骤列表
     */
    @GetMapping("/by-milestone")
    public ResponseEntity<Map<String, Object>> getStandardConstructStepsByMilestoneId(@RequestParam Long smilestoneId) {
        try {
            List<StandardConstructStep> steps = standardConstructStepService.getStandardConstructStepsByMilestoneId(smilestoneId);
            Map<String, Object> response = new HashMap<>();
            response.put("steps", steps);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取步骤列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据步骤类型获取步骤列表
     *
     * @param type 步骤类型
     * @return 步骤列表
     */
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Object>> getStandardConstructStepsByType(@RequestParam String type) {
        try {
            List<StandardConstructStep> steps = standardConstructStepService.getStandardConstructStepsByType(type);
            Map<String, Object> response = new HashMap<>();
            response.put("steps", steps);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取步骤列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}