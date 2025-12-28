package com.missoft.pms.controller;

import com.missoft.pms.entity.DatabaseBackup;
import com.missoft.pms.service.DatabaseBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backups")
@CrossOrigin(origins = "*")
public class DatabaseBackupController {

    @Autowired
    private DatabaseBackupService backupService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getRecentBackups() {
        List<DatabaseBackup> list = backupService.getRecentSuccessBackups();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Long id) {
        File file = backupService.getBackupFile(id);
        if (file != null && file.exists()) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 手动触发备份（用于测试）
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerBackup() {
        backupService.performBackup();
        backupService.cleanupOldBackups();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Backup triggered successfully");
        return ResponseEntity.ok(response);
    }
}
