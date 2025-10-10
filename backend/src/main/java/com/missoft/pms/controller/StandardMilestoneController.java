package com.missoft.pms.controller;

import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.service.StandardMilestoneService;
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
 * 标准里程碑管理控制器
 * 提供标准里程碑相关的REST API接口
 */
@RestController
@RequestMapping("/api/standard-milestones")
@CrossOrigin(origins = "*")
public class StandardMilestoneController {

    @Autowired
    private StandardMilestoneService standardMilestoneService;

    /**
     * 分页查询里程碑列表
     *
     * @param page          页码（从0开始，默认0）
     * @param size          每页大小（默认10）
     * @param milestoneName 里程碑名称（可选）
     * @param sortBy        排序字段（默认createTime）
     * @param sortDir       排序方向（默认asc）
     * @return 里程碑分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStandardMilestones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String milestoneName,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Page<StandardMilestone> milestonePage = standardMilestoneService.getStandardMilestones(
                    page, size, milestoneName, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("milestones", milestonePage.getContent());
            response.put("currentPage", milestonePage.getNumber());
            response.put("totalItems", milestonePage.getTotalElements());
            response.put("totalPages", milestonePage.getTotalPages());
            response.put("pageSize", milestonePage.getSize());
            response.put("hasNext", milestonePage.hasNext());
            response.put("hasPrevious", milestonePage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询里程碑列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询里程碑详情
     *
     * @param milestoneId 里程碑ID
     * @return 里程碑详情
     */
    @GetMapping("/{milestoneId}")
    public ResponseEntity<Map<String, Object>> getStandardMilestoneById(@PathVariable Long milestoneId) {
        try {
            StandardMilestone milestone = standardMilestoneService.getStandardMilestoneById(milestoneId);
            Map<String, Object> response = new HashMap<>();
            response.put("milestone", milestone);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑不存在");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询里程碑详情失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新里程碑
     *
     * @param standardMilestone 里程碑信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStandardMilestone(@Valid @RequestBody StandardMilestone standardMilestone) {
        try {
            StandardMilestone savedMilestone = standardMilestoneService.createStandardMilestone(standardMilestone);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "里程碑创建成功");
            response.put("milestone", savedMilestone);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新里程碑信息
     *
     * @param milestoneId       里程碑ID
     * @param standardMilestone 更新的里程碑信息
     * @return 更新结果
     */
    @PutMapping("/{milestoneId}")
    public ResponseEntity<Map<String, Object>> updateStandardMilestone(
            @PathVariable Long milestoneId,
            @Valid @RequestBody StandardMilestone standardMilestone) {
        try {
            StandardMilestone updatedMilestone = standardMilestoneService.updateStandardMilestone(milestoneId, standardMilestone);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "里程碑更新成功");
            response.put("milestone", updatedMilestone);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除里程碑
     *
     * @param milestoneId 里程碑ID
     * @return 删除结果
     */
    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<Map<String, Object>> deleteStandardMilestone(@PathVariable Long milestoneId) {
        try {
            standardMilestoneService.deleteStandardMilestone(milestoneId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "里程碑删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "里程碑删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除里程碑
     *
     * @param milestoneIds 里程碑ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteStandardMilestones(@RequestBody List<Long> milestoneIds) {
        try {
            standardMilestoneService.batchDeleteStandardMilestones(milestoneIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除里程碑成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除里程碑失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除里程碑失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查里程碑名称是否已存在（用于前端异步判重）
     *
     * @param milestoneName 里程碑名称
     * @param milestoneId   可选，编辑场景下用于排除当前里程碑ID
     * @return { exists: boolean }
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkMilestoneNameExists(
            @RequestParam String milestoneName,
            @RequestParam(required = false) Long milestoneId) {
        try {
            boolean available = standardMilestoneService.isMilestoneNameAvailable(milestoneName, milestoneId);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", !available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查里程碑名称失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有里程碑列表（不分页）
     *
     * @return 里程碑列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStandardMilestones() {
        try {
            List<StandardMilestone> milestones = standardMilestoneService.getAllStandardMilestones();
            Map<String, Object> response = new HashMap<>();
            response.put("milestones", milestones);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取里程碑列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}