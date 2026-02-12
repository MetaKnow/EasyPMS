package com.missoft.pms.controller;

import com.missoft.pms.service.ConstructingProjectCommentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/constructing-project-comment-files")
@CrossOrigin(origins = "*")
public class ConstructingProjectCommentFileController {

    @Autowired
    private ConstructingProjectCommentFileService fileService;

    @PostMapping("/{projectId}/{commentId}/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable Long projectId,
            @PathVariable Long commentId,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            var saved = fileService.uploadFiles(projectId, commentId, uploaderId, files);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "附件上传成功");
            resp.put("count", saved.size());
            resp.put("files", saved);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "附件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "附件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/by-comment/{commentId}")
    public ResponseEntity<Map<String, Object>> listByComment(@PathVariable Long commentId) {
        try {
            List<Map<String, Object>> files = fileService.listByCommentId(commentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("files", files);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<Map<String, Object>> listByProject(@PathVariable Long projectId) {
        try {
            List<Map<String, Object>> files = fileService.listByProjectId(projectId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("files", files);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> preview(@PathVariable Long fileId) {
        try {
            Path target = fileService.resolveFilePath(fileId);
            if (!Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String filename = target.getFileName().toString();
            ContentDisposition cd = ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        try {
            Path target = fileService.resolveFilePath(fileId);
            if (!Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String filename = target.getFileName().toString();
            ContentDisposition cd = ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
