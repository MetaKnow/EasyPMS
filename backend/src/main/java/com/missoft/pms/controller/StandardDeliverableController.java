package com.missoft.pms.controller;

import com.missoft.pms.entity.StandardDeliverable;
import com.missoft.pms.dto.StandardDeliverableWithNamesDTO;
import com.missoft.pms.service.StandardDeliverableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * 上传交付物模板文件（支持多文件）
     *
     * 需求实现：
     * 1) 存储到项目根目录下的 deliveryTempletes/<systemName>/ 路径（注意复数拼写）。
     * 2) 以 “交付物名称-文件原名称” 进行重命名保存。
     * 3) 同时更新交付物实体中的相对路径字段为 deliveryTempletes/<systemName>/。
     */
    @PostMapping("/{deliverableId}/templates/upload")
    public ResponseEntity<Map<String, Object>> uploadTemplates(
            @PathVariable Long deliverableId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            // 获取交付物并根据产品名称（systemName）生成存储目录（项目根）
            var deliverable = standardDeliverableService.getStandardDeliverableById(deliverableId);
            String systemName = deliverable.getSystemName() != null ? deliverable.getSystemName().trim() : "unknown";
            String safeSystemName = systemName.replaceAll("[\\\\/:*?\"<>|]", "_");

            // 项目根目录（后端目录的上一级）
            Path projectRoot = Paths.get("").toAbsolutePath().getParent();
            // 模板存储目录限定：docs/deliveryTempletes/<systemName>/
            Path docsRoot = projectRoot
                    .resolve("docs")
                    .resolve("deliveryTempletes")
                    .resolve(safeSystemName);
            Files.createDirectories(docsRoot);

            // 交付物名称作为前缀，参与重命名
            String deliverableName = deliverable.getDeliverableName() != null ? deliverable.getDeliverableName().trim() : "deliverable";
            String safeDeliverableName = deliverableName.replaceAll("[\\\\/:*?\"<>|]", "_");

            // 持久化相对目录路径：docs/deliveryTempletes/<systemName>/
            String relativeDir = "docs/deliveryTempletes/" + safeSystemName + "/";
            standardDeliverableService.updateDeliverableTemplatePath(deliverableId, relativeDir);

            List<Map<String, Object>> savedFiles = new ArrayList<>();
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;
                    String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
                    String safeOriginalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
                    // 重命名：交付物名称-文件原名称
                    String finalName = safeDeliverableName + "-" + safeOriginalName;
                    Path docsTarget = docsRoot.resolve(finalName).normalize();
                    if (!docsTarget.startsWith(docsRoot)) {
                        throw new RuntimeException("非法文件名");
                    }
                    try (InputStream in2 = file.getInputStream()) {
                        Files.copy(in2, docsTarget, StandardCopyOption.REPLACE_EXISTING);
                    }
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", finalName);
                    info.put("size", Files.size(docsTarget));
                    info.put("lastModified", Files.getLastModifiedTime(docsTarget).toMillis());
                    // 也返回每个文件的相对路径
                    info.put("relativePath", relativeDir + finalName);
                    savedFiles.add(info);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "文件上传成功");
            response.put("relativePath", relativeDir);
            response.put("files", savedFiles);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 列出交付物模板文件
     *
     * 需求实现：
     * 1) 优先列出 deliveryTempletes/<systemName>/ 目录下的所有文件。
     * 2) 同时从 docs/deliveryTempletes/<systemName>/ 目录中匹配“文件名前半部分为交付物名称”的文件并并入列表。
     * 3) 兼容历史目录 deliveryTemplete/<systemName>/（如果存在），也并入列表。
     */
    @GetMapping("/{deliverableId}/templates")
    public ResponseEntity<Map<String, Object>> listTemplates(@PathVariable Long deliverableId) {
        try {
            var deliverable = standardDeliverableService.getStandardDeliverableById(deliverableId);
            String systemName = deliverable.getSystemName() != null ? deliverable.getSystemName().trim() : "unknown";
            String safeSystemName = systemName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String deliverableName = deliverable.getDeliverableName() != null ? deliverable.getDeliverableName().trim() : "";
            String safeDeliverableName = deliverableName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String prefix = safeDeliverableName.isEmpty() ? "" : safeDeliverableName + "-";
            Path projectRoot = Paths.get("").toAbsolutePath().getParent();
            Path docsRoot = projectRoot.resolve("docs").resolve("deliveryTempletes").resolve(safeSystemName);

            // 使用有序去重集合，按插入顺序去重（避免重复文件名）
            Map<String, Map<String, Object>> byName = new LinkedHashMap<>();

            // 仅在 docs/deliveryTempletes 中列出模板（限定生效范围）
            String docsPrefix = prefix;
            if (Files.exists(docsRoot) && Files.isDirectory(docsRoot)) {
                try (Stream<Path> s = Files.list(docsRoot)) {
                    s.filter(Files::isRegularFile)
                     .filter(p -> {
                         String name = p.getFileName().toString();
                         String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                         boolean prefixMatch = docsPrefix.isEmpty() || name.startsWith(docsPrefix);
                         boolean equalsMatch = !safeDeliverableName.isEmpty() && base.equals(safeDeliverableName);
                         return prefixMatch || equalsMatch;
                     })
                     .forEach(p -> {
                         Map<String, Object> info = new HashMap<>();
                         info.put("name", p.getFileName().toString());
                         try {
                             info.put("size", Files.size(p));
                             info.put("lastModified", Files.getLastModifiedTime(p).toMillis());
                         } catch (Exception ex) {
                             info.put("size", 0L);
                             info.put("lastModified", 0L);
                         }
                         byName.putIfAbsent(p.getFileName().toString(), info);
                     });
                }
            }

            List<Map<String, Object>> files = new ArrayList<>(byName.values());

            Map<String, Object> response = new HashMap<>();
            response.put("files", files);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 下载交付物模板文件
     *
     * 范围限定：仅从 docs/deliveryTempletes/<systemName>/ 目录提供下载。
     */
    @GetMapping("/{deliverableId}/templates/{filename:.+}")
    public ResponseEntity<Resource> downloadTemplate(
            @PathVariable Long deliverableId,
            @PathVariable String filename) {
        try {
            var deliverable = standardDeliverableService.getStandardDeliverableById(deliverableId);
            String systemName = deliverable.getSystemName() != null ? deliverable.getSystemName().trim() : "unknown";
            String safeSystemName = systemName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String deliverableName = deliverable.getDeliverableName() != null ? deliverable.getDeliverableName().trim() : "";
            String safeDeliverableName = deliverableName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String requiredPrefix = safeDeliverableName.isEmpty() ? "" : safeDeliverableName + "-";
            // 安全限制：仅允许下载“当前交付物前缀”的文件，避免跨交付物访问
            if (!requiredPrefix.isEmpty()) {
                String base = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
                boolean prefixMatch = filename.startsWith(requiredPrefix);
                boolean equalsMatch = base.equals(safeDeliverableName);
                if (!prefixMatch && !equalsMatch) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            }
            Path projectRoot = Paths.get("").toAbsolutePath().getParent();
            Path docsRoot = projectRoot.resolve("docs").resolve("deliveryTempletes").resolve(safeSystemName);

            Path target = docsRoot.resolve(filename).normalize();
            Path root = docsRoot; // 用于startsWith校验的根

            if (!target.startsWith(root) || !Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + target.getFileName().toString() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除交付物模板文件
     *
     * 范围限定：仅删除 docs/deliveryTempletes/<systemName>/ 目录中的文件。
     */
    @DeleteMapping("/{deliverableId}/templates/{filename:.+}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(
            @PathVariable Long deliverableId,
            @PathVariable String filename) {
        try {
            var deliverable = standardDeliverableService.getStandardDeliverableById(deliverableId);
            String systemName = deliverable.getSystemName() != null ? deliverable.getSystemName().trim() : "unknown";
            String safeSystemName = systemName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String deliverableName = deliverable.getDeliverableName() != null ? deliverable.getDeliverableName().trim() : "";
            String safeDeliverableName = deliverableName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String requiredPrefix = safeDeliverableName.isEmpty() ? "" : safeDeliverableName + "-";
            // 安全限制：仅允许删除“当前交付物前缀”的文件，避免跨交付物误删
            if (!requiredPrefix.isEmpty()) {
                String base = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
                boolean prefixMatch = filename.startsWith(requiredPrefix);
                boolean equalsMatch = base.equals(safeDeliverableName);
                if (!prefixMatch && !equalsMatch) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "文件名不匹配当前交付物");
                    error.put("message", "禁止跨交付物删除文件");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                }
            }
            Path projectRoot = Paths.get("").toAbsolutePath().getParent();
            Path docsRoot = projectRoot.resolve("docs").resolve("deliveryTempletes").resolve(safeSystemName);

            Path targetDocs = docsRoot.resolve(filename).normalize();
            if (!(targetDocs.startsWith(docsRoot) && Files.exists(targetDocs))) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不存在");
                error.put("message", "未找到指定文件");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            Files.delete(targetDocs);

            // 如果 docs 目录已空，清空数据库中的模板路径
            boolean hasFiles = false;
            if (Files.exists(docsRoot) && Files.isDirectory(docsRoot)) {
                try (Stream<Path> s = Files.list(docsRoot)) {
                    hasFiles = s.anyMatch(Files::isRegularFile);
                }
            }
            if (!hasFiles) {
                // 清空数据库中保存的模板相对路径（仅针对 docs/deliveryTempletes 路径）
                standardDeliverableService.updateDeliverableTemplatePath(deliverableId, null);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "文件删除成功");
            response.put("filename", filename);
            response.put("pathCleared", !hasFiles);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除文件失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除文件失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
        }
    }

    // ... existing code ...