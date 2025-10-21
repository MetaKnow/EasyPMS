package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructDeliverableFile;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ConstructDeliverableFileService {

    @Autowired
    private ConstructDeliverableFileRepository fileRepository;

    /**
     * 上传并保存项目交付物文件
     */
    public List<ConstructDeliverableFile> uploadFiles(Long projectId, Long deliverableId,
                                                      Long uploaderId, Long sstepId, Long nstepId,
                                                      MultipartFile[] files) {
        if (projectId == null || deliverableId == null) {
            throw new RuntimeException("项目ID和交付物ID不能为空");
        }
        List<ConstructDeliverableFile> saved = new ArrayList<>();
        Path projectRoot = Paths.get("").toAbsolutePath().getParent();
        Path root = projectRoot
                .resolve("deliverableFiles")
                .resolve(String.valueOf(projectId))
                .resolve(String.valueOf(deliverableId));
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建文件目录失败: " + e.getMessage());
        }
        String relativeDir = "deliverableFiles/" + projectId + "/" + deliverableId + "/";

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
                Path target = root.resolve(originalName).normalize();
                if (!target.startsWith(root)) {
                    throw new RuntimeException("非法文件名");
                }
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    throw new RuntimeException("保存文件失败: " + originalName);
                }
                ConstructDeliverableFile record = new ConstructDeliverableFile();
                record.setProjectId(projectId);
                record.setDeliverableId(deliverableId);
                record.setUploaderId(uploaderId);
                record.setSstepId(sstepId);
                record.setNstepId(nstepId);
                record.setFilePath(relativeDir + originalName);
                try {
                    record.setFileSize(Files.size(target));
                } catch (Exception ex) {
                    record.setFileSize(0L);
                }
                saved.add(fileRepository.save(record));
            }
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long projectId, Long deliverableId) {
        List<ConstructDeliverableFile> records = fileRepository.findByProjectIdAndDeliverableId(projectId, deliverableId);
        List<Map<String, Object>> files = new ArrayList<>();
        Path projectRoot = Paths.get("").toAbsolutePath().getParent();
        for (ConstructDeliverableFile r : records) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            Path target = projectRoot.resolve(r.getFilePath()).normalize();
            try {
                info.put("exists", Files.exists(target));
                if (Files.exists(target)) {
                    info.put("lastModified", Files.getLastModifiedTime(target).toMillis());
                }
            } catch (Exception e) {
                info.put("exists", false);
            }
            files.add(info);
        }
        return files;
    }

    @Transactional(readOnly = true)
    public Path resolveFilePath(Long fileId) {
        ConstructDeliverableFile r = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        Path projectRoot = Paths.get("").toAbsolutePath().getParent();
        return projectRoot.resolve(r.getFilePath()).normalize();
    }

    public void deleteFile(Long fileId) {
        ConstructDeliverableFile r = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        Path target = resolveFilePath(fileId);
        try {
            if (Files.exists(target)) {
                Files.delete(target);
            }
            fileRepository.deleteById(fileId);
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }
}