package com.missoft.pms.controller;

import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.service.InterfaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interfaces")
public class InterfaceController {

    private final InterfaceService service;

    public InterfaceController(InterfaceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InterfaceEntity payload) {
        // 基本校验
        if (payload.getInterfaceType() == null || payload.getInterfaceType().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "interfaceType为必填"));
        }
        if (payload.getIntegrationSysName() == null || payload.getIntegrationSysName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "integrationSysName为必填"));
        }
        if (payload.getIntegrationSysManufacturer() == null || payload.getIntegrationSysManufacturer().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "integrationSysManufacturer为必填"));
        }
        if (payload.getProjectId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "projectId为必填"));
        }
        if (payload.getMilestoneId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "milestoneId为必填"));
        }

        InterfaceEntity saved = service.create(payload);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "created");
        resp.put("interface", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<InterfaceEntity>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.listByProject(projectId));
    }

    /**
     * 函数级注释：按接口ID删除接口及其生成的步骤关系
     * 前端在接口信息行点击删除时调用，删除成功后前端应刷新项目摘要以更新里程碑与步骤展示。
     */
    @DeleteMapping("/{interfaceId}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable Long interfaceId) {
        try {
            boolean ok = service.deleteInterfaceById(interfaceId);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "接口不存在或已删除"));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "deleted");
            resp.put("interfaceId", interfaceId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除接口失败", "message", e.getMessage()));
        }
    }
}