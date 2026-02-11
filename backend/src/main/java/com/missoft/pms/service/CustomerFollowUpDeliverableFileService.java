package com.missoft.pms.service;

import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.entity.CustomerFollowUp;
import com.missoft.pms.entity.CustomerFollowUpDeliverableFile;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.CustomerFollowUpDeliverableFileRepository;
import com.missoft.pms.repository.CustomerFollowUpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CustomerFollowUpDeliverableFileService {

    @Autowired
    private CustomerFollowUpDeliverableFileRepository fileRepository;

    @Autowired
    private AfterServiceProjectRepository projectRepository;

    @Autowired
    private CustomerFollowUpRepository followUpRepository;

    public List<CustomerFollowUpDeliverableFile> uploadFiles(Long projectId, Long recordId, String uploaderName, MultipartFile[] files) {
        if (recordId == null) {
            throw new RuntimeException("回访记录ID不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        CustomerFollowUp record = followUpRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("回访记录不存在，ID: " + recordId));
        Long effectiveProjectId = record.getAfterServiceProjectId() != null ? record.getAfterServiceProjectId() : projectId;
        if (effectiveProjectId == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        AfterServiceProject project = projectRepository.findById(effectiveProjectId)
                .orElseThrow(() -> new RuntimeException("运维项目不存在，ID: " + effectiveProjectId));

        String projNum = safe(project.getProjectNum(), "unknown");
        String projName = safe(project.getProjectName(), "未命名项目");
        String projectKey = safe(projNum) + "-" + safe(projName);
        String dateFolder = safe(record.getFollowUpDate() != null ? record.getFollowUpDate().toString() : "未知日期");
        String safeUploader = (uploaderName != null && !uploaderName.trim().isEmpty()) ? uploaderName.trim() : "未知";

        Path root = getProjectRoot()
                .resolve("afterServiceDeliverableFiles")
                .resolve(projectKey)
                .resolve("售后回访")
                .resolve(dateFolder);
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
        List<CustomerFollowUpDeliverableFile> saved = new ArrayList<>();
        String relativeDir = "afterServiceDeliverableFiles/" + projectKey + "/售后回访/" + dateFolder + "/";

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String ext = "";
            int dot = safeOriginal.lastIndexOf('.');
            if (dot >= 0) {
                ext = safeOriginal.substring(dot);
                safeOriginal = safeOriginal.substring(0, dot);
            }
            String ts = LocalDateTime.now().format(fmt);
            String finalName = safeOriginal + "-" + ts + ext;
            Path target = root.resolve(finalName).normalize();
            if (!target.startsWith(root)) {
                throw new RuntimeException("非法文件名");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("保存文件失败: " + originalName);
            }

            CustomerFollowUpDeliverableFile rec = new CustomerFollowUpDeliverableFile();
            rec.setAfterServiceProjectId(effectiveProjectId);
            rec.setFollowUpRecordId(recordId);
            rec.setUploadUser(safeUploader);
            rec.setFilePath(relativeDir + finalName);
            try {
                rec.setFileSize(Files.size(target));
            } catch (Exception ex) {
                rec.setFileSize(0L);
            }
            saved.add(fileRepository.save(rec));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long recordId) {
        List<CustomerFollowUpDeliverableFile> list = fileRepository.findByFollowUpRecordId(recordId);
        List<Map<String, Object>> files = new ArrayList<>();
        for (CustomerFollowUpDeliverableFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("afterServiceProjectId", r.getAfterServiceProjectId());
            info.put("followUpRecordId", r.getFollowUpRecordId());
            files.add(info);
        }
        return files;
    }

    public Path resolveFilePath(Long fileId) {
        CustomerFollowUpDeliverableFile rec = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        String relative = rec.getFilePath() != null ? rec.getFilePath() : "";
        Path root = getProjectRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("非法文件路径");
        }
        return target;
    }

    public void deleteFile(Long fileId) {
        CustomerFollowUpDeliverableFile rec = fileRepository.findById(fileId)
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

    public void deleteFilesByRecordId(Long recordId) {
        if (recordId == null) return;
        List<CustomerFollowUpDeliverableFile> files = fileRepository.findByFollowUpRecordId(recordId);
        for (CustomerFollowUpDeliverableFile file : files) {
            if (file == null || file.getFileId() == null) continue;
            deleteFile(file.getFileId());
        }
    }

    @Transactional(readOnly = true)
    public boolean hasFiles(Long recordId) {
        if (recordId == null) return false;
        return fileRepository.existsByFollowUpRecordId(recordId);
    }

    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    private String safe(String value) {
        return (value != null ? value.trim() : "").replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String safe(String value, String fallback) {
        String v = value != null ? value.trim() : "";
        return v.isEmpty() ? fallback : v;
    }
}
