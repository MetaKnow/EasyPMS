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
}