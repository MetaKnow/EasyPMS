package com.missoft.pms.controller;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.service.ArchieveSoftService;
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
 * 软件产品管理控制器
 * 提供软件产品相关的REST API接口
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ArchieveSoftController {

    @Autowired
    private ArchieveSoftService archieveSoftService;

    /**
     * 分页查询产品列表
     *
     * @param page        页码（从0开始，默认0）
     * @param size        每页大小（默认10）
     * @param softName    软件名称（可选）
     * @param softVersion 软件版本（可选）
     * @param sortBy      排序字段（默认softId）
     * @param sortDir     排序方向（默认desc）
     * @return 产品分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getArchieveSofts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String softName,
            @RequestParam(required = false) String softVersion,
            @RequestParam(defaultValue = "softId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Page<ArchieveSoft> archieveSoftPage = archieveSoftService.getArchieveSofts(
                    page, size, softName, softVersion, sortBy, sortDir);

            Map<String, Object> response = new HashMap<>();
            response.put("products", archieveSoftPage.getContent());
            response.put("currentPage", archieveSoftPage.getNumber());
            response.put("totalItems", archieveSoftPage.getTotalElements());
            response.put("totalPages", archieveSoftPage.getTotalPages());
            response.put("pageSize", archieveSoftPage.getSize());
            response.put("hasNext", archieveSoftPage.hasNext());
            response.put("hasPrevious", archieveSoftPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询产品列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID查询产品详情
     *
     * @param softId 产品ID
     * @return 产品详情
     */
    @GetMapping("/{softId}")
    public ResponseEntity<Map<String, Object>> getArchieveSoftById(@PathVariable Long softId) {
        try {
            ArchieveSoft archieveSoft = archieveSoftService.getArchieveSoftById(softId);
            Map<String, Object> response = new HashMap<>();
            response.put("product", archieveSoft);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品不存在");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "查询产品详情失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新产品
     *
     * @param archieveSoft 产品信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createArchieveSoft(@Valid @RequestBody ArchieveSoft archieveSoft) {
        try {
            ArchieveSoft savedArchieveSoft = archieveSoftService.createArchieveSoft(archieveSoft);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "产品创建成功");
            response.put("product", savedArchieveSoft);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品创建失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新产品信息
     *
     * @param softId      产品ID
     * @param archieveSoft 更新的产品信息
     * @return 更新结果
     */
    @PutMapping("/{softId}")
    public ResponseEntity<Map<String, Object>> updateArchieveSoft(
            @PathVariable Long softId,
            @Valid @RequestBody ArchieveSoft archieveSoft) {
        try {
            ArchieveSoft updatedArchieveSoft = archieveSoftService.updateArchieveSoft(softId, archieveSoft);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "产品更新成功");
            response.put("product", updatedArchieveSoft);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品更新失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除产品
     *
     * @param softId 产品ID
     * @return 删除结果
     */
    @DeleteMapping("/{softId}")
    public ResponseEntity<Map<String, Object>> deleteArchieveSoft(@PathVariable Long softId) {
        try {
            archieveSoftService.deleteArchieveSoft(softId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "产品删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "产品删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 批量删除产品
     *
     * @param softIds 产品ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteArchieveSofts(@RequestBody List<Long> softIds) {
        try {
            int deletedCount = archieveSoftService.deleteArchieveSofts(softIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量删除成功");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "批量删除失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 检查产品名称+版本组合是否已存在（用于前端异步判重）
     *
     * @param softName    产品名称
     * @param softVersion 产品版本
     * @param softId      可选，编辑场景下用于排除当前产品ID
     * @return { exists: boolean }
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkProductNameExists(
            @RequestParam String softName,
            @RequestParam String softVersion,
            @RequestParam(required = false) Long softId) {
        try {
            boolean exists = (softId != null)
                    ? archieveSoftService.existsBySoftNameAndVersionExcludingId(softName, softVersion, softId)
                    : archieveSoftService.existsBySoftNameAndVersion(softName, softVersion);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "检查产品名称与版本失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有产品列表（不分页）
     *
     * @return 产品列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllArchieveSofts() {
        try {
            List<ArchieveSoft> archieveSofts = archieveSoftService.getAllArchieveSofts();
            Map<String, Object> response = new HashMap<>();
            response.put("products", archieveSofts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取产品列表失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取产品统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getArchieveSoftStatistics() {
        try {
            long totalCount = archieveSoftService.getTotalArchieveSoftCount();
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取产品统计信息失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}