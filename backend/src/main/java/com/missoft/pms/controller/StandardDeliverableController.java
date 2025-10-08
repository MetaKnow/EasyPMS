package com.missoft.pms.controller;

import com.missoft.pms.entity.StandardDeliverable;
import com.missoft.pms.dto.StandardDeliverableWithNamesDTO;
import com.missoft.pms.service.StandardDeliverableService;
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
 * 标准交付物管理控制器
 * 提供标准交付物相关的REST API接口
 */
@RestController
@RequestMapping("/api/standard-deliverables")
@CrossOrigin(origins = "*")
public class StandardDeliverableController {

    @Autowired
    private StandardDeliverableService standardDeliverableService;

    /**
     * 分页查询交付物列表
     *
     * @param page            页码（从0开始，默认0）
     * @param size            每页大小（默认10）
     * @param deliverableName 交付物名称（可选）
     * @param systemName      系统名称（可选）
     * @param deliverableType 交付物类型（可选）
     * @param sstepId         标准步骤ID（可选）
     * @param milestoneId     里程碑ID（可选）
     * @param sortBy          排序字段（默认deliverableId）
     * @param sortDir         排序方向（默认desc）
     * @return 交付物分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStandardDeliverables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String deliverableName,
            @RequestParam(required = false) String systemName,
            @RequestParam(required = false) String deliverableType,
            @RequestParam(required = false) Long sstepId,
            @RequestParam(required = false) Long milestoneId,
            @RequestParam(defaultValue = "deliverableId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Page<StandardDeliverableWithNamesDTO> deliverablePage = standardDeliverableService.getStandardDeliverablesWithNames(
                    page, size, deliverableName, systemName, deliverableType, sstepId, milestoneId, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverablePage.getContent());
            response.put("currentPage", deliverablePage.getNumber());
            response.put("totalItems", deliverablePage.getTotalElements());
            response.put("totalPages", deliverablePage.getTotalPages());
            response.put("pageSize", deliverablePage.getSize());
            response.put("hasNext", deliverablePage.hasNext());
            response.put("hasPrevious", deliverablePage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询交付物详情
     *
     * @param deliverableId 交付物ID
     * @return 交付物详情
     */
    @GetMapping("/{deliverableId}")
    public ResponseEntity<Map<String, Object>> getStandardDeliverableById(@PathVariable Long deliverableId) {
        try {
            StandardDeliverable deliverable = standardDeliverableService.getStandardDeliverableById(deliverableId);
            Map<String, Object> response = new HashMap<>();
            response.put("deliverable", deliverable);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物不存在");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物详情失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新交付物
     *
     * @param standardDeliverable 交付物信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStandardDeliverable(@Valid @RequestBody StandardDeliverable standardDeliverable) {
        try {
            StandardDeliverable savedDeliverable = standardDeliverableService.createStandardDeliverable(standardDeliverable);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "交付物创建成功");
            response.put("deliverable", savedDeliverable);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新交付物信息
     *
     * @param deliverableId       交付物ID
     * @param standardDeliverable 更新的交付物信息
     * @return 更新结果
     */
    @PutMapping("/{deliverableId}")
    public ResponseEntity<Map<String, Object>> updateStandardDeliverable(
            @PathVariable Long deliverableId,
            @Valid @RequestBody StandardDeliverable standardDeliverable) {
        try {
            StandardDeliverable updatedDeliverable = standardDeliverableService.updateStandardDeliverable(deliverableId, standardDeliverable);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "交付物更新成功");
            response.put("deliverable", updatedDeliverable);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除交付物
     *
     * @param deliverableId 交付物ID
     * @return 删除结果
     */
    @DeleteMapping("/{deliverableId}")
    public ResponseEntity<Map<String, Object>> deleteStandardDeliverable(@PathVariable Long deliverableId) {
        try {
            standardDeliverableService.deleteStandardDeliverable(deliverableId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "交付物删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "交付物删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除交付物
     *
     * @param deliverableIds 交付物ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteStandardDeliverables(@RequestBody List<Long> deliverableIds) {
        try {
            standardDeliverableService.batchDeleteStandardDeliverables(deliverableIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除交付物成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除交付物失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除交付物失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据系统名称获取交付物列表
     *
     * @param systemName 系统名称
     * @return 交付物列表
     */
    @GetMapping("/by-system")
    public ResponseEntity<Map<String, Object>> getStandardDeliverablesBySystemName(@RequestParam String systemName) {
        try {
            List<StandardDeliverable> deliverables = standardDeliverableService.getStandardDeliverablesBySystemName(systemName);
            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据系统名称和交付物类型获取交付物列表
     *
     * @param systemName      系统名称
     * @param deliverableType 交付物类型
     * @return 交付物列表
     */
    @GetMapping("/by-system-and-type")
    public ResponseEntity<Map<String, Object>> getStandardDeliverablesBySystemNameAndType(
            @RequestParam String systemName,
            @RequestParam String deliverableType) {
        try {
            List<StandardDeliverable> deliverables = standardDeliverableService.getStandardDeliverablesBySystemNameAndType(systemName, deliverableType);
            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据标准步骤ID获取交付物列表
     *
     * @param sstepId 标准步骤ID
     * @return 交付物列表
     */
    @GetMapping("/by-step")
    public ResponseEntity<Map<String, Object>> getStandardDeliverablesBySstepId(@RequestParam Long sstepId) {
        try {
            List<StandardDeliverable> deliverables = standardDeliverableService.getStandardDeliverablesBySstepId(sstepId);
            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据里程碑ID获取交付物列表
     *
     * @param milestoneId 里程碑ID
     * @return 交付物列表
     */
    @GetMapping("/by-milestone")
    public ResponseEntity<Map<String, Object>> getStandardDeliverablesByMilestoneId(@RequestParam Long milestoneId) {
        try {
            List<StandardDeliverable> deliverables = standardDeliverableService.getStandardDeliverablesByMilestoneId(milestoneId);
            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有交付物列表
     *
     * @return 交付物列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStandardDeliverables() {
        try {
            List<StandardDeliverable> deliverables = standardDeliverableService.getAllStandardDeliverables();
            Map<String, Object> response = new HashMap<>();
            response.put("deliverables", deliverables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询交付物列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有不重复的系统名称列表
     *
     * @return 系统名称列表
     */
    @GetMapping("/distinct-systems")
    public ResponseEntity<Map<String, Object>> getDistinctSystemNames() {
        try {
            List<String> systemNames = standardDeliverableService.getDistinctSystemNames();
            Map<String, Object> response = new HashMap<>();
            response.put("systemNames", systemNames);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询系统名称列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查交付物名称是否存在
     *
     * @param deliverableName 交付物名称
     * @return 检查结果
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkDeliverableNameExists(@RequestParam String deliverableName) {
        try {
            boolean exists = standardDeliverableService.checkDeliverableNameExists(deliverableName);
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查交付物名称失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取交付物统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStandardDeliverableStatistics() {
        try {
            long totalCount = standardDeliverableService.getTotalStandardDeliverableCount();
            List<String> systemNames = standardDeliverableService.getDistinctSystemNames();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            response.put("systemCount", systemNames.size());
            response.put("systemNames", systemNames);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取统计信息失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}